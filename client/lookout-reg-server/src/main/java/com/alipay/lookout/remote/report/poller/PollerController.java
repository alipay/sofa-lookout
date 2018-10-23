/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.lookout.remote.report.poller;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.top.RollableTopGauge;
import com.alipay.lookout.common.utils.CommonUtil;
import com.alipay.lookout.core.GaugeWrapper;
import com.alipay.lookout.core.InfoWrapper;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.remote.step.PollableInfoWrapper;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import org.slf4j.Logger;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author xiangfeng.xzc
 * @since 2018/7/26
 */
public class PollerController implements Closeable {
    private static final Logger           LOGGER               = LookoutLoggerFactory
                                                                   .getLogger(PollerController.class);

    private static final int              DEFAULT_SLOT_COUNT   = 3;

    private static final int              DEFAULT_IDLE_SECONDS = 1800;                                //30 min
    /**
     * 比较器 按照cursor倒序排序
     */
    private static final Comparator<Slot> COMPARATOR           = new Comparator<Slot>() {
                                                                   @Override
                                                                   public int compare(Slot o1,
                                                                                      Slot o2) {
                                                                       return Longs.compare(
                                                                           o2.getCursor(),
                                                                           o1.getCursor());
                                                                   }
                                                               };

    /**
     * 注册中心
     */
    private final LookoutRegistry         registry;

    /**
     * 只有1个线程的调度器
     */
    private ScheduledExecutorService      scheduledExecutorService;

    /**
     * 采样间隔时间, step将会扩散到registry包含的所有实现了 ResettableStep 接口的 metric
     */
    private volatile long                 step                 = -1;

    /**
     * 槽数量
     */
    private volatile int                  slotCount;

    /**
     * 缓存
     */
    private volatile MetricCache          metricCache;

    /**
     * 是否处于激活状态(最近有poll过数据)
     */
    private boolean                       active               = false;

    /**
     * 定时poll数据的task的future
     */
    private ScheduledFuture<?>            idleFuture;

    /**
     * 监听器
     */
    private final List<Listener>          listeners            = new CopyOnWriteArrayList<Listener>();

    /**
     * 空闲检测定时的future
     */
    private ScheduledFuture<?>            pollerFuture;

    /**
     * 多少时间没有请求就算是空闲
     */
    private int                           idleSeconds;

    public PollerController(LookoutRegistry registry) {
        this(registry, DEFAULT_SLOT_COUNT);
    }

    public PollerController(LookoutRegistry registry, int initSlotCount) {
        this.registry = registry;
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
            CommonUtil.getNamedThreadFactory("poller-controller"),
            new ThreadPoolExecutor.AbortPolicy());
        idleSeconds = registry.getConfig().getInteger(LookoutConfig.LOOKOUT_EXPORTER_IDLE_SECONDS,
            DEFAULT_IDLE_SECONDS);
        update(registry.getCurrentStepMillis(), initSlotCount);
    }

    MetricConfig getMetricConfig() {
        return registry.getConfig();
    }

    /**
     * 因为step或slowCount的调整, 导致需要重建 MetricCache, 这个方法尽量保留已有的slot, 减少数据丢失
     *
     * @param step      step
     * @param slotCount slotCount
     * @return
     */
    private MetricCache createCache(long step, int slotCount) {
        MetricCache oldCache = this.metricCache;
        if (oldCache != null) {
            return new MetricCache(oldCache, step, slotCount);
        } else {
            return new MetricCache(registry.clock(), step, slotCount);
        }
    }

    /**
     * 修改slotCount
     *
     * @param slotCount slotCount
     */
    public void setSlotCount(int slotCount) {
        Preconditions.checkArgument(slotCount > 0, "slotCount must greater than 0");
        if (this.slotCount == slotCount) {
            return;
        }
        synchronized (this) {
            this.slotCount = slotCount;
            if (this.step <= 0) {
                return;
            }
            this.metricCache = createCache(step, slotCount);
        }
    }

    /**
     * 修改频率
     *
     * @param step step
     */
    public void setStep(long step) {
        if (this.step == step) {
            return;
        }
        synchronized (this) {
            this.step = step;

            if (this.pollerFuture != null) {
                pollerFuture.cancel(true);
            }
            this.pollerFuture = null;

            // 认为step<=0则无需启动任务
            if (step <= 0) {
                this.metricCache = null;
                return;
            }

            this.metricCache = createCache(step, slotCount);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    MetricCache cache = PollerController.this.metricCache;
                    if (!active || cache == null) {
                        return;
                    }
                    List<MetricDto> result = getMetricDtos();
                    cache.add(result);
                }
            };
            pollerFuture = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, step,
                TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 获得下一批是数据
     *
     * @param successCursors 上一次成功的 cursors
     * @return next data
     */
    public List<Slot> getNextData(Set<Long> successCursors) {
        touchTimer();
        MetricCache array = this.metricCache;
        if (array == null) {
            return Collections.emptyList();
        } else {
            List<Slot> data = array.getNextData(successCursors);
            Collections.sort(data, COMPARATOR);
            return data;
        }
    }

    public void clear() {
        MetricCache array = this.metricCache;
        if (array != null) {
            array.clear();
        }
    }

    public void destroy() {
        scheduledExecutorService.shutdownNow();
    }

    /**
     * 更新该 poller 的配置
     *
     * @param newStep      newStep
     * @param newSlotCount newSlotCount
     */
    public synchronized void update(long newStep, int newSlotCount) {
        Preconditions.checkArgument(newStep >= 0, "step must greater than 0");
        setSlotCount(newSlotCount);
        setStep(newStep);
        registry.setStep(newStep);
    }

    /**
     * 获取槽数量
     *
     * @return slot count
     */
    public int getSlotCount() {
        return slotCount;
    }

    /**
     * 获取采样步长
     *
     * @return step
     */
    public long getStep() {
        return step;
    }

    private List<MetricDto> getMetricDtos() {

        List<MetricDto> results = new ArrayList<MetricDto>();

        long polledTime = System.currentTimeMillis();
        Iterator<Metric> it = registry.iterator();
        while (it.hasNext()) {
            Metric metric = it.next();
            if (metric instanceof GaugeWrapper) {
                Gauge gauge = ((GaugeWrapper) metric).getOriginalOne();
                if (gauge instanceof RollableTopGauge) {
                    ((RollableTopGauge) gauge).roll(polledTime);
                    it.remove();
                }
            }
            if (metric instanceof InfoWrapper) {
                //ignore info
                if (registry.getConfig().getBoolean(
                    LookoutConfig.LOOKOUT_AUTOPOLL_INFO_METRIC_IGNORE, true)) {
                    continue;
                }
                if (!((PollableInfoWrapper) metric).isAutoPolledAllowed(getStep())) {
                    continue;
                }
            }

            MetricDto dto = new MetricDto();
            LookoutMeasurement lookoutMeasurement = LookoutMeasurement.from(metric, registry);
            dto.setName(lookoutMeasurement.metricId().name());
            dto.setTimestamp(lookoutMeasurement.getDate().getTime());
            dto.setMetrics(lookoutMeasurement.getValues());
            dto.setTags(lookoutMeasurement.getTags());
            results.add(dto);
        }
        return results;
    }

    /**
     * 刷新一下定时器
     */
    private synchronized void touchTimer() {
        // 说明从 idle 状态 转成 active 状态
        if (!active) {
            active = true;
            triggerActive();
        }

        // 取消旧的计时器
        ScheduledFuture<?> oldFuture = this.idleFuture;
        if (oldFuture != null && !oldFuture.isDone()) {
            oldFuture.cancel(true);
        }

        // 空闲时间到了，进入idle状态
        this.idleFuture = scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (PollerController.this) {
                    active = false;
                    triggerIdle();
                }
            }
        }, idleSeconds, TimeUnit.SECONDS);
    }

    private void triggerIdle() {
        //fallback to proactive
        registry.setProactive(true);
        // idle时不再poller 同时也清理掉cache
        this.metricCache.clear();

        LOGGER.warn("PollerController is now idle. reactive mode.");
        for (Listener listener : listeners) {
            listener.onIdle();
        }
    }

    private void triggerActive() {
        LOGGER.warn("PollerController is now active. proactive mode.");
        //reactive mode.
        registry.setProactive(false);
        for (Listener listener : listeners) {
            listener.onActive();
        }
    }

    /**
     * 添加监听器
     *
     * @param listener listener
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * 删除监听器
     *
     * @param listener listener
     */
    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void close() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
            this.scheduledExecutorService = null;
        }
        registry.destroy();
    }
}
