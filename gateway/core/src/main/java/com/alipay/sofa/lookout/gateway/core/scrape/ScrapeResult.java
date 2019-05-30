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
package com.alipay.sofa.lookout.gateway.core.scrape;

import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;

import java.util.List;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-14 23:34
 **/
public class ScrapeResult<T> {
    private T                         body;
    private Map<String, List<String>> headers;
    private ScrapeConfig              config;

    public ScrapeResult(T body, Map<String, List<String>> headers, ScrapeConfig config) {
        this.body = body;
        this.headers = headers;
        this.config = config;
    }

    public T getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public ScrapeConfig getConfig() {
        return config;
    }
}
