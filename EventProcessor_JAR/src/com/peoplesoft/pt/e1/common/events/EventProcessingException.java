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
//Imports from java namespace
//=================================================
import java.io.Serializable;
//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.util.BaseChainedException;

//=================================================
//Imports from org namespace
//=================================================

/**
 * Indicates an exception within the event processing system.
 */
public class EventProcessingException extends BaseChainedException implements Serializable
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
    public EventProcessingException(String msg)
    {
        super(msg);
    }
        
    /**
     *  {@inheritDoc}
     */
    public EventProcessingException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
        
    /**
     *  {@inheritDoc}
     */
    public EventProcessingException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
