package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributePropertiesImpl;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;

public class DevTestDynamicAttribute implements IAttributeBehavior {

    private AttributeValue value = new AttributeValue();

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("Added_short_attr");
	config.setWritable(AttrWriteType.READ);
	config.setType(short.class);
	final AttributePropertiesImpl props = new AttributePropertiesImpl();
	props.setDescription("Test description");
	props.setLabel("From db (class)");
	props.setFormat("From db (device)");
	config.setAttributeProperties(props);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return value;
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	this.value = value;

    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

}
