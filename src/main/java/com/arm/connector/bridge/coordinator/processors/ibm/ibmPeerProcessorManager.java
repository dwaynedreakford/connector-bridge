/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.arm.connector.bridge.coordinator.processors.ibm;

import com.arm.connector.bridge.coordinator.Orchestrator;
import com.arm.connector.bridge.coordinator.processors.core.Processor;
import com.arm.connector.bridge.coordinator.processors.interfaces.MDSInterface;
import com.arm.connector.bridge.coordinator.processors.interfaces.PeerInterface;
import com.arm.connector.bridge.transport.HttpTransport;
import com.arm.connector.bridge.transport.MQTTTransport;
import com.arm.connector.bridge.transport.Transport;
import java.util.ArrayList;
import java.util.Map;

/**
 * IBM Peer Processor Manager: Manages a collection of MQTT-based processors (including a generic one) for IBM Services
 * @author Doug Anson
 */
public class ibmPeerProcessorManager extends Processor implements Transport.ReceiveListener, PeerInterface { 
    private ArrayList<GenericMQTTProcessor> m_mqtt_list = null;
    private HttpTransport m_http = null;
    private int m_default_processor = 0;
    
    // Factory method for initializing the IBM MQTT collection manager
    public static ibmPeerProcessorManager createPeerProcessor(Orchestrator manager,MDSInterface mds_rest_processor,HttpTransport http) {
        // create me
        ibmPeerProcessorManager me = new ibmPeerProcessorManager(manager,http);
        
        // initialize me
        boolean starterkit_enabled = manager.preferences().booleanValueOf("enable_starterkit_addon");
        boolean iotf_enabled = manager.preferences().booleanValueOf("enable_iotf_addon");
        String mgr_config = manager.preferences().valueOf("mqtt_mgr_config");
        if (mgr_config != null && mgr_config.length() > 0) {
            // muliple MQTT brokers requested... follow configuration and assign suffixes
            String[] config = mgr_config.split(";");
            for(int i=0;i<config.length;++i) {
                if (starterkit_enabled == true && config[i].equalsIgnoreCase("sk") == true) {
                    manager.errorLogger().info("Registering StarterKit MQTT processor...");
                    GenericMQTTProcessor p = new StarterKitMQTTProcessor(manager,""+i,http);
                    p.setMDSProcessor(mds_rest_processor);
                    me.addProcessor(p);
                }
                if (starterkit_enabled == true && config[i].equalsIgnoreCase("sk-d") == true) {
                    manager.errorLogger().info("Registering StarterKit MQTT processor (default)...");
                    GenericMQTTProcessor p = new StarterKitMQTTProcessor(manager,""+i,http);
                    p.setMDSProcessor(mds_rest_processor);
                    me.addProcessor(p,true);
                }
                if (iotf_enabled == true && config[i].equalsIgnoreCase("iotf") == true) {
                    manager.errorLogger().info("Registering IoTF MQTT processor...");
                    GenericMQTTProcessor p = new IoTFMQTTProcessor(manager,new MQTTTransport(manager.errorLogger(),manager.preferences()),""+i,http);
                    p.setMDSProcessor(mds_rest_processor);
                    me.addProcessor(p);
                }
                if (iotf_enabled == true && config[i].equalsIgnoreCase("iotf-d") == true) {
                    manager.errorLogger().info("Registering IoTF MQTT processor (default)...");
                    GenericMQTTProcessor p = new IoTFMQTTProcessor(manager,new MQTTTransport(manager.errorLogger(),manager.preferences()),""+i,http);
                    p.setMDSProcessor(mds_rest_processor);
                    me.addProcessor(p,true);
                }
                if (config[i].equalsIgnoreCase("std") == true) {
                    manager.errorLogger().info("Registering Standard MQTT processor...");
                    GenericMQTTProcessor p = new GenericMQTTProcessor(manager,new MQTTTransport(manager.errorLogger(),manager.preferences()),""+i,http);
                    p.setMDSProcessor(mds_rest_processor);
                    me.addProcessor(p);
                }
                if (config[i].equalsIgnoreCase("std-d") == true) {
                    manager.errorLogger().info("Registering Standard MQTT processor (default)...");
                    GenericMQTTProcessor p = new GenericMQTTProcessor(manager,new MQTTTransport(manager.errorLogger(),manager.preferences()),""+i,http);
                    p.setMDSProcessor(mds_rest_processor);
                    me.addProcessor(p,true);
                }
            }
        }
        else {
            // single MQTT broker configuration requested
            if (iotf_enabled == true) {
                manager.errorLogger().info("Registering IoTF MQTT processor (singleton)...");
                GenericMQTTProcessor p = new IoTFMQTTProcessor(manager,new MQTTTransport(manager.errorLogger(),manager.preferences()),http);
                p.setMDSProcessor(mds_rest_processor);
                me.addProcessor(p);
            }
            else if (starterkit_enabled == true) {
                manager.errorLogger().info("Registering StarterKit MQTT processor (singleton)...");
                GenericMQTTProcessor p = new StarterKitMQTTProcessor(manager,http);
                p.setMDSProcessor(mds_rest_processor);
                me.addProcessor(p);
            }
            else {
                manager.errorLogger().info("Registering Standard MQTT processor (singleton)...");
                GenericMQTTProcessor p = new GenericMQTTProcessor(manager,new MQTTTransport(manager.errorLogger(),manager.preferences()),http);
                p.setMDSProcessor(mds_rest_processor);
                me.addProcessor(p);
            }
        }
        
        // return me
        return me;
    }
    
    // constructor
    public ibmPeerProcessorManager(Orchestrator manager,HttpTransport http) {
        super(manager,null);
        this.m_http = http;
        this.m_mqtt_list = new ArrayList<>();
    }
    
    // query the default processor for the authentication hash
    @Override
    public String createAuthenticationHash() {
        return this.createAuthenticationHash(this.m_default_processor);
    }
    
    // create an authentication hash for a specific MQTT broker
    public String createAuthenticationHash(int index) {
        if (this.m_mqtt_list.size() > 0 && index < this.m_mqtt_list.size()) {
            return this.m_mqtt_list.get(index).createAuthenticationHash();
        }
        return null;
    } 
    
    // init listeners for each MQTT broker connection
    @Override
    public void initListener() {
        for(int i=0;i<this.m_mqtt_list.size();++i) {
            this.m_mqtt_list.get(i).initListener();
        }
    }
    
    // stop listeners for each MQTT broker connection
    @Override
    public void stopListener() {
        for(int i=0;i<this.m_mqtt_list.size();++i) {
            this.m_mqtt_list.get(i).stopListener();
        }
    }
    
    // add a GenericMQTTProcessor
    public void addProcessor(GenericMQTTProcessor mqtt_processor) {
        this.addProcessor(mqtt_processor,false);
    }
    
    // add a GenericMQTTProcessor
    public void addProcessor(GenericMQTTProcessor mqtt_processor,boolean is_default) {
        if (is_default == true) {
            this.m_default_processor = this.m_mqtt_list.size();
        }
        this.m_mqtt_list.add(mqtt_processor);
    }
    
    // get the number of processors
    public int numProcessors() {
        return this.m_mqtt_list.size();
    }
    
    // get the default processor
    public GenericMQTTProcessor mqttProcessor() { 
        if (this.m_mqtt_list.size() > 0) {
            return this.m_mqtt_list.get(this.m_default_processor); 
        }
        return null;
    } 
  
    // message processor for inbound messages
    @Override
    public void onMessageReceive(String topic, String message) {
        for(int i=0;i<this.m_mqtt_list.size();++i) {
            this.m_mqtt_list.get(i).onMessageReceive(topic,message);
        }
    }

    @Override
    public void processNewRegistration(Map message) {
        for(int i=0;i<this.m_mqtt_list.size() ;++i) {
            this.m_mqtt_list.get(i).processNewRegistration(message);
        }
    }

    @Override
    public void processReRegistration(Map message) {
        for(int i=0;i<this.m_mqtt_list.size() ;++i) {
            this.m_mqtt_list.get(i).processReRegistration(message);
        }
    }

    @Override
    public String[] processDeregistrations(Map message) {
        ArrayList<String> list = new ArrayList<>();
        for(int i=0;i<this.m_mqtt_list.size() ;++i) {
            String[] tmp = this.m_mqtt_list.get(i).processDeregistrations(message);
            for(int j=0;j<tmp.length;++j) {
                list.add(tmp[j]);
            }
        }
        String[] returns = new String[list.size()];
        return list.toArray(returns);
    }

    @Override
    public void processRegistrationsExpired(Map message) {
        this.processDeregistrations(message);
    }

    @Override
    public void processAsyncResponses(Map message) {
        for(int i=0;i<this.m_mqtt_list.size() ;++i) {
            this.m_mqtt_list.get(i).processAsyncResponses(message);
        }
    }

    @Override
    public void processNotification(Map message) {
        for(int i=0;i<this.m_mqtt_list.size() ;++i) {
            this.m_mqtt_list.get(i).processNotification(message);
        }
    }
}