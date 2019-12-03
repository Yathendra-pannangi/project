//============================================================================
//
// Copyright © [2004] 
// PeopleSoft, Inc.  
// All rights reserved. PeopleSoft Proprietary and Confidential.
// PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.common.events.connectorsvc;

//=================================================
// Imports from java namespace
//=================================================
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.LinkedList;

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
import com.peoplesoft.pt.e1.common.events.InvalidCredentialsException;
import com.peoplesoft.pt.e1.common.events.NoSubscriberException;
import com.peoplesoft.pt.e1.common.events.NotSupportedException;
import com.peoplesoft.pt.e1.common.events.connectorsvc.internal.ConnectorService;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Implementation of the Connector Service EJB client.
 */
class ConnectorServiceImpl implements IConnectorService
{
    //=================================================
    // Non-public static class fields.
    //=================================================

    private static E1Logger sE1Logger =
        JdeLog.getE1Logger(ConnectorServiceImpl.class.getName());
        
    /** Number of times to retry the EJB call. */
    private static final int MAX_RETRIES = 1;

    //=================================================
    // Public static final fields.
    //=================================================

    //=================================================
    // Instance member fields.
    //=================================================

    private ConnectorService mConnectorServiceEJB = null;

    //=================================================
    // Constructors.
    //=================================================

    /**
     *  Constructor.
     * 
     *  @param  ejb  Remote interface to the admin service EJB.
     */
    ConnectorServiceImpl(ConnectorService ejb)
    {
        if (ejb == null)
        {
            throw new NullPointerException();
        }
        mConnectorServiceEJB = ejb;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * {@inheritDoc}
     */
    public String getQueueJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, EventProcessingException
    {
        int retry = 0;
        String queueJNDIName = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                queueJNDIName = mConnectorServiceEJB.getQueueJNDIName(credentials);
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.",null, null, e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get JMS queue JNDI name.";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return queueJNDIName;
    }

    /**
     * {@inheritDoc}
     */
    public String getQueueConFactJNDIName(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException
    {
        int retry = 0;
        String queueConFactJNDIName = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                queueConFactJNDIName = mConnectorServiceEJB.getQueueConFactJNDIName(credentials);
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.", null, null,e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get JMS queue connection factory JNDI name.";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return queueConFactJNDIName;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedList getSubscriptions(ConnectorCredentials credentials)
        throws InvalidCredentialsException, NoSubscriberException, EventProcessingException
    {
        int retry = 0;
        LinkedList subscriptions = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                subscriptions = mConnectorServiceEJB.getSubscriptions(credentials);
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.",null, null, e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get subscriptions.";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return subscriptions;
    }

    /**
     * {@inheritDoc}
     */
    public String getEventTemplate(ConnectorCredentials credentials, String host, int port,
        String category, String type, String environment) throws NotSupportedException,
        InvalidCredentialsException, EventProcessingException
    {
        int retry = 0;
        String template = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                template = mConnectorServiceEJB.getEventTemplate(credentials, host, port,
                                                                 category, type, environment);
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.",null, null, e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get event template for event: " + type + ".";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return template;
    }

	/**
	 * {@inheritDoc}
	 */

	public String getEventSchema(ConnectorCredentials credentials, String host, int port,
		String category, String type, String environment) throws NotSupportedException,
		InvalidCredentialsException, EventProcessingException
	{
		int retry = 0;
		String schema = null;

		// In case the J2EE server goes down between invocations by a client, allow
		// for a retry to get a new EJB instance.
		while (retry <= MAX_RETRIES)
		{
			try
			{
				schema = mConnectorServiceEJB.getEventSchema(credentials, host, port,
																 category, type, environment);
				break;
			}
			catch (NoSuchObjectException e)
			{
				retry++;
				if (retry <= MAX_RETRIES)
				{
					sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
								 + "another instance.",null,null,e);
					mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
										   getConnectorService()).mConnectorServiceEJB;
				}
				else
				{
					String msg = "Failed to get Connector Service EJB instance.";
					sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,e);
					throw new EventProcessingException(msg, e);
				}
			}
			catch (RemoteException e)
			{
				String msg = "Failed to get event schema for event: " + type + ".";
				sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,e);
				throw new EventProcessingException(msg, e);
			}
		}

		return schema;

	}

    /**
     * {@inheritDoc}
     */
    public LinkedList getEventList(ConnectorCredentials credentials, String environment)
        throws InvalidCredentialsException, EventProcessingException
    {
        int retry = 0;
        LinkedList eventList = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                eventList = mConnectorServiceEJB.getEventList(credentials, environment);
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.",null, null, e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get event list for environment: " + environment + ".";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return eventList;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public LinkedList getEventList(ConnectorCredentials credentials)
        throws InvalidCredentialsException, EventProcessingException
    {
        int retry = 0;
        LinkedList eventList = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                eventList = mConnectorServiceEJB.getEventList(credentials);
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.",null, null, e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get event list for all environments.";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return eventList;
    }

    /**
	     * {@inheritDoc}
	     */
	    public LinkedList getEventListForEnv(ConnectorCredentials credentials, String environment)
	        throws InvalidCredentialsException, EventProcessingException
	    {
	        int retry = 0;
	        LinkedList eventList = null;

	        // In case the J2EE server goes down between invocations by a client, allow
	        // for a retry to get a new EJB instance.
	        while (retry <= MAX_RETRIES)
	        {
	            try
	            {
	                eventList = mConnectorServiceEJB.getEventListForEnv(credentials, environment);
	                break;
	            }
	            catch (NoSuchObjectException e)
	            {
	                retry++;
	                if (retry <= MAX_RETRIES)
	                {
	                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
	                                 + "another instance.",null, null, e);
	                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
	                                           getConnectorService()).mConnectorServiceEJB;
	                }
	                else
	                {
	                    String msg = "Failed to get Connector Service EJB instance.";
	                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,e);
	                    throw new EventProcessingException(msg, e);
	                }
	            }
	            catch (RemoteException e)
	            {
	                String msg = "Failed to get event list for environment: " + environment + ".";
	                sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,msg,null,null,e);
	                throw new EventProcessingException(msg, e);
	            }
	        }

	        return eventList;
    }

    /**
     * {@inheritDoc}
     */
    public LinkedList getMSMQInfo() throws EventProcessingException
    {
        int retry = 0;
        LinkedList subList = null;
        
        // In case the J2EE server goes down between invocations by a client, allow
        // for a retry to get a new EJB instance.
        while (retry <= MAX_RETRIES)
        {
            try
            {
                subList = mConnectorServiceEJB.getMSMQInfo();
                break;
            }
            catch (NoSuchObjectException e)
            {
                retry++;
                if (retry <= MAX_RETRIES)
                {
                    sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
                                 + "another instance.",null, null, e);
                    mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
                                           getConnectorService()).mConnectorServiceEJB;
                }
                else
                {
                    String msg = "Failed to get Connector Service EJB instance.";
                    sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                    throw new EventProcessingException(msg, e);
                }
            }
            catch (RemoteException e)
            {
                String msg = "Failed to get MSMQ subscriber information.";
                sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
                throw new EventProcessingException(msg, e);
            }
        }
        
        return subList;
    }
	/**
		 * {@inheritDoc}
		 */
		public boolean isMSMQUser(ConnectorCredentials credentials) throws EventProcessingException
		{
			int retry = 0;
			boolean isMSMQUser = false;
        
			// In case the J2EE server goes down between invocations by a client, allow
			// for a retry to get a new EJB instance.
			while (retry <= MAX_RETRIES)
			{
				try
				{
					isMSMQUser = mConnectorServiceEJB.isMSMQUser(credentials);
					break;
				}
				catch (NoSuchObjectException e)
				{
					retry++;
					if (retry <= MAX_RETRIES)
					{
						sE1Logger.warn(LogUtils.SYS_EVENTPROCESSOR,"Connector Service EJB instance invalid.  Attempting to create "
									 + "another instance.",null, null, e);
						mConnectorServiceEJB = ((ConnectorServiceImpl)ConnectorServiceFactory.
											   getConnectorService()).mConnectorServiceEJB;
					}
					else
					{
						String msg = "Failed to get Connector Service EJB instance.";
						sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
						throw new EventProcessingException(msg, e);
					}
				}
				catch (RemoteException e)
				{
					String msg = "Failed to get MSMQ subscriber information.";
					sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg,null, null, e);
					throw new EventProcessingException(msg, e);
				}
			}
        
			return isMSMQUser;
		}
}
