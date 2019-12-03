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

import java.util.ArrayList;

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
 *  This class represents the response to a request for a list of
 *  subscriptions from a remote event client.
 */
public class GetSubscriptionsResponse extends ClientResponse
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
    
    private ArrayList mSubscriptions = new ArrayList();

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  The list of subscriptions.
     * 
     *  @return  A list of <code>com.peoplesoft.pt.e1.common.events.Subscription</code>
     *           objects.
     */
    public ArrayList getSubscriptions()
    {
        return mSubscriptions;
    }

    /**
     *  Set the list of subscriptions.
     * 
     *  @param list  A list of <code>com.peoplesoft.pt.e1.common.events.Subscription</code>
     *         objects.
     */
    public void setSubscriptions(ArrayList list)
    {
        mSubscriptions = list;
    }

}
