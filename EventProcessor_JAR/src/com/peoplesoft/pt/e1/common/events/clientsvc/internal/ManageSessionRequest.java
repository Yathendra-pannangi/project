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
 *  This class represents a request to start a session from
 *  a remote event client.
 */
public class ManageSessionRequest extends ClientRequest
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    //=================================================
    // Public static final fields.
    //=================================================
    
    /**  A request to start the session.  */
    public static final String START_SESSION = "start-session";
    
    /**  A request to stop the session.  */
    public static final String STOP_SESSION = "stop-session";
    
    /**  A request to close the session.  */
    public static final String CLOSE_SESSION = "close-session";

    //=================================================
    // Instance member fields.
    //=================================================
    
    private String mSessionId;
    
    private String mOperation;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return  session ID.
     */
    public String getSessionId()
    {
        return mSessionId;
    }

    /**
     * @param id  session ID.
     */
    public void setSessionId(String id)
    {
        mSessionId = id;
    }

    /**
     * @return  the operation.
     */
    public String getOperation()
    {
        return mOperation;
    }

    /**
     * @param op  the operation.
     */
    public void setOperation(String op)
    {
        mOperation = op;
    }

}
