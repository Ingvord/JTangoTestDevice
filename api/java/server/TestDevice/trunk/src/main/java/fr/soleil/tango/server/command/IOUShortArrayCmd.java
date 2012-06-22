package fr.soleil.tango.server.command;

import org.tango.DeviceState;
import org.tango.server.StateMachineBehavior;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class IOUShortArrayCmd implements ICommandBehavior {

    @Override
    public CommandConfiguration getConfiguration() throws DevFailed {
	final CommandConfiguration config = new CommandConfiguration();
	config.setName("IOUShortArray");
	config.setInTangoType(TangoConst.Tango_DEVVAR_USHORTARRAY);
	config.setOutTangoType(TangoConst.Tango_DEVVAR_USHORTARRAY);
	return config;
    }

    @Override
    public Object execute(final Object arg) throws DevFailed {
	final short[] val = (short[]) arg;
	final int size = val.length;
	final short[] r = new short[size];
	for (int i = 0; i < size; i++) {
	    r[i] = (short) (val[i] * 2);
	}
	return r;
    }

    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	final StateMachineBehavior state = new StateMachineBehavior();
	state.setDeniedStates(DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM);
	return state;
    }

}
