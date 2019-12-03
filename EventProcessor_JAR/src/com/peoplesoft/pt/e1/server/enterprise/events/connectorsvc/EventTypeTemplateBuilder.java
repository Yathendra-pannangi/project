//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.connectorsvc;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * The interface that is used to generate an event template.
 */
public interface EventTypeTemplateBuilder
{
    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Performs any necessary initialization logic for the template builder.
     */
    void initialize();
    
    /**
     * Performs any necessary shutdown logic for the template builder.
     */
    void shutdown();
    
    /**
     * Returns the template for an event of the given category, type, and environment.
     * 
     * @param credentials the credential information needed to validate the user
     * @param host the EnterpriseOne server name to be used to determine Z event templates
     * @param port the EnterpriseOne server port to be used to determine Z event templates
     * @param category the event category
     * @param type the event type
     * @param environment the EnterpriseOne environment
     * @return the event type template, or null if no template is retrieved
     * @throws NotSupportedException thrown when the event template request for
     *                               the given event category is not supported
     * @throws EventProcessingException if the event processing system encounters
     *                                  other errors handling this request
     */
    String getTemplate(ConnectorCredentials credentials, String host, int port,
        String category, String type, String environment)
        throws NotSupportedException, EventProcessingException;
}
