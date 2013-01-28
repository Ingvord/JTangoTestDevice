static const char *RcsId = "$Id$\n$Name$";

//+=============================================================================
//
// file :               DServer.cpp
//
// description :        C++ source for the DServer class and its commands. 
//			The class is derived from Device. It represents the
//			CORBA servant object which will be accessed from the
//			network. All commands which can be executed on a
//			DServer object are implemented in this file.
//
// project :            TANGO
//
// author(s) :          E.Taurel
//
// $Revision$
//
// $Log$
// Revision 3.2  2003/05/16 08:46:16  taurel
// Many changes for release 3.0.1. The most important ones are :
// - Timeout are backs
// - Multiple db servers (change in TANGO_HOST syntax)
// - Added methods to print DeviceData, DeviceDataHistory, DeviceAttribute and DeviceAttributeHistory instances
// - Attributes name stored in blackbox
// - Remove check if a class is created without any device
// - It's now possible to create a DeviceProxy from its alias name
// - Command, attribute names are case insensitive
// - Change parameters of some DeviceProxy logging methods
// - Change parameters of DeviceProxy asynchronous replies calls
// - New serialization model in device server (no serialization model)
// - Win32 (2000) device server service does not exit at loggoff anymore
// - Miscellaneous bug fixes
//
// Revision 3.1  2003/04/03 15:24:09  taurel
// Added methods to print DeviceData, DeviceAttribute, DeviceDataHistory
// and DeviceAttributeHistory instance
//
// Revision 3.0  2003/03/25 16:43:02  taurel
// Many changes for Tango release 3.0 including
// - Added full logging features
// - Added asynchronous calls
// - Host name of clients now stored in black-box
// - Three serialization model in DS
// - Fix miscellaneous bugs
// - Ported to gcc 3.2
// - Added ApiUtil::cleanup() and destructor methods
// - Some internal cleanups
// - Change the way how TangoMonitor class is implemented. It's a recursive
//   mutex
//
// Revision 2.9  2003/01/09 12:03:16  taurel
// - Ported to gcc 3.2
// - Added ApiUtil::cleanup() and ApiUtil::~ApiUtil() methods
// - Replace some ORB * by ORB_ptr
// - Use CORBA::ORB::is_nil() instead of comparing to NULL
//
// Revision 2.8  2002/12/16 12:07:19  taurel
// No change in code at all but only forgot th emost important line in
// list of updates in the previous release :
// - Change underlying ORB from ORBacus to omniORB
//
// Revision 2.7  2002/12/16 10:16:21  taurel
// - New method get_device_list() in Util class
// - Util::get_class_list takes DServer device into account
// - Util::get_device_by_name() takes DServer device into account
// - Util::get_device_list_by_class() takes DServer device into account
// - New parameter to the attribute::set_value() method to enable CORBA to free
// memory allocated for the attribute
//
// Revision 2.6  2002/10/17 07:43:06  taurel
// Fix bug in history stored by the polling thread :
// - We need one copy of the attribute data to build an history!!! It is true
// also for command which return data created by the DeviceImpl::create_xxx
// methods. Chnage in pollring.cpp/pollring.h/dserverpoll.cpp/pollobj.cpp
// and pollobj.h
//
// Revision 2.5  2002/10/15 11:27:19  taurel
// Fix bugs in device.cpp file :
// - Protect the state and status CORBA attribute with the device monitor
// Add the "TgLibVers" string as a #define in tango_config.h
//
// Revision 2.4  2002/08/12 15:06:54  taurel
// Several big fixes and changes
//   - Remove HP-UX specific code
//   - Fix bug in polling alogorithm which cause the thread to enter an infinite
//     loop (pollthread.cpp)
//   - For bug for Win32 device when trying to set attribute config
//     (attribute.cpp)
//
// Revision 2.3  2002/07/02 15:22:24  taurel
// Miscellaneous small changes/bug fixes for Tango CPP release 2.1.0
//     - classes reference documentation now generated using doxygen instead of doc++
//     - A little file added to the library which summarizes version number.
//       The RCS/CVS "ident" command will now tells you that release library x.y.z is composed
//       by C++ client classes set release a.b and C++ server classes set release c.d
//     - Fix incorrect field setting for DevFailed exception re-thrown from a CORBA exception
//     - It's now not possible to poll the Init command
//     - It's now possible to define a default class doc. per control system
//       instance (using property)
//     - The test done to check if attribute value has been set before it is
//       returned to caller is done only if the attribute quality is set to VALID
//     - The JTCInitialize object is now stored in the Util
//     - Windows specific : The tango.h file now also include winsock.h
//
// Revision 2.2  2002/04/30 10:50:41  taurel
// Don't check alarm on attribute if attribute quality factor is INVALID
//
// Revision 2.1  2002/04/29 12:24:03  taurel
// Fix bug in attribute::set_value method and on the check against min and max value when writing attributes
//
// Revision 2.0  2002/04/09 14:45:10  taurel
// See Tango WEB pages for list of changes
//
//
// copyleft :           European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
//-=============================================================================

#if HAVE_CONFIG_H
#include <ac_config.h>
#endif

#include <tango.h>
#include <math.h>

#ifdef WIN32
	#include <sys/timeb.h>
#else
	#include <sys/time.h>
#endif

#if ((defined WIN32) || (defined __SUNPRO_CC) || (defined GCC_STD))
	#include <iomanip>
#else
	#include <iomanip.h>
#endif

namespace Tango
{

//+----------------------------------------------------------------------------
//
// method : 		DServer::polled_device()
// 
// description : 	command to read all the devices actually polled by the
//			device server
//
// out :		The device name list in a strings sequence
//
//-----------------------------------------------------------------------------

Tango::DevVarStringArray *DServer::polled_device()
{
	NoSyncModelTangoMonitor mon(this);

	cout4 << "In polled_device command" << endl;	
	
	long nb_class = class_list.size();
	vector<string> dev_name;
	
	try
	{
		for (int i = 0;i < nb_class;i++)
		{
			long nb_dev = class_list[i]->get_device_list().size();
			for (long j = 0;j < nb_dev;j++)
			{
				if ((class_list[i]->get_device_list())[j]->is_polled() == true)
				{
					dev_name.push_back((class_list[i]->get_device_list())[j]->get_name().c_str());
				}
			}			
		}
	}
	catch (bad_alloc)
	{
		Except::throw_exception((const char *)"API_MemoryAllocation",
				        (const char *)"Can't allocate memory in server",
				        (const char *)"DServer::polled_device");
	}

//
// Return an empty sequence if no devices are polled
//
	
	if (dev_name.size() == 0)
	{
		Tango::DevVarStringArray *ret = new Tango::DevVarStringArray();
		ret->length(0);
		return ret;
	}

//
// Returned device name list to caller (sorted)
//

	sort(dev_name.begin(),dev_name.end());
	long nb_dev = dev_name.size();	
	Tango::DevVarStringArray *ret = new Tango::DevVarStringArray(nb_dev);
	ret->length(nb_dev);
	for (long k = 0;k < nb_dev;k++)
		(*ret)[k] = dev_name[k].c_str();
			
	return(ret);
	
}

//+----------------------------------------------------------------------------
//
// method : 		DServer::dev_polled_status()
// 
// description : 	command to read device polling status
//
// out :		The device polling status as a string (multiple lines)
//
//-----------------------------------------------------------------------------

Tango::DevVarStringArray *DServer::dev_poll_status(string &dev_name)
{
	NoSyncModelTangoMonitor mon(this);

	cout4 << "In dev_poll_status method" << endl;

//
// Find the device
//	

	Tango::Util *tg = Tango::Util::instance();
	DeviceImpl *dev;
	
	dev = tg->get_device_by_name(dev_name);

	vector<PollObj *> &poll_list = dev->get_poll_obj_list();
	long nb_poll_obj = poll_list.size();

//
// Return an empty sequence if nothing is polled for this device
//

	if (nb_poll_obj == 0)
	{
		Tango::DevVarStringArray *ret;
		ret = new DevVarStringArray();
		ret->length(0);
		return ret;
	}
	
	
	long i,nb_cmd,nb_attr;
	
//
// Compute how many cmds and/or attributes are polled
//

	nb_cmd = nb_attr = 0;	
	for (i = 0;i < nb_poll_obj;i++)
	{
		if (poll_list[i]->get_type() == Tango::POLL_CMD)
			nb_cmd++;
		else
			nb_attr++;
	}

//
// Allocate memory for returned strings
//

	Tango::DevVarStringArray *ret;
	ret = new DevVarStringArray(nb_poll_obj);
	ret->length(nb_poll_obj);

//
// Populate returned strings
//

	long cmd_ind = 0;
	long attr_ind = nb_cmd;
	string returned_info;
	
	for(i = 0;i < nb_poll_obj;i++)
	{
	
//
// First, the name
//

		Tango::PollObjType type = poll_list[i]->get_type();
		if (type == Tango::POLL_CMD)
		{
			returned_info = "Polled command name = ";
			long k;	
			long nb_cmd = dev->get_device_class()->get_command_list().size();
			for (k = 0;k < nb_cmd;k++)
			{
				if (dev->get_device_class()->get_command_list()[k]->get_lower_name() == poll_list[i]->get_name())
				{
					returned_info = returned_info + dev->get_device_class()->get_command_list()[k]->get_name();
					break;
				}
			}
		}		
		else
		{
			returned_info = "Polled attribute name = ";
			Attribute &att = dev->get_device_attr()->get_attr_by_name(poll_list[i]->get_name().c_str());
			returned_info = returned_info + att.get_name();
		}
				
//
// Add update period
//

		returned_info = returned_info + "\nPolling period (mS) = ";
		long tmp = poll_list[i]->get_upd();
		TangoSys_MemStream s;
		string tmp_str;
		s << tmp;
		s >> tmp_str;
		returned_info = returned_info + tmp_str;
		
		s.clear();	// clear the stream eof flag
					
//
// Add ring buffer depth
//

		returned_info = returned_info + "\nPolling ring buffer depth = ";
		long depth = dev->get_poll_ring_depth();
		if (depth == 0)
			s << DefaultPollRingDepth;
		else
			s << depth;
		s >> tmp_str;
		returned_info = returned_info + tmp_str;
		
		s.clear();	// Clear the stream eof flag
							
//
// Add a message if the data ring is empty
//

		if (poll_list[i]->is_ring_empty() == true)
		{
			returned_info = returned_info + "\nNo data recorded yet";
		}
		else
		{

//
// Take polled object ownership inorder to have coherent info returned to caller
// We don't want the polling thread to insert anew elt in polling ring while
// we are getting these data. Therefore, use the xxx_i methods
//

			{
			omni_mutex_lock sync(*(poll_list[i]));
							
//
// Add needed time to execute last command
//

			returned_info = returned_info + "\nTime needed for the last ";
			if (type == Tango::POLL_CMD)
				returned_info = returned_info + "command execution (mS) = ";
			else
				returned_info = returned_info + "attribute reading (mS) = "; 
			double tmp_db = poll_list[i]->get_needed_time_i();

			s.setf(ios::fixed);							
			s << setprecision(3) << tmp_db;
			s >> tmp_str;
			returned_info = returned_info + tmp_str; 
			
			s.clear();
			
//
// Add not updated since... info
//

			returned_info = returned_info + "\nData not updated since ";
			double since = poll_list[i]->get_last_insert_date_i();
			struct timeval now;
#ifdef WIN32
			struct _timeb now_win;
			_ftime(&now_win);
			now.tv_sec = (unsigned long)now_win.time;
			now.tv_usec = (long)now_win.millitm * 1000;
#else
			gettimeofday(&now,NULL);
#endif
			now.tv_sec = now.tv_sec - DELTA_T;
			double now_d = (double)now.tv_sec + ((double)now.tv_usec / 1000000);
			double diff_t = now_d - since;
			if (diff_t < 1.0)
			{
				long nb_msec = (long)(diff_t * 1000);
				s << nb_msec;
				s >> tmp_str;
				s.clear();
				
				returned_info = returned_info + tmp_str + " mS";
			}
			else if (diff_t < 60.0)
			{
				long nb_sec = (long)diff_t;
				long nb_msec = (long)((diff_t - nb_sec) * 1000);
				
				s << nb_sec;
				s >> tmp_str;
				s.clear();			
				returned_info = returned_info + tmp_str + " S and ";
				
				s << nb_msec;
				s >> tmp_str;
				s.clear();				
				returned_info = returned_info + tmp_str + " mS";
			}
			else
			{
				long nb_min = (long)(diff_t / 60);
				long nb_sec = (long)(diff_t - (60 * nb_min));
				long nb_msec = (long)((diff_t - (long)diff_t) * 1000);

				s << nb_min;
				s >> tmp_str;
				s.clear();
				returned_info = returned_info + tmp_str + " MN";
				
				if (nb_sec != 0)
				{
					s << nb_sec;
					s >> tmp_str;
					s.clear();
					returned_info = returned_info + " ," + tmp_str + " S";
				}
				
				if (nb_msec != 0)
				{
					s << nb_msec;
					s >> tmp_str;
					s.clear();
					returned_info = returned_info + " and " + tmp_str + " mS";
				}
			}
			
//
// Add delta_t between last record(s)
//

			try
			{
				vector<double> delta;
				poll_list[i]->get_delta_t_i(delta,4);
				
				returned_info = returned_info + "\nDelta between last records (in mS) = ";
				for (unsigned long j = 0;j < delta.size();j++)
				{
					long nb_msec = (long)(delta[j] * 1000);
					s << nb_msec;
					s >> tmp_str;
					s.clear();
					returned_info = returned_info + tmp_str;
					if (j != (delta.size() - 1))
						returned_info = returned_info + ", ";
				}				
			}
			catch (Tango::DevFailed)
			{
			}
			
			
//
// Add last polling exception fields (if any)
//

			if (poll_list[i]->is_last_an_error_i() == true)
			{
				if (type == Tango::POLL_CMD)
					returned_info = returned_info + "\nLast command execution FAILED :";
				else
					returned_info = returned_info + "\nLast attribute read FAILED :";
				Tango::DevFailed *exe_ptr = poll_list[i]->get_last_except_i();
				returned_info = returned_info + "\n\tReason = " + exe_ptr->errors[0].reason.in();
				returned_info = returned_info + "\n\tDesc = " + exe_ptr->errors[0].desc.in();
				returned_info = returned_info + "\n\tOrigin = " + exe_ptr->errors[0].origin.in();
			}
			
//
// Release polled object monitor (only a compiler block end)
//
			}
		}

//
// Init. string in sequence
//
			
		if (type == Tango::POLL_CMD)
		{
			(*ret)[cmd_ind] = CORBA::string_dup(returned_info.c_str());
			cmd_ind++;
		}
		else
		{
			(*ret)[attr_ind] = CORBA::string_dup(returned_info.c_str());
			attr_ind++;
		}
	}
				
	return(ret);
	
}

//+----------------------------------------------------------------------------
//
// method : 		DServer::add_obj_polling()
// 
// description : 	command to add one object to be polled
//
// in :			The polling parameters :
//				device name
//				object type (command or attribute)
//				object name
//				update period in mS (in the long array)
//
//-----------------------------------------------------------------------------

void DServer::add_obj_polling(const Tango::DevVarLongStringArray *argin,
			      bool with_db_upd)
{
	NoSyncModelTangoMonitor nosyn_mon(this);

	cout4 << "In add_obj_polling method" << endl;
	unsigned long i;
	for (i = 0;i < argin->svalue.length();i++)
		cout4 << "Input string = " << (argin->svalue)[i].in() << endl;
	for (i = 0;i < argin->lvalue.length();i++)
		cout4 << "Input long = " << (argin->lvalue)[i] << endl;
		
//
// Check that parameters number is correct
//

	if ((argin->svalue.length() != 3) || (argin->lvalue.length() != 1))
	{
		Except::throw_exception((const char *)"API_WrongNumberOfArgs",
					(const char *)"Incorrect number of inout arguments",
					(const char *)"DServer::add_obj_polling");
	}
	
//
// Find the device
//	

	Tango::Util *tg = Tango::Util::instance();
	DeviceImpl *dev;
	try
	{
		dev = tg->get_device_by_name((argin->svalue)[0]);
	}
	catch (Tango::DevFailed &e)
	{
		TangoSys_OMemStream o;
		o << "Device " << (argin->svalue)[0] << " not found" << ends;
		
		Except::re_throw_exception(e,(const char *)"API_DeviceNotFound",o.str(),
				   	   (const char *)"DServer::add_obj_polling");
	}

//
// Check that the command (or the attribute) exists. For command, also checks
// that it does not need input value.
//

	string obj_type((argin->svalue)[1]);
	transform(obj_type.begin(),obj_type.end(),obj_type.begin(),::tolower);
	string obj_name((argin->svalue)[2]);
	transform(obj_name.begin(),obj_name.end(),obj_name.begin(),::tolower);
	PollObjType type;
	
	if (obj_type == PollCommand)
	{
		dev->check_command_exists(obj_name);
		type = Tango::POLL_CMD;		
	}
	else if (obj_type == PollAttribute)
	{
		dev->get_device_attr()->get_attr_by_name((argin->svalue)[2]);
		type = Tango::POLL_ATTR;	
	}
	else
	{
		TangoSys_OMemStream o;
		o << "Object type " << obj_type << " not supported" << ends;
		Except::throw_exception((const char *)"API_NotSupported",o.str(),
					(const char *)"DServer::add_obj_polling");
	}
	
//
// If it's for the Init command, refuse to poll it
//

	if (obj_type == PollCommand)
	{
		if (obj_name == "init")
		{
			TangoSys_OMemStream o;
			o << "It's not possible to poll the Init command!" << ends;
			Except::throw_exception((const char *)"API_NotSupported",o.str(),
						(const char *)"DServer::add_obj_polling");
		}
	}

//
// Check that the object is not already polled
//

	vector<PollObj *> &poll_list = dev->get_poll_obj_list();
	for (i = 0;i < poll_list.size();i++)
	{
		if (poll_list[i]->get_type() == type)
		{
			if (poll_list[i]->get_name() == obj_name)
			{
				TangoSys_OMemStream o;
				if (type == Tango::POLL_CMD)
					o << "Command ";
				else
					o << "Attribute ";
				o << obj_name << " already polled" << ends;
				Except::throw_exception((const char *)"API_AlreadyPolled",
							o.str(),
							(const char *)"DServer::add_obj_polling");
			}
		}
	}
		
//
// Check that the update period is not to small
//

	long upd = (argin->lvalue)[0];
	if (upd < MIN_POLL_PERIOD)
	{
		TangoSys_OMemStream o;
		o << (argin->lvalue)[0] << " is below the min authorized period (100 mS)" << ends;
		Except::throw_exception((const char *)"API_NotSupported",o.str(),
					(const char *)"DServer::add_obj_polling");
	}
	
//
// Round the update period (which is in mS) to the next tenth of a second
//

/*	double upd_db = (double)upd;
	double remain = fmod(upd_db,(double)MIN_POLL_PERIOD);
	double base = floor(upd_db) - remain;
	if (remain >= 50)
		upd = (long)base + MIN_POLL_PERIOD;
	else
		upd = (long)base;
	cout4 << "Rounded polling period = " << upd << " mS" << endl;*/
	
//
// Create a new PollObj instance for this object
// Protect this code by a monitor in case of the polling thread using one of
// the vector element.
//

	long depth = dev->get_poll_ring_depth();

	dev->get_dev_monitor().get_monitor();	
	if (depth == 0)
		poll_list.push_back(new PollObj(dev,
						type,
						obj_name,
						upd));
	else
		poll_list.push_back(new PollObj(dev,
						type,
						obj_name,
						upd,depth));
	dev->get_dev_monitor().rel_monitor();

//
// Send command to the polling thread but wait in case of previous cmd 
// still not executed
//

	cout4 << "Sending cmd to polling thread" << endl;
	int interupted;
	TangoMonitor &mon = tg->get_poll_monitor();
	PollThCmd &shared_cmd = tg->get_poll_shared_cmd();

	{	
		omni_mutex_lock sync(mon);
		if (shared_cmd.cmd_pending == true)
		{
			mon.wait();
		}
		shared_cmd.cmd_pending = true;
		shared_cmd.cmd_code = POLL_ADD_OBJ;
		shared_cmd.dev = dev;
		shared_cmd.index = poll_list.size() - 1;

		mon.signal();
		
		cout4 << "Cmd sent to polling thread" << endl;
//
// Wait for thread to execute command
//

		while (shared_cmd.cmd_pending == true)
		{
			interupted = mon.wait(DEFAULT_TIMEOUT);
			if ((shared_cmd.cmd_pending == true) && (interupted == false))
			{
				cout4 << "TIME OUT" << endl;
				delete poll_list.back();
				poll_list.pop_back();
				Except::throw_exception((const char *)"API_CommandTimedOut",
					        	(const char *)"Polling thread blocked !!!",
					        	(const char *)"DServer::add_obj_polling");
			}
		}
	}
	cout4 << "Thread cmd normally executed" << endl;
		
//
// Update polling parameters in database (if wanted and possible)
// If the property is already there (it should not but...), only update its
// polling period
//

	if ((with_db_upd == true) && (Tango::Util::_UseDb == true))
	{
		TangoSys_MemStream s;
		string upd_str;
		s << upd;
		s >> upd_str;
		bool found = false;

		DbDatum db_info("polled_cmd");	
		if (type == Tango::POLL_CMD)
		{
			vector<string> &non_auto_list = dev->get_non_auto_polled_cmd();
			vector<string>::iterator ite;
			for (ite = non_auto_list.begin();ite < non_auto_list.end();ite++)
			{
				if (*ite == obj_name)
				{
					non_auto_list.erase(ite);
					db_info.name = "non_auto_polled_cmd";
					db_info << non_auto_list;
					found = true;
					break;
				}
			}
			if (found == false)
			{	
				vector<string> &cmd_list = dev->get_polled_cmd();
				for (i = 0;i < cmd_list.size();i = i+2)
				{
					if (cmd_list[i] == obj_name)
					{
						cmd_list[i + 1] = upd_str;
						break;
					}
				}
				if (i == cmd_list.size())
				{
					cmd_list.push_back(obj_name);
					cmd_list.push_back(upd_str);
				}
				db_info << cmd_list;
			}
		}
		else
		{
			vector<string> &non_auto_list = dev->get_non_auto_polled_attr();
			vector<string>::iterator ite;
			for (ite = non_auto_list.begin();ite < non_auto_list.end();ite++)
			{
				if (*ite == obj_name)
				{
					non_auto_list.erase(ite);
					db_info.name = "non_auto_polled_attr";
					db_info << non_auto_list;
					found = true;
					break;
				}
			}
			if (found == false)
			{	
				db_info.name = "polled_attr";		
				vector<string> &attr_list = dev->get_polled_attr();
				for (i = 0;i < attr_list.size();i = i+2)
				{
					if (attr_list[i] == obj_name)
					{
						attr_list[i + 1] = upd_str;
						break;
					}
				}
				if (i == attr_list.size())
				{
					attr_list.push_back(obj_name);
					attr_list.push_back(upd_str);
				}
				db_info << attr_list;
			}
		}	

		DbData send_data;
		send_data.push_back(db_info);	
		dev->get_db_device()->put_property(send_data);

		cout4 << "Polling properties updated" << endl;
	}
	
//
// Mark the device as polled
//

	dev->is_polled(true);
}


//+----------------------------------------------------------------------------
//
// method : 		DServer::upd_obj_polling_period()
// 
// description : 	command to upadte an already polled object update period
//
// in :			The polling parameters :
//				device name
//				object type (command or attribute)
//				object name
//				update period in mS (in the long array)
//
//-----------------------------------------------------------------------------

void DServer::upd_obj_polling_period(const Tango::DevVarLongStringArray *argin,
				     bool with_db_upd)
{
	NoSyncModelTangoMonitor nosync_mon(this);

	cout4 << "In upd_obj_polling_period method" << endl;
	unsigned long i;
	for (i = 0;i < argin->svalue.length();i++)
		cout4 << "Input string = " << (argin->svalue)[i].in() << endl;
	for (i = 0;i < argin->lvalue.length();i++)
		cout4 << "Input long = " << (argin->lvalue)[i] << endl;

//
// Check that parameters number is correct
//

	if ((argin->svalue.length() != 3) || (argin->lvalue.length() != 1))
	{
		Except::throw_exception((const char *)"API_WrongNumberOfArgs",
					(const char *)"Incorrect number of inout arguments",
					(const char *)"DServer::upd_obj_polling_period");
	}
	
//
// Find the device
//	

	Tango::Util *tg = Tango::Util::instance();
	DeviceImpl *dev;
	try
	{
		dev = tg->get_device_by_name((argin->svalue)[0]);
	}
	catch (Tango::DevFailed &e)
	{
		TangoSys_OMemStream o;
		o << "Device " << (argin->svalue)[0] << " not found" << ends;
		
		Except::re_throw_exception(e,(const char *)"API_DeviceNotFound",o.str(),
				   	   (const char *)"DServer::upd_obj_polling_period");
	}
	
//
// Check that the device is polled
//

	if (dev->is_polled() == false)
	{
		TangoSys_OMemStream o;
		o << "Device " << (argin->svalue)[0] << " is not polled" << ends;
		
		Except::throw_exception((const char *)"API_DeviceNotPolled",o.str(),
				   	(const char *)"DServer::upd_obj_polling_period");
	}
	
//
// Find the wanted object in the list of device polled object
//

	string obj_type((argin->svalue)[1]);
	transform(obj_type.begin(),obj_type.end(),obj_type.begin(),::tolower);
	string obj_name((argin->svalue)[2]);
	transform(obj_name.begin(),obj_name.end(),obj_name.begin(),::tolower);
	PollObjType type;
	
	if (obj_type == PollCommand)
	{
		type = Tango::POLL_CMD;		
	}
	else if (obj_type == PollAttribute)
	{
		type = Tango::POLL_ATTR;	
	}
	else
	{
		TangoSys_OMemStream o;
		o << "Object type " << obj_type << " not supported" << ends;
		Except::throw_exception((const char *)"API_NotSupported",o.str(),
					(const char *)"DServer::upd_obj_polling_period");
	}
		
	vector<PollObj *>::iterator ite = dev->get_polled_obj_by_type_name(type,obj_name);
		
//
// Update polling period
//

	(*ite)->update_upd((argin->lvalue)[0]);
	
//
// Send command to the polling thread
//

	TangoMonitor &mon = tg->get_poll_monitor();
	PollThCmd &shared_cmd = tg->get_poll_shared_cmd();

	{	
		omni_mutex_lock sync(mon);
		if (shared_cmd.cmd_pending == true)
		{
			mon.wait();
		}
		shared_cmd.cmd_pending = true;
		shared_cmd.cmd_code = POLL_UPD_PERIOD;
		shared_cmd.dev = dev;
		shared_cmd.name = obj_name;
		shared_cmd.type = type;
		shared_cmd.new_upd = (argin->lvalue)[0];
	
		mon.signal();
	}
	
	
//
// Update database property --> Update polling period if this object is already
// defined in the polling property. Add object name and update period if the 
// object is not known in the property
//

	if ((with_db_upd == true) && (Tango::Util::_UseDb == true))
	{
		TangoSys_MemStream s;
		string upd_str;	
		s << (argin->lvalue)[0];
		s >> upd_str;
	
		DbDatum db_info("polled_attr");		
		if (type == Tango::POLL_CMD)
		{
			db_info.name = "polled_cmd";	
			vector<string> &cmd_list = dev->get_polled_cmd();
			for (i = 0;i < cmd_list.size();i = i+2)
			{
				if (cmd_list[i] == obj_name)
				{
					cmd_list[i + 1] = upd_str;
					break;
				}
			}
			if (i == cmd_list.size())
			{
				cmd_list.push_back(obj_name);
				cmd_list.push_back(upd_str);
			}
			db_info << cmd_list;
		}
		else
		{
			vector<string> &attr_list = dev->get_polled_attr();
			for (i = 0;i < attr_list.size();i = i+2)
			{
				if (attr_list[i] == obj_name)
				{
					attr_list[i + 1] = upd_str;
					break;
				}
			}
			if (i == attr_list.size())
			{
				attr_list.push_back(obj_name);
				attr_list.push_back(upd_str);
			}
			db_info << attr_list;
		}
		
		DbData send_data;
		send_data.push_back(db_info);
		dev->get_db_device()->put_property(send_data);
	}
}


//+----------------------------------------------------------------------------
//
// method : 		DServer::rem_obj_polling()
// 
// description : 	command to remove an already polled object from the device
//			polled object list
//
// in :			The polling parameters :
//				device name
//				object type (command or attribute)
//				object name
//
//-----------------------------------------------------------------------------

void DServer::rem_obj_polling(const Tango::DevVarStringArray *argin,
			      bool with_db_upd)
{
	NoSyncModelTangoMonitor nosync_mon(this);

	cout4 << "In rem_obj_polling method" << endl;
	unsigned long i;
	for (i = 0;i < argin->length();i++)
		cout4 << "Input string = " << (*argin)[i].in() << endl;

//
// Check that parameters number is correct
//

	if (argin->length() != 3)
	{
		Except::throw_exception((const char *)"API_WrongNumberOfArgs",
					(const char *)"Incorrect number of inout arguments",
					(const char *)"DServer::rem_obj_polling");
	}
	
//
// Find the device
//	

	Tango::Util *tg = Tango::Util::instance();
	DeviceImpl *dev;
	try
	{
		dev = tg->get_device_by_name((*argin)[0]);
	}
	catch (Tango::DevFailed &e)
	{
		TangoSys_OMemStream o;
		o << "Device " << (*argin)[0] << " not found" << ends;
		
		Except::re_throw_exception(e,(const char *)"API_DeviceNotFound",o.str(),
				   	   (const char *)"DServer::rem_obj_polling");
	}
	
//
// Check that the device is polled
//

	if (dev->is_polled() == false)
	{
		TangoSys_OMemStream o;
		o << "Device " << (*argin)[0] << " is not polled" << ends;
		
		Except::throw_exception((const char *)"API_DeviceNotPolled",o.str(),
				   	(const char *)"DServer::rem_obj_polling");
	}
	
//
// Find the wanted object in the list of device polled object
//

	string obj_type((*argin)[1]);
	transform(obj_type.begin(),obj_type.end(),obj_type.begin(),::tolower);
	string obj_name((*argin)[2]);
	transform(obj_name.begin(),obj_name.end(),obj_name.begin(),::tolower);
	PollObjType type;
	
	if (obj_type == PollCommand)
	{
		type = Tango::POLL_CMD;		
	}
	else if (obj_type == PollAttribute)
	{
		type = Tango::POLL_ATTR;	
	}
	else
	{
		TangoSys_OMemStream o;
		o << "Object type " << obj_type << " not supported" << ends;
		Except::throw_exception((const char *)"API_NotSupported",o.str(),
					(const char *)"DServer::rem_obj_polling");
	}
		
	vector<PollObj *>::iterator ite = dev->get_polled_obj_by_type_name(type,obj_name);

//
// Send command to the polling thread
//

	cout4 << "Sending cmd to polling thread" << endl;
	int interupted;
	TangoMonitor &mon = tg->get_poll_monitor();
	PollThCmd &shared_cmd = tg->get_poll_shared_cmd();
	{	
		omni_mutex_lock sync(mon);
		if (shared_cmd.cmd_pending == true)
		{
			mon.signal();
		}
		shared_cmd.cmd_pending = true;
		shared_cmd.cmd_code = POLL_REM_OBJ;
		shared_cmd.dev = dev;
		shared_cmd.name = obj_name;
		shared_cmd.type = type;
	
		mon.signal();
		
		cout4 << "Cmd sent to polling thread" << endl;
	
//
// Wait for thread to execute command
//

		while (shared_cmd.cmd_pending == true)
		{
			interupted = mon.wait(DEFAULT_TIMEOUT);
			if ((shared_cmd.cmd_pending == true) && (interupted == false))
			{
				cout4 << "TIME OUT" << endl;
				Except::throw_exception((const char *)"API_CommandTimedOut",
					        	(const char *)"Polling thread blocked !!!",
					        	(const char *)"DServer::rem_obj_polling");
			}
		}
	}
	cout4 << "Thread cmd normally executed" << endl;

//
// Remove the object from the polled object list
//

	vector<PollObj *> &poll_list = dev->get_poll_obj_list();
	
	dev->get_dev_monitor().get_monitor();
	delete(*ite);
	poll_list.erase(ite);
	dev->get_dev_monitor().rel_monitor();
	
//
// Mark the device as non polled if this was the last polled object
//

	if (poll_list.empty() == true)
		dev->is_polled(false);
					
//
// Update database property. This means remove object entry in the polling 
// properties if they exist or add it to the list of device not polled
// for automatic polling defined at command/attribute level.
// Do this if possible and wanted.
//

	if ((with_db_upd == true) && (Tango::Util::_UseDb == true))
	{
		DbData send_data;
		DbDatum db_info("polled_attr");
		bool update_needed = false;
	
		if (type == Tango::POLL_CMD)
		{
			db_info.name = "polled_cmd";
			vector<string> &cmd_list = dev->get_polled_cmd();
			vector<string>::iterator s_ite;	
			for (s_ite = cmd_list.begin();s_ite < cmd_list.end();++s_ite)
			{
				if (*s_ite == obj_name)
				{
					s_ite = cmd_list.erase(s_ite);
					cmd_list.erase(s_ite);
					db_info << cmd_list;
					update_needed = true;			
					break;
				}
				++s_ite;
			}
			if (update_needed == false)
			{
				vector<string> &non_auto_cmd = dev->get_non_auto_polled_cmd();
				for (s_ite = non_auto_cmd.begin();s_ite < non_auto_cmd.end();s_ite++)
				{
					if (*s_ite == obj_name)
						break;
				}
				if (s_ite == non_auto_cmd.end())
				{
					non_auto_cmd.push_back(obj_name);
					db_info.name = "non_auto_polled_cmd";
					db_info << non_auto_cmd;
					update_needed = true;
				}
			}
		}
		else
		{
			vector<string> &attr_list = dev->get_polled_attr();
			vector<string>::iterator s_ite;	
			for (s_ite = attr_list.begin();s_ite < attr_list.end();++s_ite)
			{
				if (*s_ite == obj_name)
				{
					s_ite = attr_list.erase(s_ite);
					attr_list.erase(s_ite);
					db_info << attr_list;
					update_needed = true;			
					break;
				}
				++s_ite;
			}
			if (update_needed == false)
			{
				vector<string> &non_auto_attr = dev->get_non_auto_polled_attr();
				for (s_ite = non_auto_attr.begin();s_ite < non_auto_attr.end();s_ite++)
				{
					if (*s_ite == obj_name)
						break;
				}
				if (s_ite == non_auto_attr.end())
				{
					non_auto_attr.push_back(obj_name);
					db_info.name = "non_auto_polled_attr";
					db_info << non_auto_attr;
					update_needed = true;
				}
			}
		}

		if (update_needed == true)
		{	
			DbData send_data;
			send_data.push_back(db_info);
			dev->get_db_device()->put_property(send_data);
		
			cout4 << "Database polling properties updated" << endl;
		}
	}
}

//+----------------------------------------------------------------------------
//
// method : 		DServer::stop_polling()
// 
// description : 	command to stop the polling thread
//
//-----------------------------------------------------------------------------

void DServer::stop_polling()
{
	NoSyncModelTangoMonitor nosync_mon(this);

	cout4 << "In stop_polling method" << endl;
	
//
// Send command to the polling thread and wait for its execution
//

	int interupted;
	Tango::Util *tg = Tango::Util::instance();	
	TangoMonitor &mon = tg->get_poll_monitor();
	PollThCmd &shared_cmd = tg->get_poll_shared_cmd();

	{	
		omni_mutex_lock sync(mon);
		if (shared_cmd.cmd_pending == true)
		{
			mon.signal();
		}
		shared_cmd.cmd_pending = true;
		shared_cmd.cmd_code = POLL_STOP;
	
		mon.signal();
		
		while (shared_cmd.cmd_pending == true)
		{
			interupted = mon.wait(DEFAULT_TIMEOUT);

			if ((shared_cmd.cmd_pending == true) && (interupted == false))
			{
				cout4 << "TIME OUT" << endl;
				Except::throw_exception((const char *)"API_CommandTimedOut",
					        	(const char *)"Polling thread blocked !!!",
					        	(const char *)"DServer::stop_polling");
			}
		}
	}

//
// Update polling status
//

	tg->poll_status(false);
	string &str = get_status();
	str = "The device is ON\nThe polling is OFF";
}


//+----------------------------------------------------------------------------
//
// method : 		DServer::start_polling()
// 
// description : 	command to start the polling thread
//
//-----------------------------------------------------------------------------

void DServer::start_polling()
{
	NoSyncModelTangoMonitor nosync_mon(this);

	cout4 << "In start_polling method" << endl;
	
//
// Send command to the polling thread and wait for its execution
//

	int interupted;
	Tango::Util *tg = Tango::Util::instance();	
	TangoMonitor &mon = tg->get_poll_monitor();
	PollThCmd &shared_cmd = tg->get_poll_shared_cmd();

	{	
		omni_mutex_lock sync(mon);
		if (shared_cmd.cmd_pending == true)
		{
			mon.signal();
		}
		shared_cmd.cmd_pending = true;
		shared_cmd.cmd_code = POLL_START;
	
		mon.signal();
		
		while (shared_cmd.cmd_pending == true)
		{
			interupted = mon.wait(DEFAULT_TIMEOUT);

			if ((shared_cmd.cmd_pending == true) && (interupted == false))
			{
				cout4 << "TIME OUT" << endl;
				Except::throw_exception((const char *)"API_CommandTimedOut",
					        	(const char *)"Polling thread blocked !!!",
					        	(const char *)"DServer::start_polling");
			}
		}
	}
	
//
// Update polling status
//

	tg->poll_status(true);
	string &str = get_status();
	str = "The device is ON\nThe polling is ON";
}

} // End of Tango namespace
