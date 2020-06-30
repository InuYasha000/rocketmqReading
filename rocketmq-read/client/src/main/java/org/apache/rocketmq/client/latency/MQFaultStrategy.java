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

import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 这个类型主要是用来规划消息发送时的延迟策略。
 * 1.所有的broker延迟信息都会被记录
 * 2.发送消息时会选择延迟最低的broker来发送，提高效率
 * 3.broker延迟过高会自动减少它的消息分配，充分发挥所有服务器的能力
 * @author ;
 * 消息失败策略，延迟实现的门面类
 */
public class MQFaultStrategy {
    private final static InternalLogger log = ClientLogger.getLog();

    /**
     * key：brokerName
     * 延迟容错对象，维护延迟Brokers的信息
     * 它维护了那些消息发送延迟较高的brokers的信息，
     * 同时延迟的时间长短对应了延迟级别latencyMax
     * 时长notAvailableDuration ，sendLatencyFaultEnable 控制了是否开启发送消息延迟功能。
     */
    private final LatencyFaultTolerance<String> latencyFaultTolerance = new LatencyFaultToleranceImpl();
    /**
     * 延迟容错开关
     */
    private boolean sendLatencyFaultEnable = false;
    /**
     * 延迟级别数组
     */
    private long[] latencyMax = {50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L};
    /**
     * 不可用时长数组
     */
    private long[] notAvailableDuration = {0L, 0L, 30000L, 60000L, 120000L, 180000L, 600000L};

    public long[] getNotAvailableDuration() {
        return notAvailableDuration;
    }

    public void setNotAvailableDuration(final long[] notAvailableDuration) {
        this.notAvailableDuration = notAvailableDuration;
    }

    public long[] getLatencyMax() {
        return latencyMax;
    }

    public void setLatencyMax(final long[] latencyMax) {
        this.latencyMax = latencyMax;
    }

    public boolean isSendLatencyFaultEnable() {
        return sendLatencyFaultEnable;
    }

    public void setSendLatencyFaultEnable(final boolean sendLatencyFaultEnable) {
        this.sendLatencyFaultEnable = sendLatencyFaultEnable;
    }

    /**
     * 根据算法选择一个发送队列进行消息发送
     * @param tpInfo ;
     * @param lastBrokerName zu
     * @return ;
     * 1.首先选择一个broker==lastBrokerName并且可用的一个队列（也就是该队列并没有因为延迟过长而被加进了延迟容错对象latencyFaultTolerance 中）
     * 2.如果第一步中没有找到合适的队列，此时舍弃broker==lastBrokerName这个条件，选择一个相对较好的broker来发送
     * 3.随机选择一个队列来发送
     */
    public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName) {
        //如果打开了延迟容错的开关
        //容错策略选择消息队列逻辑。优先获取可用队列，其次选择一个broker获取队列，最差返回任意broker的一个队列
        if (this.sendLatencyFaultEnable) {
            try {

                int index = tpInfo.getSendWhichQueue().getAndIncrement();
                for (int i = 0; i < tpInfo.getMessageQueueList().size(); i++) {
                    int pos = Math.abs(index++) % tpInfo.getMessageQueueList().size();
                    if (pos < 0) {
                        pos = 0;
                    }
                    MessageQueue mq = tpInfo.getMessageQueueList().get(pos);
                    //随机选取一个队列，并且没有因为延迟过长而被加进了延迟容错对象latencyFaultTolerance 中
                    if (latencyFaultTolerance.isAvailable(mq.getBrokerName())) {
                        if (null == lastBrokerName || mq.getBrokerName().equals(lastBrokerName)) {
                            return mq;
                        }
                    }
                }

                //选择一个相对好的broker，不考虑可用性的消息队列(就是随机选择)
                final String notBestBroker = latencyFaultTolerance.pickOneAtLeast();
                int writeQueueNums = tpInfo.getQueueIdByBroker(notBestBroker);
                if (writeQueueNums > 0) {
                    //随机选取一个队列
                    final MessageQueue mq = tpInfo.selectOneMessageQueue();
                    if (notBestBroker != null) {
                        mq.setBrokerName(notBestBroker);
                        mq.setQueueId(tpInfo.getSendWhichQueue().getAndIncrement() % writeQueueNums);
                    }
                    return mq;
                } else {
                    //移除因延迟而加入的broker
                    latencyFaultTolerance.remove(notBestBroker);
                }
            } catch (Exception e) {
                log.error("Error occurred when selecting message queue", e);
            }

            return tpInfo.selectOneMessageQueue();
        }
        //未开启容错策略选择消息队列逻辑,随机选取，但不能选取lastBrokerName
        return tpInfo.selectOneMessageQueue(lastBrokerName);
    }

    /**
     * 更新broker的延迟情况
     * @param brokerName brokerName
     * @param currentLatency 当前延迟的毫秒
     * @param isolation 调用Broker是否报错
     */
    //更新延迟容错信息。当 Producer 发送消息时间过长，则逻辑认为N秒内不可用。按照latencyMax，notAvailableDuration的配置
    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation) {
        if (this.sendLatencyFaultEnable) {
            //如果isolation为true,则传递30000，那实际上需要60000毫秒这个broker才能可用？？不止60000
            long duration = computeNotAvailableDuration(isolation ? 30000 : currentLatency);
            this.latencyFaultTolerance.updateFaultItem(brokerName, currentLatency, duration);
        }
    }

    /**
     * 根据当前计算的延迟currentLatency，选择notAvailableDuration对应的不可用时间
     * 从{@link latencyMax}中找到第一比 currentLatency 小的索引，再到{@link notAvailableDuration}根据索引找出对应时间
     * 比如：latencyMax = {50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L}; currentLatency = 400L，那么取100L的索引1
     * @param currentLatency 当前延迟时间
     * @return ;
     */
    private long computeNotAvailableDuration(final long currentLatency) {
        for (int i = latencyMax.length - 1; i >= 0; i--) {
            if (currentLatency >= latencyMax[i]) {
                return this.notAvailableDuration[i];
            }
        }

        return 0;
    }
}
