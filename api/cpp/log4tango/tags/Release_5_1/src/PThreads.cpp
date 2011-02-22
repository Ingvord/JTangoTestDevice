/*
 * PThreads.cpp
 *
 * See the COPYING file for the terms of usage and distribution.
 */

#include "PortabilityImpl.hh"
#include <log4tango/threading/Threading.hh>
#ifdef LOG4TANGO_HAVE_SSTREAM
# include <sstream>
#endif

#if defined(LOG4TANGO_HAVE_THREADING) && defined(LOG4TANGO_USE_PTHREADS)

namespace log4tango {

namespace threading {

//+----------------------------------------------------------------------------
// method : get_thread_id
//----------------------------------------------------------------------------- 
std::string get_thread_id (void)  
{
  std::ostringstream oss;
  oss << ::pthread_self();
  return oss.str();
}

//+----------------------------------------------------------------------------
// method : thread_id
//----------------------------------------------------------------------------- 
long thread_id (void) 
{
  return static_cast<long>(::pthread_self());
}

//+----------------------------------------------------------------------------
// method : RecursiveMutex::RecursiveMutex
//----------------------------------------------------------------------------- 
RecursiveMutex::RecursiveMutex (void)
 : recursion_level_(0), owner_id_(0)
{
  ::pthread_mutex_init(&guard_, NULL);
#if (PthreadDraftVersion == 4)
  ::pthread_cond_init(&mutex_available_, pthread_condattr_default);
#else
  ::pthread_cond_init(&mutex_available_, 0);
#endif
}

//+----------------------------------------------------------------------------
// method : RecursiveMutex::~RecursiveMutex
//----------------------------------------------------------------------------- 
RecursiveMutex::~RecursiveMutex (void)
{
  ::pthread_cond_destroy(&mutex_available_);
  ::pthread_mutex_destroy(&guard_);
}

//+----------------------------------------------------------------------------
// method : RecursiveMutex::lock
//----------------------------------------------------------------------------- 
int RecursiveMutex::lock (long timeout)
{
  // Get current thread id
  pthread_t t_id = ::pthread_self();

  // Lock the guard
  ::pthread_mutex_lock(&guard_);

  // Is <this> recursive_mutex free?
  if (recursion_level_ == 0) {
    // Yes it is, get ownership
    owner_id_ = t_id;
  }
  else if (t_id != owner_id_) {
    // No it isn't and <t_id> is not the current owner.
    // Wait until the nesting level has dropped to zero,
    // at which point we can get ownership
    while (recursion_level_ > 0) {
      // Note: <guard_> is unlocked by following call
      if (timeout) {
        //--TODO::a la omnithread timedwait impl (see Duncan's code)
        ::pthread_cond_wait(&mutex_available_, &guard_);
      } else {
        ::pthread_cond_wait(&mutex_available_, &guard_);
      }
    }
    // At this point the <guard_> is held and <this> 
    // recursive_mutex free. Get ownership ...
    owner_id_ = t_id;
  }
    
  // At this point, we can safely increment the recursion 
  // level no matter how we got here!
  recursion_level_++;
  
  // Unlock the guard
  ::pthread_mutex_unlock(&guard_);

  // Return true (success - no timeout on wait)
  return 1;
}
  
//+----------------------------------------------------------------------------
// method : RecursiveMutex::unlock
//----------------------------------------------------------------------------- 
void RecursiveMutex::unlock(void)
{
  // Get current thread id
  pthread_t t_id = ::pthread_self();

  // Lock the guard
  ::pthread_mutex_lock(&guard_);
  
  // <t_id> must be the current owner
  if (recursion_level_ == 0 || (t_id != owner_id_)) {
    ::pthread_mutex_unlock(&guard_);
    return;
  }

  // Decrement the recursion level
  recursion_level_--;

  // Is <this> recursive_mutex free now? 
  if (recursion_level_ == 0) {
    // Release ownership
    owner_id_ = 0;
    // Inform a waiter
    ::pthread_cond_signal(&mutex_available_);
  }

  // Unlock the guard
  ::pthread_mutex_unlock(&guard_);
}

//+----------------------------------------------------------------------------
// method : RecursiveMutex::unlockn
//----------------------------------------------------------------------------- 
void RecursiveMutex::unlockn (void)
{
  // Get current thread id
  pthread_t t_id = ::pthread_self();

  // Lock the guard
  ::pthread_mutex_lock(&guard_);

  // <t_id> must be the current owner
  if (recursion_level_ == 0 || (t_id != owner_id_)) {
    ::pthread_mutex_unlock(&guard_);
    return;
  }

  // Reset the recursion level
  recursion_level_ = 0;
  
  // Release ownership
  owner_id_ = 0;

  // Inform a waiter 
  ::pthread_cond_signal(&mutex_available_);

  // Unlock the guard
  ::pthread_mutex_unlock(&guard_);
}

} // namespace threading

} // namespace log4tango

#endif // LOG4TANGO_HAVE_THREADING && LOG4TANGO_USE_PTHREADS
