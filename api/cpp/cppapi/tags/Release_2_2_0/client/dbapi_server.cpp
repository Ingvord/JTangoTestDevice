static const char *RcsId = "$Header$";
//
// dbdevice.cpp - C++ source code file for TANGO dbapi class DbServer
//
// programmer 	- Andy Gotz (goetz@esrf.fr)
//
// original 	- November 2000
//
// last changed	- 14/11/2000 
//
// version 	- 1.0
//


#include <tango.h>
                                                     
using namespace CORBA;

namespace Tango
{

//-----------------------------------------------------------------------------
//
// DbServer::DbServer() - constructor to create a DbServer object for
//			  accessing a server of this name in the specified
//			  TANGO database (import/export info and properties) 
//
//-----------------------------------------------------------------------------

DbServer::DbServer(string server_name, Database *server_dbase)
{
	name = string(server_name);
	dbase = server_dbase;
	ext_dbase = true;
}

//-----------------------------------------------------------------------------
//
// DbServer::DbServer() - constructor to create a DbServer object for
//			  accessing a server of this name without specifying
//			  the TANGO database.
//
//-----------------------------------------------------------------------------

DbServer::DbServer(string server_name)
{
	name = string(server_name);
	db_ind = ApiUtil::instance()->get_db_ind();
	ext_dbase = false;
}

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
//
// DbServer::~DbServer() - destructor to destroy a DbServer object
//
//-----------------------------------------------------------------------------

DbServer::~DbServer()
{
}

//-----------------------------------------------------------------------------
//
// DbServer::add_server() - public method to add a server and its devices
//                          to the database
//
//-----------------------------------------------------------------------------

void DbServer::add_server(DbDevInfos &dev_infos)
{
	if (ext_dbase == true)
		dbase->add_server(name, dev_infos);
	else
	{
		ApiUtil *au = ApiUtil::instance();
		(au->get_db_vect())[db_ind]->add_server(name, dev_infos);
	}
}

//-----------------------------------------------------------------------------
//
// DbServer::export_server() - public method to export devices belonging to
//                             this server to the database
//
//-----------------------------------------------------------------------------

void DbServer::export_server(DbDevExportInfos &dev_export)
{
	if (ext_dbase == true)
		dbase->export_server(name, dev_export);
	else
	{
		ApiUtil *au = ApiUtil::instance();
		(au->get_db_vect())[db_ind]->export_server(name, dev_export);
	}
}

//-----------------------------------------------------------------------------
//
// DbServer::unexport_server() - public method to unexport all devices belonging
//                               to this server in the database
//
//-----------------------------------------------------------------------------

void DbServer::unexport_server()
{
	if (ext_dbase == true)
		dbase->unexport_server(name);
	else
	{
		ApiUtil *au = ApiUtil::instance();
		(au->get_db_vect())[db_ind]->unexport_server(name);
	}
}

//-----------------------------------------------------------------------------
//
// DbServer::delete_server() - public method to delete all devices belonging
//                             to this server from the database
//
//-----------------------------------------------------------------------------

void DbServer::delete_server()
{
	if (ext_dbase == true)
		dbase->delete_server(name);
	else
	{
		ApiUtil *au = ApiUtil::instance();
		(au->get_db_vect())[db_ind]->delete_server(name);
	}
}

//-----------------------------------------------------------------------------
//
// DbServer::get_server_info() - public method to get server info
//
//-----------------------------------------------------------------------------

DbServerInfo DbServer::get_server_info()
{
	if (ext_dbase == true)
		return dbase->get_server_info(name);
	else
	{
		ApiUtil *au = ApiUtil::instance();
		return (au->get_db_vect())[db_ind]->get_server_info(name);
	}
}

} // End of Tango namespace