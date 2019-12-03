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
 * A bean representation of an event subscription.
 */
public class SubscribedToEventType implements Serializable
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
    
    /** The event category. */
    private String mCategory = null;
    
    /** The event type. */
    private String mType = null;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Default constructor.
     */
    public SubscribedToEventType()
    {}

    /**
     * 
     * @param category event category
     * @param type event type
     */
    public SubscribedToEventType(String category, String type)
    {
        mCategory = category;
        mType = type;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return event category
     */
    public String getCategory()
    {
        return mCategory;
    }
    
    /**
     * @return event type
     */
    public String getType()
    {
        return mType;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other)
    {
        boolean result = false;
        if (other instanceof SubscribedToEventType)
        {
            SubscribedToEventType otherEvent = (SubscribedToEventType)other;
            result = otherEvent.mType.equals(mType) && otherEvent.mCategory.equals(mCategory);
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        int code = 637 + mCategory.hashCode();
        code = code * 37 + mType.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "Category: " + mCategory + " Type: " + mType;
    }

    /**
     * @param category  Event category.
     */
    public void setCategory(String category)
    {
        mCategory = category;
    }

    /**
     * @param type  Event type.
     */
    public void setType(String type)
    {
        mType = type;
    }

}
