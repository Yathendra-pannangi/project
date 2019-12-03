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
//Imports from java namespace
//=================================================
import java.util.HashMap;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.NoSubscriberException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;
import com.peoplesoft.pt.e1.common.events.clientsvc.ClientSessionClosedException;
import com.peoplesoft.pt.e1.common.events.clientsvc.ClientSessionStoppedException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.AcknowledgeRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.CreateSessionRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ErrorResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventListRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventTemplateRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.GetSubscriptionsRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.MSMQSubscriberRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ManageSessionRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ReceiveRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.XmlMarshalException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.XmlMarshaller;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventEnvRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventSchemaRequest;


//=================================================
//Imports from org namespace
//=================================================


/**
 *  This class provides client services to a remote event client.
 */
public class EventClientServiceLogic
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(EventClientServiceLogic.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    //
    //  HashMap of request handlers.  The map is keyed by the request class name
    //  and contains instances of request handlers.
    //
    private HashMap mRequestHandlers = new HashMap();

    private XmlMarshaller mMarshaller = null;

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Constructor.
     *
     *  @throws XmlMarshalException  Creation of the XML marshaller failed.
     */
    public EventClientServiceLogic() throws XmlMarshalException
    {
        mMarshaller = new XmlMarshaller();

        //
        //  Populate the handler hash map.
        //
        mRequestHandlers.put(EventListRequest.class.getName(), new EventListRequestHandler());
        mRequestHandlers.put(
            GetSubscriptionsRequest.class.getName(),
            new GetSubscriptionsRequestHandler());
        mRequestHandlers.put(
            EventTemplateRequest.class.getName(),
            new EventTemplateRequestHandler());
        mRequestHandlers.put(
		    EventSchemaRequest.class.getName(),
            new EventSchemaRequestHandler());
        mRequestHandlers.put(
		    EventEnvRequest.class.getName(),
            new EventListForEnvRequestHandler());
        mRequestHandlers.put(
            CreateSessionRequest.class.getName(),
            new CreateSessionRequestHandler());
        mRequestHandlers.put(
            ManageSessionRequest.class.getName(),
            new ManageSessionRequestHandler());
        mRequestHandlers.put(
            ReceiveRequest.class.getName(),
            new ReceiveRequestHandler());
        mRequestHandlers.put(
            AcknowledgeRequest.class.getName(),
            new AcknowledgeRequestHandler());
        mRequestHandlers.put(
        MSMQSubscriberRequest.class.getName(),
        new MSMQSubscriberRequestHandler());
    }

    //=================================================
    // Methods.
    //=================================================


    /**
     *  Handle requests from a remote event client.
     *
     *  @param requestXml  The request XML document.
     *
     *  @return  The response XML document.
     *
     *  @throws EventProcessingException  error while processing.
     */
    public String handleRequest(String requestXml) throws EventProcessingException
    {
        if (sE1Logger.isDebug())
        {

            int begin;
            int end;
			String tempXml1 = null;
			String tempXml2 = null;
			StringBuffer sb1 = null;
			StringBuffer sb2 = null;
			String tempStr1 = null;
			String tempStr2 = null;
			String tempStr3 = null;
			String tempStr4 = null;

		    tempXml1 = requestXml;
			begin = tempXml1.indexOf("<credentials>");
			end = tempXml1.indexOf("</credentials>");

            /* check to see if <credentials> element exists */
 			if (begin != -1 && end != -1) {
				/* not to show password in the trace */
            	begin = tempXml1.indexOf("<password>");
            	end = tempXml1.indexOf("</password>");

            	/* check to see if <password> element exists */
				if (begin != -1 && end != -1) {
	        		tempStr1 = tempXml1.substring(0, begin + 10);
	        		tempStr2 = tempXml1.substring(end, tempXml1.length());

	        		sb1 = new StringBuffer();
	        		sb1.append(tempStr1);
	        		sb1.append("******");
	        		sb1.append(tempStr2);

	        		tempXml2 = sb1.toString();

	        		/* not to show <security-token> in the trace */
					begin = tempXml2.indexOf("<security-token>");
					end = tempXml2.indexOf("</security-token>");

					/* check to see if <security-token> element exists */
					if (begin != -1 && end != -1) {
						tempStr3 = tempXml2.substring(0, begin + 16);
						tempStr4 = tempXml2.substring(end, tempXml2.length());

						sb2 = new StringBuffer();
						sb2.append(tempStr3);
						sb2.append("******");
						sb2.append(tempStr4);

						/* trace sb2 with both <password> and <security-token> hidden */
                		sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event client request: " + sb2.toString(), null, null,null );
					} else {
						/* no <security-token>, trace sb1 with <password> hidden */
                		sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event client request: " + sb1.toString(), null, null,null );
					}
				} else {
					/* no <password> element */
					begin = tempXml1.indexOf("<security-token>");
					end = tempXml1.indexOf("</security-token>");

					/* check to see if <security-token> element exists */
					if (begin != -1 && end != -1) {
						tempStr3 = tempXml1.substring(0, begin + 16);
						tempStr4 = tempXml1.substring(end, tempXml1.length());

						sb2 = new StringBuffer();
						sb2.append(tempStr3);
						sb2.append("******");
						sb2.append(tempStr4);

						/* trace sb2 with <security-token> hidden */
                		sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event client request: " + sb2.toString(), null, null,null );
					} else {
						/* no <password> and <security-token>, trace as is */
                		sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event client request: " + requestXml, null, null,null );
					}
				}
            } else {
            	/* no <credentials> element in the XML string, trace as is */
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event client request: " + requestXml, null, null,null );
            }
        }

        //
        //  Unmarshal the request.
        //
        ClientRequest request = null;
        try
        {
            request = mMarshaller.unmarshalRequest(requestXml);
        }
        catch (XmlMarshalException e)
        {
            String msg = "failed to unmarshal event client XML request: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,  null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Call the approprate handler.
        //
        RequestHandler handler = (RequestHandler)mRequestHandlers.get(request.getClass().getName());
        ClientResponse response = null;
        if (handler != null)
        {
            try
            {
                response = handler.handleRequest(request);
            }
            catch (NotSupportedException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,e.getMessage(), null, null, e);
                response =
                    new ErrorResponse(ErrorResponse.NOT_SUPPORTED, e.getMessage());
            }
            catch (InvalidCredentialsException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,e.getMessage(), null, null,  e);
                response =
                    new ErrorResponse(ErrorResponse.INVALID_CREDENTIALS_EXCEPTION, e.getMessage());
            }
            catch (NoSubscriberException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,e.getMessage(), null, null, e);
                response =
                    new ErrorResponse(ErrorResponse.NO_SUCH_SUBSCRIBER_EXCEPTION, e.getMessage());
            }
            catch (ClientSessionStoppedException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,e.getMessage(), null, null, e);
                response =
                    new ErrorResponse(ErrorResponse.STOPPED_SESSION_EXCEPTION, e.getMessage());
            }
            catch (ClientSessionClosedException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,e.getMessage(), null, null,  e);
                response =
                    new ErrorResponse(ErrorResponse.CLOSED_SESSION_EXCEPTION, e.getMessage());
            }
            catch (EventProcessingException e)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,e.getMessage(),  null, null, e);
                response =
                    new ErrorResponse(ErrorResponse.EVENT_PROCESSING_EXCEPTION, e.getMessage());
            }
            catch (Throwable t)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"unable to process event client request: " + t.getMessage(), null, null, t);
                ErrorResponse error = new ErrorResponse();
                error.setErrorMessage("unable to process request: " + t.getMessage());
                response = error;
            }
        }
        else
        {
            String msg =
                "no handler for event client request type: " + request.getClass().getName();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            ErrorResponse error = new ErrorResponse();
            error.setErrorMessage(msg);
            error.setErrorCode(ErrorResponse.NOT_SUPPORTED);
            response = error;
        }

        //
        //  Marshal the response.
        //
        String responseXml = null;
        try
        {
            responseXml = mMarshaller.marshalResponse(response);
        }
        catch (XmlMarshalException e)
        {
            String msg = "failed to marshal event client XML response: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        if (sE1Logger.isDebug())
        {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"event client response: " + responseXml, null, null, null);
        }

        return responseXml;
    }
}
