/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.utils;

import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntersectionUtilTest {

    @Test
    void test_topic_intersects_without_wildcards() {
        final MqttTopicFilter filterA = MqttTopicFilter.builder().addLevel("a").build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("a").build();

        assertTrue(IntersectionUtil.intersects(filterA, filterB));
        assertTrue(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_is_disjoint() {
        final MqttTopicFilter filterA = MqttTopicFilter.builder().addLevel("a").build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("b").build();

        assertFalse(IntersectionUtil.intersects(filterA, filterB));
        assertFalse(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_intersects_with_multilevel_wildcards() {
        final MqttTopicFilter filterA = MqttTopicFilter.builder().addLevel("a").multiLevelWildcard().build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("a").addLevel("w").addLevel("b").build();

        assertTrue(IntersectionUtil.intersects(filterA, filterB));
        assertTrue(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_is_disjoint_with_multilevel_wildcards() {
        final MqttTopicFilter filterA = MqttTopicFilter.builder().addLevel("a").multiLevelWildcard().build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("b").addLevel("w").addLevel("c").build();

        assertFalse(IntersectionUtil.intersects(filterA, filterB));
        assertFalse(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_intersects_with_singlelevel_wildcards() {
        final MqttTopicFilter filterA = MqttTopicFilter.builder().addLevel("a").singleLevelWildcard().build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("a").addLevel("w").build();

        assertTrue(IntersectionUtil.intersects(filterA, filterB));
        assertTrue(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_intersects_with_singlelevel_wildcards_and_following() {
        final MqttTopicFilter filterA =
                MqttTopicFilter.builder().addLevel("a").singleLevelWildcard().addLevel("b").build();
        final MqttTopicFilter filterB =
                MqttTopicFilter.builder().addLevel("a").addLevel("w").singleLevelWildcard().build();

        assertTrue(IntersectionUtil.intersects(filterA, filterB));
        assertTrue(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_is_disjoint_with_singlelevel_wildcards() {
        final MqttTopicFilter filterA = MqttTopicFilter.builder().addLevel("a").singleLevelWildcard().build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("b").addLevel("w").build();

        assertFalse(IntersectionUtil.intersects(filterA, filterB));
        assertFalse(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_is_disjoint_with_singlelevel_wildcards_and_following() {
        final MqttTopicFilter filterA =
                MqttTopicFilter.builder().addLevel("a").singleLevelWildcard().addLevel("c").build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("b").addLevel("w").addLevel("c").build();

        assertFalse(IntersectionUtil.intersects(filterA, filterB));
        assertFalse(IntersectionUtil.intersects(filterB, filterA));
    }

    @Test
    void test_topic_intersects_with_singlelevel_and_multilevel_wildcards() {
        final MqttTopicFilter filterA =
                MqttTopicFilter.builder().addLevel("a").singleLevelWildcard().singleLevelWildcard().build();
        final MqttTopicFilter filterB = MqttTopicFilter.builder().addLevel("a").multiLevelWildcard().build();

        assertTrue(IntersectionUtil.intersects(filterA, filterB));
        assertTrue(IntersectionUtil.intersects(filterB, filterA));
    }
}