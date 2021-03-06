/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.integration.tests.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.beans.application.ApplicationBean;
import org.apache.stratos.common.beans.cartridge.CartridgeGroupBean;
import org.apache.stratos.common.beans.policy.deployment.ApplicationPolicyBean;
import org.apache.stratos.integration.tests.RestConstants;
import org.apache.stratos.integration.tests.StratosTestServerManager;
import org.apache.stratos.integration.tests.TopologyHandler;
import org.apache.stratos.messaging.domain.application.Application;
import org.apache.stratos.messaging.domain.application.ApplicationStatus;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * Handling the termination behavior of the group
 */
public class GroupTerminationBehaviorTest extends StratosTestServerManager {
    private static final Log log = LogFactory.getLog(GroupTerminationBehaviorTest.class);
    private static final String RESOURCES_PATH = "/group-termination-behavior-test";
    private static final int GROUP_INACTIVE_TIMEOUT = 300000;

    @Test
    public void testTerminationBehavior() {
        try {
            log.info("-------------------------------Started application termination behavior test case-------------------------------");

            String autoscalingPolicyId = "autoscaling-policy-group-termination-behavior-test";
            TopologyHandler topologyHandler = TopologyHandler.getInstance();

            boolean addedScalingPolicy = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.AUTOSCALING_POLICIES_PATH
                            + "/" + autoscalingPolicyId + ".json",
                    RestConstants.AUTOSCALING_POLICIES, RestConstants.AUTOSCALING_POLICIES_NAME);
            assertTrue(addedScalingPolicy);

            boolean addedC1 = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.CARTRIDGES_PATH + "/" + "c1-group-termination-behavior-test.json",
                    RestConstants.CARTRIDGES, RestConstants.CARTRIDGES_NAME);
            assertTrue(addedC1);

            boolean addedC2 = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.CARTRIDGES_PATH + "/" + "c2-group-termination-behavior-test.json",
                    RestConstants.CARTRIDGES, RestConstants.CARTRIDGES_NAME);
            assertTrue(addedC2);

            boolean addedC3 = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.CARTRIDGES_PATH + "/" + "c3-group-termination-behavior-test.json",
                    RestConstants.CARTRIDGES, RestConstants.CARTRIDGES_NAME);
            assertTrue(addedC3);

            boolean addedC4 = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.CARTRIDGES_PATH + "/" + "c4-group-termination-behavior-test.json",
                    RestConstants.CARTRIDGES, RestConstants.CARTRIDGES_NAME);
            assertTrue(addedC4);

            boolean addedG1 = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.CARTRIDGE_GROUPS_PATH +
                            "/" + "cartridge-groups-group-termination-behavior-test.json", RestConstants.CARTRIDGE_GROUPS,
                    RestConstants.CARTRIDGE_GROUPS_NAME);
            assertTrue(addedG1);

            CartridgeGroupBean beanG1 = (CartridgeGroupBean) restClientTenant1.
                    getEntity(RestConstants.CARTRIDGE_GROUPS, "g-sc-G4-group-termination-behavior-test",
                            CartridgeGroupBean.class, RestConstants.CARTRIDGE_GROUPS_NAME);
            assertEquals(beanG1.getName(), "g-sc-G4-group-termination-behavior-test");

            boolean addedN1 = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.NETWORK_PARTITIONS_PATH + "/" +
                            "network-partition-group-termination-behavior-test-1.json",
                    RestConstants.NETWORK_PARTITIONS, RestConstants.NETWORK_PARTITIONS_NAME);
            assertTrue(addedN1);

            boolean addedDep = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.DEPLOYMENT_POLICIES_PATH + "/" +
                            "deployment-policy-group-termination-behavior-test.json",
                    RestConstants.DEPLOYMENT_POLICIES, RestConstants.DEPLOYMENT_POLICIES_NAME);
            assertTrue(addedDep);

            boolean added = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.APPLICATIONS_PATH + "/" +
                            "group-termination-behavior-test.json", RestConstants.APPLICATIONS,
                    RestConstants.APPLICATIONS_NAME);
            assertTrue(added);

            ApplicationBean bean = (ApplicationBean) restClientTenant1.getEntity(RestConstants.APPLICATIONS,
                    "group-termination-behavior-test", ApplicationBean.class, RestConstants.APPLICATIONS_NAME);
            assertEquals(bean.getApplicationId(), "group-termination-behavior-test");

            boolean addAppPolicy = restClientTenant1.addEntity(RESOURCES_PATH + RestConstants.APPLICATION_POLICIES_PATH + "/" +
                            "application-policy-group-termination-behavior-test.json", RestConstants.APPLICATION_POLICIES,
                    RestConstants.APPLICATION_POLICIES_NAME);
            assertTrue(addAppPolicy);

            ApplicationPolicyBean policyBean = (ApplicationPolicyBean) restClientTenant1.getEntity(
                    RestConstants.APPLICATION_POLICIES,
                    "application-policy-group-termination-behavior-test", ApplicationPolicyBean.class,
                    RestConstants.APPLICATION_POLICIES_NAME);

            //deploy the application
            String resourcePath = RestConstants.APPLICATIONS + "/" + "group-termination-behavior-test" +
                    RestConstants.APPLICATIONS_DEPLOY + "/" + "application-policy-group-termination-behavior-test";
            boolean deployed = restClientTenant1.deployEntity(resourcePath,
                    RestConstants.APPLICATIONS_NAME);
            assertTrue(deployed);

            //Application active handling
            topologyHandler.assertApplicationStatus(bean.getApplicationId(),
                    ApplicationStatus.Active,tenant1Id);

            Application depApplication= topologyHandler.getApplication(bean.getApplicationId(), tenant1Id);
            String groupId = topologyHandler.generateId(depApplication.getUniqueIdentifier(),
                    "g-G1-1x0-group-termination-behavior-test", depApplication.getUniqueIdentifier() + "-1");

            String clusterIdC3 = topologyHandler.
                    getClusterIdFromAlias(bean.getApplicationId(),
                            "c3-1x0-group-termination-behavior-test",tenant1Id);

            String clusterIdC4 = topologyHandler.
                    getClusterIdFromAlias(bean.getApplicationId(),
                            "c4-1x0-group-termination-behavior-test",tenant1Id);

            String clusterIdC2 = topologyHandler.
                    getClusterIdFromAlias(bean.getApplicationId(),
                            "c2-1x0-group-termination-behavior-test",tenant1Id);

            assertCreationOfNodes(groupId, clusterIdC2);
            assertCreationOfNodes(clusterIdC3, clusterIdC4);


            //Group active handling
            topologyHandler.assertGroupActivation(bean.getApplicationId(),tenant1Id);

            //Cluster active handling
            topologyHandler.assertClusterActivation(bean.getApplicationId(),tenant1Id);

            //Terminate one member in the cluster
            TopologyHandler.getInstance().terminateMemberFromCluster(
                    "c3-group-termination-behavior-test",
                    bean.getApplicationId(),
                    mockIaasApiClient,tenant1Id);

            List<String> clusterIds = new ArrayList<String>();
            clusterIds.add(clusterIdC3);
            clusterIds.add(clusterIdC4);
            clusterIds.add(clusterIdC2);


            //Application active handling
            topologyHandler.assertApplicationStatus(bean.getApplicationId(),
                    ApplicationStatus.Active,tenant1Id);

            assertCreationOfNodes(groupId, clusterIdC2);

            assertCreationOfNodes(clusterIdC3, clusterIdC4);

            //Group active handling
            topologyHandler.assertGroupActivation(bean.getApplicationId(),tenant1Id);

            //Cluster active handling
            topologyHandler.assertClusterActivation(bean.getApplicationId(),tenant1Id);

            boolean removedGroup = restClientTenant1.removeEntity(RestConstants.CARTRIDGE_GROUPS,
                    "g-sc-G4-group-termination-behavior-test",
                    RestConstants.CARTRIDGE_GROUPS_NAME);
            assertFalse(removedGroup);

            boolean removedAuto = restClientTenant1.removeEntity(RestConstants.AUTOSCALING_POLICIES,
                    autoscalingPolicyId, RestConstants.AUTOSCALING_POLICIES_NAME);
            assertFalse(removedAuto);

            boolean removedNet = restClientTenant1.removeEntity(RestConstants.NETWORK_PARTITIONS,
                    "network-partition-group-termination-behavior-test-1",
                    RestConstants.NETWORK_PARTITIONS_NAME);
            //Trying to remove the used network partition
            assertFalse(removedNet);

            boolean removedDep = restClientTenant1.removeEntity(RestConstants.DEPLOYMENT_POLICIES,
                    "deployment-policy-group-termination-behavior-test", RestConstants.DEPLOYMENT_POLICIES_NAME);
            assertFalse(removedDep);

            //Un-deploying the application
            String resourcePathUndeploy = RestConstants.APPLICATIONS + "/" + "group-termination-behavior-test" +
                    RestConstants.APPLICATIONS_UNDEPLOY;

            boolean unDeployed = restClientTenant1.undeployEntity(resourcePathUndeploy,
                    RestConstants.APPLICATIONS_NAME);
            assertTrue(unDeployed);

            boolean undeploy = topologyHandler.assertApplicationUndeploy("group-termination-behavior-test",tenant1Id);
            if (!undeploy) {
                //Need to forcefully undeploy the application
                log.info("Force undeployment is going to start for the [application] " + "group-termination-behavior-test");

                restClientTenant1.undeployEntity(RestConstants.APPLICATIONS + "/" + "group-termination-behavior-test" +
                        RestConstants.APPLICATIONS_UNDEPLOY + "?force=true", RestConstants.APPLICATIONS);

                boolean forceUndeployed = topologyHandler.assertApplicationUndeploy("group-termination-behavior-test",tenant1Id);
                assertTrue(String.format("Forceful undeployment failed for the application %s",
                        "group-termination-behavior-test"), forceUndeployed);

            }

            boolean removed = restClientTenant1.removeEntity(RestConstants.APPLICATIONS, "group-termination-behavior-test",
                    RestConstants.APPLICATIONS_NAME);
            assertTrue(removed);

            ApplicationBean beanRemoved = (ApplicationBean) restClientTenant1.getEntity(RestConstants.APPLICATIONS,
                    "group-termination-behavior-test", ApplicationBean.class, RestConstants.APPLICATIONS_NAME);
            assertNull(beanRemoved);

            removedGroup = restClientTenant1.removeEntity(RestConstants.CARTRIDGE_GROUPS,
                    "g-sc-G4-group-termination-behavior-test",
                    RestConstants.CARTRIDGE_GROUPS_NAME);
            assertTrue(removedGroup);

            boolean removedC1 = restClientTenant1.removeEntity(RestConstants.CARTRIDGES, "c1-group-termination-behavior-test",
                    RestConstants.CARTRIDGES_NAME);
            assertTrue(removedC1);

            boolean removedC2 = restClientTenant1.removeEntity(RestConstants.CARTRIDGES, "c2-group-termination-behavior-test",
                    RestConstants.CARTRIDGES_NAME);
            assertTrue(removedC2);

            boolean removedC3 = restClientTenant1.removeEntity(RestConstants.CARTRIDGES, "c3-group-termination-behavior-test",
                    RestConstants.CARTRIDGES_NAME);
            assertTrue(removedC3);

            boolean removedC4 = restClientTenant1.removeEntity(RestConstants.CARTRIDGES, "c4-group-termination-behavior-test",
                    RestConstants.CARTRIDGES_NAME);
            assertTrue(removedC4);

            removedAuto = restClientTenant1.removeEntity(RestConstants.AUTOSCALING_POLICIES,
                    autoscalingPolicyId, RestConstants.AUTOSCALING_POLICIES_NAME);
            assertTrue(removedAuto);

            removedDep = restClientTenant1.removeEntity(RestConstants.DEPLOYMENT_POLICIES,
                    "deployment-policy-group-termination-behavior-test", RestConstants.DEPLOYMENT_POLICIES_NAME);
            assertTrue(removedDep);

            removedNet = restClientTenant1.removeEntity(RestConstants.NETWORK_PARTITIONS,
                    "network-partition-group-termination-behavior-test-1", RestConstants.NETWORK_PARTITIONS_NAME);
            assertFalse(removedNet);

            boolean removeAppPolicy = restClientTenant1.removeEntity(RestConstants.APPLICATION_POLICIES,
                    "application-policy-group-termination-behavior-test", RestConstants.APPLICATION_POLICIES_NAME);
            assertTrue(removeAppPolicy);

            removedNet = restClientTenant1.removeEntity(RestConstants.NETWORK_PARTITIONS,
                    "network-partition-group-termination-behavior-test-1", RestConstants.NETWORK_PARTITIONS_NAME);
            assertTrue(removedNet);

            log.info("-------------------------------Ended application termination behavior test case-------------------------------");

        } catch (Exception e) {
            log.error("An error occurred while handling  application termination behavior", e);
            assertTrue("An error occurred while handling  application termination behavior", false);
        }
    }

    private void assertGroupInactive(String groupId, String clusterId) {
        long startTime = System.currentTimeMillis();
        Map<String, Long> inActiveMap = TopologyHandler.getInstance().getInActiveMembers();

        while (!inActiveMap.containsKey(clusterId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            inActiveMap = TopologyHandler.getInstance().getInActiveMembers();
            if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                break;
            }
        }
        assertTrue(inActiveMap.containsKey(clusterId));

        while (!inActiveMap.containsKey(groupId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            inActiveMap = TopologyHandler.getInstance().getInActiveMembers();
            if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                break;
            }
        }
        assertTrue(inActiveMap.containsKey(groupId));

    }

    private void assertTerminatingOfNodes(String groupId, List<String> clusterIds) {
        Map<String, Long> terminatingMembers = TopologyHandler.getInstance().getTerminatingMembers();
        for (String clusterId : clusterIds) {
            long startTime = System.currentTimeMillis();
            while (!terminatingMembers.containsKey(clusterId)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                terminatingMembers = TopologyHandler.getInstance().getTerminatingMembers();
                if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                    break;
                }
            }
            assertTrue(terminatingMembers.containsKey(groupId));
        }
        long startTime = System.currentTimeMillis();
        while (!terminatingMembers.containsKey(groupId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            terminatingMembers = TopologyHandler.getInstance().getTerminatingMembers();
            if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                break;
            }
        }
        assertTrue(terminatingMembers.containsKey(groupId));

    }

    private void assertTerminationOfNodes(String groupId, List<String> clusterIds) {
        long startTime = System.currentTimeMillis();
        Map<String, Long> terminatedMembers = TopologyHandler.getInstance().getTerminatedMembers();

        for (String clusterId : clusterIds) {
            while (!terminatedMembers.containsKey(clusterId)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                terminatedMembers = TopologyHandler.getInstance().getTerminatedMembers();
                if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                    break;
                }
            }
            assertTrue(terminatedMembers.containsKey(clusterId));
        }

        while (!terminatedMembers.containsKey(groupId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            terminatedMembers = TopologyHandler.getInstance().getTerminatedMembers();
            if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                break;
            }
        }

        assertTrue(terminatedMembers.containsKey(groupId));
    }

    private void assertCreationOfNodes(String firstNodeId, String secondNodeId) {
        //group1 started first, then cluster started later
        long startTime = System.currentTimeMillis();
        Map<String, Long> activeMembers = TopologyHandler.getInstance().getActivateddMembers();
        Map<String, Long> createdMembers = TopologyHandler.getInstance().getCreatedMembers();
        //Active member should be available at the time cluster is started to create.
        while(!activeMembers.containsKey(firstNodeId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            activeMembers = TopologyHandler.getInstance().getActivateddMembers();
            if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                break;
            }
        }
        assertTrue(activeMembers.containsKey(firstNodeId));

        while(!createdMembers.containsKey(secondNodeId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            createdMembers = TopologyHandler.getInstance().getCreatedMembers();
            if ((System.currentTimeMillis() - startTime) > GROUP_INACTIVE_TIMEOUT) {
                break;
            }
        }

        assertTrue(createdMembers.containsKey(secondNodeId));

        assertTrue(createdMembers.get(secondNodeId) > activeMembers.get(firstNodeId));
    }
}
