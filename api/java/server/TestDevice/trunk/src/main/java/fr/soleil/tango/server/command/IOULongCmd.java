package fr.soleil.tango.server.command;

import org.tango.DeviceState;
import org.tango.server.StateMachineBehavior;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class IOULongCmd implements ICommandBehavior {

    @Override
    public CommandConfiguration getConfiguration() throws DevFailed {
	// TODO @Command(name = "IOULong", inTypeDesc = "Number", outTypeDesc = "Number * 2")
	final CommandConfiguration config = new CommandConfiguration();
	config.setName("IOULong");
	config.setInTangoType(TangoConst.Tango_DEV_ULONG);
	config.setOutTangoType(TangoConst.Tango_DEV_ULONG);
	return config;
    }

    @Override
    public Object execute(final Object arg) throws DevFailed {
	final int val = (Integer) arg;
	final int r = val * 2;
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
