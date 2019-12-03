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
 * Thrown when a given username does not have an associated subscriber.
 */
public class NoSubscriberException extends EventProcessingException
{
    /**
     * {@inheritDoc}
     */
    public NoSubscriberException(String msg)
    {
        super(msg);
    }

    /**
     * {@inheritDoc}
     */
    public NoSubscriberException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * {@inheritDoc}
     */
    public NoSubscriberException(Throwable cause)
    {
        super(cause);
    }
}
