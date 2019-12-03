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
import java.util.Iterator;

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
 * Enumerates the various event sequencing algorithms the event system supports.
 */
public final class EventSequencingType implements Serializable
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static HashMap sInstances = new HashMap();

    //=================================================
    // Public static final fields.
    //=================================================
    
    /** Events can be delivered in any sequence. */
    public static final EventSequencingType ANY_SEQUENCE
        = new EventSequencingType(10, "AnySequence");
    
    /** Events are guaranteed to be deliver in sequence by event type. */
    public static final EventSequencingType BY_EVENT_TYPE
        = new EventSequencingType(20, "ByEventType");
    
    /** 
     *  Events are guaranteed to be deliver in sequence by originating
     *  user session.
     */
    public static final EventSequencingType BY_USER_SESSION
        = new EventSequencingType(30, "ByUserSession");
    
    /**
     *  Events are guaranteed to be deliver in sequence by their original
     *  generation sequence.
     *  <br>
     *  Note: This option does not permit concurrent processing and may impact
     *  performance.
     */
    public static final EventSequencingType GLOBAL_FIFO
        = new EventSequencingType(40, "GlobalFIFO");

    //=================================================
    // Instance member fields.
    //=================================================
    
    private final int     mValue;
    
    private final String  mLabel;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Private constructor to prevent external creation of instances.
     */
    private EventSequencingType(int value, String label)
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
        
        if (other instanceof EventSequencingType)
        {
            EventSequencingType otherState = (EventSequencingType)other;
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
     * Get the event event sequencing type represented by a string value.
     * 
     * @param value  The string value of a sequencing type.  The case of the value 
     *               is ignored when searching for a matching type.
     * 
     * @return  The requested sequencing type.
     * 
     * @throws UnknownSequencingTypeException  The string value specified does
     *         not correspond to any sequencing type.
     */
    public static EventSequencingType getSequencingType(String value)
        throws UnknownSequencingTypeException
    {
        EventSequencingType result = null;
        
        Iterator iter = sInstances.values().iterator();
        while (iter.hasNext())
        {
            EventSequencingType type = (EventSequencingType)iter.next();
            if (value.equalsIgnoreCase(type.mLabel))
            {
                result = type;
                break;
            }
        }
        
        if (result == null)
        {
            throw new UnknownSequencingTypeException("Label=" + value);
        }
        
        return result;
    }
    
    /**
     *  Get the string label.
     * 
     *  @return  The string label.
     */
    public String getLabel()
    {
        return mLabel;
    }
}
