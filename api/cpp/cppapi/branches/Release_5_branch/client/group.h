//=============================================================================
//
// file :               group.h
//
// description :        Include for Tango Group impl.
//
// project :            TANGO
//
// author(s) :          N.Leclercq
//
// copyleft :           European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
//=============================================================================

#ifndef _GROUP_H_
#define _GROUP_H_

#include <tango.h>

namespace Tango {

//=============================================================================
// THREAD SAFE IMPL OPTION
//=============================================================================
#define TANGO_GROUP_HAS_THREAD_SAFE_IMPL 1

//=============================================================================
// FORWARD DECLARATIONS
//=============================================================================
class Group;
class GroupElement;

//=============================================================================
// Misc. Typedefs
//=============================================================================
//- group content (individual devices and/or sub-groups)
typedef std::vector<GroupElement*> GroupElements;
//- group content iterator
typedef GroupElements::iterator GroupElementsIterator;
//-----------------------------------------------------------------------------
//- define what is a list of token (for name pattern management)
typedef std::vector<std::string> TokenList;
//=============================================================================
// class ExtRequestDesc : an asynch. request holder for groups
//-----------------------------------------------------------------------------
class AsynchRequest
{
  //- An asynch. request repository is maintain by each GroupDeviceElement
  friend class GroupDeviceElement;
  
  public:
    //- ctor
    AsynchRequest (long _rid, const std::string& _obj_name) 
      : rq_id(_rid), obj_name(_obj_name)
    {
      //-noop impl
    };
    //- ctor
    AsynchRequest (long _rid, const std::string& _obj_name, const DevFailed& _df) 
      : rq_id(_rid), obj_name(_obj_name), rq_ex(_df) 
    {
      //-noop impl
    };
    //- dtor
    virtual ~AsynchRequest () {
      //-noop impl
    };
    
  private:
    //- request ID
    long rq_id;
    //- name of request object (command or attribute)
    std::string obj_name;
    //- DevFailed containing potential error
    DevFailed rq_ex;
};
//-----------------------------------------------------------------------------
//- asynch. request repository
typedef std::map<long, AsynchRequest> AsynchRequestRep;
//- asynch. request repository iterator
typedef AsynchRequestRep::iterator AsynchRequestRepIterator;
//- asynch. request repository value
typedef AsynchRequestRep::value_type AsynchRequestRepValue;
//=============================================================================

//=============================================================================
// class GroupReply : base class for group reply 
//=============================================================================
class GroupReply 
{
public:
  //- enable/disable exception - returns the previous mode
  static bool enable_exception (bool exception_mode = true);
  //- default ctor
  GroupReply ();
  //- ctor
  GroupReply (const std::string& dev_name, 
              const std::string& obj_name);
  //- ctor
  GroupReply (const std::string& dev_name, 
              const std::string& obj_name,
              const DevFailed& exception,
              bool  has_failed = true);
  //- dtor
  virtual ~GroupReply ();
  //- has_failed accessor
  bool has_failed (void) const { 
    return has_failed_m;
  }
  //- device name accessor
  const std::string& dev_name (void) const {
    return dev_name_m;
  }
  //- object (i.e. command or attribute) name accessor
  const std::string& obj_name (void) const {
    return obj_name_m;
  }
  //- error stack accessor
  const DevErrorList& get_err_stack (void) const {
    return exception_m.errors;
  }
protected:
  //- exception flag (enable/disable)
  static bool exception_enabled;
  //- the device name
  std::string dev_name_m;
  //- command or attribute name
  std::string obj_name_m;
  //- has_failed_m: true is an error occurred, false otherwise
  bool has_failed_m;
  //- exception: valid if has_failed_m set to true
  DevFailed exception_m;
};

//=============================================================================
// class GroupCmdReply : reply to command executed on a group
//=============================================================================
class GroupCmdReply : public GroupReply
{
public:
  //- default ctor
  GroupCmdReply ();
  //- copy ctor
  GroupCmdReply (const GroupCmdReply& src);
  //-
  GroupCmdReply (const std::string& dev_name, 
                 const std::string& obj_name, 
                 const DeviceData& data);
  //- ctor
  GroupCmdReply (const std::string& dev_name, 
                 const std::string& obj_name, 
                 const DevFailed& exception);
  //- dtor
  virtual ~GroupCmdReply ();
  //- data accessor (may throw Tango::DevFailed)
  /*const*/ DeviceData& get_data (void);
  //- template data exctractor method
  template <typename T> bool operator >> (T& dest) {
    if (GroupReply::has_failed_m && exception_enabled) {
      throw GroupReply::exception_m;
    }
    std::bitset<DeviceData::numFlags> bs;
    data_m.exceptions(exception_enabled ? bs.set() : bs.reset()); 
    bool result;
    try {
      result = data_m >> dest;
    }
    catch (const DevFailed& df) {
      GroupReply::exception_m = df;
      if (exception_enabled) {
        throw GroupReply::exception_m;
      }
      result = false;
    }
    catch (...) {
      Tango::DevErrorList errors(1);
		  errors.length(1);
		  errors[0].severity = Tango::ERR;
		  errors[0].desc = CORBA::string_dup("unknown exception caught");
		  errors[0].reason = CORBA::string_dup("an error occured while trying to extract data");
		  errors[0].origin = CORBA::string_dup("GroupCmdReply::operator>>");
      DevFailed df(errors);
      GroupReply::exception_m = df;
      if (exception_enabled) {
        throw GroupReply::exception_m;
      }
      result = false;
    }
    return result;
  }
  //- data exctractor method for DevVarLongStringArray
  bool extract (std::vector<long>& vl, std::vector<std::string>& vs);
  //- data exctractor method for DevVarDoubleStringArray
  bool extract (std::vector<double>& vd, std::vector<std::string>& vs);
private:
  //- data: valid if GroupReply::has_failed_m set to false
  DeviceData data_m;
};

//=============================================================================
// class GroupAttrReply : reply to an attribute reading executed on a group
//=============================================================================
class GroupAttrReply : public GroupReply
{
public:
  //- ctor
  GroupAttrReply ();
  //- ctor
  GroupAttrReply (const std::string& dev_name, 
                  const std::string& obj_name, 
                  const DeviceAttribute& data);
  //- ctor
  GroupAttrReply (const std::string& dev_name, 
                  const std::string& obj_name, 
                  const DevFailed& exception);
  //- dtor
  virtual ~GroupAttrReply ();
  //- data accessor (may throw Tango::DevFailed)
  /*const*/ DeviceAttribute& get_data (void);
  //- template data exctractor method
  template <typename T> bool operator >> (T& dest) {
    if (GroupReply::has_failed_m && exception_enabled) {
      throw GroupReply::exception_m;
    }
    std::bitset<DeviceAttribute::numFlags> bs;
    data_m.exceptions(exception_enabled ? bs.set() : bs.reset()); 
    bool result;
    try {
      result = data_m >> dest;
    }
    catch (const DevFailed& df) {
      GroupReply::exception_m = df;
      if (exception_enabled) {
        throw GroupReply::exception_m;
      }
      result = false;
    }
    catch (...) {
      Tango::DevErrorList errors(1);
		  errors.length(1);
		  errors[0].severity = Tango::ERR;
		  errors[0].desc = CORBA::string_dup("unknown exception caught");
		  errors[0].reason = CORBA::string_dup("an error occured while trying to extract data");
		  errors[0].origin = CORBA::string_dup("GroupAttrReply::operator>>");
      DevFailed df(errors);
      GroupReply::exception_m = df;
      if (exception_enabled) {
        throw GroupReply::exception_m;
      }
      result = false;
    }
    return result;
  }
private:
  //- data: valid if GroupReply::has_failed_m set to false
  DeviceAttribute data_m;
};

//=============================================================================
// class GroupReplyList : a simple list of GroupReply
//=============================================================================
class GroupReplyList : public std::vector<GroupReply>
{
  typedef std::vector<GroupReply> Inherited;

  friend class Group;

public:
  //- ctor
  GroupReplyList();
  //- dtor
  virtual ~GroupReplyList();
  //- has_failed accessor
  bool has_failed (void) const { 
    return has_failed_m;
  }
  //- reset the error list
  inline void reset (void) { 
    clear(); 
    has_failed_m = false;
  };
  //- push_back overload 
  void push_back (const GroupReply& r) {
    if (r.has_failed()) {
      has_failed_m = true;
    }
    Inherited::push_back(r);
  }
private:
  //- has_failed_m: true if at least one error occurred, false otherwise
  bool has_failed_m;
};

//=============================================================================
// class GroupCmdReplyList : a simple list of GroupCmdReply
//=============================================================================
class GroupCmdReplyList : public std::vector<GroupCmdReply>
{
  typedef std::vector<GroupCmdReply> Inherited;

  friend class Group;

public:
  //- ctor
  GroupCmdReplyList();
  //- dtor
  virtual ~GroupCmdReplyList();
  //- has_failed accessor method
  bool has_failed (void) const { 
    return has_failed_m;
  }
  //- reset the error list
  inline void reset (void) { 
    clear(); 
    has_failed_m = false;
  };
  //- push_back overload 
  void push_back (const GroupCmdReply& cr) {
    if (cr.has_failed()) {
      has_failed_m = true;
    }
    Inherited::push_back(cr);
  }
private:
  //- has_failed_m: true if at least one error occurred, false otherwise
  bool has_failed_m;
};

//=============================================================================
// class GroupAttrReplyList : a simple list of GroupAttrReply
//=============================================================================
class GroupAttrReplyList : public std::vector<GroupAttrReply>
{
  typedef std::vector<GroupAttrReply> Inherited;

  friend class Group;

public:
  //- ctor
  GroupAttrReplyList();
  //- dtor
  virtual ~GroupAttrReplyList();
  //- has_failed accessor method
  bool has_failed (void) const { 
    return has_failed_m;
  }
  //- reset the error list
  inline void reset (void) { 
    clear(); 
    has_failed_m = false;
  };
  //- push_back overload 
  void push_back (const GroupAttrReply& ar) {
    if (ar.has_failed()) {
      has_failed_m = true;
    }
    Inherited::push_back(ar);
  }
private:
  //- has_failed_m: true if at least one error occurred, false otherwise
  bool has_failed_m;
};

//=============================================================================
// class GroupElementFactory : a GroupElement factory
//=============================================================================
class GroupElementFactory 
{

  friend class Tango::Group;

  //- instanciatethe GroupElement which name matches the specified pattern
  static GroupElements instanciate (const std::string& p);

  //- forbidden methods
  GroupElementFactory();
  ~GroupElementFactory();
  GroupElementFactory& operator=(const GroupElementFactory& other);
};

//=============================================================================
// class GroupElement: base class for all tango group element
//=============================================================================
class GroupElement
{
public:
  //- Group management methods 
  //---------------------------------------------
  //- 
  virtual void add (const std::string& s);
  //-
  virtual void add (const std::vector<std::string>& sl);
  //-
  virtual void remove (const std::string& s, bool forward = true);
  //-
  virtual void remove (const std::vector<std::string>& sl, bool forward = true);
  //-
  virtual bool contains (const std::string& n, bool forward = true);
  //-
  virtual DeviceProxy* get_device (const std::string& n);
  //-
  virtual DeviceProxy* get_device (long idx);
  //-
  virtual DeviceProxy* operator[] (long idx);
  //-
  virtual Group* get_group (const std::string& n);
  //-

  //- Tango methods (~ DeviceProxy interface) 
  //---------------------------------------------
  //- 
  virtual bool ping (bool forward = true) = 0;
  //-
  virtual long command_inout_asynch (const std::string& c, bool forget = false, bool forward = true, long reserved = -1) = 0;
  //- 
  virtual long command_inout_asynch (const std::string& c,  const DeviceData& d, bool forget = false, bool forward = true, long reserved = -1) = 0;
  //- 
  virtual GroupCmdReplyList command_inout_reply (long req_id, long timeout_ms = 0) = 0;
  //-
  virtual long read_attribute_asynch (const std::string& a, bool forward = true, long reserved = -1) = 0;
  //-
  virtual GroupAttrReplyList read_attribute_reply (long req_id, long timeout_ms = 0) = 0;
  //-
  virtual long write_attribute_asynch (const DeviceAttribute& d, bool forward = true, long reserved = -1) = 0;
  //-
  virtual GroupReplyList write_attribute_reply (long req_id, long timeout_ms = 0) = 0;
  //-

  //- Misc.
  //---------------------------------------------
  //- 
  inline const std::string& get_name (void) const {
    return name;
  };
  //-
  inline const std::string get_fully_qualified_name (void) const {
    if (parent) {
      return parent->get_fully_qualified_name() + "." + name;
    } 
    return name;
  };
  //-
  inline bool name_equals (const std::string& n) {
    return n == name  || n == get_fully_qualified_name();
  }
  //-
  inline bool name_matches (const std::string& n) {
    TokenList tokens = tokenize(n);
    return match(name, tokens) || match(get_fully_qualified_name(), tokens);
  }
  //-
  virtual long get_size (bool forward = true) = 0;
  //- 
  virtual ~GroupElement();

  //- Some of the following public methods should be protected but C++ 
  //- does not allow to execute a protected method using an instance of 
  //- a parent class. Some others are defined for internal use. 
  //- DO NOT USE THEM (UNLESS YOU KNOW WHAT YOU ARE DOING).
  //---------------------------------------------------------------------
  //-
  virtual GroupElement* find (const std::string& n, bool forward = true);
  //-
  virtual GroupElement* get_parent (void) {
    return parent;
  };
  //-
  virtual GroupElement* set_parent (GroupElement* _parent) {
    GroupElement* previous_parent = parent;
    parent = _parent;
    return previous_parent;
  };
  //- 
  virtual bool is_device (void) = 0;
  //- 
  virtual bool is_group (void) = 0;
  //-
  virtual void dump (int indent_level = 0) = 0;
  //-
  virtual void dump (TangoSys_OMemStream& oms, int indent_level = 0) = 0;
  //-

protected:
  //- ctor: creates an GroupElement named <name>
  GroupElement (const std::string& name, GroupElement* parent = 0);
  //-
  TokenList tokenize (const std::string& p);
  //-
  bool match (const std::string& p, const TokenList& tokens);
  //-

private:
  //- element name
  std::string name;
  //- parent element 
  GroupElement* parent;
  //- forbidden methods
  GroupElement ();
  GroupElement (const GroupElement&);
  GroupElement& operator=(const GroupElement&);
};

//=============================================================================
// class Group: actual tango group implementation
//=============================================================================
class Group : public GroupElement
{
  typedef std::map<long, bool> AsynchRequestDesc;
  typedef AsynchRequestDesc::iterator AsynchRequestDescIt;
  typedef AsynchRequestDesc::value_type AsynchRequestDescVal;

public:

  //- Ctor & Dtor 
  //---------------------------------------------
  //- ctor: creates an empty group named <name>
  Group (const std::string& name);
  //- dtor: release resources
  virtual ~Group();

  //- Group management methods 
  //---------------------------------------------
  //- 
  virtual void add (Group* group);
  //-
  virtual void add (const std::string& pattern);
  //-
  virtual void add (const std::vector<std::string>& patterns);
  //-
  virtual void remove (const std::string& pattern, bool forward = true);
  //-
  virtual void remove (const std::vector<std::string>& patterns, bool forward = true);
  //-
  virtual void remove_all (void);
  //-
  virtual bool contains (const std::string& pattern, bool forward = true);
  //-
  virtual DeviceProxy* get_device (const std::string& device_name);
  //-
  virtual DeviceProxy* get_device (long idx);
  //-
  virtual DeviceProxy* operator[] (long idx);
  //-
  virtual Group* get_group (const std::string& group_name);

  //- Tango methods (~ DeviceProxy interface) 
  //---------------------------------------------
  //- 
  virtual bool ping (bool forward = true);
  //-
  GroupCmdReplyList command_inout (const std::string& c, bool forward = true);
  //-
  GroupCmdReplyList command_inout (const std::string& c, const DeviceData& d, bool forward = true);
  //-
  template<typename T> GroupCmdReplyList command_inout (const std::string& c, /*const*/ std::vector<T>& d, bool forward = true)
  {
    long id = command_inout_asynch(c, d, false, forward);
    return command_inout_reply(id);
  }
  //-
  virtual long command_inout_asynch (const std::string& c, bool forget = false, bool forward = true, long reserved = -1);
  //-
  virtual long command_inout_asynch (const std::string& c, const DeviceData& d, bool forget = false, bool forward = true, long reserved = -1);
  //-
  template<typename T> long command_inout_asynch (const std::string& c, /*const*/ std::vector<T>& d, bool forget = false, bool forward = true, long reserved = -1)
  {  
    #ifdef TANGO_GROUP_HAS_THREAD_SAFE_IMPL
      omni_mutex_lock guard(elements_mutex);
    #endif
    long gsize = get_size_i(forward);
    if (gsize != d.size()) {
      TangoSys_OMemStream desc;
		  desc << "the size of the input argument list must equal the number of device in the group" 
           << " [expected:" 
           << gsize 
           << " - got:" 
           << d.size()
           << "]"
           << ends;
      ApiDataExcept::throw_exception((const char*)"API_MethodArgument", 
                                     (const char*)desc.str().c_str(), 
                                     (const char*)"Group::command_inout_asynch");
    }
    if (reserved == -1) {
      reserved = next_req_id();
    }
    Tango::DeviceData dd;
    if (forward) 
    {
      GroupElements te = get_hiearchy();
      for (unsigned int i = 0; i < te.size(); i++) {
        dd << d[i];
        te[i]->command_inout_asynch(c, dd, forget, false, reserved);
      }
    } 
    else 
    {
      for (unsigned int i = 0, j = 0; i < elements.size(); i++) {
        if (elements[i]->is_device()) {
          dd << d[j++];
          elements[i]->command_inout_asynch(c, dd, forget, false, reserved);
        }
      }
    }
    if (forget == false) {
      push_async_request(reserved, forward);
    }
    return reserved;
  }
  //-
  virtual GroupCmdReplyList command_inout_reply (long req_id, long timeout_ms = 0);
  //-
  virtual GroupAttrReplyList read_attribute (const std::string& a, bool forward = true);
  //-
  virtual long read_attribute_asynch (const std::string& a, bool forward = true, long reserved = -1);
  //-
  virtual GroupAttrReplyList read_attribute_reply (long req_id, long timeout_ms = 0);
  //-
  virtual GroupReplyList write_attribute (const DeviceAttribute& d, bool forward = true);
  //-
  template<typename T> GroupReplyList write_attribute (const std::string& n, /*const*/ std::vector<T>& d, bool forward = true)
  {
    long id = write_attribute_asynch(n, d, forward);
    return write_attribute_reply(id);
  }
  //-
  virtual long write_attribute_asynch (const DeviceAttribute& d, bool forward = true, long reserved = -1);
  //-
  template<typename T> long write_attribute_asynch (const std::string& a, /*const*/ std::vector<T>& d, bool forward = true, long reserved = -1)
  {
    #ifdef TANGO_GROUP_HAS_THREAD_SAFE_IMPL
     omni_mutex_lock guard(elements_mutex);
    #endif
    GroupReplyList rl;
    long gsize = get_size_i(forward);
    if (gsize != d.size()) {
      TangoSys_OMemStream desc;
		  desc << "the size of the input argument list must equal the number of device in the group" 
           << " [expected:" 
           << gsize 
           << " - got:" 
           << d.size()
           << "]"
           << ends;
      ApiDataExcept::throw_exception((const char*)"API_MethodArgument", 
                                     (const char*)desc.str().c_str(), 
                                     (const char*)"Group::write_attribute_asynch");
    }
    if (reserved == -1) {
      reserved = next_req_id();
    }
    DeviceAttribute da(const_cast<string&>(a),  0.);
    if (forward) 
    {
      GroupElements te = get_hiearchy();
      for (unsigned int i = 0; i < te.size(); i++) {
        da << d[i];
        te[i]->write_attribute_asynch(da, false, reserved);
      }
    } 
    else 
    {
      for (unsigned int i = 0, j = 0; i < elements.size(); i++) {
        if (elements[i]->is_device()) {
          da << d[j++];
          elements[i]->write_attribute_asynch(da, false, reserved);
        }
      }
    }
    push_async_request(reserved, forward);
    return reserved;
  }
  //-
  virtual GroupReplyList write_attribute_reply (long req_id, long timeout_ms = 0);
  //-

  //- Misc.
  //---------------------------------------------
  //-
  std::vector<std::string> get_device_list (bool forward = true);

  virtual long get_size (bool forward = true);

  //- Some of the following public methods should be protected but C++ 
  //- does not allow to execute a protected method using an instance of 
  //- a parent class. Some others are defined for internal use. 
  //- DO NOT USE THEM (UNLESS YOU KNOW WHAT YOU ARE DOING).
  //---------------------------------------------------------------------
  //-
  virtual void dump (int indent_level = 0);
  //- 
  virtual void dump (TangoSys_OMemStream& oms, int indent_level = 0);
  //-
  virtual GroupElement* find (const std::string& n, bool forward = true);
  //- 
  virtual bool is_device (void);
  //- 
  virtual bool is_group (void);

private:
  //-
  long next_req_id (void);
  //-
  bool add_i (GroupElement* e, bool forward = true);
  //-
  void remove_i (const std::string& p, bool forward = true); 
  //-
  GroupElement* find_i (const std::string& n, bool forward = true);
  //-
  GroupElements get_hiearchy (void);
  //-
  Group* get_group_i (const std::string& n);
  //-
  long get_size_i (bool fwd);
  //- 
  void push_async_request (long rid, bool forwarded);
  //-
  void pop_async_request (long rid);
  //-
#ifdef TANGO_GROUP_HAS_THREAD_SAFE_IMPL
  omni_mutex elements_mutex;
#endif
  //- elements
  GroupElements elements;
  //- asynch request repository
  AsynchRequestDesc arp;
  //- pseudo asynch. req. id generator
  long asynch_req_id;
  //- forbidden methods
  Group ();
  Group (const Group&);
  Group& operator=(const Group&);
};

//=============================================================================
// class GroupDeviceElement: a device element
//=============================================================================
class GroupDeviceElement : public GroupElement
{
  friend class GroupElementFactory;

public:
  //-
  virtual DeviceProxy* get_device (const std::string& n);
  //-
  virtual DeviceProxy* get_device (long idx);
  //-
  virtual DeviceProxy* operator[] (long idx);
  //-
  virtual bool ping (bool forward = true);
  //-
  virtual long command_inout_asynch (const std::string& c, bool forget = false, bool forward = true, long reserved = -1);
  //-
  virtual long command_inout_asynch (const std::string& c, const DeviceData& d, bool forget = false, bool forward = true, long reserved = -1);
  //-
  virtual GroupCmdReplyList command_inout_reply (long req_id, long timeout_ms = 0);
  //-
  virtual long read_attribute_asynch (const std::string& a, bool forward = true, long reserved = -1);
  //-
  virtual GroupAttrReplyList read_attribute_reply (long req_id, long timeout_ms = 0);
  //-
  virtual long write_attribute_asynch (const DeviceAttribute& d, bool forward = true, long reserved = -1);
  //-
  virtual GroupReplyList write_attribute_reply (long req_id, long timeout_ms = 0);
  //-
  virtual void dump (int indent_level = 0);
  //-
  virtual void dump (TangoSys_OMemStream& oms, int indent_level = 0);
  //-
  virtual long get_size (bool forward = true);
  //-

  //- Some of the following public methods should be protected but C++ 
  //- does not allow to execute a protected method using an instance of 
  //- a parent class. Some others are defined for internal use. 
  //- DO NOT USE THEM (UNLESS YOU KNOW WHAT YOU ARE DOING).
  //---------------------------------------------------------------------
  //- 
  virtual bool is_device (void);
  //- 
  virtual bool is_group (void);

private:
  //- ctor: creates an GroupDeviceElement named <name>
  GroupDeviceElement (const std::string& name);
  //- dtor: release resources
  virtual ~GroupDeviceElement();
  //- build connection to the device (may throw DevFailed)
  DeviceProxy* connect (void);
  //- close connection
  void disconnect (void);
  //- a trick to get a valid device proxy or an exception
  inline DeviceProxy* dev_proxy (void) {
    return dp ? dp : connect();
  }
  //- the device proxy
  DeviceProxy *dp;
  //- asynch request repository
  AsynchRequestRep arp;
  //- Forbidden methods
  GroupDeviceElement ();
  GroupDeviceElement (const GroupDeviceElement&);
  GroupDeviceElement& operator=(const GroupDeviceElement&);
};

} // namespace Tango


#endif /* _GROUP_H_ */

