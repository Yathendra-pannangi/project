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
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ManageSessionRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ManageSessionResponse;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of session management requests.
 */
class ManageSessionRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(ManageSessionRequestHandler.class.getName());

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
        ManageSessionRequest sessionRequest = (ManageSessionRequest)request;
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                "event manage session request, userId="
                    + sessionRequest.getCredentials().getUsername()
                    + "  sessionId=" + sessionRequest.getSessionId(), null, null, null);
        }

        //
        //  Get the session object.
        //
        EventClientSession session =
            EventClientSessionManager.getSession(sessionRequest.getSessionId());
        if (session == null)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,
                "failed to find client event session, sessionId=" + sessionRequest.getSessionId(),  null, null, null);
            throw new EventProcessingException("event client session not found");
        }

        String operation = sessionRequest.getOperation();
        if (operation.equals(ManageSessionRequest.START_SESSION))
        {
            session.start();
        }
        else if (operation.equals(ManageSessionRequest.STOP_SESSION))
        {
            session.stop();
        }
        else if (operation.equals(ManageSessionRequest.CLOSE_SESSION))
        {
            session.close();
        }
        else
        {
            String msg = "unknown event session management operation: " + operation;
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null, null);
            throw new EventProcessingException(msg);
        }

        return new ManageSessionResponse();
    }
}
