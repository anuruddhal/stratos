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

package org.apache.stratos.messaging.domain.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.domain.application.locking.ApplicationLockHierarchy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
  DO NOT make this a singleton
 */
public class Applications implements Serializable {

    private static Log log = LogFactory.getLog(Applications.class);

    private Map<String, Application> applicationMap;

    private boolean initialized;

    public Applications() {
        this.applicationMap = new HashMap<String, Application>();
    }

    public synchronized void addApplication(Application application) {
        this.applicationMap.put(application.getUniqueIdentifier(), application);
    }

    public synchronized Application getApplication(String applicationUuid) {
        return this.getApplications().get(applicationUuid);
    }

    public synchronized Application getApplicationByTenant(String applicationId, int tenantId) {
        if(getApplications() != null) {
            for (Application application : this.getApplications().values()) {
                if (application.getId().equals(applicationId) && application.getTenantId() == tenantId) {
                    return application;
                }
            }
        }

        return null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public synchronized void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public synchronized boolean applicationExists(String appId) {
        return this.getApplications().containsKey(appId);
    }

    public synchronized Map<String, Application> getApplications() {
        return applicationMap;
    }

    public synchronized void removeApplication(String appId) {
        this.applicationMap.remove(appId);
        ApplicationLockHierarchy.getInstance().removeLockForApplication(appId);
    }
}
