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
import java.util.HashMap;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.server.enterprise.events.db.F90710_EventTransfer;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Represents the various states that an event can exist in while moving
 * through the system.
 */
public final class EventState implements Serializable
{
    //=================================================
    // Public static final fields.
    //=================================================
    
    private static HashMap sInstances = new HashMap();
    
    /** Committed event state. */
    public static final EventState COMMITTED = 
        new EventState(F90710_EventTransfer.STATE_COMMITTED, "Committed");
    
    /** InProcess event state. */
    public static final EventState IN_PROCESS = 
        new EventState(F90710_EventTransfer.STATE_IN_PROCESS, "InProcess");
    
    /** Processed event state. */
    public static final EventState PROCESSED = 
        new EventState(F90710_EventTransfer.STATE_PROCESSED, "Processed");
    
    /** Failed event state. */
    public static final EventState FAILED = 
        new EventState(F90710_EventTransfer.STATE_FAILED, "Failed");

    //=================================================
    // Static class fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private int     mValue;
    
    private String  mLabel;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Private constructor to prevent external creation of instances.
     */
    private EventState(int value, String label)
    {
        mValue = value;
        mLabel = label;
        
        sInstances.put(new Integer(value), this);
    }

    //=================================================
    // Methods.
    //=================================================
        
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other)
    {
        boolean result = false;
        
        if (other instanceof EventState)
        {
            EventState otherState = (EventState)other;
            if (otherState.mValue == mValue)
            {
                result = true;
            }
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return mValue;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return mLabel + ":" + Integer.toString(mValue);
    }
    
    /**
     * Get the event state represented by a numeric value.
     * 
     * @param value  The numeric value of an event state.
     * 
     * @return  The requested event state.
     * 
     * @throws UnknownEventStateException  The numeric value specified does
     *         not correspond to any known event state.
     */
    public static EventState getState(int value) throws UnknownEventStateException
    {
        EventState result = (EventState)sInstances.get(new Integer(value));
        if (result == null)
        {
            throw new UnknownEventStateException("Unknown event state=" + value);
        }
        return result;
    }
    
    /**
     * @return Event state value.
     */
    public int getValue()
    {
        return mValue;
    }

}
