//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks
// of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.clientsvc;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ReceiveRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ReceiveResponse;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of receive requests.
 */
class ReceiveRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(ReceiveRequestHandler.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see com.peoplesoft.pt.e1.server.enterprise.events.clientsvc.
     *      RequestHandler#handleRequest(com.peoplesoft.pt.e1.common.
     *      events.clientsvc.internal.ClientRequest)
     */
    public ClientResponse handleRequest(ClientRequest request) throws EventProcessingException
    {
        ReceiveRequest receiveRequest = (ReceiveRequest)request;
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                "event receive request, userId="
                    + receiveRequest.getCredentials().getUsername()
                    + "  sessionId=" + receiveRequest.getSessionId(), null, null, null);
        }

        //
        //  Get the session object.
        //
        EventClientSession session =
            EventClientSessionManager.getSession(receiveRequest.getSessionId());
        if (session == null)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, 
                "failed to find client event session, sessionId=" + receiveRequest.getSessionId(), null, null, null);
            throw new EventProcessingException("event client session not found");
        }

        long timeout = receiveRequest.getTimeout();
        EventMessage event = session.receive(timeout);
        ReceiveResponse response = new ReceiveResponse();
        response.setEvent(event);

        return response;
    }
}
