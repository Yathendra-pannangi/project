//============================================================================
//
// Copyright � [2004] 
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
 *  This class represents a request for a list of defined events from
 *  a remote event client.
 */
public class EventListRequest extends ClientRequest
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
    
    private String mEnvironment = null;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return The requested environment.
     */
    public String getEnvironment()
    {
        return mEnvironment;
    }

    /**
     * @param env  The requested environment.
     */
    public void setEnvironment(String env)
    {
        mEnvironment = env;
    }

}
