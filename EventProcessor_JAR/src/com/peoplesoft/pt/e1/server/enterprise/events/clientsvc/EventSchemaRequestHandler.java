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
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventSchemaRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventSchemaResponse;
import com.peoplesoft.pt.e1.common.events.connectorsvc.ConnectorServiceFactory;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of event template requests.
 */
class EventSchemaRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventSchemaRequestHandler.class.getName());

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
        EventSchemaRequest schemaRequest = (EventSchemaRequest)request;

        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,
                "event schema request, userId=" + schemaRequest.getCredentials().getUsername(),null,null,null);
        }

        //
        //  Get an instance of the connector service interface and call it.
        //
        IConnectorService service = ConnectorServiceFactory.getConnectorService();
        String schema =
            service.getEventSchema(
                schemaRequest.getCredentials(),
                schemaRequest.getHost(),
                schemaRequest.getPort(),
                schemaRequest.getCategory(),
                schemaRequest.getType(),
                schemaRequest.getEnvironment());

        //
        //  Build the response.
        //
        EventSchemaResponse response = new EventSchemaResponse();
        response.setSchemaXml(schema);
        return response;
    }
}
