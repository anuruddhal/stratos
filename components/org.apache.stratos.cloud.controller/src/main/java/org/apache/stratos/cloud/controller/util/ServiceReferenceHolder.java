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
package org.apache.stratos.cloud.controller.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.caching.impl.DistributedMapProvider;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;

/**
 * Singleton class to hold all the service references.
 */
public class ServiceReferenceHolder {

    private static ServiceReferenceHolder instance;
    private TaskService taskService;
    private Registry registry;
    private AxisConfiguration axisConfiguration;
    private DistributedMapProvider distributedMapProvider;

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        if (instance == null) {
            instance = new ServiceReferenceHolder();
        }
        return instance;
    }
    
    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }
    
    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }
    
    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
    
	public void setRegistry(UserRegistry governanceSystemRegistry) {
		registry = governanceSystemRegistry;
    }

	public Registry getRegistry() {
	    return registry;
    }

    public void setDistributedMapProvider(DistributedMapProvider distributedMapProvider) {
        this.distributedMapProvider = distributedMapProvider;
    }

    public DistributedMapProvider getDistributedMapProvider() {
        return distributedMapProvider;
    }
}
