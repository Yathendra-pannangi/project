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
 *  Indicates an error has occurd while attempting to communicate with the
 *  client service on the server.
 */
public class ClientServiceAccessException extends EventProcessingException
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
    public ClientServiceAccessException(String reason)
    {
        super(reason);
    }

    /**
     * Constructs an exception with the associated reason and a linked exception.
     * 
     * @param reason the reason
     * @param rootException the linked exception
     */
    public ClientServiceAccessException(String reason, Exception rootException)
    {
        super(reason, rootException);
    }

    //=================================================
    //Methods.
    //=================================================

}