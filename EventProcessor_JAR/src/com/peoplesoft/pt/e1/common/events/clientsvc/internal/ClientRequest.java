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
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;

//=================================================
// Imports from org namespace
//=================================================

/**
 * This class is the base class for all requests from an event client.
 */
public abstract class ClientRequest
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
    
    private ConnectorCredentials  mCredentials;
    
    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  The connector credentials.
     */
    public ConnectorCredentials getCredentials()
    {
        return mCredentials;
    }

    /**
     * @param credentials  The connector credentails.
     */
    public void setCredentials(ConnectorCredentials credentials)
    {
        mCredentials = credentials;
    }

}

