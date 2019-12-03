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
import java.util.ArrayList;

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
 *  This class represents the response to a request for a list of defined
 *  events from a remote event client.
 */
public class EventListResponse extends ClientResponse
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
    
    private ArrayList mEventList = new ArrayList();

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  The list of defined event types.
     * 
     *  @return  A list of <code>com.peoplesoft.pt.e1.common.events.EventTypeDefinition</code>
     *           objects.
     */
    public ArrayList getEventList()
    {
        return mEventList;
    }

    /**
     *  Set the list of event type definitions.
     * 
     *  @param list  A list of <code>com.peoplesoft.pt.e1.common.events.EventTypeDefinition</code>
     *         objects.
     */
    public void setEventList(ArrayList list)
    {
        mEventList = list;
    }

}
