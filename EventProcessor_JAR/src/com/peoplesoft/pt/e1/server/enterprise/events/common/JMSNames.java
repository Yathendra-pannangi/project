//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.common;

//=================================================
//Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Define constants for the JMS resource JNDI names used by the event system.
 */
public final class JMSNames
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  Queue connection factory JNDI name.  */
    public static final String Q_CON_FACTORY = 
        "jms/com/peoplesoft/pt/e1/server/enterprise/events/QueueConnectionFactory";
    
    /**  Trigger queue JNDI name.  */
    public static final String TRIGGER_Q = 
        "jms/com/peoplesoft/pt/e1/server/enterprise/events/TriggerQueue";
    
    /**
     * The following is the root JNDI name of the event queues. The two digit queue
     * index number is appended to this root to form the full JNDI name of the queue.
     */
    public static final String EVENT_Q_ROOT
        = "jms/com/peoplesoft/pt/e1/server/enterprise/events/EventQueue";
    
    /**  Source routing queue JNDI name.  */
    public static final String SOURCE_ROUTE_Q
        = "jms/com/peoplesoft/pt/e1/server/enterprise/events/SourceRouteQueue";
    
    /**  Cache reload topic connection factory. */
    public static final String RELOAD_TOPIC_FACTORY
        = "jms/com/peoplesoft/pt/e1/server/enterprise/events/DataReloadTopicConnectionFactory";
    
    /**  Cache reload topic. */
    public static final String RELOAD_TOPIC
        = "jms/com/peoplesoft/pt/e1/server/enterprise/events/DataReloadTopic";

    public static final String ESB_QUEUE
        = "jms/com/peoplesoft/pt/e1/server/enterprise/events/ESBQueue00";
    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Private constructor to prevent creation of an instance of this class.
     */
    private JMSNames()
    {
    }

    //=================================================
    // Methods.
    //=================================================
}
