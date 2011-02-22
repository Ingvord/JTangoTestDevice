//+============================================================================
//
// file :               GetLoggingLevelCmd.java
//
// description :        Java source code for the command GetLoggingLevel.
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

public class GetLoggingLevelCmd extends Command implements TangoConst
{
 /**
  * GetLoggingLevelCmd ctor
  */
	public GetLoggingLevelCmd(String name, int in, int out, String in_desc, String out_desc)
	{
		super(name, in, out);
    set_in_type_desc(in_desc);
    set_out_type_desc(out_desc);
	}

 /**
  * Executes the GetLoggingLevelCmd TANGO command
  */
	public Any execute(DeviceImpl device, Any in_any) throws DevFailed
	{
		Util.out4.println("GetLoggingLevelCmd::execute(): arrived");
    
    String[] dvsa = null;
		try {
      dvsa = extract_DevVarStringArray(in_any);
		}
		catch (DevFailed df) {
			Util.out3.println("GetLoggingLevelCmd::execute() --> Wrong argument type");
			Except.re_throw_exception(df,
                                "API_IncompatibleCmdArgumentType",
                                "Imcompatible command argument type, expected type is : DevVarStringArray",
                                "GetLoggingLevelCmd.execute");
		}
    
    Any out_any = insert(Logging.instance().get_logging_level(dvsa));

		Util.out4.println("Leaving GetLoggingLevelCmd.execute()");	
    
    return out_any;
	}

}
