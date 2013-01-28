//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TANGO clent/server API.
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision$
//
// $Log$
// Revision 1.10  2009/03/12 09:07:55  pascal_verdier
// *** empty log message ***
//
// Revision 1.9  2008/10/10 11:34:15  pascal_verdier
// Headers changed for LGPL conformity.
//
// Revision 1.8  2008/04/16 12:58:29  pascal_verdier
// Event subscribtion stateless added.
//
// Revision 1.7  2008/04/11 08:08:44  pascal_verdier
// *** empty log message ***
//
// Revision 1.6  2008/04/11 07:14:09  pascal_verdier
// AttConfig event management added.
//
// Revision 1.5  2007/09/13 09:22:32  ounsy
// Add java.io.serializable to the dtata classe
//
// Revision 1.4  2007/08/23 08:32:57  ounsy
// updated change from api/java
//
// Revision 1.3  2005/12/02 09:54:04  pascal_verdier
// java import have been optimized.
//
// Revision 1.2  2004/03/19 10:24:35  ounsy
// Modification of the overall Java event client Api for synchronization with tango C++ Release 4
//
// Revision 1.1  2004/03/08 11:43:23  pascal_verdier
// *** empty log message ***
//-======================================================================
/*
 * EventDispatcher.java
 *
 * Created on October 3, 2003, 5:12 PM
 */

package fr.esrf.TangoApi.events;


import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CallBack;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.event.EventListenerList;

/**
 *
 * @author  pascal_verdier
 */
public abstract class EventDispatcher extends CallBack 
                                     implements TangoConst, java.io.Serializable {
    
    /** Creates a new instance of EventDispatcher */

    public EventDispatcher(DeviceProxy device_proxy) {
            event_supplier = device_proxy;
            event_listeners = new EventListenerList();
    }
    
    public DeviceProxy getEventSupplier() {
        return event_supplier;
    }
    //=======================================================================
    //=======================================================================
    protected int subscribe_periodic_event(String attr_name,String[] filters, boolean stateless)
                  throws DevFailed
    {
        return event_supplier.subscribe_event(attr_name,
				PERIODIC_EVENT, this, filters, stateless);
    }

    //=======================================================================
    //=======================================================================
    protected int subscribe_change_event(String attr_name,String[] filters, boolean stateless) 
                  throws DevFailed
    {
        return event_supplier.subscribe_event(attr_name,
				CHANGE_EVENT, this, filters, stateless);
    }

    //=======================================================================
    //=======================================================================
    protected int subscribe_quality_change_event(String attr_name,String[] filters, boolean stateless) 
                  throws DevFailed
    {
        return event_supplier.subscribe_event(attr_name,
				QUALITY_EVENT, this, filters, stateless);
    }

    //=======================================================================
    //=======================================================================
	protected int subscribe_archive_event(String attr_name,String[] filters, boolean stateless) 
				  throws DevFailed
	{
		return event_supplier.subscribe_event(attr_name,
				ARCHIVE_EVENT, this, filters, stateless);
	}

    //=======================================================================
    //=======================================================================
	protected int subscribe_user_event(String attr_name,String[] filters, boolean stateless) 
				  throws DevFailed
	{
		return event_supplier.subscribe_event(attr_name,
				USER_EVENT, this, filters, stateless);
	}

    //=======================================================================
    //=======================================================================
	protected int subscribe_att_config_event(String attr_name,String[] filters, boolean stateless) 
				  throws DevFailed
	{
		return event_supplier.subscribe_event(attr_name,
				ATT_CONF_EVENT, this, filters, stateless);
	}
   //=======================================================================
    //=======================================================================
	protected int subscribe_data_ready_event(String attr_name,String[] filters, boolean stateless) 
				  throws DevFailed
	{
		return event_supplier.subscribe_event(attr_name,
				DATA_READY_EVENT, this, filters, stateless);
	}

    //=======================================================================
    //=======================================================================
    public void unsubscribe_event(int event_id)
                   throws DevFailed
    {
        event_supplier.unsubscribe_event(event_id);
    }

    //=======================================================================
    //=======================================================================
    public void push_event(EventData event_data) {
        dispatch_event(event_data);
    }
        
    
    public abstract void dispatch_event(EventData event_data);
        
    protected EventListenerList event_listeners = null;
    protected DeviceProxy       event_supplier  = null;
    
}