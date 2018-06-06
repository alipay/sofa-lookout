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
package com.alipay.lookout.starter.support.converter;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Utils;
import com.alipay.lookout.common.LookoutConstants;
import com.alipay.lookout.common.LookoutIdNameConstants;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.*;

/**
 * IndicatorConvert
 *
 * @author yangguanchao
 * @since 2018/06/05
 */
public class IndicatorConvert {

    private static final Logger   logger                       = LookoutLoggerFactory
                                                                   .getLogger(IndicatorConvert.class);

    /***
     * 忽略返回 {@link Metric} 关键字数组集合
     */
    private static final String[] IGNORED_METRIC_NAME_PREFIXES = new String[] {
            LookoutIdNameConstants.JVM_SYSTEM_PROP_NAME,
            LookoutIdNameConstants.JVM_SYSTEM_PROP_NAME       };

    private static boolean isIgnoredMetrics(String namePrefix) {
        if (StringUtils.isBlank(namePrefix)) {
            return false;
        }
        for (String ignoredMetricNamePrefix : IGNORED_METRIC_NAME_PREFIXES) {
            if (namePrefix.contains(ignoredMetricNamePrefix)) {
                return true;
            }
        }
        return false;
    }

    /***
     * 将 lookout 的 {@link Indicator} 实例转换为 {@link Metric} 实例列表,用于浏览器展示
     * @param indicator Lookout 指标指示器
     * @return Actuator 维度度量指标
     */
    public static List<Metric> convertFromIndicator(Indicator indicator) {
        if (indicator == null) {
            return null;
        }
        List<Metric> indicatorMetricList = new LinkedList<Metric>();
        Id id = indicator.id();
        Date date = new Date(indicator.getTimestamp());
        String namePrefix = "";
        if (id != null) {
            namePrefix = Utils.toMetricName(id);
        }
        //忽略集合
        if (isIgnoredMetrics(namePrefix)) {
            return indicatorMetricList;
        }

        try {
            Collection<Measurement> measurements = indicator.measurements();
            for (Measurement measurement : measurements) {
                String name = measurement.name();
                Object measureValue = measurement.value();
                if (measureValue instanceof Number) {
                    Number valueNumber = (Number) measureValue;
                    Metric<Number> metric = new Metric<Number>(namePrefix + LookoutConstants.DOT
                                                               + name, valueNumber, date);
                    indicatorMetricList.add(metric);
                } else if (measureValue instanceof Map) {
                    Map<String, Object> valueMap = (Map<String, Object>) measureValue;
                    for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                        String keyName = entry.getKey();
                        Object value = entry.getValue();
                        if (value == null) {
                            continue;
                        }
                        if (value instanceof Number) {
                            Number valueMapNumber = (Number) value;
                            Metric<Number> metric = new Metric<Number>(namePrefix
                                                                       + LookoutConstants.DOT
                                                                       + name
                                                                       + LookoutConstants.DOT
                                                                       + keyName, valueMapNumber,
                                date);
                            indicatorMetricList.add(metric);
                        } else {
                            //忽略信息
                            logger.debug("Lookout value is not instance of Number. Value type is ["
                                         + value.getClass() + "].Ignored Lookout prefix = " + "["
                                         + namePrefix + LookoutConstants.DOT + name
                                         + "] Measurement = [" + entry.toString() + "]");
                        }
                    }
                } else {
                    //忽略信息
                    logger.debug("Lookout value is not instance of Number. Value type is ["
                                 + measureValue.getClass() + "].Ignored Lookout prefix = " + "["
                                 + namePrefix + "] Measurement = [" + measurement.toString() + "]");
                }
            }
        } catch (Exception exception) {
            logger.error("Indicator converted from Lookout Exception!", exception);
        }
        return indicatorMetricList;
    }
}
