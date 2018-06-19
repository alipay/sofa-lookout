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
package com.alipay.lookout.api;

import com.alipay.lookout.common.Assert;

import java.util.Arrays;
import java.util.Map;

/**
 * standard implementation
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
final class DefaultId implements Id {

    private final String name;
    private final TagSet tags;

    public DefaultId(String name) {
        this(name, TagSet.EMPTY);
    }

    DefaultId(String name, TagSet tags) {
        this.name = Assert.notNull(name, "name");
        this.tags = Assert.notNull(tags, "tags");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Iterable<Tag> tags() {
        return tags;
    }

    @Override
    public DefaultId withTag(Tag tag) {
        return new DefaultId(name, tags.add(tag));
    }

    @Override
    public DefaultId withTag(String key, String value) {
        return new DefaultId(name, tags.add(key, value));
    }

    @Override
    public DefaultId withTags(Tag... ts) {
        return new DefaultId(name, tags.addAll(ts));
    }

    @Override
    public DefaultId withTags(Iterable<Tag> ts) {
        return new DefaultId(name, tags.addAll(ts));
    }

    @Override
    public DefaultId withTags(Map<String, String> ts) {
        return new DefaultId(name, tags.addAll(ts));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof DefaultId))
            return false;
        DefaultId other = (DefaultId) obj;
        return name.equals(other.name) && tags.equals(other.tags);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] { name, tags });
    }

    @Override
    public String toString() {
        return name + tags;
    }
}
