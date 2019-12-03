//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.connectorsvc;

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
 *  @deprecated  replaced by com.peoplesoft.pt.e1.common.events.NoSubscriberException
 */
public class NoSubscriberException extends com.peoplesoft.pt.e1.common.events.NoSubscriberException
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
