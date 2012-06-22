package fr.soleil.tango.server.command;

import org.tango.server.StateMachineBehavior;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class IOSeqVecULongCmd implements ICommandBehavior {

    @Override
    public CommandConfiguration getConfiguration() throws DevFailed {
	final CommandConfiguration config = new CommandConfiguration();
	config.setName("IOSeqVecULong");
	config.setInTangoType(TangoConst.Tango_DEVVAR_ULONGARRAY);
	config.setOutTangoType(TangoConst.Tango_DEVVAR_ULONGARRAY);
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
