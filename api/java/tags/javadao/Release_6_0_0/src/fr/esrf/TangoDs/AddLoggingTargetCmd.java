//+============================================================================
//
// file :               AddLoggingTargetCmd.java
//
// description :        Java source code for the command AddLoggingTarget.
//			This is a DServerClass TANGO command. An instance of this class is 
//      automatically attached to the DServer instance at startup.
//
// project :            TANGO
//
// author(s) :          N.leclercq
//
// copyleft :           European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
//-============================================================================

package fr.esrf.TangoDs;

import fr.esrf.Tango.DevFailed;
import org.omg.CORBA.Any;

public class AddLoggingTargetCmd extends Command implements TangoConst
{
 /**
  * AddLoggingTargetCmd ctor
  */
	public AddLoggingTargetCmd(String name, int in, int out, String in_desc)
	{
		super(name, in, out);
    set_in_type_desc(in_desc);
	}

 /**
  * Executes the AddLoggingTargetCmd TANGO command
  */
	public Any execute(DeviceImpl device, Any in_any) throws DevFailed
	{
		Util.out4.println("AddLoggingTargetCmd::execute(): arrived");
    
    String[] dvsa = null;
		try {
      dvsa = extract_DevVarStringArray(in_any);
		}
		catch (DevFailed df) {
			Util.out3.println("AddLoggingTargetCmd::execute() --> Wrong argument type");
			Except.re_throw_exception(df,
                                "API_IncompatibleCmdArgumentType",
                                "Imcompatible command argument type, expected type is : DevVarStringArray",
                                "AddLoggingTargetCmd.execute");
		}
    
    Logging.instance().add_logging_target(dvsa);
    
    return Util.return_empty_any("AddLoggingTarget");
	}

}