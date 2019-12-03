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

//=================================================
// Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSQueue;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Base class for output queues.
 */
abstract class OutputQueue extends JMSQueue
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Static class fields.
    //=================================================
    
    //=================================================
    // Instance member fields.
    //=================================================
    
    private String mSubscriberUsername;
        
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
    OutputQueue(String username, String queueJNDIName, String connectionFactoryJNDIName)
    {
        super(queueJNDIName, connectionFactoryJNDIName, true);
        mSubscriberUsername = username;
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
    abstract void sendEventMessage(EventMessage eventMsg) throws EventProcessingException;

    /**
     *  Get the subscriber username this queue is assocated with.
     * 
     *  @return  Subscriber username.
     */
    public String getSubscriberUsername()
    {
        return mSubscriberUsername;
    }

}
