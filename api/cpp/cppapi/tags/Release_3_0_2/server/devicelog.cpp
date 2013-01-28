//+=============================================================================
//
// file :	  DeviceLog.cpp
//
// description :  Logging oriented methods of the DeviceImpl class
//
// project :	  TANGO
//
// author(s) :	  N.Leclercq - SOLEIL
//
// $Revision$
//
// $Log$
// Revision 3.0  2003/03/25 16:42:00  taurel
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
// Revision 2.3  2003/03/13 15:17:50  nleclercq
// Minor modifications on logging implementation
//
// Revision 2.2  2003/03/11 17:55:52  nleclercq
// Switch from log4cpp to log4tango
//
// Revision 2.1  2003/02/17 14:57:40  taurel
// Added the new Tango logging stuff (Thanks Nicolas from Soleil)
//
//
// copyleft :	  European Synchrotron Radiation Facility
//		  BP 220, Grenoble 38043
//		  FRANCE
//
//-=============================================================================

#if HAVE_CONFIG_H
#include <ac_config.h>
#endif

#include <tango.h>

#ifdef TANGO_HAS_LOG4TANGO

#include <logging.h>

namespace Tango
{

//+-------------------------------------------------------------------------
// method : DeviceImpl::get_logger_i
//--------------------------------------------------------------------------
log4tango::Logger* DeviceImpl::get_logger_i (void)
{ 
  try {
    // trace
    cout4 << "Entering DeviceImpl::get_logger_i" << endl;
    // instanciate the logger (
    // shame on me for a such huggly impl. but polymorphism
    // can't be used here !
    if (ext->logger == 0) {
      if (get_device_class()->get_name() == "DServer") {
                ext->logger = Logging::get_core_logger();
      }
      else {
        // get device name
        std::string dev_name(get_name());
        // avoid case sensitive troubles
        std::transform(dev_name.begin(), 
                              dev_name.end(), 
                              dev_name.begin(), 
                              ::tolower);
        // instanciate the logger using device name
        ext->logger = new log4tango::Logger(dev_name);
        if (ext->logger == 0) {
          ext->logger = Logging::get_core_logger();
        }
        // set default level
        ext->logger->set_level(log4tango::Level::WARN);
        // save current level
        ext->saved_log_level = log4tango::Level::WARN;
      }
    }
    // trace
    cout4 << "Leaving DeviceImpl::get_logger_i" << endl;
  } catch (...) {
    // save our souls...
   ext->logger = Logging::get_core_logger();
  }
  return ext->logger;
}

//+-------------------------------------------------------------------------
// method : DeviceImpl::init_logger
//--------------------------------------------------------------------------
void DeviceImpl::init_logger (void)
{
  try {
    // trace
    cout4 << "Entering DeviceImpl::init_logger" << endl;
    // get Tango::Util instance
    Tango::Util *tg = Tango::Util::instance();
    // get cmd line logging level then ...
    int trace_level = tg->get_trace_level();
    // ... convert it to log4tango level
    log4tango::Level::Value cmd_line_level;
    // does the logging level set from cmd line?
    int level_set_from_cmd_line = 1;
    if (trace_level <= 0) {
      level_set_from_cmd_line = 0;
      cmd_line_level = log4tango::Level::OFF;
    } else if (trace_level <= 2) {
      cmd_line_level = log4tango::Level::INFO;
    } else {
      cmd_line_level = log4tango::Level::DEBUG;
    }
    // add a console target if logging level set from cmd line
    if (level_set_from_cmd_line) {
      // add a console target if logging level set from cmd line
      Logging::add_logging_target(get_logger(), kLogTargetConsole, 0);
    }
    if (tg->_UseDb == false) {
      // done if we are not using the database
      cout4 << "Leaving DeviceImpl::init_logger" << endl;
      return;
    }
    // get both logging level and targets from database
    DbData db_data;
    db_data.push_back(DbDatum("logging_level"));
    db_data.push_back(DbDatum("logging_target"));
    db_data.push_back(DbDatum("logging_rft"));
    try { 
      db_dev->get_property(db_data);
    } catch (...) {
      // error: set logging level then return
      get_logger()->set_level(cmd_line_level);
      return;
    }
    // set logging level (if not set from cmd line)
    std::string log_level_property;
    if (!level_set_from_cmd_line && db_data[0].is_empty() == false) {
      db_data[0] >> log_level_property;
      // avoid case sensitive troubles
      std::transform(log_level_property.begin(), log_level_property.end(), 
                     log_level_property.begin(), ::toupper);
      cout4 << "Initial logging level set to [" << log_level_property << "]" << endl;
      // convert from string to log4tango level
      log4tango::Level::Value log4tango_level = log4tango::Level::WARN;
      try {
          log4tango_level = Logging::tango_to_log4tango_level(log_level_property, false);
      } catch (...) {
         // ignore exception
      }
      // set logger's level (from property)
      get_logger()->set_level(log4tango_level);
    }
    else {
      // set logger's level (from cmd line)
      get_logger()->set_level(cmd_line_level);
    }
    // save current logging level
    ext->saved_log_level = get_logger()->get_level();
    // get rolling threshold for file targets 
    long rft_property = static_cast<long>(kDefaultRollingThreshold);
    if (db_data[2].is_empty() == false) {
      db_data[2] >> rft_property;
    }
    // save current rolling threshold
    ext->rft = static_cast<size_t>(rft_property);
    // set logging targets 
    std::vector<std::string> log_target_property;
    if (db_data[1].is_empty() == false) {
      db_data[1] >> log_target_property;
      // attach targets to logger
      for (unsigned int i = 0; i < log_target_property.size(); i++) {
              Logging::add_logging_target(get_logger(), log_target_property[i], 0);
      }
    }
    // set rolling file threshold for file targets
    Logging::set_rolling_file_threshold(get_logger(), ext->rft);
    // trace 
    cout4 << "Leaving DeviceImpl::init_logger" << endl;
  } 
  catch (...) {
    // igore any exception
  }
}

//+-------------------------------------------------------------------------
// method : DeviceImpl::start_logging
//--------------------------------------------------------------------------
void DeviceImpl::start_logging (void) {
  get_logger()->set_level(ext->saved_log_level);
}

//+-------------------------------------------------------------------------
// method : DeviceImpl::stop_logging
//--------------------------------------------------------------------------
void DeviceImpl::stop_logging (void) {
  ext->saved_log_level = get_logger()->get_level();
  get_logger()->set_level(log4tango::Level::OFF);
}

} // namespace Tango

#endif // TANGO_HAS_LOG4TANGO