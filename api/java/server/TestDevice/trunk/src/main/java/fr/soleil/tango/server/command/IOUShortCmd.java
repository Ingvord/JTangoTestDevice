package fr.soleil.tango.server.command;

import org.tango.DeviceState;
import org.tango.server.StateMachineBehavior;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class IOUShortCmd implements ICommandBehavior {

    @Override
    public CommandConfiguration getConfiguration() throws DevFailed {
	final CommandConfiguration config = new CommandConfiguration();
	config.setName("IOUShort");
	config.setInTangoType(TangoConst.Tango_DEV_USHORT);
	config.setOutTangoType(TangoConst.Tango_DEV_USHORT);
	return config;
    }

    @Override
    public Object execute(final Object arg) throws DevFailed {
	final short val = (Short) arg;
	final short r = (short) (val * 2);
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
