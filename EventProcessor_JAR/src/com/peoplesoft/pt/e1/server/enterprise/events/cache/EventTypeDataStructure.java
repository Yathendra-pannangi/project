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
 *  Describes a data structure used within an event's data payload.
 */
public class EventTypeDataStructure
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
    
    private String mDSName = null;
    
    private String mEventName = null;
    
    private Object mSpec;
    
    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Constructor.
     * 
     * @param dsName  Name of the data structure.
     * 
     * @param eventName  Event type name for the data structure.
     * 
     * @param spec    Spec for the data structor.
     */
    public EventTypeDataStructure(String dsName, String eventName, Object spec)
    {
        mDSName = dsName;
        mEventName = eventName;
        mSpec = spec;
    }
    
    //=================================================
    // Methods.
    //=================================================
    
    /**
     * This method returns a DS spec object.
     * @return data structure spec.
     */
    public Object getSpecs()
    {
        return mSpec;       
    }
    
    /**
     *  Get the data structure name.
     *  @return  data structure name.
     */
    public String getDSName()
    {
        return mDSName;
    }
    
    /**
     * @return  The event type assocated with the data structure.
     */
    public String getEventType()
    {
        return mEventName;
    }

    /**
     * @param type  Event type assocated with the data structure.
     */
    public void setEventType(String type)
    {
        mEventName = type;
    }

}
