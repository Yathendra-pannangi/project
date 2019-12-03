//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.cache;

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
 *  Describes an event that is subscribed to.
 */
public class SubscribedToEventType
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
    
    private String mCategory = "";
    
    private String mType = "";

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param category  Event category.
     * 
     *  @param type  Event type.
     */
    public SubscribedToEventType(String category, String type)
    {
        mCategory = category;
        mType     = type;
    }
    
    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  Category of event.
     */
    public String getCategory()
    {
        return mCategory;
    }
    
    /**
     * @return  Type of event.
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
}
