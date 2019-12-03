//============================================================================
//
//Copyright © [2004] 
//PeopleSoft, Inc.  
//All rights reserved. PeopleSoft Proprietary and Confidential.
//PeopleSoft, PeopleTools and PeopleBooks are registered trademarks of PeopleSoft, Inc.
//
//============================================================================

package com.peoplesoft.pt.e1.server.enterprise.events.jdenet;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
//Imports from javax namespace
//=================================================

//=================================================
//Imports from com namespace
//=================================================
import com.jdedwards.base.logging.E1Logger;
import com.jdedwards.base.logging.JdeLog;
import com.jdedwards.base.logging.log4j.LogUtils;
import com.jdedwards.system.net.JdeMsg;
import com.jdedwards.system.net.ListenerContext;
import com.jdedwards.system.net.ProcessKernel;
import com.jdedwards.system.net.ProcessKernelException;
import com.peoplesoft.pt.e1.common.events.EventProcessingException;
import com.peoplesoft.pt.e1.server.common.events.adminservice.AdminServiceFactory;
import com.peoplesoft.pt.e1.server.common.events.adminservice.IAdminService;

//=================================================
// Imports from org namespace
//=================================================

/**
 * This class handles JdeNet messages requesting a reload of the event cache.
 */
class ReloadSubscriberCacheHandler extends ProcessKernel
{
    //=================================================
    // Non-public static class fields.
    //=================================================
    
    private static E1Logger sE1Logger = JdeLog.getE1Logger(ReloadSubscriberCacheHandler.class.getName());

    //=================================================
    // Public static final fields.
    //=================================================   

    //=================================================
    // Instance member fields.
    //================================================= 

    private boolean mInitialized = false;

    //=================================================
    // Constructors.
    //=================================================

    /**
     * Constructor.
     */
    public ReloadSubscriberCacheHandler()
    {}

    //=================================================
    // Methods.
    //=================================================
    
    /**
     *  Processes reload requests.
     * 
     * @param m the message.
     * 
     * @throws ProcessKernelException  Error during processing.
     * 
     * @see ProcessKernel
     */
    public void process(JdeMsg m) throws ProcessKernelException
    {
        try
        {
            IAdminService service = AdminServiceFactory.getAdminService();
            service.reloadData(IAdminService.SUBSCRIBER_CATEGORY);
        }
        catch (EventProcessingException e)
        {
            String msg = "failed to reload event subscriber cache: " + e.getMessage();
            sE1Logger.severe(LogUtils.SYS_EVENTPROCESSOR,msg, null, null, e);
            throw new ProcessKernelException(msg);
        }
    }

    /**
     *  Initialize the handler.
     * 
     *  @param context the context.
     *  
     *  @throws ProcessKernelException Error during processing.
     * 
     *  @see ProcessKernel
     */
    public void initialize(ListenerContext context) throws ProcessKernelException
    {}

    /**
     *  Shutdown the handler.
     * 
     *  @throws ProcessKernelException ex
     */
    public void shutdown() throws ProcessKernelException
    {}
}
