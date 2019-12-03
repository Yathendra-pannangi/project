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
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.net.JdeConnectionManager;
import com.jdedwards.system.net.JdeDataPacket;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.JdeNetTimeoutException;
import com.jdedwards.system.net.JdeNetUtil;
import com.jdedwards.system.net.JdePacket;
import com.jdedwards.system.net.JdeSocket;
import com.jdedwards.system.net.JdeUnicodePacket;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Sends a JDENET message to a specified host and port.
 */
public class JdenetMsg
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger = JdeLog.getE1Logger(JdenetMsg.class.getName()); 

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    /** The EnterpriseOne server host name. */
    private String mHost = null;
    
    /** The EnterpriseOne server port name. */
    private int mPort = 0;
    
    /** The timeout value for the message. */
    private int mTimeout = 0;
    
    //=================================================
    // Constructors.
    //=================================================

    /**
     * Constructs a JdenetMsg with all the required values.
     * 
     * @param host The EnterpriseOne server host name
     * @param port The EnterpriseOne server port name
     * @param timeout The timeout value for the message
     */
    public JdenetMsg(String host, int port, int timeout)
    {
        mHost = host;
        mPort = port;
        mTimeout = timeout;
    }

    //=================================================
    // Methods.
    //=================================================
    
    /**
     * Executes the JdenetMsg.
     * 
     * @param messageType the message type
     * @param packets the message packets
     * @return the returned message
     * @throws EventProcessingException if the JDENET message has errors in sending or receiving
     */
    public String[] execute(int messageType, String[] packets) throws EventProcessingException
    {
        JdeSocket socket = null;
        boolean socketOk = true;
        
        // create JdeMsg instance
        JdeMsg msg = new JdeMsg(messageType, JdeNetUtil.NET_DEFAULT_FLAGS,
                                JdeNetUtil.NET_DEFAULT_PRIORITY);
                                
        // create the packets
        if (packets != null)
        {
            for (int i=0; i<packets.length; i++)
            {
                msg.addUnicodeData(packets[i]);
            }
        }
        
        // send and receive the JDENET message
        try
        {
            socket = JdeConnectionManager.getManager().checkout(mHost, mPort, mTimeout);
            socket.setTimeout(mTimeout);
            msg.execute(socket);
        }
        catch (JdeNetTimeoutException e)
        {
            String errorMsg = "JDENET message timed out while sending message type "
                         + messageType;
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg,null, null, e);
            throw new EventProcessingException(errorMsg, e);
        }
        catch (IOException e)
        {
            // invalidate the socket
            socketOk = false;
            
            String errorMsg = "JDENET message error while sending message type "
                              + messageType;
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg,null, null, e);
            throw new EventProcessingException(errorMsg, e);
        }
        finally
        {
            if (socket != null)
            {
                if (socketOk)
                {
                    JdeConnectionManager.getManager().checkin(socket);
                }
                else
                {
                    JdeConnectionManager.getManager().checkinBad(socket);
                }
            }
        }
        
        // get the response
        int numPacketsRcv = msg.getNumPackets();
        String[] rcvMsg = new String[numPacketsRcv];
        for (int i=0; i<numPacketsRcv; i++)
        {
            JdePacket packet = msg.getPacket(i);
            if (packet instanceof JdeUnicodePacket)
            {
                rcvMsg[i] = ((JdeUnicodePacket)packet).getString();
            }
            else if (packet instanceof JdeDataPacket)
            {
                try
                {
                    rcvMsg[i] = new String(((JdeDataPacket)packet).getData(), "UTF8");
                }
                catch (UnsupportedEncodingException e)
                {
                    String errorMsg = "Unsupported character encoding in JDENET message "
                                      + "received from EnterpriseOne server.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,errorMsg,null, null, e);
                    throw new EventProcessingException(errorMsg, e);
                }
            }
        }
        
        return rcvMsg;
    }
}
