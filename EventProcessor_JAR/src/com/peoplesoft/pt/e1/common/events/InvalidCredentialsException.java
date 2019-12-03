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
 * Indicates an invalid combination of credentials has been submitted
 * to the events processing system.
 */
public class InvalidCredentialsException extends EventProcessingException
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
     *  {@inheritDoc}
     */
    public InvalidCredentialsException(String msg)
    {
        super(msg);
    }

    /**
     *  {@inheritDoc}
     */
    public InvalidCredentialsException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     *  {@inheritDoc}
     */
    public InvalidCredentialsException(Throwable cause)
    {
        super(cause);
    }

    //=================================================
    // Methods.
    //=================================================

}
