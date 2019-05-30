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
package com.alipay.sofa.lookout.gateway.core.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-13 23:18
 **/
public class TimeUtilTest {

    @Test
    public void teststr2Time() {
        long x = TimeUtil.str2Time("2018-03-29T08:27:21.200Z");
        Assert.assertEquals(1522312041200l, x);
    }

    @Test
    public void testtimestamp2ISODate() {
        String x = TimeUtil.timestamp2ISODate(1522312041200l);
        System.out.println(x);
        Assert.assertNotNull(x);
    }

    @Test
    public void testparse() {
        Assert.assertEquals(1000, TimeUtil.parse("1s"));
        Assert.assertEquals(60000, TimeUtil.parse("1m"));
        Assert.assertEquals(3600000, TimeUtil.parse("1h"));
        Assert.assertEquals(86400000, TimeUtil.parse("1d"));
    }

    @Test(expected = RuntimeException.class)
    public void testparseException() {
        Assert.assertEquals(86400000, TimeUtil.parse("1w"));
    }
}
