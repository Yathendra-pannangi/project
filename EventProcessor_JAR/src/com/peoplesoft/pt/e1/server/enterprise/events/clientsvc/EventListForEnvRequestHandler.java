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
import java.util.ArrayList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;

import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventEnvRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventEnvResponse;
import com.peoplesoft.pt.e1.common.events.connectorsvc.ConnectorServiceFactory;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of event list requests.
 */
class EventListForEnvRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================

	private static E1Logger sE1Logger = JdeLog.getE1Logger(EventListForEnvRequestHandler.class.getName());

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
        EventEnvRequest elRequest = (EventEnvRequest)request;

        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event list request, userId=" + elRequest.getCredentials().getUsername(),null,null,null);
        }

        //
        //  Get an instance of the connector service interface and call it.
        //
        IConnectorService service = ConnectorServiceFactory.getConnectorService();
        ConnectorCredentials credentials = elRequest.getCredentials();
        String env = elRequest.getEnvironment();
        List eventDefinitions = null;
        eventDefinitions = service.getEventListForEnv(credentials, env);
        //
        //  Build the response.
        //
        EventEnvResponse response = new EventEnvResponse();
        response.setEventEnv(new ArrayList(eventDefinitions));
        return response;
    }
}
