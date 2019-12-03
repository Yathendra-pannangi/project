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
 * Metadata for an event required by Connector clients.
 */
public class EventTypeDefinition implements Serializable
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
    
    private String mCategory = null;
    private String mType = null;
    private String mEnvironment = null;
    private boolean mIsActive = false;
    private boolean mIsSubscribedTo = false;
    
    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Default constructor.
     */
    public EventTypeDefinition()
    {}

    /**
     * Constructs an EventTypeDefinition with all required fields.
     * 
     * @param category the event category
     * @param type the event type
     * @param environment the environment associated with this event
     * @param isActive whether the event is active in the associated environment
     * @param isSubscribedTo whether the event has even a single subscriber in
     *                       the associated environment
     */
    public EventTypeDefinition(String category, String type, String environment,
                               boolean isActive, boolean isSubscribedTo)
    {
        mCategory = category;
        mType = type;
        mEnvironment = environment;
        mIsActive = isActive;
        mIsSubscribedTo = isSubscribedTo;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return the event category
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @return the event type
     */
    public String getType()
    {
        return mType;
    }
    
    /**
     * @return the environment associated with this event
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }
    
    /**
     * @return whether the event is active in the given environment
     */
    public boolean isActive()
    {
        return mIsActive;
    }

    /**
     * @return whether any subscriber has subscribed to this event in the given environment
     */
    public boolean isSubscribedTo()
    {
        return mIsSubscribedTo;
    }
    
    
    /**
     *  {@inheritDoc}
     */
    public String toString()
    {
        return "Category: " + mCategory + "  Type: " + mType + "  Environment: " + mEnvironment
               + "  isActive: " + mIsActive + "  isSubscribedTo: " + mIsSubscribedTo;
    }

    /**
     * @param category  The event category.
     */
    public void setCategory(String category)
    {
        mCategory = category;
    }

    /**
     * @param env  The environment.
     */
    public void setEnvironment(String env)
    {
        mEnvironment = env;
    }

    /**
     * @param active  Is the event active or not?
     */
    public void setActive(boolean active)
    {
        mIsActive = active;
    }

    /**
     * @param sub Is the event subscribed to?
     */
    public void setSubscribedTo(boolean sub)
    {
        mIsSubscribedTo = sub;
    }

    /**
     * @param type  The event type.
     */
    public void setType(String type)
    {
        mType = type;
    }

}
