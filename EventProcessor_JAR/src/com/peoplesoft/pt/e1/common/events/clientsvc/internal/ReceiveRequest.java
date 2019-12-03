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
 *  This class represents a request to receive an event from
 *  a remote event client.
 */
public class ReceiveRequest extends ClientRequest
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
    
    private long mTimeout;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return  the timeout value.
     */
    public long getTimeout()
    {
        return mTimeout;
    }

    /**
     * @param t  the timeout value.
     */
    public void setTimeout(long t)
    {
        mTimeout = t;
    }

    /**
     * @return  the session ID.
     */
    public String getSessionId()
    {
        return mSessionId;
    }

    /**
     * @param id  the session ID.
     */
    public void setSessionId(String id)
    {
        mSessionId = id;
    }

}
