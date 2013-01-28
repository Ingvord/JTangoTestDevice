//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the CmdDoneEvent class definition .
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
// Revision 1.5  2008/10/10 11:33:07  pascal_verdier
// Headers changed for LGPL conformity.
//
// Revision 1.4  2007/08/23 08:32:59  ounsy
// updated change from api/java
//
// Revision 3.7  2005/12/02 09:51:15  pascal_verdier
// java import have been optimized.
//
// Revision 3.6  2004/12/07 09:30:30  pascal_verdier
// Exception classes inherited from DevFailed added.
//
// Revision 3.5  2004/03/12 13:15:21  pascal_verdier
// Using JacORB-2.1
//
// Revision 3.0  2003/04/29 08:03:28  pascal_verdier
// Asynchronous calls added.
// Logging related methods.
// little bugs fixed.
//
//-======================================================================

package fr.esrf.TangoApi;
 
import fr.esrf.Tango.DevError;


/**
 *	Object defining result of an asynchronous call.
 */


public class  CmdDoneEvent
{
	public DeviceProxy 	device;
	public String		cmdname;
	public DeviceData	argout;
	public DevError[]	errors;
	public boolean		err;

	//===============================================================
	/**
	 *	Object constructor
	 */
	//===============================================================
	public CmdDoneEvent(DeviceProxy dev, String cmd, 
						DeviceData arg, DevError[] err_in)
	{
		device  = dev;
		cmdname = cmd;
		argout  = arg;
		errors  = err_in;
		if (errors==null)
			err = false;
		else
			err = (errors.length!=0) ;
	}
	//===============================================================
	//===============================================================
}