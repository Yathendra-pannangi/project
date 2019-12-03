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

import com.peoplesoft.pt.e1.common.events.EventMessage;

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
 *  This class represents the response to a request to receive the next event
 *  from a remote event client.
 */
public class ReceiveResponse extends ClientResponse
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
    
    private EventMessage mEvent = null;

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * @return  the received event or <code>null</code> if the receive request
     *          timed out before an event was available.
     */
    public EventMessage getEvent()
    {
        return mEvent;
    }

    /**
     * @param event  the event message.
     */
    public void setEvent(EventMessage event)
    {
        mEvent = event;
    }

}
