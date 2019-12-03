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
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.NamingException;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.naming.JNDIUtil;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  This class represents a remote event client session using standard JMS APIs.
 */
class EventClientSessionStdJms extends EventClientSession
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventClientSessionStdJms.class.getName());

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
    EventClientSessionStdJms(
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
        //
        //  Create a queue session.
        //
        QueueConnectionFactory queueConFact = null;
        Queue queue = null;
        try
        {
            queueConFact = (QueueConnectionFactory)JNDIUtil.lookup(getConnectionFactoryJndiName());
            queue = (Queue)JNDIUtil.lookup(getQueueJndiName());
        }
        catch (NamingException e)
        {
            String msg = "error during JNDI lookup of JMS queue objects.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
        catch (ClassCastException e)
        {
            String msg = "error during JNDI lookup of JMS queue objects.";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg,null, null, e);
            throw new EventProcessingException(msg, e);
        }
        
        initializeSession(queueConFact, queue);
    }
}
