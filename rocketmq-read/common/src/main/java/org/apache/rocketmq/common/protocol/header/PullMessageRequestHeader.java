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

/**
 * $Id: PullMessageRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.common.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * Consumer发起的拉取消息的请求Header 拉取消息请求Header
 * @author ;
 */
public class PullMessageRequestHeader implements CommandCustomHeader {
    /**
     * 消费组 消费者分组
     */
    @CFNotNull
    private String consumerGroup;
    /**
     * topic
     */
    @CFNotNull
    private String topic;
    /**
     * 队列id
     */
    @CFNotNull
    private Integer queueId;
    /**
     * 队列偏移量
     * 这里queueOffset的用途如下：
     * 每次用户请求putMessage的时候，
     * 将queueOffset返回给客户端使用，这里的queueoffset表示逻辑上的队列偏移。
     */
    @CFNotNull
    private Long queueOffset;
    /**
     * 最大消息数量
     */
    @CFNotNull
    private Integer maxMsgNums;
    /**
     * 系统flag
     * 第 0 位 FLAG_COMMIT_OFFSET ：标记请求提交消费进度位置，和 commitOffset 配合。
     * 第 1 位 FLAG_SUSPEND ：标记请求是否挂起请求，和 suspendTimeoutMillis 配合。当拉取不到消息时， Broker 会挂起请求，直到有消息。最大挂起时间：suspendTimeoutMillis 毫秒
     * 第 2 位 FLAG_SUBSCRIPTION ：是否过滤订阅表达式，和 subscription 配置
     */
    @CFNotNull
    private Integer sysFlag;
    /**
     * 消费到的offset
     * 提交消费进度位置
     */
    @CFNotNull
    private Long commitOffset;
    /**
     * 挂起请求的超时时间
     */
    @CFNotNull
    private Long suspendTimeoutMillis;
    /**
     * tag 订阅表达式
     */
    @CFNullable
    private String subscription;
    /**
     * subVersion 订阅版本号
     * 订阅版本号。请求时，如果版本号不对，则无法拉取到消息，需要重新获取订阅信息，使用最新的订阅版本号。
     */
    @CFNotNull
    private Long subVersion;
    /**
     * 表达式类型
     */
    private String expressionType;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Long getQueueOffset() {
        return queueOffset;
    }

    public void setQueueOffset(Long queueOffset) {
        this.queueOffset = queueOffset;
    }

    public Integer getMaxMsgNums() {
        return maxMsgNums;
    }

    public void setMaxMsgNums(Integer maxMsgNums) {
        this.maxMsgNums = maxMsgNums;
    }

    public Integer getSysFlag() {
        return sysFlag;
    }

    public void setSysFlag(Integer sysFlag) {
        this.sysFlag = sysFlag;
    }

    public Long getCommitOffset() {
        return commitOffset;
    }

    public void setCommitOffset(Long commitOffset) {
        this.commitOffset = commitOffset;
    }

    public Long getSuspendTimeoutMillis() {
        return suspendTimeoutMillis;
    }

    public void setSuspendTimeoutMillis(Long suspendTimeoutMillis) {
        this.suspendTimeoutMillis = suspendTimeoutMillis;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public Long getSubVersion() {
        return subVersion;
    }

    public void setSubVersion(Long subVersion) {
        this.subVersion = subVersion;
    }

    public String getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }
}
