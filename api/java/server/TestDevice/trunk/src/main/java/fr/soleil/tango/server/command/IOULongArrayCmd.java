package fr.soleil.tango.server.command;

import org.tango.server.StateMachineBehavior;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

/**
 * 
 @Command(name = "IOULongArray", inTypeDesc = "Array of unsigned long", outTypeDesc = "This array * 2")
 * 
 * @author ABEILLE
 * 
 */
public class IOULongArrayCmd implements ICommandBehavior {

    @Override
    public CommandConfiguration getConfiguration() throws DevFailed {
	final CommandConfiguration config = new CommandConfiguration();
	config.setName("IOULongArray");
	config.setInTangoType(TangoConst.Tango_DEVVAR_ULONGARRAY);
	config.setOutTangoType(TangoConst.Tango_DEVVAR_ULONGARRAY);
	return config;
    }

    @Override
    public Object execute(final Object arg) throws DevFailed {
	final int[] val = (int[]) arg;
	final int size = val.length;
	final int[] r = new int[size];
	for (int i = 0; i < size; i++) {
	    r[i] = val[i] * 2;
	}
	return r;
    }

    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	final StateMachineBehavior state = new StateMachineBehavior();
	// state.setDeniedStates(DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM);
	return state;
    }

}
