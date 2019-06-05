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
package com.alipay.sofa.lookout.server.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 *
 * @author yuanxuan
 * @version $Id: ServerTestBase.java, v 0.1 2019年05月28日 11:51 yuanxuan Exp $
 */
public class ServerTestBase {

    protected String loadMockResponse(String name, Consumer<String> lineFn) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(name);
        StringBuilder content = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                content.append(line);
                if (lineFn != null) {
                    lineFn.accept(line);
                }
            }
        } finally {
            if (in != null) {
                in.close();
                br.close();
            }
        }
        return content.toString();
    }

}