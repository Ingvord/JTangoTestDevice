static const char *RcsId = "$Header$";
//
// dbdatum.cpp 	- C++ source code file for TANGO dbapi class DbDatum
//
// programmer 	- Andy Gotz (goetz@esrf.fr)
//
// original 	- October 2000
//
// last changed	- 17/10/2000 
//
// version 	- 1.0
//
// $Log$
// Revision 2.2  2002/10/14 09:32:43  taurel
// Fix bugs in devapi_base.cpp file :
// - In read_attribute and read_attributes method of the DeviceProxy class
//   Do not create sequence the same way if the call is local or remote.
// - Add reconnection in the Connection::set_timeout_millis method
// - Add flags to the Connection::set_timeout_millis method
// - Fix bug in the DeviceProxy constructor when device is not marked as exported
//   in the database. The constructor was not stateless in this case.
//
// Revision 2.1  2002/08/12 12:43:23  taurel
// Fix bug in DeviceProxy::write_attributes method when writing several
// attributes in one call. (File devapi_base.cpp)
//
// Revision 2.0  2002/06/28 13:43:07  taurel
// Lot of changes since last releases :
// 	- Database object managed as a singleton per control system
// 	- Support all tango device naming syntax (using TANGO_HOST env.
//  	  variable, without env variable and non database device)
// 	- No more copy during read_attribute and command_inout
// 	- Added some missing methods
// 	- Build an exception class hierarchy
// 	- Added correct management of device time-out
// 	- Support all Tango device interface release 2 features
// 	  (data/attribute comming from polling buffer, polling related methods,
// 	   command/attribute result history)
//
// Revision 1.13  2002/04/29 12:11:25  goetz
// New change in Database::delete_device_attribute_property. The fix done in the previous release was not enough
//
// Revision 1.12  2002/04/29 05:43:09  goetz
// Check in by ET. Fix bug (i=i+n_props+1) in Database:delete_device_attribute_property, delete_class_attribute_property and put_class_attribute_property
//
// Revision 1.11  2002/03/18 07:20:56  goetz
// new DeviceProxy() stateless now; added inline method name()
//
// Revision 1.10  2002/03/01 15:48:06  goetz
// added get_attribute_list() method
//
// Revision 1.9  2002/02/28 17:00:52  goetz
// intermediate checkin
//
// Revision 1.8  2002/02/18 20:42:59  goetz
// supports attributes, added lots of new methods, changed some old ones
//
// Revision 1.7  2002/02/11 20:16:58  goetz
// added inline methods for Tango CORBA sequence types
//
// Revision 1.6  2001/12/04 21:34:46  goetz
// fixed bug in Database::put_device_attribute_property
//
// Revision 1.5  2001/09/27 13:04:47  goetz
// added (const char*) to all throw_exceptions; ported to dll on Windows
//
// Revision 1.4  2001/08/13 13:40:00  goetz
// improved exception handling; ported to Windows
//
// Revision 1.3  2001/08/03 14:24:38  goetz
// implemented reconnect; catching CORBA::Exception's and rethrowing them
//
// Revision 1.2  2001/08/02 14:50:12  goetz
// added support for bool, DevState and DevVarCharArray types
//
// Revision 1.1.1.1  2001/07/18 08:42:24  goetz
// Imported sources
//
// Revision 1.12  2001/06/08 12:55:06  goetz
// fixed bug with extracting vectors under Windows
//
//

#include <tango.h>


#ifdef STRSTREAM
#include <iomanip.h>
#else
#include <iomanip>
#endif
                                                      
using namespace CORBA;

namespace Tango
{

//-----------------------------------------------------------------------------
//
// DbDatum::DbDatum() - constructor to create DbDatum specifying name
//
//-----------------------------------------------------------------------------

DbDatum::DbDatum(string p_name)
{
	name = p_name;
	value_size = 0;
	value_string.resize(0);
}

//-----------------------------------------------------------------------------
//
// DbDatum::DbDatum() - constructor to create DbDatum without arguments
//
//-----------------------------------------------------------------------------

DbDatum::DbDatum()
{
}

//-----------------------------------------------------------------------------
//
// DbDatum::~DbDatum() - destructor to destroy DbDatum
//
//-----------------------------------------------------------------------------

DbDatum::~DbDatum()
{
}

//-----------------------------------------------------------------------------
//
// DbDatum::size() - returns number of elements in DbDatum
//
//-----------------------------------------------------------------------------

int DbDatum::size()
{
	return value_string.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::is_empty() - returns true or false if datum is empty
//
//-----------------------------------------------------------------------------

bool DbDatum::is_empty()
{
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"The DbDatum object is empty",
					     (const char*)"DbDatum::is_empty");
		}
		return true;
	}
	else
	{
		return false;
	}
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(short &) - insert a short into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (short datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	ostream << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */
	value_string.resize(1);
	value_string[0] = string(ostream.str());
#ifdef STRSTREAM
	delete [] ostream.str();
#endif /* STRSTREAM */
	value_type = DEV_SHORT;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(short &) - extract a short from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (short &datum)
{
	bool ret;
	
	if (value_string.size() == 0) 
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"Cannot extract short, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(short)");
		}
		ret = false;
	}
	else
	{

#ifdef STRSTREAM
		istrstream istream(value_string[0].c_str());
#else
		istringstream istream(value_string[0]);
#endif /* STRSTREAM */
		istream >> datum;
		if (!istream)
		{
			if (exceptions_flags.test(wrongtype_flag))
			{
				ApiDataExcept::throw_exception((const char *)"API_IncompatibleArgumentType",
							       (const char *)"Cannot extract, data in DbDatum is not a short",
							       (const char *)"DbDatum::operator >>(short)");
			}
			ret = false;
		}
		else
			ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(unsigned short &) - insert a unsigned short into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (unsigned short datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	ostream << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */
	value_string.resize(1);
	value_string[0] = string(ostream.str());
#ifdef STRSTREAM
	delete [] ostream.str();
#endif /* STRSTREAM */
	value_type = DEV_USHORT;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(unsigned short &) - extract a unsigned short from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (unsigned short& datum)
{
	bool ret;
	if (value_string.size() == 0) 
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract unsigned short, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(unsigned short)");
		}
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		istrstream istream(value_string[0].c_str());
#else
		istringstream istream(value_string[0]);
#endif /* STRSTREAM */
		istream >> datum;
		if (!istream)
		{
			if (exceptions_flags.test(wrongtype_flag))
			{
				ApiDataExcept::throw_exception((const char *)"API_IncompatibleArgumentType",
							       (const char *)"Cannot extract, data in DbDatum is not an unsigned short",
							       (const char *)"DbDatum::operator >>(unsigned short)");
			}
			ret = false;
		}
		else
			ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(long &) - insert a long into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (long datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	ostream << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */
	value_string.resize(1);
	value_string[0] = string(ostream.str());
#ifdef STRSTREAM
	delete [] ostream.str();
#endif /* STRSTREAM */
	value_type = DEV_LONG;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(long &) - extract a long from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (long& datum)
{
	bool ret;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"Api_EmptyDbDbDatum",
					     (const char*)"cannot extract long, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(long)");
		}
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		istrstream istream(value_string[0].c_str());
#else
		istringstream istream(value_string[0]);
#endif /* STRSTREAM */
		istream >> datum;
		if (!istream)
		{
			if (exceptions_flags.test(wrongtype_flag))
			{
				ApiDataExcept::throw_exception((const char *)"API_IncompatibleArgumentType",
							       (const char *)"Cannot extract, data in DbDatum is not a long",
							       (const char *)"DbDatum::operator >>(long)");
			}
			ret = false;
		}
		else
			ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(unsigned long &) - insert a unsigned short into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (unsigned long datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	ostream << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */
	value_string.resize(1);
	value_string[0] = string(ostream.str());
#ifdef STRSTREAM
	delete [] ostream.str();
#endif /* STRSTREAM */
	value_type = DEV_ULONG;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(unsigned long &) - extract a unsigned short from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (unsigned long& datum)
{
	bool ret;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract unsigned long, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(unsigned long)");
		}
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		istrstream istream(value_string[0].c_str());
#else
		istringstream istream(value_string[0]);
#endif /* STRSTREAM */
		istream >> datum;
		if (!istream)
		{
			if (exceptions_flags.test(wrongtype_flag))
			{
				ApiDataExcept::throw_exception((const char *)"API_IncompatibleArgumentType",
							       (const char *)"Cannot extract, data in DbDatum is not an unsigned long",
							       (const char *)"DbDatum::operator >>(unsigned long)");
			}
			ret = false;
		}
		else
			ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(float &) - insert a float into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (float datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	ostream << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */
	value_string.resize(1);
	value_string[0] = string(ostream.str());
#ifdef STRSTREAM
	delete [] ostream.str();
#endif /* STRSTREAM */
	value_type = DEV_FLOAT;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(float &) - extract a float from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (float& datum)
{
	bool ret;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"Api_EmptyDbDbDatum",
					     (const char*)"cannot extract float, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(float)");
		}
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		istrstream istream(value_string[0].c_str());
#else
		istringstream istream(value_string[0]);
#endif /* STRSTREAM */
		istream >> datum;
		if (!istream)
		{
			if (exceptions_flags.test(wrongtype_flag))
			{
				ApiDataExcept::throw_exception((const char *)"API_IncompatibleArgumentType",
							       (const char *)"Cannot extract, data in DbDatum is not a float",
							       (const char *)"DbDatum::operator >>(float)");
			}
			ret = false;
		}
		else
			ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(double &) - insert a double into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (double datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
#ifndef _HPUX_SOURCE
	ostream << std::setprecision(15) << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */ 
#else
	ostream << setprecision(15) << datum;
#ifdef STRSTREAM
	ostream << ends;
#endif /* STRSTREAM */
#endif /* _HPUX_SOURCE */
	value_string.resize(1);
	value_string[0] = string(ostream.str());
#ifdef STRSTREAM
	delete [] ostream.str();
#endif /* STRSTREAM */
	value_type = DEV_DOUBLE;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(double &) - extract a double from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (double& datum)
{
	bool ret;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"Api_EmptyDbDatum",
					     (const char*)"cannot extract double, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(double)");
		}
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		istrstream istream(value_string[0].c_str());
#else
		istringstream istream(value_string[0]);
#endif /* STRSTREAM */
		istream >> std::setprecision(15) >> datum;
		if (!istream)
		{
			if (exceptions_flags.test(wrongtype_flag))
			{
				ApiDataExcept::throw_exception((const char *)"API_IncompatibleArgumentType",
							       (const char *)"Cannot extract, data in DbDatum is not a double",
							       (const char *)"DbDatum::operator >>(double)");
			}
			ret = false;
		}
		else
			ret = true;
	}
	
	return ret;

}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(string &) - insert a string into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (string& datum)
{
	value_string.resize(1);
	value_string[0] = datum;
	value_type = DEV_STRING;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(string &) - extract a string from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (string& datum)
{
	bool ret;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract string, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(string)");
		}
		ret = false;
	}
	else
	{
		datum = value_string[0];
		ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(char* &) - insert a char* into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (char*& datum)
{
	value_string.resize(1);
	value_string[0] = datum;
	value_type = DEV_STRING;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(const char* &) - insert a const char* into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (const char*& datum)
{
	value_string.resize(1);
	value_string[0] = datum;
	value_type = DEV_STRING;
	value_size = 1;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(const char* &) - extract a const char* from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (const char*& datum)
{
	bool ret;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract string, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(string)");
		}
		ret = false;
	}
	else
	{
		datum = value_string[0].c_str();
		ret = true;
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<short> &) - insert a vector<short> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<short>& datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		ostream << datum[i];
#ifdef STRSTREAM
		ostream << ends;
#endif /* STRSTREAM */
		value_string[i] = string(ostream.str());
#ifdef STRSTREAM
		ostream.rdbuf()->freeze(false);
#endif /* STRSTREAM */
		ostream.seekp(0);
	}
	value_type = DEVVAR_SHORTARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<short> &) - extract a vector<short> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<short>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract short vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<short>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		strstream iostream;
#else
		stringstream iostream;
#endif /* STRSTREAM */
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			iostream.clear(); iostream.seekp (0); iostream.seekg(0);
			iostream << value_string[i] << ends;
			iostream >> datum[i];
			if (!iostream)
			{
				if (exceptions_flags.test(wrongtype_flag))
				{
					TangoSys_OMemStream desc;
					desc << "Cannot extract short vector, elt number ";
					desc << i+1 << " is not a short" << ends;

					ApiDataExcept::throw_exception((const char*)"API_IncompatibleArgumentType",
					     desc.str(),
					     (const char*)"DbDatum::operator >>(vector<short>)");
				}
				ret = false;
				break;
			}
		}
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<unsigned short> &) - insert a vector<unsigned short> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<unsigned short>& datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		ostream << datum[i];
#ifdef STRSTREAM
		ostream << ends;
#endif /* STRSTREAm */
		value_string[i] = string(ostream.str());
#ifdef STRSTREAM
		ostream.rdbuf()->freeze(false);
#endif /* STRSTREAM */
		ostream.seekp(0);
	}
	value_type = DEVVAR_USHORTARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<unsigned short> &) - extract a vector<unsigned short> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<unsigned short>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract unsigned short vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<unsigned short>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		strstream iostream;
#else
		stringstream iostream;
#endif /* STRSTREAM */
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			iostream.clear(); iostream.seekp (0); iostream.seekg(0);
			iostream << value_string[i] << ends;
			iostream >> datum[i];
			if (!iostream)
			{
				if (exceptions_flags.test(wrongtype_flag))
				{
					TangoSys_OMemStream desc;
					desc << "Cannot extract unsigned short vector, elt number ";
					desc << i+1 << " is not an unsigned short" << ends;

					ApiDataExcept::throw_exception((const char*)"API_IncompatibleArgumentType",
					     desc.str(),
					     (const char*)"DbDatum::operator >>(vector<unsigned short>)");
				}
				ret = false;
				break;
			}
		}
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<long> &) - insert a vector<long> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<long>& datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		ostream << datum[i];
#ifdef STRSTREAM
		ostream << ends;
#endif /* STRSTREAM */
		value_string[i] = string(ostream.str());
#ifdef STRSTREAM
		ostream.rdbuf()->freeze(false);
#endif /* STRSTREAM */
		ostream.seekp(0);
	}
	value_type = DEVVAR_LONGARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<long> &) - extract a vector<long> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<long>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract long vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<long>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		strstream iostream;
#else
		stringstream iostream;
#endif /* STRSTREAM */
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			iostream.clear(); iostream.seekp (0); iostream.seekg(0);
			iostream << value_string[i] << ends;
			iostream >> datum[i];
			if (!iostream)
			{
				if (exceptions_flags.test(wrongtype_flag))
				{
					TangoSys_OMemStream desc;
					desc << "Cannot extract long vector, elt number ";
					desc << i+1 << " is not a long" << ends;

					ApiDataExcept::throw_exception((const char*)"API_IncompatibleArgumentType",
					     desc.str(),
					     (const char*)"DbDatum::operator >>(vector<long>)");
				}
				ret = false;
				break;
			}
		}
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<unsigned long> &) - insert a vector<unsigned long> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<unsigned long>& datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		ostream << datum[i];
#ifdef STRSTREAM
		ostream << ends;
#endif /* STRSTREAM */
		value_string[i] = string(ostream.str());
#ifdef STRSTREAM
		ostream.rdbuf()->freeze(false);
#endif /* STRSTREAM */
		ostream.seekp(0);
	}
	value_type = DEVVAR_ULONGARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<unsigned long> &) - extract a vector<unsigned long> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<unsigned long>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract unsigned long vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<unsigned long>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		strstream iostream;
#else
		stringstream iostream;
#endif /* STRSTREAM */
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			iostream.clear(); iostream.seekp (0); iostream.seekg(0);
			iostream << value_string[i];
			iostream >> datum[i];
			if (!iostream)
			{
				if (exceptions_flags.test(wrongtype_flag))
				{
					TangoSys_OMemStream desc;
					desc << "Cannot extract unsigned long vector, elt number ";
					desc << i+1 << " is not an unsigned long" << ends;

					ApiDataExcept::throw_exception((const char*)"API_IncompatibleArgumentType",
					     desc.str(),
					     (const char*)"DbDatum::operator >>(vector<unsigned long>)");
				}
				ret = false;
				break;
			}
		}
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<float> &) - insert a vector<float> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<float>& datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		ostream << datum[i];
#ifdef STRSTREAM
		ostream << ends;
#endif /* STRSTREAM */
		value_string[i] = string(ostream.str());
#ifdef STRSTREAM
		ostream.rdbuf()->freeze(false);
#endif /* STRSTREAM */
		ostream.seekp(0);
	}
	value_type = DEVVAR_FLOATARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<float> &) - extract a vector<float> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<float>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract float vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<float>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		strstream iostream;
#else
		stringstream iostream;
#endif /* STRSTREAM */
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			iostream.clear(); iostream.seekp (0); iostream.seekg(0);
			iostream << value_string[i] << ends;
			iostream >> datum[i];
			if (!iostream)
			{
				if (exceptions_flags.test(wrongtype_flag))
				{
					TangoSys_OMemStream desc;
					desc << "Cannot extract float vector, elt number ";
					desc << i+1 << " is not a float" << ends;

					ApiDataExcept::throw_exception((const char*)"API_IncompatibleArgumentType",
					     desc.str(),
					     (const char*)"DbDatum::operator >>(vector<float>)");
				}
				ret = false;
				break;
			}
		}
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<double> &) - insert a vector<double> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<double>& datum)
{
#ifdef STRSTREAM
	ostrstream ostream;
#else
	ostringstream ostream;
#endif /* STRSTREAM */
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		ostream << datum[i];
#ifdef STRSTREAM
		ostream << ends;
#endif /* STRSTREAM */
		value_string[i] = string(ostream.str());
#ifdef STRSTREAM
		ostream.rdbuf()->freeze(false);
#endif /* STRSTREAM */
		ostream.seekp(0);
	}
	value_type = DEVVAR_DOUBLEARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<double> &) - extract a vector<double> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<double>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract double vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<float>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
#ifdef STRSTREAM
		strstream iostream;
#else
		stringstream iostream;
#endif /* STRSTREAM */
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			iostream.clear(); iostream.seekp (0); iostream.seekg(0);
			iostream << value_string[i];
			iostream >> std::setprecision(15) >> datum[i];
			if (!iostream)
			{
				if (exceptions_flags.test(wrongtype_flag))
				{
					TangoSys_OMemStream desc;
					desc << "Cannot extract double vector, elt number ";
					desc << i+1 << " is not a double" << ends;

					ApiDataExcept::throw_exception((const char*)"API_IncompatibleArgumentType",
					     desc.str(),
					     (const char*)"DbDatum::operator >>(vector<double>)");
				}
				ret = false;
				break;
			}
		}
	}
	
	return ret;
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator <<(vector<string> &) - insert a vector<string> into DbDatum
//
//-----------------------------------------------------------------------------

void DbDatum::operator << (vector<string>& datum)
{
	value_string.resize(datum.size());
	for (int i=0; i<datum.size(); i++)
	{
		value_string[i] = datum[i];
	}
	value_type = DEVVAR_STRINGARRAY;
	value_size = datum.size();
}

//-----------------------------------------------------------------------------
//
// DbDatum::operator >>(vector<string> &) - extract a vector<string> from DbDatum
//
//-----------------------------------------------------------------------------

bool DbDatum::operator >> (vector<string>& datum)
{
	bool ret = true;
	if (value_string.size() == 0)
	{
		if (exceptions_flags.test(isempty_flag))
		{
			ApiDataExcept::throw_exception((const char*)"API_EmptyDbDatum",
					     (const char*)"cannot extract string vector, no data in DbDatum object ",
					     (const char*)"DbDatum::operator >>(vector<string>)");
		}
		datum.resize(0);
		ret = false;
	}
	else
	{
		datum.resize(value_string.size());
		for (int i=0; i<value_string.size(); i++)
		{
			datum[i] = value_string[i];
		}
	}
	
	return ret;
}

} // End of Tango namespace
