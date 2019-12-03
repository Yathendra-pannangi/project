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
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.AcknowledgeRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.AcknowledgeResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of acknowledge requests.
 */
class AcknowledgeRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(AcknowledgeRequestHandler.class.getName());

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
        AcknowledgeRequest ackRequest = (AcknowledgeRequest)request;
        
        if (sE1Logger.isDebug())
        {
            String msg = "acknowledge event request, sessionId=" + ackRequest.getSessionId();
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
        }

        //
        //  Get the session object.
        //
        EventClientSession session =
            EventClientSessionManager.getSession(ackRequest.getSessionId());
        if (session == null)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                "failed to find client event session, sessionId=" + ackRequest.getSessionId(), null, null, null);
            throw new EventProcessingException("event client session not found");
        }
        
        //
        //  Acknowledge events.
        //
        session.acknowledgeEvents();

        return new AcknowledgeResponse();
    }
}
