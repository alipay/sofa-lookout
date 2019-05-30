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
package com.alipay.sofa.lookout.server.prom.ql.ast;

import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;

/**
 * It represents a number.
 * <p>
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class NumberLiteral implements Expr {
    double val;

    public NumberLiteral(double val) {
        this.val = val;
    }

    @Override
    public ValueType type() {
        return ValueType.scalar;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    @Override
    public void expr() {

    }
}
