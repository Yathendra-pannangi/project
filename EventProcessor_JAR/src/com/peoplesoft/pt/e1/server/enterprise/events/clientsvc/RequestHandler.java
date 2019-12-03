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
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Interface for all event client request handlers.
 */
interface RequestHandler
{
    /**
     *  Handle a client request.
     * 
     *  @param request  The client request.
     * 
     *  @return  The response to the client.
     * 
     *  @throws EventProcessingException  an error occured during processing.
     */
    ClientResponse handleRequest(ClientRequest request) throws EventProcessingException;
}
