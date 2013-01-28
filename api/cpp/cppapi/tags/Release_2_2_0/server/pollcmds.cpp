static const char *RcsId = "$Header$";

//+=============================================================================
//
// file :               PollCmds.cpp
//
// description :        C++ source for the DServerClass and for the 
//			command class defined for this class. The singleton
//			class derived from DeviceClass. It implements the
//			command list and all properties and methods required
//			by the DServer once per process.
//
// project :            TANGO
//
// author(s) :          E.Taurel
//
// $Revision$
//
// $Log$
// Revision 2.7  2002/12/16 10:16:22  taurel
// - New method get_device_list() in Util class
// - Util::get_class_list takes DServer device into account
// - Util::get_device_by_name() takes DServer device into account
// - Util::get_device_list_by_class() takes DServer device into account
// - New parameter to the attribute::set_value() method to enable CORBA to free
// memory allocated for the attribute
//
// Revision 2.6  2002/10/17 07:43:07  taurel
// Fix bug in history stored by the polling thread :
// - We need one copy of the attribute data to build an history!!! It is true
// also for command which return data created by the DeviceImpl::create_xxx
// methods. Chnage in pollring.cpp/pollring.h/dserverpoll.cpp/pollobj.cpp
// and pollobj.h
//
// Revision 2.5  2002/10/15 11:27:20  taurel
// Fix bugs in device.cpp file :
// - Protect the state and status CORBA attribute with the device monitor
// Add the "TgLibVers" string as a #define in tango_config.h
//
// Revision 2.4  2002/08/12 15:06:55  taurel
// Several big fixes and changes
//   - Remove HP-UX specific code
//   - Fix bug in polling alogorithm which cause the thread to enter an infinite
//     loop (pollthread.cpp)
//   - For bug for Win32 device when trying to set attribute config
//     (attribute.cpp)
//
// Revision 2.3  2002/07/02 15:22:25  taurel
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
// Revision 2.1  2002/04/29 12:24:04  taurel
// Fix bug in attribute::set_value method and on the check against min and max value when writing attributes
//
// Revision 2.0  2002/04/09 14:45:11  taurel
// See Tango WEB pages for list of changes
//
//
// copyleft :           European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
//-=============================================================================

#include <algorithm>

#include <tango.h>
#include <pollcmds.h>

namespace Tango
{

//+-------------------------------------------------------------------------
//
// method : 		PolledDeviceCmd::PolledDeviceCmd 
// 
// description : 	constructors for Command class PolledDevice
//
//--------------------------------------------------------------------------

PolledDeviceCmd::PolledDeviceCmd(const char *name,
			         Tango::CmdArgType in,
			         Tango::CmdArgType out,
			         const char *out_desc):Command(name,in,out)
{
	set_out_type_desc(out_desc);
}


//+-------------------------------------------------------------------------
//
// method : 		PolledDeviceCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *PolledDeviceCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "PolledDevice::execute(): arrived " << endl;

//
// Call the device method and return to caller
//

	return insert((static_cast<DServer *>(device))->polled_device());
}

//+-------------------------------------------------------------------------
//
// method : 		DevPollStatusCmd::DevPollStatusCmd 
// 
// description : 	constructors for Command class DevPollStatus
//
//--------------------------------------------------------------------------

DevPollStatusCmd::DevPollStatusCmd(const char *name,
			           Tango::CmdArgType in,
			           Tango::CmdArgType out,
			           const char *in_desc,
				   const char *out_desc):Command(name,in,out)
{
	set_in_type_desc(in_desc);
	set_out_type_desc(out_desc);
}


//+-------------------------------------------------------------------------
//
// method : 		DevPollStatusCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *DevPollStatusCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "DevPollStatus::execute(): arrived " << endl;
	
//
// Extract the input string
//

	const char *tmp_name;
	if ((in_any >>= tmp_name) == false)
	{
		Except::throw_exception((const char *)"API_IncompatibleCmdArgumentType",
				        (const char *)"Imcompatible command argument type, expected type is : string",
				        (const char *)"DevPollStatusCmd::execute");
	}
	string d_name(tmp_name);
	cout4 << "Received string = " << d_name << endl;
	
//
// Call the device method and return to caller
//

	return insert((static_cast<DServer *>(device))->dev_poll_status(d_name));
}


//+-------------------------------------------------------------------------
//
// method : 		AddObjPollingCmd::AddObjPollingCmd 
// 
// description : 	constructors for Command class DevPollStatus
//
//--------------------------------------------------------------------------

AddObjPollingCmd::AddObjPollingCmd(const char *name,
			           Tango::CmdArgType in,
			           Tango::CmdArgType out,
			           string &in_desc):Command(name,in,out)
{
	set_in_type_desc(in_desc);
}


//+-------------------------------------------------------------------------
//
// method : 		AddObjPollingCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *AddObjPollingCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "AddObjPolling::execute(): arrived " << endl;
	
//
// Extract the input structure
//

	const DevVarLongStringArray *tmp_data;
	if ((in_any >>= tmp_data) == false)
	{
		Except::throw_exception((const char *)"API_IncompatibleCmdArgumentType",
				        (const char *)"Imcompatible command argument type, expected type is : DevVarLongStringArray",
				        (const char *)"AddObjPollingCmd::execute");
	}
	
//
// Call the device method and return to caller
//

	(static_cast<DServer *>(device))->add_obj_polling(tmp_data);
	
//
// Return to caller
//

	CORBA::Any *ret = return_empty_any("AddObjPolling");
	return ret;
}


//+-------------------------------------------------------------------------
//
// method : 		UpdObjPollingPeriodCmd::UpdObjPollingPeriodCmd 
// 
// description : 	constructors for Command class UpdObjPolledPeriod
//
//--------------------------------------------------------------------------

UpdObjPollingPeriodCmd::UpdObjPollingPeriodCmd(const char *name,
			           	       Tango::CmdArgType in,
			           	       Tango::CmdArgType out,
			           	       string &in_desc):Command(name,in,out)
{
	set_in_type_desc(in_desc);
}


//+-------------------------------------------------------------------------
//
// method : 		UpdObjPollingPeriodCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *UpdObjPollingPeriodCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "UpdObjPollingPeriod::execute(): arrived " << endl;
	
//
// Extract the input structure
//

	const DevVarLongStringArray *tmp_data;
	if ((in_any >>= tmp_data) == false)
	{
		Except::throw_exception((const char *)"API_IncompatibleCmdArgumentType",
				        (const char *)"Imcompatible command argument type, expected type is : DevVarLongStringArray",
				        (const char *)"UpdObjPollingPeriodCmd::execute");
	}
	
//
// Call the device method and return to caller
//

	(static_cast<DServer *>(device))->upd_obj_polling_period(tmp_data);
	
//
// Return to caller
//

	CORBA::Any *ret = return_empty_any("UpdObjPollingPeriod");
	return ret;
}


//+-------------------------------------------------------------------------
//
// method : 		RemObjPollingCmd::RemObjPollingCmd 
// 
// description : 	constructors for Command class RemObjPolled
//
//--------------------------------------------------------------------------

RemObjPollingCmd::RemObjPollingCmd(const char *name,
			           Tango::CmdArgType in,
			           Tango::CmdArgType out,
			           string &in_desc):Command(name,in,out)
{
	set_in_type_desc(in_desc);
}


//+-------------------------------------------------------------------------
//
// method : 		RemObjPollingCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *RemObjPollingCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "RemObjPolling::execute(): arrived " << endl;
	
//
// Extract the input structure
//

	const DevVarStringArray *tmp_data;
	if ((in_any >>= tmp_data) == false)
	{
		Except::throw_exception((const char *)"API_IncompatibleCmdArgumentType",
				        (const char *)"Imcompatible command argument type, expected type is : DevVarStringArray",
				        (const char *)"RemObjPollingCmd::execute");
	}
	
//
// Call the device method and return to caller
//

	(static_cast<DServer *>(device))->rem_obj_polling(tmp_data);
	
//
// Return to caller
//

	CORBA::Any *ret = return_empty_any("RemObjPolling");
	return ret;
}

//+-------------------------------------------------------------------------
//
// method : 		StartPollingCmd::StartPollingCmd 
// 
// description : 	constructors for Command class RemObjPolled
//
//--------------------------------------------------------------------------

StopPollingCmd::StopPollingCmd(const char *name,
			       Tango::CmdArgType in,
			       Tango::CmdArgType out):Command(name,in,out)
{
}


//+-------------------------------------------------------------------------
//
// method : 		StartPollingCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *StopPollingCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "StopPolling::execute(): arrived " << endl;
	
//
// Call the device method and return to caller
//

	(static_cast<DServer *>(device))->stop_polling();
	
//
// Return to caller
//

	CORBA::Any *ret = return_empty_any("StopPolling");
	return ret;
}

//+-------------------------------------------------------------------------
//
// method : 		StartPollingCmd::StartPollingCmd 
// 
// description : 	constructors for Command class RemObjPolled
//
//--------------------------------------------------------------------------

StartPollingCmd::StartPollingCmd(const char *name,
			         Tango::CmdArgType in,
			         Tango::CmdArgType out):Command(name,in,out)
{
}


//+-------------------------------------------------------------------------
//
// method : 		StartPollingCmd::execute 
// 
// description : 	Trigger the execution of the method really implemented
//			the command in the DServer class
//
//--------------------------------------------------------------------------

CORBA::Any *StartPollingCmd::execute(DeviceImpl *device, const CORBA::Any &in_any)
{

	cout4 << "StartPolling::execute(): arrived " << endl;
	
//
// Call the device method and return to caller
//

	(static_cast<DServer *>(device))->start_polling();
	
//
// Return to caller
//

	CORBA::Any *ret = return_empty_any("StartPolling");
	return ret;
}

} // End of Tango namespace