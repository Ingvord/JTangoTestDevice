static const char *RcsId = "$Header$";

//+============================================================================
//
// file :               BlackBox.cpp
//
// description :        C++ source code for the BlackBoxElt and BlackBox
//			classes. These classes are used to implement the 
//			tango device server black box. There is one
//			black box for each Tango device. This black box
//			keeps info. on all the activities on a device.
//			A client is able to retrieve these data via a Device
//			attribute
//
// project :            TANGO
//
// author(s) :          A.Gotz + E.Taurel
//
// $Revision$
//
// $Log$
// Revision 2.7  2002/12/16 10:15:35  taurel
// - New method get_device_list() in Util class
// - Util::get_class_list takes DServer device into account
// - Util::get_device_by_name() takes DServer device into account
// - Util::get_device_list_by_class() takes DServer device into account
// - New parameter to the attribute::set_value() method to enable CORBA to free
// memory allocated for the attribute
//
// Revision 2.6  2002/10/17 07:43:05  taurel
// Fix bug in history stored by the polling thread :
// - We need one copy of the attribute data to build an history!!! It is true
// also for command which return data created by the DeviceImpl::create_xxx
// methods. Chnage in pollring.cpp/pollring.h/dserverpoll.cpp/pollobj.cpp
// and pollobj.h
//
// Revision 2.5  2002/10/15 11:27:18  taurel
// Fix bugs in device.cpp file :
// - Protect the state and status CORBA attribute with the device monitor
// Add the "TgLibVers" string as a #define in tango_config.h
//
// Revision 2.4  2002/08/12 15:06:53  taurel
// Several big fixes and changes
//   - Remove HP-UX specific code
//   - Fix bug in polling alogorithm which cause the thread to enter an infinite
//     loop (pollthread.cpp)
//   - For bug for Win32 device when trying to set attribute config
//     (attribute.cpp)
//
// Revision 2.3  2002/07/02 15:22:23  taurel
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
// Revision 2.2  2002/04/30 10:50:40  taurel
// Don't check alarm on attribute if attribute quality factor is INVALID
//
// Revision 2.1  2002/04/29 12:24:02  taurel
// Fix bug in attribute::set_value method and on the check against min and max value when writing attributes
//
// Revision 2.0  2002/04/09 14:45:08  taurel
// See Tango WEB pages for list of changes
//
// Revision 1.6  2001/10/08 09:03:10  taurel
// See tango WEB pages for list of changes
//
// Revision 1.5  2001/07/04 12:27:09  taurel
// New methods re_throw_exception(). Read_attributes supports AllAttr mnemonic A new add_attribute()method in DeviceImpl class New way to define attribute properties New pattern to prevent full re-compile For multi-classes DS, it is now possible to use the Util::get_device_by_name() method in device constructor Adding << operator ovebloading Fix devie CORBA ref. number when device constructor sends an excep.
//
// Revision 1.4  2001/05/04 09:28:12  taurel
// Fix bugs in DServer::restart() method and in Util::get_device_by_name() method
//
// Revision 1.3  2001/03/30 08:03:44  taurel
// Fix bugs in attributes. For linux, add signal_handler in its own thread, change the way to kill server. For all system, change DevRestart philosophy.
//
// Revision 1.2  2001/03/09 08:20:14  taurel
// Fix bug in the MultiClassAttribute::init_class_attribute() method. Also remove the DbErr_DeviceNotDefined define.
//
// Revision 1.1.1.1  2001/02/27 08:46:20  taurel
// Imported sources
//
// Revision 1.3  2000/04/13 10:40:39  taurel
// Added attribute support
//
// Revision 1.2  2000/02/04 11:00:13  taurel
// Just update revision number
//
// Revision 1.1.1.1  2000/02/04 10:58:27  taurel
// Imported sources
//
//
// copyleft :           European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
//-============================================================================

#include <tango.h>
#include <blackbox.h>

#include <stdio.h>

#ifdef WIN32
#include <sys/types.h>
#include <sys/timeb.h>
#else
#include <sys/time.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#endif /* WIN32 */

namespace Tango
{

//+-------------------------------------------------------------------------
//
// method : 		BlackBoxElt::BlackBoxElt 
// 
// description : 	Constructor for the BlackBoxElt class.
//			This constructor simply set the internal value to their
//			default
//
//--------------------------------------------------------------------------

BlackBoxElt::BlackBoxElt()
{
	req_type = Req_Unknown;
	attr_type = Attr_Unknown;
	op_type = Op_Unknown;
	when.tv_sec = when.tv_usec = 0;
	host = "Unknown";	
//	host_ip = NULL;
}

BlackBoxElt::~BlackBoxElt()
{
//	if (host_ip != NULL)
//		OCI::IIOP::InetAddr_free(host_ip);
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::BlackBox 
// 
// description : 	Two constructors for the BlackBox class. The first one
//			does not take any argument and construct a black box
//			with the default depth.
//			The second one create a black box with a depth defined 
//			by the argument.
//
// argument : in : 	- max_size : The black box depth
//
//--------------------------------------------------------------------------

BlackBox::BlackBox():box(DefaultBlackBoxDepth)
{
	insert_elt = 0;
	nb_elt = 0;
	max_elt = DefaultBlackBoxDepth;	
	
	Tango::Util *tg = Tango::Util::instance();
	orb = tg->get_orb();	
}

BlackBox::BlackBox(long max_size):box(max_size)
{
	insert_elt = 0;
	nb_elt = 0;
	max_elt = max_size;		

	Tango::Util *tg = Tango::Util::instance();
	orb = tg->get_orb();
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::insert_attr
// 
// description : 	This method insert a new element in the black box when
//			this element is a attribute
//
// argument : in : 	- attr : The attribute type
//
//--------------------------------------------------------------------------


void BlackBox::insert_attr(BlackBoxElt_AttrType attr)
{
	
//
// Insert elt in the box
//

	box[insert_elt].req_type = Req_Attribute;
	box[insert_elt].attr_type = attr;
	box[insert_elt].op_type = Op_Unknown;

#ifdef WIN32
//
// Note that the exact conversion between milli-sec and u-sec will be done
// only when data is send back to user. This save some times in unnecessary
// computation
//
	struct _timeb t;
	_ftime(&t);
	
	box[insert_elt].when.tv_usec = (long)t.millitm;
	box[insert_elt].when.tv_sec = (unsigned long)t.time;
#else	
	struct timezone tz;
	gettimeofday(&box[insert_elt].when,&tz);
#endif

//
// get client address
//

	get_client_host();
			
//
// manage insert and read indexes
//

	inc_indexes();
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::insert_attr
// 
// description : 	This method insert a new element in the black box when
//			this element is a attribute
//
// argument : in : 	- attr : The attribute type
//			- host : The client host
//
//--------------------------------------------------------------------------


void BlackBox::insert_attr(BlackBoxElt_AttrType attr,const char *host)
{

//
// Insert elt in the box
//

	box[insert_elt].req_type = Req_Attribute;
	box[insert_elt].attr_type = attr;
	box[insert_elt].op_type = Op_Unknown;
	box[insert_elt].host = host;

#ifdef WIN32
//
// Note that the exact conversion between milli-sec and u-sec will be done
// only when data is send back to user. This save some times in unnecessary
// computation
//
	struct _timeb t;
	_ftime(&t);
	
	box[insert_elt].when.tv_usec = (long)t.millitm;
	box[insert_elt].when.tv_sec = (unsigned long)t.time;
#else	
	struct timezone tz;
	gettimeofday(&box[insert_elt].when,&tz);
#endif
			
//
// manage insert and read indexes
//

	inc_indexes();
}


//+-------------------------------------------------------------------------
//
// method : 		BlackBox::insert_cmd
// 
// description : 	This method insert a new element in the black box when
//			this element is a call to the operation command_inout
//
// argument : in : 	- c_name : The command name
//
//--------------------------------------------------------------------------


void BlackBox::insert_cmd(const char *cmd,long vers)
{

//
// Insert elt in the box
//

	box[insert_elt].req_type = Req_Operation;
	box[insert_elt].attr_type = Attr_Unknown;
	if (vers == 1)
		box[insert_elt].op_type = Op_Command_inout;
	else
		box[insert_elt].op_type = Op_Command_inout_2;
	box[insert_elt].cmd_name = cmd;
#ifdef WIN32
//
// Note that the exact conversion between milli-sec and u-sec will be done
// only when data is send back to user. This save some times in unnecessary
// computation
//
	struct _timeb t;
	_ftime(&t);
	
	box[insert_elt].when.tv_usec = (long)t.millitm;
	box[insert_elt].when.tv_sec = (unsigned long)t.time;
#else	
	struct timezone tz;
	gettimeofday(&box[insert_elt].when,&tz);
#endif

//
// get client address
//

	get_client_host();
		
//
// manage insert and read indexes
//

	inc_indexes();
}


//+-------------------------------------------------------------------------
//
// method : 		BlackBox::insert_cmd
// 
// description : 	This method insert a new element in the black box when
//			this element is a call to the operation command_inout
//
// argument : in : 	- c_name : The command name
//			- host : The client host
//
//--------------------------------------------------------------------------


void BlackBox::insert_cmd(const char *cmd,const char *host,long vers)
{

//
// Insert elt in the box
//

	box[insert_elt].req_type = Req_Operation;
	box[insert_elt].attr_type = Attr_Unknown;
	if (vers == 1)
		box[insert_elt].op_type = Op_Command_inout;
	else
		box[insert_elt].op_type = Op_Command_inout_2;	
	box[insert_elt].cmd_name = cmd;
	box[insert_elt].host = host;
#ifdef WIN32
//
// Note that the exact conversion between milli-sec and u-sec will be done
// only when data is send back to user. This save some times in unnecessary
// computation
//
	struct _timeb t;
	_ftime(&t);
	
	box[insert_elt].when.tv_usec = (long)t.millitm;
	box[insert_elt].when.tv_sec = (unsigned long)t.time;
#else	
	struct timezone tz;
	gettimeofday(&box[insert_elt].when,&tz);
#endif
	
//
// manage insert and read indexes
//

	inc_indexes();
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::insert_op
// 
// description : 	This method insert a new element in the black box when
//			this element is a call to an operation which is not
//			the command_inout operation
//
// argument : in : 	- cmd : The operation type
//
//--------------------------------------------------------------------------


void BlackBox::insert_op(BlackBoxElt_OpType op)
{

//
// Insert elt in the box
//

	box[insert_elt].req_type = Req_Operation;
	box[insert_elt].attr_type = Attr_Unknown;
	box[insert_elt].op_type = op;
#ifdef WIN32
//
// Note that the exact conversion between milli-sec and u-sec will be done
// only when data is send back to user. This save some times in unnecessary
// computation
//
	struct _timeb t;
	_ftime(&t);
	
	box[insert_elt].when.tv_usec = (long)t.millitm;
	box[insert_elt].when.tv_sec = (unsigned long)t.time;
#else	
	struct timezone tz;
	gettimeofday(&box[insert_elt].when,&tz);
#endif

//
// get client address
//

	get_client_host();
		
//
// manage insert and read indexes
//

	inc_indexes();
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::insert_op
// 
// description : 	This method insert a new element in the black box when
//			this element is a call to an operation which is not
//			the command_inout operation
//
// argument : in : 	- cmd : The operation type
//			- host : The client host
//
//--------------------------------------------------------------------------


void BlackBox::insert_op(BlackBoxElt_OpType op,const char *host)
{

//
// Insert elt in the box
//

	box[insert_elt].req_type = Req_Operation;
	box[insert_elt].attr_type = Attr_Unknown;
	box[insert_elt].op_type = op;
	box[insert_elt].host = host;
#ifdef WIN32
//
// Note that the exact conversion between milli-sec and u-sec will be done
// only when data is send back to user. This save some times in unnecessary
// computation
//
	struct _timeb t;
	_ftime(&t);
	
	box[insert_elt].when.tv_usec = (long)t.millitm;
	box[insert_elt].when.tv_sec = (unsigned long)t.time;
#else	
	struct timezone tz;
	gettimeofday(&box[insert_elt].when,&tz);
#endif
	
//
// manage insert and read indexes
//

	inc_indexes();
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::inc_indexes
// 
// description : 	This private method increment the indexes used to acces
//			the box itself. This is necessary because the box must
//			be managed as a circular buffer
//
//--------------------------------------------------------------------------


void BlackBox::inc_indexes()
{
	insert_elt++;
	if (insert_elt == max_elt)
		insert_elt = 0;
		
	if (nb_elt != max_elt)
		nb_elt++;
}

//+-------------------------------------------------------------------------
//
// method : 		get_client_host
// 
// description : 	This private method retrieves the client host IP
//			address (the number). IT USES ORBACUS SPECIFIC
//			CLASSES
//
//--------------------------------------------------------------------------

void BlackBox::get_client_host()
{
/*	OCI::Current_var current;
	try
	{
		CORBA::Object_var baseCurrent = orb->resolve_initial_references("OCICurrent");
		current = OCI::Current::_narrow(baseCurrent);
	}
	catch(...)
	{
		current = NULL;
	}
	
	if (current != (OCI::Current_var)NULL)
	{
		
		OCI::TransportInfo_var info = current->get_oci_transport_info();
		OCI::IIOP::TransportInfo_var iiopInfo = OCI::IIOP::TransportInfo::_narrow(info);
		
		if (!CORBA::is_nil(iiopInfo))
		{
			if (box[insert_elt].host_ip != NULL)
				OCI::IIOP::InetAddr_free(box[insert_elt].host_ip);

			box[insert_elt].host_ip = iiopInfo->remote_addr();
			box[insert_elt].host.erase();
			box[insert_elt].host = "Defined";
		}
		else
		{
			box[insert_elt].host.erase();
			box[insert_elt].host = "Unknown";
		}
	}*/
}
	
//+-------------------------------------------------------------------------
//
// method : 		BlackBox::build_info_as_str
// 
// description : 	Translate all the info stored in a black box element
//			into a readable string.
//
// argument : in : 	- index : The black box element index
//
//--------------------------------------------------------------------------

void BlackBox::build_info_as_str(long index)
{
	char date_str[25];
//
// Convert time to a string
//

	date_ux_to_str(box[index].when,date_str);
	elt_str = date_str; 
	
//
// Add request type and command name in case of
//

	elt_str = elt_str + " : ";
	if (box[index].req_type == Req_Operation)
	{
		elt_str = elt_str + "Operation ";
		switch (box[index].op_type)
		{
		case Op_Command_inout :
			elt_str = elt_str + "command_inout (cmd = " + box[index].cmd_name + ") ";
			break;
			
		case Op_Ping :
			elt_str = elt_str + "ping ";
			break;
			
		case Op_Info :
			elt_str = elt_str + "info ";
			break;

		case Op_BlackBox :
			elt_str = elt_str + "blackbox ";
			break;

		case Op_Command_list :
			elt_str = elt_str + "command_list_query ";
			break;
		
		case Op_Command :
			elt_str = elt_str + "command_query ";
			break;
										
		case Op_Get_Attr_Config :
			elt_str = elt_str + "get_attribute_config ";
			break;
			
		case Op_Set_Attr_Config :
			elt_str = elt_str + "set_attribute_config ";
			break;
			
		case Op_Read_Attr :
			elt_str = elt_str + "read_attributes ";
			break;
			
		case Op_Write_Attr :
			elt_str = elt_str + "write_attributes ";
			break;
			
		case Op_Command_inout_2 :
			elt_str = elt_str + "command_inout_2 (cmd = " + box[index].cmd_name + ") ";
			break;
			
		case Op_Command_list_2 :
			elt_str = elt_str + "command_list_query_2 ";
			break;
			
		case Op_Command_2 :
			elt_str = elt_str + "command_query_2 ";
			break;
			
		case Op_Get_Attr_Config_2 :
			elt_str = elt_str + "get_attribute_config_2 ";
			break;
			
		case Op_Read_Attr_2 :
			elt_str = elt_str + "read_attributes_2 ";
			break;
			
		case Op_Command_inout_history_2 :
			elt_str = elt_str + "command_inout_history_2 ";
			break;
			
		case Op_Read_Attr_history_2 :
			elt_str = elt_str + "read_attribute_history_2";
			break;
			
		case Op_Unknown :
			elt_str = elt_str + "unknown operation !!!!!";
			return;
		}
	}	
	else if (box[index].req_type == Req_Attribute)
	{
		elt_str = elt_str + "Attribute ";
		switch (box[index].attr_type)
		{
		case Attr_Name :
			elt_str = elt_str + "name ";
			break;
			
		case Attr_Description :
			elt_str = elt_str + "description ";
			break;

		case Attr_Status :
			elt_str = elt_str + "status ";
			break;
			
		case Attr_State :
			elt_str = elt_str + "state ";
			break;
			
		case Attr_AdmName :
			elt_str = elt_str + "adm_name ";
			break;
						
		case Attr_Unknown :
			elt_str = elt_str + "unknown attribute !!!!!";
			return;
		}
	}
	else
	{
		elt_str = elt_str + "Unknown CORBA request type !!!!!";
		return;
	}

//
// Add client host if defined
//

/*	if (box[index].host != "Unknown")
	{	
        	struct hostent *ho;
#ifdef WIN32
		struct in_addr ad;
		ad.s_addr = (box[index].host_ip[0] << 24) + \
			    (box[index].host_ip[1] << 16) + \
			    (box[index].host_ip[2] << 8) + \
			     box[index].host_ip[3];
		unsigned long ip_net = htonl(ad.s_addr);
		ho = gethostbyaddr((char *)(&ip_net),sizeof(ip_net),AF_INET);
#else
		unsigned long ip = (box[index].host_ip[0] << 24) + \
				   (box[index].host_ip[1] << 16) + \
				   (box[index].host_ip[2] << 8) + \
				    box[index].host_ip[3];
#ifdef __linux
		unsigned long ip_net = htonl(ip);
		ho = gethostbyaddr((char *)(&ip_net),sizeof(ip),AF_INET);
#else
		ho = gethostbyaddr((char *)(&ip),sizeof(ip),AF_INET);
#endif /* __linux */
/*#endif /* WIN32 */
/*		if (ho == NULL)
		{
			return;
		}
		else
		{
			elt_str = elt_str + "requested from ";
			elt_str = elt_str + ho->h_name;
		}
	}*/	
		
	return;
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::read
// 
// description : 	Read black box element as strings. The newest element
//			is return in the first position
//
// argument : in : 	- index : The number of element to read
//
//--------------------------------------------------------------------------

Tango::DevVarStringArray *BlackBox::read(long wanted_elt)
{

//
// Throw exeception if the wanted element is stupid and if there is no element
// stored in the black box
//

	if (wanted_elt <= 0)
	{
		Except::throw_exception((const char *)"API_BlackBoxArgument",
				      (const char *)"Argument to read black box out of range",
				      (const char *)"BlackBox::read");
	}
	if (nb_elt == 0)
	{
		Except::throw_exception((const char *)"API_BlackBoxEmpty",
				      (const char *)"Nothing stored yet in black-box",
				      (const char *)"BlackBox::read");
	}
			
//
// Limit wanted element to a reasonable value
//

	if (wanted_elt > max_elt)
		wanted_elt = max_elt;
		
	if (wanted_elt > nb_elt)
		wanted_elt = nb_elt;
		
//
// Read black box elements
//

	Tango::DevVarStringArray *ret;
	try
	{
	
		ret = new Tango::DevVarStringArray(wanted_elt);
                ret->length(wanted_elt);

		long read_index;
		if (insert_elt == 0)
			read_index = max_elt - 1;
		else
			read_index = insert_elt - 1;
		for (long i = 0;i < wanted_elt;i++)
		{
			build_info_as_str(read_index);
			(*ret)[i] = elt_str.c_str();
		
			read_index--;
			if (read_index < 0)
				read_index = max_elt - 1;
		}
	}
	catch (bad_alloc)
	{
		Except::throw_exception((const char *)"API_MemoryAllocation",
				      (const char *)"Can't allocate memory in server",
				      (const char *)"BlackBox::read");
	}		
	return(ret);
}

//+-------------------------------------------------------------------------
//
// method : 		BlackBox::date_ux_to_str
// 
// description : 	Convert a UNIX date (number of seconds since EPOCH)
//			to a string of the following format :
//			dd/mm/yyyy hh24:mi:ss:xx
//
// argument : in : 	- ux_date : The UNIX date in a timeval structure
//			- str_date : Pointer to char array where the date will
//				     be stored (must be allocated)
//
//--------------------------------------------------------------------------

void BlackBox::date_ux_to_str(timeval &ux_date,char *str_date)
{
	long i;
	char month[5];
	char *ux_str;
	char unix_date[30];

/* Convert UNIX date to a string in UNIX format */

	ux_str = ctime((time_t *)&(ux_date.tv_sec));
	strcpy(unix_date,ux_str);

/* Copy day */

	for (i = 0;i < 2;i++)
		str_date[i] = unix_date[i + 8];
	str_date[2] = '/';
	str_date[3] = '\0';
	if (str_date[0] == ' ')
		str_date[0] = '0';

/* Copy month */

	for (i = 0;i < 3;i++)
		month[i] = unix_date[i + 4];
	month[3] = '\0';

	switch(month[0])
	{
		case 'J' : if (month[1] == 'u')
			   {
				if (month[2] == 'n')
					strcat(str_date,"06/");
				else
					strcat(str_date,"07/");
			   }
			   else
				strcat(str_date,"01/");
			   break;

		case 'F' : strcat(str_date,"02/");
			   break;

		case 'M' : if (month[2] == 'r')
				strcat(str_date,"03/");
			   else
				strcat(str_date,"05/");
			   break;

		case 'A' : if (month[1] == 'p')
				strcat(str_date,"04/");
			   else
				strcat(str_date,"08/");
			   break;

		case 'S' : strcat(str_date,"09/");
			   break;

		case 'O' : strcat(str_date,"10/");
		           break;

		case 'N' : strcat(str_date,"11/");
			   break;

		case 'D' : strcat(str_date,"12/");
			   break;
	}

	str_date[6] = '\0';

/* Copy year */

	strcat(str_date,&(unix_date[20]));
	str_date[10] = '\0';
	
/* Copy date remaining */

	strcat(str_date," ");
	for (i = 0;i < 8;i++)
		str_date[i + 11] = unix_date[i + 11];
	str_date[19] = '\0';
	
/* Add milliseconds */

#ifdef WIN32
	sprintf(&(str_date[19]),":%.2d",(int)(ux_date.tv_usec/10));
#else
	sprintf(&(str_date[19]),":%.2d",(int)(ux_date.tv_usec/10000));
#endif

}

} // End of Tango namespace