package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class ULong64Attr implements IAttributeBehavior {

    private long uLongattrrw = 200;

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("ULong64_attr");
	config.setWritable(AttrWriteType.READ);
	config.setTangoType(TangoConst.Tango_DEV_ULONG64, AttrDataFormat.SCALAR);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return new AttributeValue(uLongattrrw);
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	uLongattrrw = (Long) value.getValue();
    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

}
