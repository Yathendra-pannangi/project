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
import com.jdedwards.system.security.SecurityServer;
import com.jdedwards.system.security.SecurityServerException;
import com.jdedwards.system.security.SecurityServerInstance;
import com.jdedwards.system.security.SecurityServerResponse;
import com.jdedwards.system.security.SecurityToken;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.CreateSessionRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.CreateSessionResponse;
import com.peoplesoft.pt.e1.common.events.connectorsvc.ConnectorServiceFactory;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of event list requests.
 */
class CreateSessionRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(CreateSessionRequestHandler.class.getName());

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
        CreateSessionRequest sessionRequest = (CreateSessionRequest)request;
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                "create event session request, userId="
                    + sessionRequest.getCredentials().getUsername(), null, null, null);
        }
        
        //
        //  Verify that the security token is valid.
        //
        try
        {
			IConnectorService service = ConnectorServiceFactory.getConnectorService();		  
			boolean isMSMQUser = service.isMSMQUser(request.getCredentials());
			if(!isMSMQUser)
			{
			SecurityServer secServer = SecurityServerInstance.getInstance();
            SecurityToken token = new SecurityToken(request.getCredentials().getSecurityToken());
            SecurityServerResponse secResponse = secServer.validateInteropToken(token);
            if (!secResponse.isLoginValid())
            {
                throw new InvalidCredentialsException("security token is not valid");
            }
			}
        }
        catch (SecurityServerException e)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"failed to locate security server: " + e.getMessage(), null, null, e);
            throw new EventProcessingException("can not validate security token");
        }

        //
        //  Create the session.
        //
        EventClientSession session =
            EventClientSessionManager.createSession(sessionRequest.getCredentials());

        //
        //  Build the response.
        //
        CreateSessionResponse response = new CreateSessionResponse();
        response.setSessionId(session.getSessionId());
        return response;
    }
}
