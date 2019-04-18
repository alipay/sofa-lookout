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
package com.alipay.lookout.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by kevin.luy@alipay.com on 2018/4/4.
 */
public class ClassUtilTest {

    @Test
    public void testNewInstance() {
        ArrayList list = ClassUtil.newInstance(ArrayList.class.getName(), null, null);
        Assert.assertNotNull(list);

        Integer integer = ClassUtil.newInstance(Integer.class.getName(), new Class[] { int.class },
            new Object[] { 5 });
        Assert.assertEquals("5", integer.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testNewInstanceException() {
        ClassUtil.newInstance(ArrayList.class.getName() + "x", null, null);
    }
}
