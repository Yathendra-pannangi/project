//============================================================================
//
// Copyright © [2004]
// PeopleSoft, Inc.
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.common.events.clientsvc;


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
import com.peoplesoft.pt.e1.common.events.EventTypeDefinition;
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.NoSubscriberException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.AcknowledgeRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.CharacterEncoding;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ClientResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.CreateSessionRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.CreateSessionResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ErrorResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventEnvRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventEnvResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventListRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventListResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventTemplateRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventTemplateResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventSchemaRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.EventSchemaResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.GetSubscriptionsRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.GetSubscriptionsResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.MSMQSubscriberRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.MSMQSubscriberResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ManageSessionRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ReceiveRequest;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.ReceiveResponse;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.XmlMarshalException;
import com.peoplesoft.pt.e1.common.events.clientsvc.internal.XmlMarshaller;

//=================================================
// Imports from java namespace
//=================================================
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;
import java.util.LinkedList;


//=================================================
// Imports from org namespace
//=================================================

/**
 *  This class provides remote event clients with access to the server without
 *  using the J2EE client functionality.
 */
public class ClientService {
    //=================================================
    // Non-public static class fields.
    //=================================================
    private static E1Logger sE1Logger = JdeLog.getE1Logger(ClientService.class.getName());
    private static final String SECTION = "EVENTS";
    private static final String SERVICE_URL = "eventServiceURL";

    //=================================================
    // Public static final fields.
    //=================================================
    //=================================================
    // Instance member fields.
    //=================================================
    private URL mServiceUrl = null;
    private XmlMarshaller mMarshaller = null;
    private EventEnvResponse response;

    private String sessionId = null;
    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Constructor.
     *
     *  @throws EventProcessingException  Failed to initialize.
     */
    public ClientService() throws EventProcessingException {
        String serviceUrlString = JdeProperty.getProperty(SECTION, SERVICE_URL,
                null);

        if ((serviceUrlString == null) || (serviceUrlString.length() == 0)) {
            String msg = "failed to get event service URL from INI file";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }

        try {
            mServiceUrl = new URL(serviceUrlString);
        } catch (MalformedURLException e) {
            String msg = "invalid event service URL from INI file: " +
                serviceUrlString;
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventProcessingException(msg, e);
        }

        try {
            mMarshaller = new XmlMarshaller();
        } catch (XmlMarshalException e) {
            String msg = "failed to create event XML marshaller: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }

    public ClientService(String serviceUrlString) throws EventProcessingException {

        if ((serviceUrlString == null) || (serviceUrlString.length() == 0)) {
            String msg = "failed to get event service URL from INI file";
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }

        try {
            mServiceUrl = new URL(serviceUrlString);
        } catch (MalformedURLException e) {
            String msg = "invalid event service URL from INI file: " +
                serviceUrlString;
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new EventProcessingException(msg, e);
        }

        try {
            mMarshaller = new XmlMarshaller();
        } catch (XmlMarshalException e) {
            String msg = "failed to create event XML marshaller: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * Retrieves all the subscriptions for the user.
     *
     * @param credentials the credential information needed to validate the user
     * @return a LinkedList of com.peoplesoft.pt.e1.common.events.Subscription
     *         objects
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws NoSubscriberException if there is no matching subscriber for the given username
     * @throws ClientServiceAccessException  error while attemping to communicate with server
     * @throws EventProcessingException if the subscribers cannot be retrieved from the cache
     */
    public LinkedList getSubscriptions(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException,
            ClientServiceAccessException, EventProcessingException {
        //
        //  Build the request.
        //
        GetSubscriptionsRequest request = new GetSubscriptionsRequest();
        request.setCredentials(credentials);

        //
        //  Send the request.
        //
        GetSubscriptionsResponse response = (GetSubscriptionsResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        LinkedList result = new LinkedList(response.getSubscriptions());

        return result;
    }

    /**
     * Retrieves the event template for the given event category, event type,
     * and EnterpriseOne environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param host the EnterpriseOne server name to be used to retrieve Z event templates
     * @param port the EnterpriseOne server port to be used to retrieve Z event templates
     * @param category the event category
     * @param type the event type
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return the event template
     * @throws NotSupportedException thrown when the event template request for
     *                               the given event category is not supported
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws ClientServiceAccessException  error while attemping to communicate with server
     * @throws EventProcessingException if the event processing system encounters
     *                                  other errors handling this request
     */
    public String getEventTemplate(ConnectorCredentials credentials,
        String host, int port, String category, String type, String environment)
        throws NotSupportedException, InvalidCredentialsException,
            ClientServiceAccessException, EventProcessingException {
        //
        //  Build the request.
        //
        EventTemplateRequest request = new EventTemplateRequest();
        request.setCredentials(credentials);
        request.setHost(host);
        request.setPort(port);
        request.setCategory(category);
        request.setType(type);
        request.setEnvironment(environment);

        //
        //  Send the request.
        //
        EventTemplateResponse response = (EventTemplateResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        String template = response.getTemplateXml();

        return template;
    }
    /**
     * Retrieves the event template for the given event category, event type,
     * and EnterpriseOne environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param host the EnterpriseOne server name to be used to retrieve Z event templates
     * @param port the EnterpriseOne server port to be used to retrieve Z event templates
     * @param category the event category
     * @param type the event type
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return the event template
     * @throws NotSupportedException thrown when the event template request for
     *                               the given event category is not supported
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws ClientServiceAccessException  error while attemping to communicate with server
     * @throws EventProcessingException if the event processing system encounters
     *                                  other errors handling this request
     */
    public String getEventSchema(ConnectorCredentials credentials,
        String host, int port, String category, String type, String environment)
        throws NotSupportedException, InvalidCredentialsException,
            ClientServiceAccessException, EventProcessingException {

        String template = getEventTemplate(credentials,host,port,category,type,environment);

        //
        //  Build the request.
        //
        EventSchemaRequest request = new EventSchemaRequest();
        request.setCredentials(credentials);
        request.setHost(host);
        request.setPort(port);
        request.setCategory(category);
        request.setType(type);
        request.setEnvironment(environment);

        //
        //  Send the request.
        //
        EventSchemaResponse response = (EventSchemaResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        String schema = response.getSchemaXml();

        return schema;
    }
    /**
     * Retrieves the list of event types, both active and inactive,
     * that are valid for a given environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return a LinkedList of com.peoplesoft.pt.e1.common.events.EventTypeDefinition
     *         objects
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws ClientServiceAccessException  error while attemping to communicate with server
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     */
    public LinkedList getEventList(ConnectorCredentials credentials,
        String environment)
        throws InvalidCredentialsException, ClientServiceAccessException,
            EventProcessingException {
        //
        //  Build the request.
        //
        EventListRequest request = new EventListRequest();
        request.setCredentials(credentials);
        request.setEnvironment(environment);

        //
        //  Send the request.
        //
        EventListResponse response = (EventListResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        LinkedList result = new LinkedList(response.getEventList());

        return result;
    }

    /**
     * Retrieves the list of all event types, both active and inactive,
     * for any EnterpriseOne environment.
     *
     * @param credentials the credential information needed to validate the user
     * @return a LinkedList of the event types as Strings
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws ClientServiceAccessException  error while attemping to communicate with server
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     */
    public LinkedList getEventList(ConnectorCredentials credentials)
        throws InvalidCredentialsException, ClientServiceAccessException,
            EventProcessingException {
        LinkedList eventDefs = getEventList(credentials, null);
        LinkedList result = new LinkedList();

        for (Iterator iter = eventDefs.iterator(); iter.hasNext();) {
            EventTypeDefinition event = (EventTypeDefinition) iter.next();
            result.add(event.getType());
        }

        return result;
    }

    /**
     * Retrieves the list of event types irrespective of subscriber
     * that are valid for a given environment.
     *
     * @param credentials the credential information needed to validate the user
     * @param environment the EnterpriseOne environment corresponding to the event
     * @return a LinkedList of com.peoplesoft.pt.e1.common.events.EventTypeDefinition
     *         objects
     * @throws InvalidCredentialsException if the user supplied invalid credentials
     * @throws ClientServiceAccessException  error while attemping to communicate with server
     * @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                  the list of events
     */
    public LinkedList getEventListForEnv(ConnectorCredentials credentials,
        String environment)
        throws InvalidCredentialsException, ClientServiceAccessException,
            EventProcessingException {
        //
        //  Build the request.
        //
        EventEnvRequest request = new EventEnvRequest();
        request.setCredentials(credentials);
        request.setEnvironment(environment);
         //
        //  Send the request.
        //
        response = (EventEnvResponse) sendRequest(request);
        /*
        catch(Exception e){
        sLogger.debug("response: "+e.getMessage());
        sLogger.error("response: "+e.getMessage());
        */
        //
        //  Handle the response.
        //
        LinkedList result = new LinkedList(response.getEventEnv());
        return result;
    }

    /**
             * Retrieves the list of MSMQ Subscribers
             * @return a LinkedList of com.peoplesoft.pt.e1.common.events.connectorsvc. MSMQSubscriber
             *         objects
             * @throws InvalidCredentialsException if the user supplied invalid credentials
             * @throws ClientServiceAccessException  error while attemping to communicate with server
             * @throws EventProcessingException if the EventTypeCache cannot retrieve
             *                                  the list of events
             */
    public LinkedList getMSMQSubscribers()
        throws InvalidCredentialsException, ClientServiceAccessException,
            EventProcessingException {
        //
        //  Build the request.
        //
        MSMQSubscriberRequest request = new MSMQSubscriberRequest();

        //
        //  Send the request.
        //
        MSMQSubscriberResponse response = (MSMQSubscriberResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        LinkedList result = new LinkedList(response.getMSMQSubscribers());

        return result;
    }

    /**
     *  Create a new event session with the remote event service.
     *
     *  @param credentials  credentials of the subscriber creating the session.
     *
     *  @return  the session ID of the session to be used in subsequent calls.
     *
     *  @throws InvalidCredentialsException if the user supplied invalid credentials.
     *
     *  @throws ClientServiceAccessException  error while attemping to communicate with server
     *
     *  @throws EventProcessingException if the EventTypeCache cannot retrieve
     *                                   the list of events.
     */
    public String createEventSession(ConnectorCredentials credentials)
        throws InvalidCredentialsException, ClientServiceAccessException,
            EventProcessingException {
        //
        //  Build the request.
        //
        CreateSessionRequest request = new CreateSessionRequest();
        request.setCredentials(credentials);

        //
        //  Send the request.
        //
        CreateSessionResponse response = (CreateSessionResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        String sessionId = response.getSessionId();

        return sessionId;
    }

    /**
     *  Close the event session.
     *
     *  @param credentials  credentials of the subscriber making the request.
     *
     *  @param sessionId  ID of the session.
     *
     *  @throws ClientServiceAccessException  error while attemping to communicate with server
     *
     *  @throws EventProcessingException  error closing the session.
     */
    public void closeSession(ConnectorCredentials credentials, String sessionId)
        throws ClientServiceAccessException, EventProcessingException {
        ManageSessionRequest request = new ManageSessionRequest();
        request.setCredentials(credentials);
        request.setSessionId(sessionId);
        request.setOperation(ManageSessionRequest.CLOSE_SESSION);
        sendRequest(request);
    }

    /**
     *  Start the event session.
     *
     *  @param credentials  credentials of the subscriber making the request.
     *
     *  @param sessionId  ID of the session.
     *
     *  @throws ClientSessionClosedException  the session has already been closed.
     *
     *  @throws ClientServiceAccessException  error while attemping to communicate with server
     *
     *  @throws EventProcessingException  error starting the session.
     */
    public void startSession(ConnectorCredentials credentials, String sessionId)
        throws ClientSessionClosedException, ClientServiceAccessException,
            EventProcessingException {
        ManageSessionRequest request = new ManageSessionRequest();
        request.setCredentials(credentials);
        request.setSessionId(sessionId);
        request.setOperation(ManageSessionRequest.START_SESSION);
        sendRequest(request);
    }

    /**
     *  Stop the current event session.
     *
     *  @param credentials  credentials of the subscriber making the request.
     *
     *  @param sessionId  ID of the session.
     *
     *  @throws ClientSessionClosedException  the session has already been closed.
     *
     *  @throws ClientServiceAccessException  error while attemping to communicate with server
     *
     *  @throws EventProcessingException  error stopping the session.
     */
    public void stopSession(ConnectorCredentials credentials, String sessionId)
        throws ClientSessionClosedException, ClientServiceAccessException,
            EventProcessingException {
        ManageSessionRequest request = new ManageSessionRequest();
        request.setCredentials(credentials);
        request.setSessionId(sessionId);
        request.setOperation(ManageSessionRequest.STOP_SESSION);
        sendRequest(request);
    }

    /**
     *  Receive the next available event.  If an event is not available the caller
     *  Will block until either an event is available for delivery or the specified
     *  time-out period has elapsed.  A timeout of zero never expires, and the call
     *  blocks indefinitely.
     *
     *  @param credentials  credentials of the subscriber making the request.
     *
     *  @param sessionId  ID of the session.
     *
     *  @param timeout the timeout value (in milliseconds).
     *
     *  @return the event or <code>null</code> if no event was available within the
     *          timeout period.
     *
     *  @throws ClientSessionStoppedException  the session is currently stopped.
     *
     *  @throws ClientSessionClosedException  the session has already been closed.
     *
     *  @throws ClientServiceAccessException  error while attemping to communicate with server
     *
     *  @throws EventProcessingException  error receiving events.
     */
    public EventMessage receive(ConnectorCredentials credentials,
        String sessionId, long timeout)
        throws ClientSessionStoppedException, ClientSessionClosedException,
            ClientServiceAccessException, EventProcessingException {
        //
        //  Build the request.
        //
        ReceiveRequest request = new ReceiveRequest();
        request.setCredentials(credentials);
        request.setSessionId(sessionId);
        request.setTimeout(timeout);

        //
        //  Send the request.
        //
        ReceiveResponse response = (ReceiveResponse) sendRequest(request);

        //
        //  Handle the response.
        //
        EventMessage event = response.getEvent();

        return event;
    }

    /**
     *  Acknowledges all consumed events of the session.
     *
     *  @param credentials  credentials of the subscriber making the request.
     *
     *  @param sessionId  ID of the session.
     *
     *  @throws ClientSessionStoppedException  the session is currently stopped.
     *
     *  @throws ClientSessionClosedException  the session has already been closed.
     *
     *  @throws ClientServiceAccessException  error while attemping to communicate with server
     *
     *  @throws EventProcessingException  error acknowledging events.
     */
    public void acknowledgeEvents(ConnectorCredentials credentials,
        String sessionId)
        throws ClientSessionStoppedException, ClientSessionClosedException,
            ClientServiceAccessException, EventProcessingException {
        AcknowledgeRequest request = new AcknowledgeRequest();
        request.setCredentials(credentials);
        request.setSessionId(sessionId);
        sendRequest(request);
    }

    private ClientResponse sendRequest(ClientRequest request)
        throws ClientServiceAccessException, EventProcessingException {
        //
        //  Marshal the request into XML.
        //
        String requestXml = null;

        try {
            requestXml = mMarshaller.marshalRequest(request);
        } catch (XmlMarshalException e) {
            String msg = "error marshaling event request into XML: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }

        //
        //  Send the request to the event service.
        //
        if (sE1Logger.isDebug()) {
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
						sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"sending event client request: " + sb2.toString(), null, null, null);
					} else {
						/* no <security-token>, trace sb1 with <password> hidden */
						sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"sending event client request: " + sb1.toString(), null, null, null);
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
						sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"sending event client request: " + sb2.toString(), null, null, null);
					} else {
						/* no <password> and <security-token>, trace as is */
						sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"sending event client request: " + requestXml, null, null, null);
					}
				}
			} else {
				/* no <credentials> element in the XML string, trace as is */
				sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"sending event client request: " + requestXml, null, null, null);
			}
        }

        String responseXml = sendHttpRequest(requestXml);

        if (sE1Logger.isDebug()) {
            sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"received event client response: " + responseXml, null, null, null);
        }

        //
        //  Unmarshal the response.
        //
        ClientResponse response = null;

        try {
            response = mMarshaller.unmarshalResponse(responseXml);
        } catch (XmlMarshalException e) {
            String msg = "error marshaling event response from XML: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"response XML: " + responseXml, null, null, null);
            throw new EventProcessingException(msg, e);
        }

        //
        //  See if an error was returned.
        //
        if (response instanceof ErrorResponse) {
            handleErrorResponse((ErrorResponse) response);
        }

        return response;
    }

    private String sendHttpRequest(String request)
        throws ClientServiceAccessException, EventProcessingException {
        //
        //  Open a HTTP connection.
        //
        HttpURLConnection httpConnection = null;

        try {
            httpConnection = (HttpURLConnection) mServiceUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            httpConnection.setRequestProperty("Content-Type",
                "text/xml; charset=" + CharacterEncoding.ENCODING);
            httpConnection.setRequestProperty("Accept", "text/xml");

            httpConnection.connect();
        } catch (IOException e) {
            String msg = "failed to connect to event service: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new ClientServiceAccessException(msg, e);
        }

        //
        //  Send the request.
        //
        try {
            OutputStream out = httpConnection.getOutputStream();
            Writer wout = new OutputStreamWriter(out, CharacterEncoding.ENCODING);
            wout.write(request);
            wout.flush();
            wout.close();
        } catch (IOException e) {
            String msg = "failed to send request to event service: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new ClientServiceAccessException(msg, e);
        }

        //
        //  Check the response code.
        //
        try {
            int responseCode = httpConnection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                String msg = "bad status code returned from event service: " +
                    httpConnection.getResponseMessage();
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
                throw new ClientServiceAccessException(msg);
            }
        } catch (IOException e) {
            String msg = "failed to get response status from event service: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new ClientServiceAccessException(msg, e);
        }

        //
        //  Read the response.
        //
        String response = null;

        try {
            InputStream inStream = httpConnection.getInputStream();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(
                        inStream, CharacterEncoding.ENCODING));
            StringBuffer buffer = new StringBuffer();
            String line = null;

            while ((line = inReader.readLine()) != null) {
                buffer.append(line);
            }

            inStream.close();
            response = buffer.toString();
        } catch (IOException e) {
            String msg = "failed to read response from event service: " +
                e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null,e);
            throw new ClientServiceAccessException(msg, e);
        }

        return response;
    }

    private void handleErrorResponse(ErrorResponse response)
        throws EventProcessingException {
        //
        //  An error occured.  Throw the approprate exception.
        //
        switch (response.getErrorCode()) {
        case ErrorResponse.INVALID_CREDENTIALS_EXCEPTION:
            throw new InvalidCredentialsException(response.getErrorMessage());

        case ErrorResponse.NO_SUCH_SUBSCRIBER_EXCEPTION:
            throw new NoSubscriberException(response.getErrorMessage());

        case ErrorResponse.NOT_SUPPORTED:
            throw new NotSupportedException(response.getErrorMessage());

        case ErrorResponse.CLOSED_SESSION_EXCEPTION:
            throw new ClientSessionClosedException(response.getErrorMessage());

        case ErrorResponse.STOPPED_SESSION_EXCEPTION:
            throw new ClientSessionStoppedException(response.getErrorMessage());

        default:
            throw new EventProcessingException(response.getErrorMessage());
        }
    }

        public String getSessionId(){
	        return sessionId;
	    }

	    public void setSessionId(String sessionId){
	        this.sessionId = sessionId;
    }
}
