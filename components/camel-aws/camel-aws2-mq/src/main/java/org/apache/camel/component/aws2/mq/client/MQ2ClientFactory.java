/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws2.mq.client;

import org.apache.camel.component.aws2.mq.MQ2Configuration;
import org.apache.camel.component.aws2.mq.client.impl.MQ2ClientOptimizedImpl;
import org.apache.camel.component.aws2.mq.client.impl.MQ2ClientStandardImpl;

/**
 * Factory class to return the correct type of AWS MQ client.
 */
public final class MQ2ClientFactory {

    private MQ2ClientFactory() {
    }

    /**
     * Return the correct AWS Mq client (based on remote vs local).
     * 
     * @param  configuration configuration
     * @return               MqClient
     */
    public static MQ2InternalClient getMqClient(MQ2Configuration configuration) {
        return Boolean.TRUE.equals(configuration.isUseDefaultCredentialsProvider())
                ? new MQ2ClientOptimizedImpl(configuration) : new MQ2ClientStandardImpl(configuration);
    }
}
