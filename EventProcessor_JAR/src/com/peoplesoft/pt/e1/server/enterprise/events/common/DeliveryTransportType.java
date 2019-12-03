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

import com.peoplesoft.pt.e1.server.enterprise.events.db.F90706_EventSubscriber;

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
 * Enumerates the various event delivery transports the event system supports.
 */
public final class DeliveryTransportType implements Serializable
{
   
   
    //=================================================
    // Public static final fields.
    //=================================================
    
    private static HashMap sInstances = new HashMap();
    
    /** Java connector. */
    public static final DeliveryTransportType JAVA_CONNECTOR
        = new DeliveryTransportType(10, F90706_EventSubscriber.TRANSPORT_JAVA_CONNECTOR);
    
    /** COM connector. */
    public static final DeliveryTransportType COM_CONNECTOR
        = new DeliveryTransportType(20, F90706_EventSubscriber.TRANSPORT_COM_CONNECTOR);
    
    /** JDENET. */
    public static final DeliveryTransportType JDENET
        = new DeliveryTransportType(30, F90706_EventSubscriber.TRANSPORT_JDENET);
    
    /** MSMQ. */
    public static final DeliveryTransportType MSMQ
        = new DeliveryTransportType(40, F90706_EventSubscriber.TRANSPORT_MSMQ);
    
    /** MQSeries Queue. */
    public static final DeliveryTransportType MQSERIES_QUEUE 
        = new DeliveryTransportType(50, F90706_EventSubscriber.TRANSPORT_MQSERIES_QUEUE);

	/** JMS Topic. */
	
	public static final DeliveryTransportType JMSTOPIC
		= new DeliveryTransportType(60, F90706_EventSubscriber.TRANSPORT_JMS_TOPIC);


    /** JMS Queue. */

    public static final DeliveryTransportType JMSQUEUE
            = new DeliveryTransportType(70, F90706_EventSubscriber.TRANSPORT_JMS_QUEUE);



    //=================================================
    // Static class fields.
    //=================================================

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
    private DeliveryTransportType(int value, String label)
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
        
        if (other instanceof DeliveryTransportType)
        {
            DeliveryTransportType otherState = (DeliveryTransportType)other;
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
     * Get the delivery transport type represented by a numeric value.
     * 
     * @param value  The numeric value of an event state.
     * 
     * @return  The requested delivery transport type.
     * 
     * @throws UnknownDeliveryTransportException  The numeric value specified does
     *         not correspond to any delivery transport.
     */
    public static DeliveryTransportType getTransportType(int value)
        throws UnknownDeliveryTransportException
    {
        
        DeliveryTransportType result = (DeliveryTransportType)sInstances.get(new Integer(value));
        if (result == null)
        {
            throw new UnknownDeliveryTransportException("Unknown delivery transport type=" 
                                                        + value);
        }        
        return result;
    }
    
    /**
     * Get the delivery transport type represented by a numeric value.
     * 
     * @param value  The string value of an delivery transport.  The case of the 
     *               value is ignored when searching for a matching transport type.
     * 
     * @return  The requested delivery transport type.
     * 
     * @throws UnknownDeliveryTransportException  The string value specified does
     *         not correspond to any delivery transport.
     */
    public static DeliveryTransportType getTransportType(String value)
        throws UnknownDeliveryTransportException
    {
        if (value == null)
        {
            throw new UnknownDeliveryTransportException("Label=null");
        }
        
        DeliveryTransportType result = null;
        Iterator iter = sInstances.values().iterator();
        while (iter.hasNext())
        {
            DeliveryTransportType transport = (DeliveryTransportType)iter.next();
            if (value.equalsIgnoreCase(transport.mLabel))
            {
                result = transport;
                break;
            }
        }
        
        if (result == null)
        {
            throw new UnknownDeliveryTransportException("Label=" + value);
        }
        
        return result;
    }
}
