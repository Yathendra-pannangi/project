//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.clientsvc;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import javax.jms.JMSException;

import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  This class represents a remote event client session using IBM's embedded MQ APIs.
 *  
 *  At the time the client service was developed (EnterpriseOne Tools release 8.95A)
 *  the WebSphere embedded JMS system did not functions the same as WebLogic or the
 *  J2EE reference implementation.  In particular, an exception is thrown by WebSphere
 *  when acknowledging a JMS message.  This class uses the IBM specific APIs to create
 *  the JMS objects used so as to avoid this exception.
 */
class EventClientSessionEmbeddedMQ extends EventClientSession
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventClientSessionEmbeddedMQ.class.getName());

    private static final String SECTION = "EVENTS";

    private static final String EMBEDDED_MQ_PORT = "embeddedMQPort";

    private static final String EMBEDDED_MQ_SERVER_NAME = "embeddedMQServerName";

    private static final String EMBEDDED_MQ_HOST_NAME = "embeddedMQHostName";

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *   {@inheritDoc}
     */
    EventClientSessionEmbeddedMQ(
        String sessionId,
        ConnectorCredentials credentials,
        String connFactoryJndi,
        String queueJndi)
    {
        super(sessionId, credentials, connFactoryJndi, queueJndi);
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Initialize the session.
     * 
     *  @throws EventProcessingException  error during initialization.
     */
    void initialize() throws EventProcessingException
    {
        MQQueueConnectionFactory queueConFact = null;
        MQQueue queue = null;

        try
        {
            queueConFact = new MQQueueConnectionFactory();
            queueConFact.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
            queueConFact.setQueueManager(getEmbeddedMQManagerName());
            queueConFact.setHostName("localhost");
            queueConFact.setChannel("WAS.JMS.SVRCONN");
            queueConFact.setPort(getEmbeddedMQPort());

            queue = new MQQueue();
            queue.setBaseQueueName(getEmbeddedMQQueueName(getQueueJndiName()));
        }
        catch (JMSException e)
        {
            String msg = "error during creation of Queue Connection Factory: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        initializeSession(queueConFact, queue);
    }
    
    private int getEmbeddedMQPort() throws EventProcessingException
    {
        String portString = JdeProperty.getProperty(SECTION, EMBEDDED_MQ_PORT, null);
        if (portString != null)
        {
            int port = Integer.parseInt(portString);
            return port;
        }
        else
        {
            String msg = "failed to find " + EMBEDDED_MQ_PORT + " property in INI file";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
    }
    
    private String getEmbeddedMQQueueName(String queueJndiName) throws EventProcessingException
    {
        int index = queueJndiName.lastIndexOf('/');
        if (index < 0)
        {
            String msg = "can't convert queue JNDI name to MQ queue name: " + queueJndiName;
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        String mqName = "WQ_" + queueJndiName.substring(index+1, queueJndiName.length());
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"using embedded MQ queue: " + mqName, null, null, null);
        }
        
        return mqName;
    }
    
    private String getEmbeddedMQManagerName() throws EventProcessingException
    {
        String serverName = JdeProperty.getProperty(SECTION, EMBEDDED_MQ_SERVER_NAME, null);
        if (serverName != null)
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"using embedded MQ server: " + serverName, null, null, null);
            }
        }
        else
        {
            String msg = "failed to find " + EMBEDDED_MQ_SERVER_NAME + " property in INI file";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        String hostName = JdeProperty.getProperty(SECTION, EMBEDDED_MQ_HOST_NAME, null);
        if (hostName != null)
        {
            if (sE1Logger.isDebug())
            {
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"using embedded MQ host: " + hostName, null, null, null);
            }
        }
        else
        {
            String msg = "failed to find " + EMBEDDED_MQ_HOST_NAME + " property in INI file";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        String name = "WAS_" + hostName + "_" + serverName;
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"using embedded MQ manager: " + name, null, null, null);
        }
        return name;
    }
}
