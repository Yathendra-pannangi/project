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
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSTopic;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Base class for output topics.
 */
abstract class OutputTopic extends JMSTopic
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
     *  @param topicJNDIName  The JNDI name for the topic.
     * 
     *  @param connectionFactoryJNDIName  The JNDI name of the topic connection factory.
     */
    OutputTopic(String username, String topicJNDIName, String topicConnectionFactoryJNDIName)
    {
        super(topicJNDIName, topicConnectionFactoryJNDIName, true);
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
     *  Get the subscriber username this topic is assocated with.
     * 
     *  @return  Subscriber username.
     */
    public String getSubscriberUsername()
    {
        return mSubscriberUsername;
    }

}
