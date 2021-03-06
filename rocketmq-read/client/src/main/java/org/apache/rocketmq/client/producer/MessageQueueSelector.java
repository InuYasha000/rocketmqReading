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
package org.apache.rocketmq.client.producer;

import java.util.List;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * MessageQueueSelector。消息队列选择器。
 * 当发送顺序消息的时候会用到，需要用户自己实现
 * 消息选择算法
 * @author ;
 */
public interface MessageQueueSelector {
    /**
     * 选择一个消息队列
     * @param mqs 队列列表
     * @param msg msg 消息
     * @param arg 参数
     * @return 队列
     */
    MessageQueue select(final List<MessageQueue> mqs, final Message msg, final Object arg);
}
