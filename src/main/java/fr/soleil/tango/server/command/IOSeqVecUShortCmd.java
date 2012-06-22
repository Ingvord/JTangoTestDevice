package fr.soleil.tango.server.command;

import org.tango.server.StateMachineBehavior;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

/**
 * Execute command "IOSeqVecUShort". <br>
 * description:
 * 
 * @param iOSeqVecUShortIn
 *            DevVarUShortArray, Input unsigned short array
 * @return DevVarUShortArray, Output unsigned short array
 */
public class IOSeqVecUShortCmd implements ICommandBehavior {

    @Override
    public CommandConfiguration getConfiguration() throws DevFailed {
	final CommandConfiguration config = new CommandConfiguration();
	config.setName("IOSeqVecUShort");
	config.setInTangoType(TangoConst.Tango_DEVVAR_USHORTARRAY);
	config.setOutTangoType(TangoConst.Tango_DEVVAR_USHORTARRAY);
	return config;
    }

    @Override
    public Object execute(final Object arg) throws DevFailed {
	return arg;
    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

}
