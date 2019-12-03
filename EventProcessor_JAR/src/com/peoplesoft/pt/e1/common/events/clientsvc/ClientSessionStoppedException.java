//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.clientsvc;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Thrown when a synchronous EventSession has been stopped (or never started) and any event
 * receipt operation on that session is attempted.
 */
public class ClientSessionStoppedException extends EventProcessingException
{
    //=================================================
    //Non-public static class fields.
    //=================================================

    //=================================================
    //Public static final fields.
    //=================================================

    //=================================================
    //Instance member fields.
    //=================================================

    //=================================================
    //Constructors.
    //=================================================

    /**
     * Constructs an exception with the associated reason.
     * 
     * @param reason the reason
     */
    public ClientSessionStoppedException(String reason)
    {
        super(reason);
    }

    /**
     * Constructs an exception with the associated reason and a linked exception.
     * 
     * @param reason the reason
     * @param rootException the linked exception
     */
    public ClientSessionStoppedException(String reason, Exception rootException)
    {
        super(reason, rootException);
    }

    //=================================================
    //Methods.
    //=================================================

}