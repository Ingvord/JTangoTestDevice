package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class UShortAttrSpectrum implements IAttributeBehavior {

    private AttributeValue val = new AttributeValue();

    public UShortAttrSpectrum() throws DevFailed {
	val.setValue(new short[] { 333, 444 });
    }

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("UShort_spec_attr");
	config.setWritable(AttrWriteType.READ);
	config.setTangoType(TangoConst.Tango_DEV_USHORT, AttrDataFormat.SPECTRUM);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return val;
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	val = value;
    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

}
