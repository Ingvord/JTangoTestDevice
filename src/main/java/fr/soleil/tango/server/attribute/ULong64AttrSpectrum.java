package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class ULong64AttrSpectrum implements IAttributeBehavior {

    private final AttributeValue val = new AttributeValue();

    public ULong64AttrSpectrum() throws DevFailed {
	val.setValue(new long[] { 8888, 88888, 888888 });
    }

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("ULong64_spec_attr_rw");
	config.setWritable(AttrWriteType.READ_WRITE);
	config.setTangoType(TangoConst.Tango_DEV_ULONG64, AttrDataFormat.SPECTRUM);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return val;
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	// val = value;
    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

}
