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
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================

/**
 * Thrown when a request is made for something in the events processing
 * system that is an unsupported feature.
 */
public class NotSupportedException extends EventProcessingException
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     * {@inheritDoc}
     */
    public NotSupportedException(String msg)
    {
        super(msg);
    }

    /**
     * {@inheritDoc}
     */
    public NotSupportedException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * {@inheritDoc}
     */
    public NotSupportedException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================
}
