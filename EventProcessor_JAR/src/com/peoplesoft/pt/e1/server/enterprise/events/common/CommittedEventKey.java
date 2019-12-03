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
import java.io.Serializable;

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
 *  Represents the key information for an event that has been committed to the database.
 */
public class CommittedEventKey implements Serializable
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
    
    private String      mEventID;
    
    private EventState  mState;
    
    private String      mCategory;
    
    private String      mType;
    
    private String      mOriginatingEnvironment;
    
    private String      mOriginatingUserSession;
    
    private long        mSequenceNumber;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  The event category.
     */
    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @return  The originating user environment.
     */
    public String getOriginatingEnvironment()
    {
        return mOriginatingEnvironment;
    }

    /**
     * @return  The originating user session ID.
     */
    public String getOriginatingUserSession()
    {
        return mOriginatingUserSession;
    }

    /**
     * @return  The event state.
     */
    public EventState getState()
    {
        return mState;
    }

    /**
     * @return  The event type.
     */
    public String getType()
    {
        return mType;
    }

    /**
     * @param string  The event category
     */
    public void setCategory(String string)
    {
        mCategory = string;
    }

    /**
     * @param string  The originating environment.
     */
    public void setOriginatingEnvironment(String string)
    {
        mOriginatingEnvironment = string;
    }

    /**
     * @param string  The originating user session ID.
     */
    public void setOriginatingUserSession(String string)
    {
        mOriginatingUserSession = string;
    }

    /**
     * @param state  The event state.
     */
    public void setState(EventState state)
    {
        mState = state;
    }

    /**
     * @param string  The event type.
     */
    public void setType(String string)
    {
        mType = string;
    }

    /**
     * @return  The event sequence number.
     */
    public long getSequenceNumber()
    {
        return mSequenceNumber;
    }

    /**
     * @param l  The event sequence number.
     */
    public void setSequenceNumber(long l)
    {
        mSequenceNumber = l;
    }

    /**
     * @return  Event ID.
     */
    public String getEventID()
    {
        return mEventID;
    }

    /**
     * @param id  Event ID.
     */
    public void setEventID(String id)
    {
        mEventID = id;
    }
}
