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
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.GetSubscriptionsRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.GetSubscriptionsResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.MSMQSubscriberRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.MSMQSubscriberResponse;
import com.peoplesoft.pt.e1.common.events.connectorsvc.ConnectorServiceFactory;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;

//=================================================
// Imports from java namespace
//=================================================
import java.util.ArrayList;
import java.util.List;


//=================================================
// Imports from org namespace
//=================================================

/**
 *  Handler of get subscriptions requests.
 */
public class MSMQSubscriberRequestHandler implements RequestHandler {
    //	=================================================
    // Non-public static class fields.
    //=================================================
    private static E1Logger sE1Logger = JdeLog.getE1Logger(MSMQSubscriberRequestHandler.class.getName());

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
    public ClientResponse handleRequest(ClientRequest request)
        throws EventProcessingException {
        MSMQSubscriberRequest subRequest = (MSMQSubscriberRequest) request;

        if (sE1Logger.isDebug()) {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "MSMQ Subscriber list request", null,null, null);
        }

        //
        //  Get an instance of the connector service interface and call it.
        //
        IConnectorService service = ConnectorServiceFactory.getConnectorService();
        List msmqSubscribers = service.getMSMQInfo();

        //
        //  Build the response.
        //
        MSMQSubscriberResponse response = new MSMQSubscriberResponse();
        response.setMSMQSubscribers(new ArrayList(msmqSubscribers));

        return response;
    }
}
