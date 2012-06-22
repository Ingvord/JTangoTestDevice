package fr.soleil.tango.server.attribute;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributePropertiesImpl;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;

import fr.esrf.Tango.ArchiveEventProp;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.ChangeEventProp;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.EventProperties;
import fr.esrf.Tango.PeriodicEventProp;

/**
 * Attribute Long_attr_w, DevLong, Scalar, WRITE<br>
 * description: Test description
 */
public class LongAttrW implements IAttributeBehavior {

    private int longattrw = 0;

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("Long_attr_w");
	config.setType(int.class);
	config.setWritable(AttrWriteType.WRITE);
	final AttributePropertiesImpl props = new AttributePropertiesImpl();
	props.setDescription("Test description");
	props.setLabel("Test label");
	props.setDisplayUnit("Et ta soeur");
	props.setStandardUnit("100");
	props.setUnit("Kilogramme");
	props.setFormat("Tres long");
	props.setMinAlarm("1");
	props.setMaxAlarm("99999");
	props.setMinValue("0");
	props.setMaxValue("100000");
	props.setMinWarning("2");
	props.setMaxWarning("99998");
	props.setDeltaVal("10000");
	props.setDeltaT("1");
	final ChangeEventProp changeEvtProp = new ChangeEventProp("20000", "30000", new String[0]);
	final PeriodicEventProp periodicEvtProp = new PeriodicEventProp("2000", new String[0]);
	final ArchiveEventProp archiEvtProp = new ArchiveEventProp("22222", "33333", "3000", new String[0]);
	final EventProperties evtProps = new EventProperties(changeEvtProp, periodicEvtProp, archiEvtProp);
	props.setEventProp(evtProps);
	config.setAttributeProperties(props);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return new AttributeValue(longattrw);
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	longattrw = (Integer) value.getValue();

    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

    public int getLongattrw() {
	return longattrw;
    }

    public void setLongattrw(final int longattrw) {
	this.longattrw = longattrw;
    }

}
