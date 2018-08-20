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
package com.alipay.lookout.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public final class MetricsImporterLocator {
    private static List<MetricsImporter> metricsImporters = null;

    private MetricsImporterLocator() {
    }

    /**
     * locates all metric importers (and locate only once)
     *
     * @return
     */
    public static synchronized Collection<MetricsImporter> locate() {
        if (metricsImporters == null) {
            metricsImporters = new ArrayList<MetricsImporter>(3);
            ServiceLoader<MetricsImporter> mis = ServiceLoader.load(MetricsImporter.class);
            for (MetricsImporter metricsImporter : mis) {
                //TODO Class String name duplication check
                metricsImporters.add(metricsImporter);
            }
        }
        return metricsImporters;
    }
}
