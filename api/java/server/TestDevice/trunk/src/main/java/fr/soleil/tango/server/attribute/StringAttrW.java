package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;

public class StringAttrW implements IAttributeBehavior {

    private String stringValue = "";

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("String_attr_w");
	config.setType(String.class);
	config.setFormat(AttrDataFormat.SCALAR);
	config.setWritable(AttrWriteType.WRITE);
	config.setMemorized(true);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return new AttributeValue(stringValue);
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	stringValue = (String) value.getValue();
    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

    public String getStringValue() {
	return stringValue;
    }

    public void setStringValue(final String stringValue) {
	this.stringValue = stringValue;
    }
}
