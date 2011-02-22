/* 
 * example of a client using the TANGO device api.
 */

#include <tango.h>
#include <assert.h>

#define	coutv	if (verbose == true) cout

using namespace Tango;

int main(int argc, char **argv)
{
	DeviceProxy *device;
	bool verbose = false;
	
	if (argc == 1)
	{
		cout << "usage: %s device [-v]" << endl;
		exit(-1);
	}

	string device_name = argv[1];

	if (argc == 3)
	{
		if (strcmp(argv[2],"-v") == 0)
			verbose = true;
	}
	
	try 
	{
		device = new DeviceProxy(device_name);
	}
        catch (CORBA::Exception &e)
        {
              	Except::print_exception(e);
		exit(1);
        }

	coutv << endl << "new DeviceProxy(" << device->name() << ") returned" << endl << endl;

	try
	{
	
// Change timeout (with a useless name as a workaround for ORBacus bug)

//		device->set_timeout_millis(6000);
				
// Write one attribute

		long id;
		
		DeviceAttribute send;
		
		send.set_name("attr_asyn_write");
		long lg = 222;
		send << lg;
		
		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		bool finish = false;
		long nb_not_arrived = 0;
		while (finish == false)
		{
			try
			{
				device->write_attribute_reply(id);
				finish = true;
			}
			catch (AsynReplyNotArrived )
			{
				finish = false;
				coutv << "Attribute not yet written" << endl;
				nb_not_arrived++;
			}
			if (finish == false)
				Tango_sleep(1);
		}
		
		assert ( nb_not_arrived >= 2);
		
		cout << "   Asynchronous write_attribute in polling mode --> OK" << endl;

// Write attribute to check polling with blocking with timeout
		
		id = device->write_attribute_asynch(send);
//		assert( id == 2);
		
// Check if attribute returned

		finish = false;
		nb_not_arrived = 0;
		while (finish == false)
		{
			try
			{
				device->write_attribute_reply(id,200);
				finish = true;
			}
			catch (AsynReplyNotArrived )
			{
				coutv << "Attribute not yet written" << endl;
				nb_not_arrived++;
			}
		}
		
		assert ( nb_not_arrived >= 4);

		cout << "   Asynchronous write_attribute in blocking mode with call timeout --> OK" << endl;
		
// Write an attribute to check polling with blocking

		id = device->write_attribute_asynch(send);
//		assert( id == 3);
		
// Check if attribute returned

		device->write_attribute_reply(id,0);
		
		cout << "   Asynchronous write_attribute in blocking mode --> OK" << endl;

//---------------------------------------------------------------------------
//
//			Now test Timeout exception and asynchronous calls
//
//---------------------------------------------------------------------------
		
// Change timeout in order to test asynchronous calls and timeout

//		device->set_timeout_millis(2000);

// Write an attribute

		send.set_name("attr_asyn_write_to");
		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		finish = false;
		bool to = false;
		nb_not_arrived = 0;
		while (finish == false)
		{
			try
			{
				device->write_attribute_reply(id);
				finish = true;
			}
			catch (AsynReplyNotArrived )
			{
				finish = false;
				nb_not_arrived++;
				coutv << "Attribute not yet written" << endl;
			}
			catch (CommunicationFailed &e)
			{
				finish = true;
				if (strcmp(e.errors[1].reason,"API_DeviceTimedOut") == 0)
				{
					to = true;
					coutv << "Timeout exception" << endl;
				}
				else
					coutv << "Comm exception" << endl;
			}
			if (finish == false)
				Tango_sleep(1);
		}
		assert ( to == true );
		assert ( nb_not_arrived >= 2 );
		
		cout << "   Device timeout exception with non blocking write_attribute_reply --> OK" << endl;
		
// Write an attribute to check timeout with polling and blocking with timeout

		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		finish = false;
		to = false;
		nb_not_arrived = 0;
		while (finish == false)
		{
			try
			{
				device->write_attribute_reply(id,500);
				finish = true;
			}
			catch (AsynReplyNotArrived )
			{
				coutv << "Attribute not yet written" << endl;
				nb_not_arrived++;
			}
			catch (CommunicationFailed &e)
			{
				finish = true;
				if (strcmp(e.errors[1].reason,"API_DeviceTimedOut") == 0)
				{
					to = true;
					coutv << "Timeout exception" << endl;
				}
				else
					coutv << "Comm exception" << endl;
			}
		}
		assert( to == true );
		assert( nb_not_arrived >= 2);
		
		cout << "   Device timeout with blocking write_attribute_reply with call timeout --> OK" << endl;
		
// Write an attribute to check polling with blocking

		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		to = false;
		try
		{
			device->write_attribute_reply(id,0);
			finish = true;
		}
		catch (CommunicationFailed &e)
		{
			if (strcmp(e.errors[1].reason,"API_DeviceTimedOut") == 0)
			{
				to = true;
				coutv << "Timeout exception" << endl;
			}
			else
				coutv << "Comm exception" << endl;
		}
		assert(to == true );
		
		cout << "   Device timeout with blocking write_attribute_reply --> OK" << endl;

//---------------------------------------------------------------------------
//
//			Now test DevFailed exception sent by server
//
//---------------------------------------------------------------------------

		cout << "   Waiting for server to execute all previous requests" << endl;
		Tango_sleep(5);

// Change timeout in order to test asynchronous calls and DevFailed exception

//		device->set_timeout_millis(5000);

// Write attribute

		send.set_name("attr_asyn_write_except");

		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		finish = false;
		bool failed = false;
		nb_not_arrived = 0;
		while (finish == false)
		{
			try
			{
				device->write_attribute_reply(id);
				finish = true;
			}
			catch (AsynReplyNotArrived )
			{
				finish = false;
				nb_not_arrived++;
				coutv << "Attribute not yet written" << endl;
			}
			catch (DevFailed &e)
			{
				finish = true;
				if (strcmp(e.errors[0].reason,"aaa") == 0)
				{
					failed = true;
					coutv << "Server exception" << endl;
				}
				else
					coutv << "Comm exception" << endl;
			}
			if (finish == false)
				Tango_sleep(1);
		}
		assert ( failed == true );
		assert ( nb_not_arrived >= 2);
		
		cout << "   Device exception with non blocking write_attribute_reply --> OK" << endl;
		
// Write an attribute to check timeout with polling and blocking with timeout

		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		finish = false;
		failed = false;
		
		while (finish == false)
		{
			try
			{
				device->write_attribute_reply(id,200);
				finish = true;
			}
			catch (AsynReplyNotArrived )
			{
				coutv << "Attribute not yet written" << endl;
			}
			catch (DevFailed &e)
			{
				finish = true;
				if (strcmp(e.errors[0].reason,"aaa") == 0)
				{
					failed = true;
					coutv << "Server exception" << endl;
				}
				else
					coutv << "Comm exception" << endl;
			}
		}
		assert( failed == true );
		
		cout << "   Device exception with blocking write_attribute_reply with call timeout --> OK" << endl;
		
// Write an attribute to check polling with blocking

		id = device->write_attribute_asynch(send);
		
// Check if attribute returned

		failed = false;
		try
		{
			device->write_attribute_reply(id,0);
			finish = true;
		}
		catch (DevFailed &e)
		{
			finish = true;
			if (strcmp(e.errors[0].reason,"aaa") == 0)
			{
				failed = true;
				coutv << "Server exception" << endl;
			}
			else
				coutv << "Comm exception" << endl;
		}
		
		assert(failed == true );
				
		cout << "   Device exception with blocking write_attribute_reply --> OK" << endl;
										
	}
	catch (Tango::DevFailed &e)
	{
		Except::print_exception(e);
		exit(-1);
	}
	catch (CORBA::Exception &ex)
	{
		Except::print_exception(ex);
		exit(-1);
	}
		
	delete device;
	
	return 0;
}
