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
package com.alipay.lookout.starter.support.reg;

import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.reg.prometheus.PrometheusRegistry;
import org.springframework.beans.factory.DisposableBean;

/**
 * Created by kevin.luy@alipay.com on 2018/5/11.
 */
public class PrometheusMetricsRegistryFactory
                                             implements
                                             MetricsRegistryFactory<PrometheusRegistry, MetricConfig>,
                                             DisposableBean {

    /**
     * singleton {@link PrometheusRegistry}
     */
    private PrometheusRegistry prometheusRegistry = null;

    @Override
    public synchronized PrometheusRegistry get(MetricConfig metricConfig) {
        if (this.prometheusRegistry == null) {
            this.prometheusRegistry = new PrometheusRegistry(metricConfig);
        }
        return this.prometheusRegistry;
    }

    @Override
    public void destroy() throws Exception {
        if (this.prometheusRegistry != null) {
            this.prometheusRegistry.close();
        }
    }
}
