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
import java.util.Iterator;
import java.util.Map;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;
import com.peoplesoft.pt.e1.common.events.ConnectorCredentials;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;
import com.peoplesoft.pt.e1.common.events.connectorsvc.IConnectorService;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeCache;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDataStructure;
import com.peoplesoft.pt.e1.server.enterprise.events.cache.EventTypeDefinition;
import com.peoplesoft.pt.e1.server.enterprise.events.common.EventXMLBuilder;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Generates an XML event template in the legacy EnterpriseOne XML format.
 */
public class EnterpriseOneLegacyTemplateBuilder implements EventTypeTemplateBuilder
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(
                                    EnterpriseOneLegacyTemplateBuilder.class.getName());
    private static EventTypeCache sEventTypeCache = EventTypeCache.getInstance();

    /** JDENET message type associated with a Z event template request. */
    private static final int Z_EVENT_TEMPLATE_REQUEST = 922;

    // jas.ini sections and settings
    private static final String JDENET_SECTION = "JDENET";
    private static final String SERVER_TIMEOUT = "enterpriseServerTimeout";

    //=================================================
    // Public static final fields.
    //=================================================
    
    //=================================================
    // Instance member fields.
    //=================================================

    //=================================================
    // Constructors.
    //=================================================

    /**
     * Default constructor.
     */
    public EnterpriseOneLegacyTemplateBuilder()
    {
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * {@inheritDoc}
     */
    public void initialize()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown()
    {
    }

    /**
     * {@inheritDoc}
     */
    public String getTemplate(ConnectorCredentials credentials, String host, int port,
        String category, String type, String environment)
        throws NotSupportedException, EventProcessingException
    {
        if (category.equals(IConnectorService.CATEGORY_REALTIME)
            || category.equals(IConnectorService.CATEGORY_XAPI))
        {
            // get list of data structures associated with event type
            EventTypeDefinition def =
                sEventTypeCache.getEventTypeDefinition(category, type, environment);
            if (def == null)
            {
                String msg =
                    "spec not found for requested event templete, event=" + category + ":" + type;
				sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                throw new EventProcessingException(msg);
            }
            Map dsList = def.getDataStructures();
            
            // retrieve the data structure names and corresponding EventTypeDataStructures
            int size = dsList.size();
            String[] dsNames = new String[size];
            EventTypeDataStructure[] eventTypeStructs = new EventTypeDataStructure[size];
            Iterator iter = dsList.keySet().iterator();
            for (int i = 0; iter.hasNext(); i++)
            {
                dsNames[i] = (String)iter.next();
                eventTypeStructs[i] = (EventTypeDataStructure)dsList.get(dsNames[i]);
            }
            
            // construct the EventMessage
            EventXMLBuilder builder = new EventXMLBuilder();
            EventMessage message = new EventMessage();
            message.setCategory(category);
            message.setType(type);
            message.setEnvironment(environment);
            
            // retrieve the template
            return builder.eventsXMLTemplateGenerator(message, dsNames, eventTypeStructs);
        }
        else if (category.equals(IConnectorService.CATEGORY_ZFILE))
        {
            // send and receive the message
            JdenetMsg msg = new JdenetMsg(host, port, getTimeout());
            String[] packets = new String[] {credentials.getUsername(), environment, type};
            String[] rcvMsg = msg.execute(Z_EVENT_TEMPLATE_REQUEST, packets);
            
            // Check format of received message:
            // rcvMsg[0]: error code ("0" means success)
            // rcvMsg[1]: template data (only sent if first packet is "0")
            int rcvMsgLength = rcvMsg.length;
            if (rcvMsgLength == 0 || rcvMsgLength > 2)
            {
                String errorMsg = "Invalid number of packets received (" + rcvMsgLength
                                  + ") from server during Z event template request.";
				sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg, null, null, null);
                throw new EventProcessingException(errorMsg);
            }
            else if (rcvMsg[0].equals("0"))
            {
                if (rcvMsgLength == 2)
                {
                    return rcvMsg[1]; // the template
                }
                else // no error in first packet, but number of packets should be 2 and only 2
                {
                    String errorMsg = "No Z event template data received from server even though "
                                      + "first packet from server indicated success in generating "
                                      + "template.";
					sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg, null, null, null);
                    throw new EventProcessingException(errorMsg);
                }
            }
            else // error generating template on server side
            {
                String errorMsg = "Error code: " + rcvMsg[0] + " received during "
                                  + "Z event template request.";
				sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg, null, null, null);
                throw new EventProcessingException(errorMsg);
            }
        }
        else
        { // workflow and any other event category is not currently supported
            String msg = "Event template request for category: " + category
                         + " is not supported.";
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new NotSupportedException(msg);
        }
    }

    /**
     *  Get the enterprise server timeout value.
     * 
     * @return the timeout value
     * @throws EventProcessingException if the timeout value is not set in the INI file
     */
    private int getTimeout() throws EventProcessingException
    {
        int port = JdeProperty.getProperty(JDENET_SECTION, SERVER_TIMEOUT, -1);
        if (port < 0)
        {
            String msg = "No enterprise server timeout value found in INI file";
			sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        return port;
    }
}
