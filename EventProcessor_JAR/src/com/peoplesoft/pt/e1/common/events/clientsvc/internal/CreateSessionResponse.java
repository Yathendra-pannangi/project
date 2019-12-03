//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.clientsvc.internal;

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
 *  This class represents the response to a request to create a
 *  new session from a remote event client.
 */
public class CreateSessionResponse extends ClientResponse
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
    
    private String mSessionId;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    

    /**
     * @return  the session ID of the new session.
     */
    public String getSessionId()
    {
        return mSessionId;
    }

    /**
     * @param sessionId  the session ID of the new session.
     */
    public void setSessionId(String sessionId)
    {
        mSessionId = sessionId;
    }

}
