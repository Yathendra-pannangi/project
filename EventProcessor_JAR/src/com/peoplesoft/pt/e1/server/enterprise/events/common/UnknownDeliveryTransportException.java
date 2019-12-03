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

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Indacates that an unknown event state was encountered.
 */
public class UnknownDeliveryTransportException extends EventProcessingException
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

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     *  {@inheritDoc}
     */
    public UnknownDeliveryTransportException(String msg)
    {
        super(msg);
    }
    
    /**
     *  {@inheritDoc}
     */
    public UnknownDeliveryTransportException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    /**
     *  {@inheritDoc}
     */
    public UnknownDeliveryTransportException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
