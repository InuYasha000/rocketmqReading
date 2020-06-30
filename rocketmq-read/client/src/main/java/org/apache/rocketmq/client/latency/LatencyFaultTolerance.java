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

package org.apache.rocketmq.client.latency;

/**
 * producer选取broker发送消息的时候，延迟容错策略的接口
 * @param <T> ;
 * @author ;
 */
public interface LatencyFaultTolerance<T> {
    /**
     * 更新延迟容错策略
     * @param name brokerName
     * @param currentLatency 当前延迟时间
     * @param notAvailableDuration 不可用时长(后面会加上当前时间System.currentTimeMillis()),在这个时间段内的Broker会被规避
     */
    void updateFaultItem(final T name, final long currentLatency, final long notAvailableDuration);

    /**
     * 是否可用
     * @param name brokername
     * @return ;
     */
    boolean isAvailable(final T name);
    /**
     * LatencyFaultTolerance 中移除某个BrokerName
     * @param name name
     */
    void remove(final T name);

    /**
     * 尝试从规避的Broker获取一个至少可用的broker
     * @return ;
     */
    T pickOneAtLeast();
}
