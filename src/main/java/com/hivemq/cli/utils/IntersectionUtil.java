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
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.List;

public class IntersectionUtil {

    private static final short MULTI_WILDCARD_SIZE = 1;

    public static boolean intersects(final @NotNull MqttTopicFilter filterA, final @NotNull MqttTopicFilter filterB) {
        // Topics are equal (fast evaluation)
        if (filterA.matches(filterB) || filterB.matches(filterA)) {
            Logger.debug("Topic filter \"{}\" matches \"{}\" and therefore they intersect.",
                    filterA.toString(),
                    filterB.toString());
            return true;
        }

        // Topics have no wildcards and are not equal (fast evaluation)
        if (!filterA.containsWildcards() && !filterB.containsWildcards()) {
            if (!filterA.matches(filterB)) {
                Logger.debug(
                        "Topic filters \"{}\" and \"{}\" (without wildcards) do not match and therefore are disjoint.",
                        filterA.toString(),
                        filterB.toString());
                return false;
            }
        }

        final List<String> filterALevels = filterA.getLevels();
        final List<String> filterBLevels = filterB.getLevels();
        final int filterALevelSize = filterALevels.size();
        final int filterBLevelSize = filterBLevels.size();

        if (filterALevelSize > filterBLevelSize) { // FilterB is smaller
            if (!filterB.containsMultiLevelWildcard()) {
                Logger.debug("Topic filter \"{}\" has less levels than topic filter \"{}\" " +
                                "and does not contain a multi level wildcard. Therefore, it is disjoint.",
                        filterB.toString(),
                        filterA.toString());
                return false;
            } else { // FilterB contains a MultiLevelWildcard and leading topic levels match filterA
                for (int levelIndex = 0; levelIndex < filterBLevelSize - MULTI_WILDCARD_SIZE; levelIndex++) {
                    if (singleLevelDisjoint(filterALevels.get(levelIndex), filterBLevels.get(levelIndex))) {
                        final List<String> filterAIntersectedLevels = filterALevels.subList(0, levelIndex);
                        final List<String> filterBIntersectedLevels = filterBLevels.subList(0, levelIndex);
                        final String filterADisjointLevel = filterALevels.get(levelIndex);
                        final String filterBDisjointLevel = filterBLevels.get(levelIndex);
                        Logger.debug("Topic filters intersected up to level {} (\"{}\"<=>\"{}\") " +
                                        "but disjoint at level {} with \"{}\"<=/=>\"{}\"",
                                levelIndex,
                                String.join("/", filterAIntersectedLevels),
                                String.join("/", filterBIntersectedLevels),
                                levelIndex + 1,
                                filterADisjointLevel,
                                filterBDisjointLevel);
                        return false;
                    }
                }
            }
        } else if (filterALevelSize < filterBLevelSize) { // FilterA is smaller
            if (!filterA.containsMultiLevelWildcard()) {
                Logger.debug("Topic filter \"{}\" has less levels than topic filter \"{}\" " +
                                "and does not contain a multi level wildcard. Therefore, it is disjoint.",
                        filterA.toString(),
                        filterB.toString());
                return false;
            } else { // FilterA contains a MultiLevelWildcard and leading topic levels match filterB
                for (int levelIndex = 0; levelIndex < filterALevelSize - MULTI_WILDCARD_SIZE; levelIndex++) {
                    if (singleLevelDisjoint(filterALevels.get(levelIndex), filterBLevels.get(levelIndex))) {
                        final List<String> filterAIntersectedLevels = filterALevels.subList(0, levelIndex);
                        final List<String> filterBIntersectedLevels = filterBLevels.subList(0, levelIndex);
                        final String filterADisjointLevel = filterALevels.get(levelIndex);
                        final String filterBDisjointLevel = filterBLevels.get(levelIndex);
                        Logger.debug("Topic filters intersected up to level {} (\"{}\"<=>\"{}\") " +
                                        "but disjoint at level {} with \"{}\"<=/=>\"{}\"",
                                levelIndex,
                                String.join("/", filterAIntersectedLevels),
                                String.join("/", filterBIntersectedLevels),
                                levelIndex + 1,
                                filterADisjointLevel,
                                filterBDisjointLevel);
                        return false;
                    }
                }
            }
        } else { // Filters are of same size
            for (int levelIndex = 0; levelIndex < filterBLevelSize; levelIndex++) {
                if (singleLevelDisjoint(filterALevels.get(levelIndex), filterBLevels.get(levelIndex))) {
                    final List<String> filterAIntersectedLevels = filterALevels.subList(0, levelIndex + 1);
                    final List<String> filterBIntersectedLevels = filterBLevels.subList(0, levelIndex + 1);
                    final String filterADisjointLevel = filterALevels.get(levelIndex);
                    final String filterBDisjointLevel = filterBLevels.get(levelIndex);
                    Logger.debug("Topic filters intersected up to level {} (\"{}\"<=>\"{}\") " +
                                    "but disjoint at level {} with \"{}\"<=/=>\"{}\"",
                            levelIndex,
                            String.join("/", filterAIntersectedLevels),
                            String.join("/", filterBIntersectedLevels),
                            levelIndex + 1,
                            filterADisjointLevel,
                            filterBDisjointLevel);
                    return false;
                }
            }
        }
        Logger.debug("Topic filter \"{}\" and \"{}\" intersect.", filterA.toString(), filterB.toString());
        return true;
    }

    private static boolean singleLevelDisjoint(final @NotNull String levelA, final @NotNull String levelB) {
        if (levelA.equals(levelB)) {
            return false;
        } else if (levelA.equals(String.valueOf(MqttTopicFilter.MULTI_LEVEL_WILDCARD))) {
            return false;
        } else if (levelB.equals(String.valueOf(MqttTopicFilter.MULTI_LEVEL_WILDCARD))) {
            return false;
        } else if (levelA.equals(String.valueOf(MqttTopicFilter.SINGLE_LEVEL_WILDCARD))) {
            return false;
        } else {
            return !levelB.equals(String.valueOf(MqttTopicFilter.SINGLE_LEVEL_WILDCARD));
        }
    }
}
