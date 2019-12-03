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
import java.util.Iterator;
import java.util.LinkedList;
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
import com.peoplesoft.pt.e1.common.events.EventTypeDefinition;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventListRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventListResponse;
import com.peoplesoft.pt.e1.common.events.connectorsvc.ConnectorServiceFactory;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of event list requests.
 */
class EventListRequestHandler implements RequestHandler
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventListRequestHandler.class.getName());

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
        EventListRequest elRequest = (EventListRequest)request;
        
        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "event list request, userId=" + elRequest.getCredentials().getUsername(), null, null, null);
        }

        //
        //  Get an instance of the connector service interface and call it.
        //
        IConnectorService service = ConnectorServiceFactory.getConnectorService();
        ConnectorCredentials credentials = elRequest.getCredentials();
        String env = elRequest.getEnvironment();
        List eventDefinitions = null;
        if (env != null)
        {
            eventDefinitions = service.getEventList(credentials, env);
        }
        else
        {
            //
            //  This version of getEventList returns a list of strings.  Make
            //  EventTypeDefinition objects from the strings.
            //
            eventDefinitions = new LinkedList();
            List eventTypes = service.getEventList(credentials);
            for (Iterator iter = eventTypes.iterator(); iter.hasNext();)
            {
                String eventType = (String)iter.next();
                EventTypeDefinition eventDef = new EventTypeDefinition();
                eventDef.setType(eventType);
                eventDefinitions.add(eventDef);
            }
        }

        //
        //  Build the response.
        //
        EventListResponse response = new EventListResponse();
        response.setEventList(new ArrayList(eventDefinitions));
        return response;
    }
}
