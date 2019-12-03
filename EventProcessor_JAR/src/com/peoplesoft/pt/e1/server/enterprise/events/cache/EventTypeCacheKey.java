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
 *  Acts as the key value for an entry in the event type definition cache.
 */
final class EventTypeCacheKey
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
    
    private String mEnvironment = "";

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  Constructor.
     * 
     *  @param cat  Category.
     * 
     *  @param type  Type.
     * 
     *  @param env  Environment.
     */
    EventTypeCacheKey(String cat, String type, String env)
    {
        mCategory    = cat;
        mType        = type;
        mEnvironment = env;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
        boolean result = false;
        if (obj instanceof EventTypeCacheKey)
        {
            EventTypeCacheKey other = (EventTypeCacheKey)obj;
            result = mCategory.equals(other.mCategory) && mType.equals(other.mType)
                     && mEnvironment.equals(other.mEnvironment);
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        int code =   637 + mCategory.hashCode();
        code = code * 37 + mType.hashCode();
        code = code * 37 + mEnvironment.hashCode();
        
        return code;
    }
}
