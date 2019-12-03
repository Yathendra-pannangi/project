//============================================================================
//
// Copyright © [2004]
// PeopleSoft, Inc.
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================
package com.peoplesoft.pt.e1.server.enterprise.events.adaptor;

//=================================================
//Imports from java namespace
//=================================================
import java.io.IOException;
import java.io.StringReader;

//=================================================
//Imports from javax namespace
//=================================================
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.lib.JdeProperty;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.JdeNetException;
import com.jdedwards.system.net.JdeNetUtil;
import com.jdedwards.system.net.JdeSocket;
import com.peoplesoft.pt.e1.common.events.EventMessage;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSNames;
import com.peoplesoft.pt.e1.server.enterprise.events.common.JMSQueue;

//=================================================
//Imports from org namespace
//=================================================
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * This class creates a JDENetSourceRouter. Waits till the
 * message is put in source router queue. After message is received
 * JDENetSourceRouter will try to send it to the source routed subscriber
 * only once. If fails, discards the message.
 */
public class JDENetSourceRouter extends Thread
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static final char UTF16_BYTE_ORDER_MARKER = '\uFEFF';
    private static final int XML_DISPATCH_KERNEL_MSG = 13515;
    private static final int MSG_FLAGS = 0x0001;
    
    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(JDENetSourceRouter.class.getName());
        
    private static int sTimeout = 
                JdeProperty.getProperty("JDENET", "enterpriseServerTimeout", 90000);

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================
    
    private QueueReceiver mReceiver = null;
    
    private boolean mRun = true;
    
    private boolean mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================
    
    /**
     * Constructor to create source router.
     */
    public JDENetSourceRouter()
    {}
    
    //=================================================
    // Methods.
    //=================================================

    /**
     * This method starts a new thread for the source routed event handler.
     */
    public void run()
    {
        if (sE1Logger.isApp())
        {
            sE1Logger.app(LogUtils.SYS_EVENTPROCESSOR, "Starting SourceRouterTransportAdaptor.", null, null, null);
        }
        
        JMSQueue queue = null;
        try
        {
            queue =
                AdaptorUtil.getInstance().createQueueConnection(
                    JMSNames.SOURCE_ROUTE_Q,
                    null,
                    Session.AUTO_ACKNOWLEDGE,
                    false);
            mReceiver = queue.getQueueReceiver();
        }
        catch(EventProcessingException ex)
        {
            sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,
                "Error creating a source router queue connection, source router aborting: "
                    + ex.getMessage(), null, null,
                ex);
            return;
        }
        
        DocumentBuilder builder = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        catch(ParserConfigurationException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, 
                "Error getting a DocumentBuilder in SourceRouter, SourceRouter aborting: "
                    + ex.getMessage(), null, null,
                ex);
            return;
        }
        
        while (getRun())
        {
            try
            {
                if(!mInitialized){
                    reinitialize();
                }            
                //
                //  Block waiting for the next source routed event message.
                //
                Message message = null;
                try
                {
                    message = mReceiver.receive();
                    mInitialized = true;
                }
                catch (JMSException ex)
                {
                    mInitialized = false;
                    return;
                    //
                    //  Only generate an error message in the log if the thread has not
                    //  been commanded to shutdown.
                    //
//                    if (getRun())
//                    {
//                        String msg =
//                            "Error occurred while reading message from source router queue. "
//                                + ex.getMessage();
//                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, null);
//                        sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source routed event not sent", null, null, null);
//                        continue;
//                    }
                }
                
                //
                //  Check that the message is of the proper type.
                //
                if (!(message instanceof ObjectMessage))
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Invalid message type sent from Event Processing System", null, null, null);
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source routed event not sent", null, null, null);
                    continue;
                }
                ObjectMessage objMessage = (ObjectMessage)message;
                EventMessage eventMessage = null;
                
                //
                //  Extract the EventMessage object out of the JMS message.
                //
                try
                {
                    eventMessage = (EventMessage)objMessage.getObject();
                }
                catch (JMSException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Error retrieving EventMessage object from JMS message.", null, null, e);
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source routed event not sent", null, null, e);
                    continue;
                }
                
                //
                //  Get the source routing information, which is an XML document containing
                //  the destination host and port.
                //
                String sourceRouterInfo = eventMessage.getSourceRoute();
                if (sE1Logger.isApp())
                {
                    sE1Logger.app(LogUtils.SYS_EVENTPROCESSOR, "The SourceRouter Info is: " + sourceRouterInfo, null, null, null);
                }
                if ((sourceRouterInfo == null) || (sourceRouterInfo.length() == 0))
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source Routing Info is null/blank.", null, null, null);
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source routed event not sent", null, null, null);
                    continue;
                }

                //
                //  Look for a UTF-16 byte order marker at the start of the string.  If one
                //  is present skip over it so that the XML parser does not fail.
                //
                char first = sourceRouterInfo.charAt(0);
                if (first == UTF16_BYTE_ORDER_MARKER)
                {
                    sourceRouterInfo = sourceRouterInfo.substring(1);
                }
                
                //
                //  Parse the XML.
                //
                InputSource xmlInput = new InputSource(new StringReader(sourceRouterInfo));
                Document document = builder.parse(xmlInput);
                
                //
                //  Extract the host and port.
                //
                Element hostElement = (Element)document.getElementsByTagName("host").item(0);
                String host = hostElement.getFirstChild().getNodeValue();
                Element portElement = (Element)document.getElementsByTagName("port").item(0);
                int port = 0;
                try
                {
                    port = Integer.parseInt(portElement.getFirstChild().getNodeValue());
                    if (sE1Logger.isDebug())
                    {
                        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Source router host and port is : " + host + " " + port, null, null, null);
                    }
                }
                catch (NumberFormatException ex)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, 
                        "Error getting a port number from source routing XML docuemnt."
                            + " Source routed event not sent."
                            + ex.getMessage(), null, null,
                        ex);
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source routed event not sent", null, null, null);
                    continue;
                }
                
                //
                //  Send the event.
                //
                sendSourceRoutedMessage(eventMessage.getXMLPayload(), host, port);
            }
            catch (Exception ex)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, 
                    "Exception occured while sending a message in "
                        + "SourceRouter. "
                        + ex.getMessage(),
				        null, null,ex);
            }
        }
        
        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "SourceRouter thread exiting", null, null, null);
    }

    private void sendSourceRoutedMessage(String payLoad, String host, int port)
        throws JdeNetException
    {
        //
        //  Attempt to open a socket connection.  If one can't be opened then log
        //  a warning and return.
        //
        JdeSocket soc = null;
        try
        {
            soc = AdaptorUtil.getInstance().createJDENetConnection(host, port, sTimeout);
        }
        catch (AdaptorException e)
        {
            //
            //  Failed to open a connection.  Log a message and return.
            //
            StringBuffer buffer = new StringBuffer("Failed to open socket connection to [");
            buffer.append(host).append(":").append(port);
            buffer.append("] for source routed event, event not delivered");
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, buffer.toString(), null, null, e);
            return;
        }
        
        if((soc != null) && (soc.isSocketGood()))
        {
            JdeMsg netMsg = new JdeMsg(XML_DISPATCH_KERNEL_MSG,MSG_FLAGS,
                                       JdeNetUtil.NET_DEFAULT_PRIORITY);
            //XMLDiapatch kernel excepts bytes only. 
            //So you cannot pass a unicode string or a UTF-8 String                            
            netMsg.addData(payLoad.getBytes());
            try
            {
                netMsg.send(soc);
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Source router sent an event with payload: "
                                    + payLoad, null, null, null);
                }
            }
            catch (IOException ex)
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "IOException occured while sending an event in "
                                   + "SourceRouter. "+ ex.getMessage(), null, null, ex);
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Source routed event may not have been delivered", null, null, null);
            }                        
            
            //
            //  Disconnect the socket.
            //
            soc.disconnect();
        }
    }

    /**
     * Stops the JdenetSourceRouter Adaptor thread.
     */
    public void stopJdenetSourceRouter()
    {
        setRun(false);
        try
        {
            mReceiver.close();
        }
        catch (JMSException ex)
        {
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR, "Receiver close() failed. " + ex, null, null, ex);
        }
    }
    
    /**
     *  These two methods for getting and setting run must be synchronized since
     *  the value of run is used for communication between threads.
     */
    private synchronized boolean getRun()
    {
        return mRun;
    }
    
    private synchronized void setRun(boolean run)
    {
        mRun = run;
    }
    
    private void reinitialize(){        
        try {
        if(mReceiver != null){
                try {
                    mReceiver.close();
                } catch (JMSException e) {
                     sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, "Receiver close() failed. " + e, null, null, e);
                }
            }
            JMSQueue queue = 
                    AdaptorUtil.getInstance().createQueueConnection(
                JMSNames.SOURCE_ROUTE_Q,
                null,
                Session.AUTO_ACKNOWLEDGE,
                false);
            mReceiver = queue.getQueueReceiver();
        } catch (EventProcessingException e) {
             sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,
                 "Error creating a source router queue connection, source router aborting: "
                     + e.getMessage(), null, null,
                 e);
        }      
    
    }
}
