package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class ULongAttr implements IAttributeBehavior {

    private int uLongattrrw = 100;

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("ULong_attr");
	config.setWritable(AttrWriteType.READ);
	config.setTangoType(TangoConst.Tango_DEV_ULONG, AttrDataFormat.SCALAR);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return new AttributeValue(uLongattrrw);
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	uLongattrrw = (Integer) value.getValue();

    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

}