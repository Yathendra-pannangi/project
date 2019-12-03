//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.common.events.connectorsvc;


//=================================================
// Imports from java namespace
//=================================================
import java.io.Serializable;


//=================================================
// Imports from javax namespace
//=================================================
//=================================================
// Imports from com namespace
//=================================================
//=================================================
// Imports from org namespace
//=================================================

/**
 * MSMQSubscriber is a bean class that contains the subscriber name, queue name, and queue label
 * for an MSMQ subscriber.
 */
public class MSMQSubscriber implements Serializable {
    //=================================================
    // Non-public static class fields.
    //=================================================
    //=================================================
    // Public static final fields.
    //=================================================
    //=================================================
    // Instance member fields.
    //=================================================

    /** The MSMQ subscriber name. */
    private String mSubscriberName = null;

    /** The MSMQ queue name for the subscriber. */
    private String mQueueName = null;

    /** The MSMQ queue label for the subscriber. */
    private String mQueueLabel = null;

    //=================================================
    // Constructors.
    //=================================================

    /**
             *  Default constructor.
             */
    public MSMQSubscriber() {
    }

    /**
     * Creates the MSMQSubscriber object with all required properties.
     *
     * @param subscriberName the MSMQ subscriber name
     * @param queueName      the MSMQ queue name for the subscriber
     * @param queueLabel     the MSMQ label for the subscriber
     */
    public MSMQSubscriber(String subscriberName, String queueName,
        String queueLabel) {
        mSubscriberName = subscriberName;
        mQueueName = queueName;
        mQueueLabel = queueLabel;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return the MSMQ subscriber name
     */
    public String getSubscriberName() {
        return mSubscriberName;
    }

    /**
     * @return the MSMQ label for the subscriber
     */
    public String getQueueLabel() {
        return mQueueLabel;
    }

    /**
     * @return the MSMQ name for the subscriber
     */
    public String getQueueName() {
        return mQueueName;
    }

    /**
     * @param string
     */
    public void setQueueLabel(String string) {
        mQueueLabel = string;
    }

    /**
     * @param string
     */
    public void setQueueName(String string) {
        mQueueName = string;
    }

    /**
     * @param string
     */
    public void setSubscriberName(String string) {
        mSubscriberName = string;
    }
}
