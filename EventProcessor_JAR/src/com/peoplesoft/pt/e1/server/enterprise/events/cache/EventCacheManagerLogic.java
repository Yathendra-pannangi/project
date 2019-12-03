//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.cache;

//=================================================
//Imports from java namespace
//=================================================
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.database.base.JDBComparisonOp;
import com.jdedwards.database.base.JDBDatabaseAccess;
import com.jdedwards.database.base.JDBException;
import com.jdedwards.database.base.JDBFieldComparison;
import com.jdedwards.database.base.JDBFieldMap;
import com.jdedwards.database.base.JDBResultSet;
import com.jdedwards.system.lib.JdeProperty;
import com.jdedwards.system.net.JdeConnectionManager;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.JdeNetUtil;
import com.jdedwards.system.net.JdeSocket;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.enterprise.events.adaptor.AdaptorManager;
import com.peoplesoft.pt.e1.server.enterprise.events.common.DataReloadCategory;
import com.peoplesoft.pt.e1.server.enterprise.events.db.EventDBUtil;
import com.peoplesoft.pt.e1.server.enterprise.events.db.F98611_DataSourceMaster;

//=================================================
//Imports from org namespace
//=================================================

/**
 *  Implementation of the event cache manager logic.
 */
public class EventCacheManagerLogic
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = 
        JdeLog.getE1Logger(EventCacheManagerLogic.class.getName());
        
    private static final String EVENTS_SECTION = "EVENTS";
        
    private static final String JDENET_SECTION = "JDENET";
    
    private static final String SERVER_PORT = "serviceNameConnect";
    
    private static final String SERVER_TIMEOUT = "enterpriseServerTimeout";
    
    private static final String OVERRIDE_HOSTS = "enterpriseServerHostList";
    
    private static final int JDENET_MSG_TYPE = 923;

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
     *  Reload data for a specified category.
     * 
     *  @param category  The category to be reloaded.
     */
    public void reloadData(DataReloadCategory category)
    {
        sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Reloading data category: " + category, null, null, null);
        try
        {
            if (category.equals(DataReloadCategory.EVENT_TYPE_DEFINITIONS))
            {
                EventTypeCache.getInstance().reload();
                sendEventTypeReloadMessages();
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Completed reloading data category: " + category, null, null, null);
            }
            else if (category.equals(DataReloadCategory.SUBSCRIBER_DEFINITIONS))
            {
                SubscriberCache.getInstance().reload();
                AdaptorManager.notifyReload();
                sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR,"Completed reloading data category: " + category, null, null, null);
            }
            else
            {
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Unknown data reload category: " + category, null, null, null);
            }
        }
        catch (EventProcessingException e)
        {
            String msg = "Exception reloading cached data: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
        }
    }
    
    /**
     *  Send event type cache reload messages to the C kernels.
     */
    private void sendEventTypeReloadMessages() throws EventProcessingException
    {
        List hosts = getLogicHosts();
        
        int port = getPort();
        long timeout = getTimeout();
        JdeConnectionManager conManager = JdeConnectionManager.getManager();
        
        for (Iterator iter = hosts.iterator(); iter.hasNext();)
        {
            String hostname = (String)iter.next();
            JdeSocket socket = null;
            try
            {
                if (sE1Logger.isDebug())
                {
                    sE1Logger.debug(LogUtils.SYS_EVENTPROCESSOR, "Sending event cache reload message to host: " + hostname, null, null, null);
                }
                socket = conManager.checkout(hostname, port, timeout);
                JdeMsg message =
                    new JdeMsg(
                        JDENET_MSG_TYPE,
                        JdeNetUtil.NET_DEFAULT_FLAGS,
                        JdeNetUtil.NET_DEFAULT_PRIORITY);
                message.send(socket);
            }
            catch (IOException e)
            {
                String msg =
                    "Failed to send event data reload JDENET message to "
                        + hostname
                        + ": "
                        + e.getMessage();
                sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR, msg, null, null, null);
            }
            finally
            {
                if (socket != null)
                {
                    conManager.checkin(socket);
                }
            }
        }
    }
    
    /**
     *  Get the list of logic data source hosts.  These are the hosts that need to receive
     *  a cache refresh message.
     */
    private List getLogicHosts() throws EventProcessingException
    {
        //
        //  First, see if a host list over-ride is available from the configuration file.
        //  If there is then just return that list.
        //
        List hosts = getLogicHostsOverride();
        if (hosts != null)
        {
            return hosts;
        }
        
        //
        //  No over-ride host list was found so build the logic hosts list from the
        //  informaiton in the database.
        //
        hosts = new LinkedList();
        JDBResultSet results = null;
        JDBDatabaseAccess connection = null;
        try
        {
            connection = EventDBUtil.getDefaultDBAccess();
            JDBFieldComparison selection =
                new JDBFieldComparison(
                    F98611_DataSourceMaster.OCM1,
                    JDBComparisonOp.EQ,
                    F98611_DataSourceMaster.LOGICAL_DATA_SOURCE);
            results =
                connection.selectDistinct(
                    F98611_DataSourceMaster.TABLE,
                    F98611_DataSourceMaster.FIELDS,
                    selection,
                    null);
            while (results.hasMoreRows())
            {
                JDBFieldMap fields = results.fetchNext();
                String hostname = (String)fields.getString(F98611_DataSourceMaster.SRVR);
                if (hostname != null)
                {
                    hostname = hostname.trim();
                    if (hostname.length() > 0)
                    {
                        hosts.add(hostname);
                    }
                }
            }
        }
        catch (JDBException e)
        {
            String msg = "Failed to get logic hosts: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new EventProcessingException(msg, e);
        }
        finally
        {
            if (results != null)
            {
                try
                {
                    results.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close result set: " + e.getMessage(), null, null, e);
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (JDBException e)
                {
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,"Failed to close database connection: " + e.getMessage(), null, null, e);
                }
            }
        }
        
        return hosts;
    }
    
    /**
     *  Get the lists of enterprise server hosts from the properties file.  This is expected
     *  to be a common seperated list of hosts names if the property is present.
     */
    private List getLogicHostsOverride()
    {
        List hosts = null;
        String overrideHosts = JdeProperty.getProperty(EVENTS_SECTION, OVERRIDE_HOSTS, null);
        if ((overrideHosts != null) && (overrideHosts.trim().length() > 0))
        {
            hosts = new LinkedList();
            StringTokenizer tokenizer = new StringTokenizer(overrideHosts, ",");
            while (tokenizer.hasMoreElements())
            {
                String host = tokenizer.nextToken().trim();
                hosts.add(host);
            }
        }
        
        return hosts;
    }
    
    /**
     *  Get the port the enterprise server listens on.
     */
    private int getPort() throws EventProcessingException
    {
        int port = JdeProperty.getProperty(JDENET_SECTION, SERVER_PORT, -1);
        if (port < 0)
        {
            StringBuffer buffer = new StringBuffer(90);
            buffer.append("No enterprise server port found in INI file:");
            buffer.append(" section=").append(JDENET_SECTION);
            buffer.append(" property=").append(SERVER_PORT);
            String msg = buffer.toString();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        return port;
    }
    
    /**
     *  Get the enterprise server timeout value.
     */
    private int getTimeout() throws EventProcessingException
    {
        int timeout = JdeProperty.getProperty(JDENET_SECTION, SERVER_TIMEOUT, -1);
        if (timeout < 0)
        {
            StringBuffer buffer = 
                new StringBuffer(90);
            buffer.append("No enterprise server timeout value found in INI file:");
            buffer.append(" section=").append(JDENET_SECTION);
            buffer.append(" property=").append(SERVER_TIMEOUT);
            String msg = buffer.toString();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, null);
            throw new EventProcessingException(msg);
        }
        
        return timeout;
    }
}
