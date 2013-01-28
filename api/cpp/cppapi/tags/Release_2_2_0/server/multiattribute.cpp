static const char *RcsId = "$Header$";

//+============================================================================
//
// file :               MultiAttribute.cpp
//
// description :        C++ source code for the MultiAttribute class. This class
//			is used to manage attribute.
//			A Tango Device object instance has one
//			MultiAttribute object which is an aggregate of
//			Attribute or WAttribute objects
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
// Revision 2.4  2002/08/12 15:06:54  taurel
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
// Revision 2.0  2002/04/09 14:45:10  taurel
// See Tango WEB pages for list of changes
//
// Revision 1.6  2001/10/08 09:03:13  taurel
// See tango WEB pages for list of changes
//
// Revision 1.5  2001/07/04 12:27:11  taurel
// New methods re_throw_exception(). Read_attributes supports AllAttr mnemonic A new add_attribute()method in DeviceImpl class New way to define attribute properties New pattern to prevent full re-compile For multi-classes DS, it is now possible to use the Util::get_device_by_name() method in device constructor Adding << operator ovebloading Fix devie CORBA ref. number when device constructor sends an excep.
//
// Revision 1.4  2001/05/04 09:28:14  taurel
// Fix bugs in DServer::restart() method and in Util::get_device_by_name() method
//
// Revision 1.3  2001/03/30 08:03:45  taurel
// Fix bugs in attributes. For linux, add signal_handler in its own thread, change the way to kill server. For all system, change DevRestart philosophy.
//
// Revision 1.2  2001/03/09 08:20:16  taurel
// Fix bug in the MultiClassAttribute::init_class_attribute() method. Also remove the DbErr_DeviceNotDefined define.
//
// Revision 1.1.1.1  2001/02/27 08:46:21  taurel
// Imported sources
//
// Revision 1.3  2000/04/13 10:40:41  taurel
// Added attribute support
//
//
// copyleft :           European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
//-============================================================================

#include <tango.h>
#include <multiattribute.h>
#include <classattribute.h>

#include <functional>
#include <algorithm>

namespace Tango
{

//
// Define the optional attribute name and default value
//

static OptAttrProp Tango_OptAttrProp[] = {
	{"label",LabelNotSpec},
	{"description",DescNotSpec},
	{"unit",UnitNotSpec},
	{"standard_unit",StdUnitNotSpec},
	{"display_unit",DispUnitNotSpec},
	{"format",FormatNotSpec},
	{"min_value",AlrmValueNotSpec},
	{"max_value",AlrmValueNotSpec},
	{"min_alarm",AlrmValueNotSpec},
	{"max_alarm",AlrmValueNotSpec},
	{"writable_attr_name",AssocWritNotSpec}
};

//
// A classical function which will be used as unary predicate for the find_if
// algo. It must be used with the bind2nd adapter to be transform as unary and
// with the ptr_fun adapter to be transform as a function object
//

static bool WantedProp_f(AttrProperty a,const char *n)
{
	return (a.get_name() == n);
}



//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::MultiAttribute
// 
// description : 	constructor for the MultiAttribute class from the 
//			device device name and a pointer to the DeviceClass
//			object
//
// argument : in : 	- dev_name : The device name
//			- dev_class_ptr : Pointer to the DeviceClass object
//
//--------------------------------------------------------------------------

MultiAttribute::MultiAttribute(string &dev_name,DeviceClass *dev_class_ptr)
:ext(NULL)
{
	long i;
	cout4 << "Entering MultiAttribute class constructor for device " << dev_name << endl;
	
//
// Retrieve attr name list
//

	vector<Attr *> &tmp_attr_list = dev_class_ptr->get_class_attr()->get_attr_list();
	long nb_attr = tmp_attr_list.size();
	
//
// Get device attribute properties
// No need to implement
// a retry here (in case of db server restart) because the db reconnection
// is forced by the get_property call executed during xxxClass construction
// before we reach this code.
//

	if (nb_attr != 0)
	{
		Tango::Util *tg = Tango::Util::instance();
		vector<string> db_dev_attr_prop;
		if (tg->_UseDb == true)
		{
			CORBA::Any send;
			Tango::DevVarStringArray *send_list = new Tango::DevVarStringArray(nb_attr + 1);
			send_list->length(nb_attr + 1);
	
			(*send_list)[0] = CORBA::string_dup(dev_name.c_str());
			for (i = 0;i < nb_attr;i++)
				(*send_list)[i + 1] = CORBA::string_dup(tmp_attr_list[i]->get_name().c_str());
			
			send <<= send_list;
			CORBA::Any_var received_prop = tg->get_database()->get_dbase()->command_inout("DbGetDeviceAttributeProperty",send);
			const Tango::DevVarStringArray *db_attr_prop_list;
			if ((received_prop >>= db_attr_prop_list) == false)
			{
				TangoSys_OMemStream o;
				o << "Can't get device attribute properties for device " << dev_name << ends;
					
				Except::throw_exception((const char *)"API_DatabaseAccess",
				                	o.str(),
				                	(const char *)"MultiAttribute::MultiAttribute");
			}
		
//
// Create a vector of strings with the values returned from db
//

			long nb_db_data = db_attr_prop_list->length();
			nb_db_data = nb_db_data - 2;
			for (i = 0;i < nb_db_data;i++)
				db_dev_attr_prop.push_back(string((*db_attr_prop_list)[i + 2]));
		}
						
//
// Build property list for each attribute
//

		TangoSys_MemStream o;
		long ind = 0;
	
		for (i = 0;i < nb_attr;i++)
		{
	
//
// Get attribute class properties
//

			Attr &attr = dev_class_ptr->get_class_attr()->get_attr(tmp_attr_list[i]->get_name());
			vector<AttrProperty> &class_prop = attr.get_class_properties();
			vector<AttrProperty> &def_user_prop = attr.get_user_default_properties();

//
// If the attribute has some properties defined at device level, build a vector
// of these properties
//
		
			vector<AttrProperty> dev_prop;

			if (tg->_UseDb == true)
			{
				ind++;
				long nb_prop;
				o << db_dev_attr_prop[ind] << ends;
				o >> nb_prop;
				ind++;

				o.seekp(0);
				o.seekg(0);

				for (long j = 0;j < nb_prop;j++)
				{
					dev_prop.push_back(AttrProperty(db_dev_attr_prop[ind],
									db_dev_attr_prop[ind + 1]));
					ind = ind + 2;
				}
			}

//
// Concatenate these two attribute properties levels
//
	
			vector<AttrProperty> prop_list;
			concat(dev_prop,class_prop,prop_list);
			add_user_default(prop_list,def_user_prop);
			add_default(prop_list);
		
//
// Check and get attribute data type
//

			long data_type = attr.get_type();
		
//
// Create an Attribute instance
//

			if ((attr.get_writable() == Tango::WRITE) ||
			    (attr.get_writable() == Tango::READ_WRITE))
				attr_list.push_back(new WAttribute(prop_list,attr,dev_name));
			else
				attr_list.push_back(new Attribute(prop_list,attr,dev_name));
		
//
// If it is writable, add it to the writable attribute list
//

			Tango::AttrWriteType w_type = attr_list[i]->get_writable();
			if ((w_type == Tango::WRITE) ||
			    (w_type == Tango::READ_WRITE))
			{
				writable_attr_list.push_back(i);
			}
		
//
// If one of the alarm properties is defined, add it to the alarmed attribute
// list
//

			if (attr_list[i]->is_alarmed() == true)
			{
				if (w_type != Tango::WRITE)
					alarm_attr_list.push_back(i);
			}
			
			cout4 << *(attr_list[i]) << endl;
		}
	}
	
//
// For each attribute, check if the writable_attr_name is set and in this
// case, check if the associated attribute exists and is writable
//

	for (i = 0;i < nb_attr;i++)
	{
		check_associated(i,dev_name);
	}
	
	cout4 << "Leaving MultiAttribute class constructor" << endl;
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::~MultiAttribute
// 
// description : 	destructor for the MultiAttribute class. It simply 
//			delete all the Attribute object stored in its
//			attr_list data member
//
//--------------------------------------------------------------------------

MultiAttribute::~MultiAttribute()
{
	for(long i = 0;i < attr_list.size();i++)
		delete attr_list[i];
		
	delete ext;
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::concat
// 
// description : 	Concatenate porperties defined at the class level and
//			at the device level. Prperties defined at the device
//			level have the highest priority
//
// in :			dev_prop : The device properties
//			class_prop : The class properties
//
// out :		result : The resulting vector
//
//--------------------------------------------------------------------------


void MultiAttribute::concat(vector<AttrProperty> &dev_prop,
			    vector<AttrProperty> &class_prop,
			    vector<AttrProperty> &result)
{
	
//
// Copy all device properties
//

	long i;
	for (i = 0;i < dev_prop.size();i++)
		result.push_back(dev_prop[i]);
		
//
// Add class properties if they have not been redefined at the device level
//

	vector<AttrProperty> tmp_result = result;
	long nb_class_check = class_prop.size();
	for (i = 0;i < nb_class_check;i++)
	{
		vector<AttrProperty>::iterator pos;

		pos = find_if(tmp_result.begin(),tmp_result.end(),
			      bind2nd(WantedProp<AttrProperty,string,bool>(),class_prop[i].get_name()));
		
		if (pos != tmp_result.end())
			continue;
		else
			result.push_back(class_prop[i]);
	}
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::add_default
// 
// description : 	Add default value for optional property if they
//			are not defined
//
// in :			prop_list : The already defined property vector
//
//--------------------------------------------------------------------------

void MultiAttribute::add_default(vector<AttrProperty> &prop_list)
{
	long nb_opt_prop = sizeof(Tango_OptAttrProp)/sizeof(OptAttrProp);

//
// For all the optional attribute properties, search in the already built
// vector of attributes if they are defined. If yes, continue. Otherwise,
// add a new property with the default value
//
	
	for (long i = 0;i < nb_opt_prop;i++)
	{
		vector<AttrProperty>::iterator pos;
		string opt_prop_name(Tango_OptAttrProp[i].name);
		
		pos = find_if(prop_list.begin(),prop_list.end(),
			      bind2nd(WantedProp<AttrProperty,string,bool>(),opt_prop_name));
			      
		if (pos == prop_list.end())
			prop_list.push_back(AttrProperty(Tango_OptAttrProp[i].name,Tango_OptAttrProp[i].default_value));
	}
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::add_user_default
// 
// description : 	Add default value for optional property if they
//			are not defined
//
// in :			prop_list : The already defined property vector
//			user_default : The user defined default property values
//
//--------------------------------------------------------------------------

void MultiAttribute::add_user_default(vector<AttrProperty> &prop_list,
				      vector<AttrProperty> &user_default)
{

//
// Add default user properties if they have not been defined in the database
//

	long nb_user = user_default.size();
	for (int i = 0;i < nb_user;i++)
	{
		vector<AttrProperty>::iterator pos;

		pos = find_if(prop_list.begin(),prop_list.end(),
			      bind2nd(WantedProp<AttrProperty,string,bool>(),user_default[i].get_name()));
		
		if (pos != prop_list.end())
			continue;
		else
			prop_list.push_back(user_default[i]);
	}
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::check_associated
// 
// description :	Check if the writable_attr_name property is set and
//			in this case, check if the associated attribute exists
//			and is writable. This is necessary only for attribute
//			of the READ_WITH_WRITE or READ_WRITE types 
//
// argument : in : 	- index : The index of the attribute to checked in the
//				  attr vector
//			- dev_name : The device name
//
//--------------------------------------------------------------------------

void MultiAttribute::check_associated(long index,string &dev_name)
{
	if ((attr_list[index]->get_writable() == Tango::READ_WITH_WRITE) ||
	    (attr_list[index]->get_writable() == Tango::READ_WRITE))
	{
		
		if (attr_list[index]->get_data_format() != Tango::SCALAR)
		{
			TangoSys_OMemStream o;
				
			o << "Device --> " << dev_name;
			o << "\nProperty writable_attr_name for attribute " << attr_list[index]->get_name();
			o << " is defined but this attribute data format is not SCALAR" << ends;
			Except::throw_exception((const char *)"API_AttrOptProp",
						o.str(),
						(const char *)"MultiAttribute::MultiAttribute");
		}
			
		long j;
		string &assoc_name = attr_list[index]->get_assoc_name();
		for (j = 0;j < writable_attr_list.size();j++)
		{
			if (attr_list[writable_attr_list[j]]->get_name() == assoc_name)
				break;
		}
		if (j == writable_attr_list.size())
		{
			TangoSys_OMemStream o;

			o << "Device --> " << dev_name;		
			o << "\nProperty writable_attr_name for attribute " << attr_list[index]->get_name(); 
			o << " is set to " << assoc_name;
			o << ", but this attribute does not exists or is not writable" << ends;
			Except::throw_exception((const char *)"API_AttrOptProp",
						o.str(),
						(const char *)"MultiAttribute::MultiAttribute");
		}

//
// Also check if the associated write attribute is a scalar one
//
			
		if (attr_list[writable_attr_list[j]]->get_data_format() != Tango::SCALAR)
		{
			TangoSys_OMemStream o;
				
			o << "Device --> " << dev_name;		
			o << "\nProperty writable_attr_name for attribute " << attr_list[index]->get_name();
			o << " is set to " << assoc_name;
			o << ", but this attribute is not of the SCALAR data format" << ends;
			Except::throw_exception((const char *)"API_AttrOptProp",
						o.str(),
						(const char *)"MultiAttribute::MultiAttribute");
		}

//
// Check that the two associated attributes have the same data type
//
			
		if (attr_list[writable_attr_list[j]]->get_data_type() != attr_list[index]->get_data_type())
		{
			TangoSys_OMemStream o;
				
			o << "Device --> " << dev_name;		
			o << "\nProperty writable_attr_name for attribute " << attr_list[index]->get_name();
			o << " is set to " << assoc_name;
			o << ", but these two attributes do not support the same data type" << ends;
			Except::throw_exception((const char *)"API_AttrOptProp",
						o.str(),
						(const char *)"MultiAttribute::MultiAttribute");
		}
			
		attr_list[index]->set_assoc_ind(writable_attr_list[j]);
	}

}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::add_attribute
// 
// description : 	Construct a new attribute object and add it to the
//			device attribute list
//
// argument : in : 	- dev_name : The device name
//			- dev_class_ptr : Pointer to the DeviceClass object
//			- index : Index in class attribute list of the new
//				  device attribute
//
//--------------------------------------------------------------------------

void MultiAttribute::add_attribute(string &dev_name,
				   DeviceClass *dev_class_ptr,
				   long index)
{
	cout4 << "Entering MultiAttribute::add_attribute" << endl;
	long i;
	
//
// Retrieve device class attribute list
//

	vector<Attr *> &tmp_attr_list = dev_class_ptr->get_class_attr()->get_attr_list();
	long nb_attr = 1;
	
//
// Get device attribute properties
// No need to implement
// a retry here (in case of db server restart) because the db reconnection
// is forced by the get_property call executed during xxxClass construction
// before we reach this code.
//

	Tango::Util *tg = Tango::Util::instance();
	vector<string> db_dev_attr_prop;
	if (tg->_UseDb == true)
	{
		CORBA::Any send;
		Tango::DevVarStringArray *send_list = new Tango::DevVarStringArray(2);
		send_list->length(2);
	
		(*send_list)[0] = CORBA::string_dup(dev_name.c_str());
		(*send_list)[1] = CORBA::string_dup(tmp_attr_list[index]->get_name().c_str());
			
		send <<= send_list;
		CORBA::Any_var received_prop = tg->get_database()->get_dbase()->command_inout("DbGetDeviceAttributeProperty",send);
		const Tango::DevVarStringArray *db_attr_prop_list;
		if ((received_prop >>= db_attr_prop_list) == false)
		{
			TangoSys_OMemStream o;
			o << "Can't get device attribute properties for device " << dev_name << ends;
					
			Except::throw_exception((const char *)"API_DatabaseAccess",
				       		 o.str(),
				        	(const char *)"MultiAttribute::MultiAttribute");
		}
	
//
// Create a vector of strings with the values returned from db
//

		long nb_db_data = db_attr_prop_list->length();
		nb_db_data = nb_db_data - 2;
		for (i = 0;i < nb_db_data;i++)
			db_dev_attr_prop.push_back(string((*db_attr_prop_list)[i + 2]));
	}

	TangoSys_MemStream o;
	long ind = 0;
	
//
// Get attribute class properties
//

	Attr &attr = dev_class_ptr->get_class_attr()->get_attr(tmp_attr_list[index]->get_name());
	vector<AttrProperty> &class_prop = attr.get_class_properties();
	vector<AttrProperty> &def_user_prop = attr.get_user_default_properties();

//
// If the attribute has some properties defined at device level, build a vector
// of these properties
//
		
	vector<AttrProperty> dev_prop;

	if (tg->_UseDb == true)
	{
		ind++;
		long nb_prop;
		o << db_dev_attr_prop[ind] << ends;
		o >> nb_prop;
		ind++;

		o.seekp(0);
		o.seekg(0);

		for (long j = 0;j < nb_prop;j++)
		{
			dev_prop.push_back(AttrProperty(db_dev_attr_prop[ind],
							db_dev_attr_prop[ind + 1]));
			ind = ind + 2;
		}
	}

//
// Concatenate these two attribute properties levels
//
	
	vector<AttrProperty> prop_list;
	concat(dev_prop,class_prop,prop_list);
	add_user_default(prop_list,def_user_prop);
	add_default(prop_list);
		
//
// Check and get attribute data type
//

	long data_type = attr.get_type();
		
//
// Create an Attribute instance
//

	if ((attr.get_writable() == Tango::WRITE) ||
	    (attr.get_writable() == Tango::READ_WRITE))
		attr_list.push_back(new WAttribute(prop_list,attr,dev_name));
	else
		attr_list.push_back(new Attribute(prop_list,attr,dev_name));
	index = attr_list.size() - 1;
		
//
// If it is writable, add it to the writable attribute list
//

	Tango::AttrWriteType w_type = attr_list[index]->get_writable();
	if ((w_type == Tango::WRITE) ||
	    (w_type == Tango::READ_WRITE))
	{
		writable_attr_list.push_back(index);
	}
		
//
// If one of the alarm properties is defined, add it to the alarmed attribute
// list
//

	if (attr_list[index]->is_alarmed() == true)
	{
		if (w_type != Tango::WRITE)
			alarm_attr_list.push_back(index);
	}

//
// Check if the writable_attr_name property is set and in this
// case, check if the associated attribute exists and is writable
//

	check_associated(index,dev_name);
	
	cout4 << "Leaving MultiAttribute::add_attribute" << endl;
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::get_attr_by_name
// 
// description : 	Return a reference to the the Attribute object for
//			the wanted attribue
//
// in :			attr_name : The attribute name
//
// This method returns a reference to the wanted attribute or throws an
// exception id the attribute is not found
//
//--------------------------------------------------------------------------

Attribute &MultiAttribute::get_attr_by_name(const char *attr_name)
{
	vector<Attribute *>::iterator pos;
	
	pos = find_if(attr_list.begin(),attr_list.end(),
		      bind2nd(WantedAttr<Attribute *,const char *,bool>(),attr_name));
		      
	if (pos == attr_list.end())
	{
		cout3 << "MultiAttribute::get_attr_by_name throwing exception" << endl;
		TangoSys_OMemStream o;
		
		o << attr_name << " attribute not found" << ends;
		Except::throw_exception((const char *)"API_AttrNotFound",
				      o.str(),
				      (const char *)"MultiAttribute::get_attr_by_name");
	}
	
	return *(*pos);
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::get_w_attr_by_name
// 
// description : 	Return a reference to the the Attribute object for
//			the wanted attribue
//
// in :			attr_name : The attribute name
//
// This method returns a reference to the wanted attribute or throws an
// exception id the attribute is not found
//
//--------------------------------------------------------------------------

WAttribute &MultiAttribute::get_w_attr_by_name(const char *attr_name)
{
	vector<Attribute *>::iterator pos;
	
	pos = find_if(attr_list.begin(),attr_list.end(),
		      bind2nd(WantedAttr<Attribute *,const char *,bool>(),attr_name));
		      
	if (pos == attr_list.end())
	{
		cout3 << "MultiAttribute::get_w_attr_by_name throwing exception" << endl;
		TangoSys_OMemStream o;
		
		o << attr_name << " attribute not found" << ends;
		Except::throw_exception((const char *)"API_AttrNotFound",
				      o.str(),
				      (const char *)"MultiAttribute::get_w_attr_by_name");
	}

		
	return static_cast<WAttribute &>(*(*pos));
}


//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::get_attr_ind_by_name
// 
// description : 	Return the index in the Attribute object vector of
//			a specified attribute
//
// in :			attr_name : The attribute name
//
// This method returns the index of the wanted attribute or throws an
// exception id the attribute is not found
//
//--------------------------------------------------------------------------

long MultiAttribute::get_attr_ind_by_name(const char *attr_name)
{
	long i;

	long nb_attr = attr_list.size();
	string st(attr_name);	
	for (i = 0;i < nb_attr;i++)
	{
		if (attr_list[i]->get_name() == st)
			break;
	}
	
	if (i == nb_attr)
	{
		cout3 << "MultiAttribute::get_attr_ind_by_name throwing exception" << endl;
		TangoSys_OMemStream o;
		
		o << attr_name << " attribute not found" << ends;
		Except::throw_exception((const char *)"API_AttrNotFound",
				      o.str(),
				      (const char *)"MultiAttribute::get_attr_ind_by_name");
	}
	
	return i;
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::check_alarm
// 
// description : 	check alarm on all the attribute where one alarm is
//			defined
//
// This method returns a boolen set to true if one of the attribute with
// an alarm defined is in alarm state
//
//--------------------------------------------------------------------------

bool MultiAttribute::check_alarm()
{
	long i;
	bool ret,tmp_ret;

	tmp_ret = false;
	ret = false;	
	for (i = 0;i < alarm_attr_list.size();i++)
	{
		tmp_ret = check_alarm(alarm_attr_list[i]);
		if (tmp_ret == true)
			ret = true;
	}
	
	return ret;
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::read_alarm
// 
// description : 	Add a message in the device status string if one of
//			the device attribute is in the alarm state
//
// in :			status : The device status
//
//--------------------------------------------------------------------------

void MultiAttribute::read_alarm(string &status)
{
	long i;
	
	for (i = 0;i < alarm_attr_list.size();i++)
	{
		Attribute &att = get_attr_by_ind(alarm_attr_list[i]);

//
// Add a message for low level alarm
//

		if (att.is_min_alarm() == true)
		{
			string &attr_label = att.get_label();
			string str;
			if (attr_label == LabelNotSpec)
			{
				str = "\nAlarm : Value too low for attribute ";
				str = str + att.get_name();
			}
			else
			{
				str = "\nAlaam : Value too low for ";
				str = str + attr_label;
			}				
			status = status + str;
		}

//
// Add a message for high level alarm
//
			
		if (att.is_max_alarm() == true)
		{
			string &attr_label = att.get_label();
			string str;
			if (attr_label == LabelNotSpec)
			{
				str = "\nAlarm : Value too high for attribute ";
				str = str + att.get_name();
			}
			else
			{
				str = "\nAlarm : Value too high for ";
				str = str + attr_label;
			}				
			status = status + str;
		}
	}
}

//+-------------------------------------------------------------------------
//
// method : 		MultiAttribute::add_write_value
// 
// description : 	For scalar attribute with an associated write 
//			attribute, the read_attributes CORBA operation also
//			returns the write value. This method gets the associated
//			write attribute value and adds it to the read
//			attribute
//
// in :			att : Reference to the attribute which must be read
//
//--------------------------------------------------------------------------

void MultiAttribute::add_write_value(Attribute &att)
{
	WAttribute &assoc_att = get_w_attr_by_ind(att.get_assoc_ind());
	
	switch (att.get_data_type())
	{
	case Tango::DEV_SHORT :
		Tango::DevShort sh_write_val;
		assoc_att.get_write_value(sh_write_val);
		
		att.add_write_value(sh_write_val);
		break;
		
	case Tango::DEV_LONG :
		Tango::DevLong lg_write_val;
		assoc_att.get_write_value(lg_write_val);
		
		att.add_write_value(lg_write_val);
		break;
		
	case Tango::DEV_DOUBLE :
		Tango::DevDouble db_write_val;
		assoc_att.get_write_value(db_write_val);
		
		att.add_write_value(db_write_val);
		break;
		
	case Tango::DEV_STRING :
		Tango::DevString str_write_val;
		assoc_att.get_write_value(str_write_val);
		
		att.add_write_value(str_write_val);
		break;
	}
}

} // End of Tango namespace