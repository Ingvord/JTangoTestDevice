package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;
import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;

public class DoubleAttrW implements IAttributeBehavior {

    private double doubleValue = 0;

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("Double_attr_w");
	config.setType(double.class);
	config.setFormat(AttrDataFormat.SCALAR);
	config.setWritable(AttrWriteType.WRITE);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return new AttributeValue(doubleValue);
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	doubleValue = (Double) value.getValue();
	if (Double.isNaN(doubleValue)) {
	    doubleValue = 0;
	    DevFailedUtils.throwDevFailed("Nan not allowed");
	}
    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

    public double getDoubleValue() {
	return doubleValue;
    }

    public void setDoubleValue(final double doubleValue) {
	this.doubleValue = doubleValue;
    }

}
