/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.manager.subscription.tenancy;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cloud.controller.pojo.CartridgeInfo;
import org.apache.stratos.cloud.controller.pojo.Properties;
import org.apache.stratos.cloud.controller.pojo.Property;
import org.apache.stratos.manager.client.CloudControllerServiceClient;
import org.apache.stratos.manager.dao.Cluster;
import org.apache.stratos.manager.exception.ADCException;
import org.apache.stratos.manager.exception.AlreadySubscribedException;
import org.apache.stratos.manager.exception.NotSubscribedException;
import org.apache.stratos.manager.exception.UnregisteredCartridgeException;
import org.apache.stratos.manager.payload.BasicPayloadData;
import org.apache.stratos.manager.payload.PayloadData;
import org.apache.stratos.manager.payload.PayloadFactory;
import org.apache.stratos.manager.repository.Repository;
import org.apache.stratos.manager.subscriber.Subscriber;
import org.apache.stratos.manager.subscription.utils.CartridgeSubscriptionUtils;
import org.apache.stratos.manager.utils.ApplicationManagementUtil;
import org.apache.stratos.manager.utils.CartridgeConstants;

import java.util.Map;
import java.util.Set;


public class SubscriptionSingleTenantBehaviour extends SubscriptionTenancyBehaviour {

    private static Log log = LogFactory.getLog(SubscriptionSingleTenantBehaviour.class);

    public PayloadData create (String alias, Cluster cluster, Subscriber subscriber, Repository repository, CartridgeInfo cartridgeInfo,
                        String subscriptionKey, Map<String, String> customPayloadEntries) throws ADCException, AlreadySubscribedException {

        //set the cluster and hostname
        //cartridgeSubscription.getCluster().setClusterDomain(cartridgeSubscription.getAlias() + "." +
        //        cartridgeSubscription.getCluster().getHostName() + "." + cartridgeSubscription.getType() + ".domain");
        String clusterId = alias + "." + cartridgeInfo.getType() + ".domain";

        // limit the cartridge alias to 30 characters in length
        if (clusterId.length() > 30) {
            clusterId = CartridgeSubscriptionUtils.limitLengthOfString(clusterId, 30);
        }
        cluster.setClusterDomain(clusterId);
        // set hostname
        cluster.setHostName(alias + "." + cluster.getHostName());

        //Create the payload
        BasicPayloadData basicPayloadData = CartridgeSubscriptionUtils.createBasicPayload(cartridgeInfo, subscriptionKey, cluster, repository,
                alias, subscriber);
        //Populate the basic payload details
        basicPayloadData.populatePayload();

        PayloadData payloadData = PayloadFactory.getPayloadDataInstance(cartridgeInfo.getProvider(),
                cartridgeInfo.getType(), basicPayloadData);

        // get the payload parameters defined in the cartridge definition file for this cartridge type
        if (cartridgeInfo.getProperties() != null && cartridgeInfo.getProperties().length != 0) {

            for (Property property : cartridgeInfo.getProperties()) {
                // check if a property is related to the payload. Currently this is done by checking if the
                // property name starts with 'payload_parameter.' suffix. If so the payload param name will
                // be taken as the substring from the index of '.' to the end of the property name.
                if (property.getName()
                        .startsWith(CartridgeConstants.CUSTOM_PAYLOAD_PARAM_NAME_PREFIX)) {
                    String payloadParamName = property.getName();
                    payloadData.add(payloadParamName.substring(payloadParamName.indexOf(".") + 1), property.getValue());
                }
            }
        }

        //check if there are any custom payload entries defined
        if (customPayloadEntries != null) {
            //add them to the payload
            Set<Map.Entry<String,String>> entrySet = customPayloadEntries.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                payloadData.add(entry.getKey(), entry.getValue());
            }
        }

        return payloadData;
    }

    public void register (CartridgeInfo cartridgeInfo, Cluster cluster, PayloadData payloadData, String autoscalePolicyName,
                          String deploymentPolicyName, Properties properties) throws ADCException, UnregisteredCartridgeException {

        log.info("Payload: " + payloadData.getCompletePayloadData().toString());

        ApplicationManagementUtil.registerService(cartridgeInfo.getType(),
                cluster.getClusterDomain(),
                cluster.getClusterSubDomain(),
                payloadData.getCompletePayloadData(),
                payloadData.getBasicPayloadData().getTenantRange(),
                cluster.getHostName(),
                autoscalePolicyName,
                deploymentPolicyName,
                properties);
    }

    public void remove (String clusterId, String alias) throws ADCException, NotSubscribedException {

        try {
            CloudControllerServiceClient.getServiceClient().terminateAllInstances(clusterId);

        } catch (AxisFault e) {
            String errorMsg = "Error in terminating cartridge subscription, alias " + alias;
            log.error(errorMsg);
            throw new ADCException(errorMsg, e);

        } catch (Exception e) {
            String errorMsg = "Error in terminating cartridge subscription, alias " + alias;
            log.error(errorMsg);
            throw new ADCException(errorMsg, e);
        }

        log.info("Terminated all instances of " + clusterId);

        try {
            CloudControllerServiceClient.getServiceClient().unregisterService(clusterId);

        } catch (Exception e) {
            String errorMsg = "Error in unregistering service cluster with domain " + clusterId;
            log.error(errorMsg);
            throw new ADCException(errorMsg, e);
        }

        log.info("Unregistered service cluster, domain " + clusterId);
    }
}
