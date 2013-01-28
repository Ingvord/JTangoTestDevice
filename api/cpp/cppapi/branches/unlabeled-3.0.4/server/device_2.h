//=============================================================================	
//
// file :		Device.h
//
// description :	Include for the Device root classes. 
//			Three classes are declared in this file :
//				The Device class
//				The DeviceClass class
//
// project :		TANGO
//
// author(s) :		A.Gotz + E.Taurel
//
// $Revision$
//
// $Log$
// Revision 3.0  2003/03/25 16:41:53  taurel
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
// Revision 2.8  2002/12/16 12:06:22  taurel
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
// Revision 2.0  2002/04/09 14:45:09  taurel
// See Tango WEB pages for list of changes
//
// Revision 1.6  2001/10/08 09:03:12  taurel
// See tango WEB pages for list of changes
//
// Revision 1.5  2001/07/04 12:27:10  taurel
// New methods re_throw_exception(). Read_attributes supports AllAttr mnemonic A new add_attribute()method in DeviceImpl class New way to define attribute properties New pattern to prevent full re-compile For multi-classes DS, it is now possible to use the Util::get_device_by_name() method in device constructor Adding << operator ovebloading Fix devie CORBA ref. number when device constructor sends an excep.
//
// Revision 1.4  2001/05/04 09:28:13  taurel
// Fix bugs in DServer::restart() method and in Util::get_device_by_name() method
//
// Revision 1.3  2001/03/30 08:03:45  taurel
// Fix bugs in attributes. For linux, add signal_handler in its own thread, change the way to kill server. For all system, change DevRestart philosophy.
//
// Revision 1.2  2001/03/09 08:20:15  taurel
// Fix bug in the MultiClassAttribute::init_class_attribute() method. Also remove the DbErr_DeviceNotDefined define.
//
// Revision 1.1.1.1  2001/02/27 08:46:20  taurel
// Imported sources
//
// Revision 1.3  2000/04/13 10:40:40  taurel
// Added attribute support
//
// Revision 1.2  2000/02/04 11:00:14  taurel
// Just update revision number
//
// Revision 1.1.1.1  2000/02/04 10:58:28  taurel
// Imported sources
//
//
// copyleft :		European Synchrotron Radiation Facility
//			BP 220, Grenoble 38043
//			FRANCE
//
//=============================================================================

#ifndef _DEVICE_2_H
#define _DEVICE_2_H

#include <tango.h>

namespace Tango
{

class DeviceClass;

//=============================================================================
//
//			The Device_2Impl class
//
//
// description :	This class is derived directly from the Tango::Device_skel
//			class generated by CORBA. It represents the CORBA 
//			servant which will be accessed by the client. 
//			It implements all the methods
//			and attributes defined in the IDL interface for Device.
//
//=============================================================================

class Device_2ImplExt
{
};

/**
 * Base class for all TANGO device since version 2.
 *
 * This class inherits from DeviceImpl class which itself inherits from 
 * CORBA classes where all the network layer is implemented.
 * This class has been created since release 2 of Tango library where the IDL
 * Tango module has been modified in order to create a Device_2 interface
 * which inherits from the original Device interface
 *
 * $Author$
 * $Revision$
 */
 
class Device_2Impl : public virtual POA_Tango::Device_2,
		     public DeviceImpl
{
public:

/**@name Constructors
 * Miscellaneous constructors */
//@{
/**
 * Constructs a newly allocated Device_2Impl object from its name.
 * 
 * The device description field is set to <i>A Tango device</i>. The device
 * state is set to unknown and the device status is set to 
 * <b>Not Initialised</b>
 *
 * @param 	device_class	Pointer to the device class object
 * @param	dev_name	The device name
 *
 */
	Device_2Impl(DeviceClass *device_class,string &dev_name);

/**
 * Constructs a newly allocated Device_2Impl object from its name and its description.
 *
 * The device
 * state is set to unknown and the device status is set to 
 * <i>Not Initialised</i>
 *
 * @param 	device_class	Pointer to the device class object
 * @param	dev_name	The device name
 * @param	desc	The device description
 *
 */		
	Device_2Impl(DeviceClass *device_class,string &dev_name,string &desc);

/**
 * Constructs a newly allocated Device_2Impl object from all its creation 
 * parameters.
 *
 * The device is constructed from its name, its description, an original state
 * and status
 *
 * @param 	device_class	Pointer to the device class object
 * @param	dev_name	The device name
 * @param	desc 		The device description
 * @param	dev_state 	The device initial state
 * @param	dev_status	The device initial status
 *
 */		
	Device_2Impl(DeviceClass *device_class,
	           string &dev_name,string &desc,
	           Tango::DevState dev_state,string &dev_status); 

/**
 * Constructs a newly allocated Device_2Impl object from all its creation 
 * parameters with some default values.
 *
 * The device is constructed from its name, its description, an original state
 * and status. This constructor defined default values for the description,
 * state and status parameters. The default device description is <i>A TANGO device</i>.
 * The default device state is <i>UNKNOWN</i> and the default device status 
 * is <i>Not initialised</i>.
 *
 * @param 	device_class	Pointer to the device class object
 * @param	dev_name	The device name
 * @param	desc	The device desc
 * @param	dev_state 	The device initial state
 * @param	dev_status	The device initial status
 *
 */		   
	Device_2Impl(DeviceClass *device_class,
	           const char *dev_name,const char *desc = "A TANGO device",
	           Tango::DevState dev_state = Tango::UNKNOWN,
	           const char *dev_status = "Not Initialised");
//@}

/**@name Destructor
 * Only one desctructor is defined for this class */
//@{
/**
 * The device desctructor.
 */		   
	virtual ~Device_2Impl() {delete ext;}
//@}


/**@name CORBA operation methods
 * Method defined to implement TANGO device CORBA operation */
//@{
/**
 * Execute a command.
 *
 * It's the master method executed when a "command_inout_2" CORBA operation is
 * requested by a client. It updates the device black-box, call the 
 * TANGO command handler and returned the output Any
 *
 * @param in_cmd The command name
 * @param in_any The command input data packed in a CORBA Any
 * @param source The data source. This parameter is new in Tango release 2. It
 * allows a client to choose the data source between the device itself or the
 * data cache for polled command.
 * @return The command output data packed in a CORBA Any object
 * @exception DevFailed Re-throw of the exception thrown by the command_handler
 * method.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#DevFailed">here</a> to read
 * <b>DevFailed</b> exception specification
 */				
	virtual CORBA::Any *command_inout_2(const char *in_cmd,
					    const CORBA::Any &in_data,
					    Tango::DevSource source)
	throw (Tango::DevFailed, CORBA::SystemException);
	
/**
 * Get device command list.
 *
 * Invoked when the client request the command_list_query_2 CORBA operation.
 * It updates the device black box and returns an array of DevCmdInfo_2 object
 * with one object for each command.
 *
 * @return The device command list. One DevCmdInfo_2 is initialised for each
 * device command. Since Tango release 2, the command display level field has 
 * been added to this structure
 */
	virtual Tango::DevCmdInfoList_2 *command_list_query_2()
	throw (Tango::DevFailed, CORBA::SystemException);

/**
 * Get command info.
 *
 * Invoked when the client request the command_query_2 CORBA operation.
 * It updates the device black box and returns a DevCmdInfo_2 object for the 
 * command with name passed
 * to the method as parameter.
 *
 * @param command The command name
 * @return A DevCmdInfo_2 initialised for the wanted command.
 * @exception DevFailed Thrown if the command does not exist.
 * Since Tango release 2, the command display level field has 
 * been added to this structure.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#DevFailed">here</a> to read
 * <b>DevFailed</b> exception specification
 */	
	virtual Tango::DevCmdInfo_2 *command_query_2(const char *command)
	throw (Tango::DevFailed, CORBA::SystemException);
	
/**
 * Read attribute(s) value.
 *
 * Invoked when the client request the read_attributes_2 CORBA operation.
 * It returns to the client one AttributeValue structure for each wanted
 * attribute.
 *
 * @param names The attribute(s) name list
 * @param source The data source. This parameter is new in Tango release 2. It
 * allows a client to choose the data source between the device itself or the
 * data cache for polled attribute.
 * @return A sequence of AttributeValue structure. One structure is initialised
 * for each wanted attribute with the attribute value, the date and the attribute 
 * value quality. Click <a href="../../../tango_idl/idl_html/Tango.html#AttributeValue">here</a>
 * to read <b>AttributeValue</b> structure definition.
 * @exception DevFailed Thrown if the attribute does not exist.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#DevFailed">here</a> to read
 * <b>DevFailed</b> exception specification
 */
    	virtual Tango::AttributeValueList *read_attributes_2(const Tango::DevVarStringArray& names,
							     Tango::DevSource source)
	throw (Tango::DevFailed, CORBA::SystemException);

/**
 * Get attribute(s) configuration.
 *
 * Invoked when the client request the get_attribute_config_2 CORBA operation.
 * It returns to the client one AttributeConfig_2 structure for each wanted
 * attribute. All the attribute properties value are returned in this 
 * AttributeConfig_2 structure. Since Tango release 2, the attribute display
 * level field has been added to this structure.
 *
 * @param names The attribute(s) name list
 * @return A sequence of AttributeConfig_2 structure. One structure is initialised
 * for each wanted attribute. Click <a href="../../../tango_idl/idl_html/Tango.html#AttributeConfig">here</a>
 * to read <b>AttributeConfig_2</b> structure specification.
 *
 * @exception DevFailed Thrown if the attribute does not exist.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#DevFailed">here</a> to read
 * <b>DevFailed</b> exception specification
 */
    	virtual Tango::AttributeConfigList_2 *get_attribute_config_2(const Tango::DevVarStringArray& names)
        throw(Tango::DevFailed, CORBA::SystemException);


/**
 * Read attribute value history.
 *
 * Invoked when the client request the read_attribute_history_2 CORBA operation.
 * This operation allows a client to retrieve attribute value history for
 * polled attribute. The depth of the history is limited to the depth of
 * the device server internal polling buffer.
 * It returns to the client one DevAttrHistory structure for each record.
 *
 * @param name The attribute name
 * @param n The record number.
 * @return A sequence of DevAttrHistory structure. One structure is initialised
 * for each record with the attribute value, the date and in case of the attribute
 * returns an error when it was read, the DevErrors data.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#AttributeValue">here</a>
 * to read <b>DevAttrHistory</b> structure definition.
 * @exception DevFailed Thrown if the attribute does not exist or is not polled.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#DevFailed">here</a> to read
 * <b>DevFailed</b> exception specification
 */
	virtual Tango::DevAttrHistoryList *read_attribute_history_2(const char* name,
								  CORBA::Long n)
	throw(Tango::DevFailed, CORBA::SystemException);

/**
 * Read command value history.
 *
 * Invoked when the client request the command_inout_history_2 CORBA operation.
 * This operation allows a client to retrieve command return value history for
 * polled command. The depth of the history is limited to the depth of
 * the device server internal polling buffer.
 * It returns to the client one DevCmdHistory structure for each record.
 *
 * @param command The command name
 * @param n The record number.
 * @return A sequence of DevCmdHistory structure. One structure is initialised
 * for each record with the command return value (in an Any), the date
 * and in case of the command returns an error when it was read, the 
 * DevErrors data.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#AttributeValue">here</a>
 * to read <b>DevCmdHistory</b> structure definition.
 * @exception DevFailed Thrown if the attribute does not exist or is not polled.
 * Click <a href="../../../tango_idl/idl_html/Tango.html#DevFailed">here</a> to read
 * <b>DevFailed</b> exception specification
 */
 	
	virtual Tango::DevCmdHistoryList *command_inout_history_2(const char* command,
								CORBA::Long n)
	throw(Tango::DevFailed, CORBA::SystemException);
//@}
	
private:
	Device_2ImplExt				*ext;
};

} // End of Tango namespace

#endif // _DEVICE_H