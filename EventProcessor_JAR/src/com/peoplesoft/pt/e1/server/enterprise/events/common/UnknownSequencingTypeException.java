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
 * Indacates that an unknown event sequencing type was encountered.
 */
public class UnknownSequencingTypeException extends EventProcessingException
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
    public UnknownSequencingTypeException(String msg)
    {
        super(msg);
    }
    
    /**
     *  {@inheritDoc}
     */
    public UnknownSequencingTypeException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    /**
     *  {@inheritDoc}
     */
    public UnknownSequencingTypeException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
