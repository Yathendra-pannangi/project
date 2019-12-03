//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.eventprocessor;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.TextMessage;

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;

import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

import javax.naming.Context;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Manages an XML text output queue.  An XML text output queue only sends the XML data payload
 *  for the event to the queue as a text message.
 */
class XMLTextOutputQueue extends OutputQueue
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(XMLTextOutputQueue.class.getName());
    
    //=================================================
    // Instance member fields.
    //=================================================
    
        
    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param username  The subscriber username.
     * 
     *  @param queueJNDIName  The JNDI name for the queue.
     * 
     *  @param connectionFactoryJNDIName  The JNDI name of the queue connection factory.
     */
    XMLTextOutputQueue(String username, String queueJNDIName, String connectionFactoryJNDIName)
    {
        super(username, queueJNDIName, connectionFactoryJNDIName);
    }

    //=================================================
    // Methods.
    //=================================================
        
    /**
     * Delivers event message to subscriber.
     * 
     * @param  eventMsg  The event message to send.
     * 
     * @throws EventProcessingException  An error occured sending the event message.
     */
    void sendEventMessage(EventMessage eventMsg) throws EventProcessingException
    {
        //
        //  Verify that initialization has been performed.
        //
        if (!isInitialized())
        {
            throw new IllegalStateException(OutputQueue.class.getName() + " not initialized");
        }
        
        //
        //  Generate some debug information about the event.
        //
        if (sE1Logger.isDebug())
        {
            StringBuffer buffer = new StringBuffer("Sending event:");
            buffer.append(System.getProperty("line.separator"));
            buffer.append(" queue=").append(getQueueJNDIName());
            buffer.append(System.getProperty("line.separator"));
            buffer.append(" category=").append(eventMsg.getCategory());
            buffer.append(System.getProperty("line.separator"));
            buffer.append(" type=").append(eventMsg.getType());
            buffer.append(System.getProperty("line.separator"));
            buffer.append(" XML=").append(eventMsg.getXMLPayload());
            buffer.append(System.getProperty("line.separator"));
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, buffer.toString(), null, null, null);
        }

        try
        {
            TextMessage message = getQueueSession().createTextMessage();
            message.setText(eventMsg.getXMLPayload());
            getQueueSender().send(message);
            
            //8488371 - OAS10.1.3.1 trans commit().  Only for OAS not WAS
             String contextFactory = JdeProperty.getProperty("EVENTS", "initialContextFactory", null);
             if (!"com.ibm.websphere.naming.WsnInitialContextFactory".equals(contextFactory))
             {
            getQueueSession().commit();
             }
            
            
        }
        catch (JMSException e)
        {
            String msg = "Failed to send event message: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }
}
