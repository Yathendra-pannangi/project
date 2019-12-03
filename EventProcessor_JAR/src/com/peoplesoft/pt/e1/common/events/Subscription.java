//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events;

//=================================================
// Imports from java namespace
//=================================================
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
 * Representation of a single subscription in the event processing system.
 */
public class Subscription implements Serializable
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    /** The subscription name. */
    private String mName = null;
    
    /** The subscription description. */
    private String mDescription = null;
    
    /** The SubscribedToEventTypes that are associated with this subscription. */
    private HashSet mSubscribedToEventTypes = null;
    
    /** The EnterpriseOne environments that are associated with this subscription. */
    private HashSet mEnvironments = null;
    
    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Default constructor.
     */
    public Subscription()
    {}

    /**
     * Constructs a Subscription with the given name and description.
     * 
     * @param name the subscription name
     * @param description the subscription description
     * @param eventTypes the set of SubscribedToEventTypes
     * @param envs the set of environments (as Strings)
     */
    public Subscription(String name, String description, HashSet eventTypes, HashSet envs)
    {
        mName = name;
        mDescription = description;
        mSubscribedToEventTypes = eventTypes;
        mEnvironments = envs;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return the subscription description
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @return the subscription environments as Strings
     */
    public Set getEnvironments()
    {
        return mEnvironments;
    }

    /**
     * @return the subscription name
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @return the set of SubscribedToEventType instances associated with this subscription
     */
    public Set getSubscribedToEventTypes()
    {
        return mSubscribedToEventTypes;
    }

    /**
     *  {@inheritDoc}
     */
    public String toString()
    {
        return "Name: "
            + mName
            + "  Description: "
            + mDescription
            + "  Environment: "
            + mEnvironments
            + "  SubscribedToEventTypes: "
            + mSubscribedToEventTypes;
    }
    
    /**
     * @param description  The description.
     */
    public void setDescription(String description)
    {
        mDescription = description;
    }

    /**
     * @param set  The set of subscribed to environments.
     */
    public void setEnvironments(Set set)
    {
        mEnvironments = new HashSet(set);
    }

    /**
     * @param name  The subscription name.
     */
    public void setName(String name)
    {
        mName = name;
    }

    /**
     * @param set  The subscribed to event types.
     */
    public void setSubscribedToEventTypes(Set set)
    {
        mSubscribedToEventTypes = new HashSet(set);
    }

}
