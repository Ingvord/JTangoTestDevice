//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  Java code to define Polled Object.
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 3.2  2005/12/02 09:55:46  pascal_verdier
// java import have been optimized.
//
// Revision 3.1  2004/05/14 13:47:57  pascal_verdier
// Compatibility with Tango-2.2.0 cpp
// (polling commands and attibites).
//
//
// Copyleft 2000 by European Synchrotron Radiation Facility, Grenoble, France
//-======================================================================


package fr.esrf.TangoDs;

/**
 * Java code to define Polled Object.
 *
 * @author	$Author$
 * @version	$Revision$
 */

import fr.esrf.Tango.*;
import org.omg.CORBA.Any;

public class PollObj
{
	protected DeviceImpl	dev;			
	protected int			type;			
	protected String 		name;			
	protected TimeVal		upd;			
	protected TimeVal		needed_time;	
	protected double		max_delta_t;	
	protected PollRing		ring;			

//==========================================================================
/**
 *	Constructor for the PollObj class. 
 *
 * @param d		The device pointer
 * @param ty	The polled object type
 * @param na	The polled object name
 * @param user_upd	The polling update period (in mS)
 */
//==========================================================================
	public PollObj(DeviceImpl d, int ty, String na, int user_upd)
	{
		this.dev = d;
		this.type = ty;
		this.name = na;
		this.ring = new PollRing(d.get_poll_ring_depth());

		this.needed_time = new TimeVal(0,0,0);
		this.upd         = new TimeVal();
		if (user_upd < 1000)
		{
			upd.tv_usec = user_upd * 1000;
			upd.tv_sec = 0;
		}
		else
		{
			upd.tv_sec = user_upd / 1000;
			upd.tv_usec = (user_upd - (upd.tv_sec * 1000)) * 1000;
		}
		max_delta_t = (user_upd / 1000.0) * dev.get_poll_old_factor();
	}
//==========================================================================
/**
 *	This method insert a new element in the object ring
 *			buffer when its real data
 *
 * @param res		The Any returned by the command
 * @param when		The date when data was read
 * @param needed	The time needed to exceute command/attribute reading
 */
//==========================================================================
	synchronized void insert_data(Any res, TimeVal when, TimeVal needed)
	{
		ring.insert_data(res,when);
		needed_time = needed;
	}
//==========================================================================
/**
 *	This method insert a new element in the object ring
 *			buffer when its real data
 *
 * @param res		The Any returned by the command
 * @param when		The date when data was read
 * @param needed	The time needed to exceute command/attribute reading
 */
//==========================================================================
	synchronized void insert_data(AttributeValue res, TimeVal when, TimeVal needed)
	{
		ring.insert_data(res, when);
		needed_time = needed;
	}
//==========================================================================
/**
 *	This method insert a new element in the ring buffer
 *			when this element is an exception
 *
 * @param res		The Any returned by the command
 * @param when		The date when data was read
 * @param needed	The time needed to exceute command/attribute reading
 */
//==========================================================================
	synchronized void insert_except(DevFailed res, TimeVal when, TimeVal needed)
	{
		ring.insert_except(res,when);
		needed_time = needed;
	}
//==========================================================================
/**
 *	This method returns the last data stored in ring
 *	for a polled command or throw an exception if the
 *	command failed when it was executed
 */
//==========================================================================
	synchronized Any get_last_cmd_result() throws DevFailed
	{
		return ring.get_last_cmd_result();
	}
//==========================================================================
/**
 *	This method returns the last data stored in ring
 *	for a polled attribute or throw an exception if the
 *	read attribuite operation failed when it was executed
 *
 * @param new_upd	The new update period (in mS)
 */
//==========================================================================
	void update_upd(long new_upd)
	{
		if (new_upd < 1000)
		{
			upd.tv_usec = (int)(new_upd * 1000);
			upd.tv_sec = 0;
		}
		else
		{
			upd.tv_sec = (int)( new_upd / 1000);
			upd.tv_usec = (int)((new_upd - (upd.tv_sec * 1000)) * 1000);
		}
		max_delta_t = (int)(new_upd / 1000.0) * dev.get_poll_old_factor();	
	}
//==========================================================================
/**
 *	This method get command history from the ring buffer
 *
 * @param n	records number
 * @return the sequence where command result is stored.
 */
//==========================================================================
	synchronized DevCmdHistory[] get_cmd_history(int n)
	{
		return ring.get_cmd_history(n);	
	}
//==========================================================================
/**
 *	This method get attribute history from the ring buffer
 *
 * @param	n	records number
 * @param	attr_type The attribute type
 * @return the sequence where command result is stored.
 */
//==========================================================================
	synchronized DevAttrHistory[] get_attr_history(int n, int attr_type)
	{
		return ring.get_attr_history(n, attr_type);
	}


	//===============================================================
	//===============================================================
	double get_authorized_delta()
	{
		return max_delta_t;
	}
	//===============================================================
	//===============================================================
	synchronized boolean is_ring_empty()
	{
		return is_ring_empty_i();
	}
	//===============================================================
	//===============================================================
	boolean is_ring_empty_i()
	{
		return ring.is_empty();
	}
	//===============================================================
	//===============================================================
	synchronized int get_upd()
	{
		return get_upd_i();
	}	
	//===============================================================
	//===============================================================
	int get_upd_i()
	{
		return  ((upd.tv_sec * 1000) + (upd.tv_usec / 1000));
	}
	//===============================================================
	//===============================================================
	synchronized String get_name()
	{
		return get_name_i();
	}
	//===============================================================
	//===============================================================
	String get_name_i()
	{
		return name;
	}
	//===============================================================
	//===============================================================
	synchronized int get_needed_time()
	{
		return get_needed_time_i();
	}
	//===============================================================
	//===============================================================
	int get_needed_time_i()
	{
		return (int)((needed_time.tv_sec * 1000) + (needed_time.tv_usec / 1000.0));
	}
	//===============================================================
	//===============================================================
	synchronized int get_type()
	{
		return get_type_i();
	}
	//===============================================================
	//===============================================================
	int get_type_i()
	{
		return type;
	}
	//===============================================================
	//===============================================================
	synchronized double get_last_insert_date()
	{
		return get_last_insert_date_i();
	}
	//==========================================================================
	//==========================================================================
	double get_last_insert_date_i()
	{
		TimeVal last = ring.get_last_insert_date();
		double last_d = (double)last.tv_sec + ((double)last.tv_usec / 1000000);
		return last_d;
	}
	//===============================================================
	//===============================================================
	synchronized boolean is_last_an_error()
	{
		return is_last_an_error_i();
	}
	//===============================================================
	//===============================================================
	boolean is_last_an_error_i()
	{
		return ring.is_last_an_error();
	}
	//===============================================================
	//===============================================================
	DevFailed  synchronizedget_last_except()
	{
		return get_last_except_i();
	}
	//===============================================================
	//===============================================================
	DevFailed get_last_except_i()
	{
		return ring.get_last_except();
	}
	//===============================================================
	//===============================================================
	synchronized double[] get_delta_t(int nb) throws DevFailed
	{
		return get_delta_t_i(nb);
	}
	//===============================================================
	//===============================================================
	double[] get_delta_t_i(int nb) throws DevFailed
	{
		return ring.get_delta_t(nb);
	}
	//===============================================================
	//===============================================================
	synchronized int get_elt_nb_in_buffer()
	{
		return get_elt_nb_in_buffer_i();
	}
	//===============================================================
	//===============================================================
	int get_elt_nb_in_buffer_i()
	{
		return ring.size();
	}
	//===============================================================
	//===============================================================
	AttributeValue get_last_attr_value() throws DevFailed
	{
		return ring.get_last_attr_value();
	}
	//===============================================================
	//===============================================================
}
