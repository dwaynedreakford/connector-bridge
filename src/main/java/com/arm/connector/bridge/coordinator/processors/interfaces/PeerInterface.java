/**
 * @file    PeerInterface.java
 * @brief   Generic Peer Interface for the connector bridge
 * @author  Doug Anson
 * @version 1.0
 * @see
 *
 * Copyright 2015. ARM Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.arm.connector.bridge.coordinator.processors.interfaces;

import com.arm.connector.bridge.coordinator.processors.core.AsyncResponseManager;
import java.util.Map;

/**
 * This interface defines the exposed methods of a peer processor (such as a GenericMQTTProcessor) that are consumed by the mDS processor
 * @author Doug Anson
 */
public interface PeerInterface {
    // create peer-centric authentication hash for mDS webhook authentication
    public String createAuthenticationHash();
        
    // process a new endpoint registration message from mDS
    public void processNewRegistration(Map message);
    
    // process an endpoint re-registration message from mDS
    public void processReRegistration(Map message);
    
    // process an endpoint de-registration message from mDS
    public String[] processDeregistrations(Map message);
    
    // process an endpoint registrations-expired message from mDS
    public void processRegistrationsExpired(Map message);
    
    // process an endpoint async response result from mDS
    public void processAsyncResponses(Map message);
    
    // record an async response to process later
    public void recordAsyncResponse(String response,String uri,Map ep,AsyncResponseProcessor processor);
    
    // process an endpoint resource notification message from mDS
    public void processNotification(Map message);
        
    // start/stop peer listeners
    public void initListener();
    public void stopListener();
}
