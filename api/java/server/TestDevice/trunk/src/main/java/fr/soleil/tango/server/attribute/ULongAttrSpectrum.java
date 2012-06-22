package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class ULongAttrSpectrum implements IAttributeBehavior {

    private final AttributeValue val = new AttributeValue();

    public ULongAttrSpectrum() throws DevFailed {
	val.setValue(new int[] { 2222, 22222, 222222 });
    }

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("ULong_spec_attr_rw");
	config.setWritable(AttrWriteType.READ_WRITE);
	config.setTangoType(TangoConst.Tango_DEV_ULONG, AttrDataFormat.SPECTRUM);
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
