package fr.soleil.tango.server.attribute;

import java.util.Arrays;

import org.tango.server.StateMachineBehavior;
import org.tango.server.attribute.AttributeConfiguration;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.attribute.IAttributeBehavior;
import org.tango.server.attribute.ISetValueUpdater;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

public class PollbuffRW implements IAttributeBehavior, ISetValueUpdater {

    private String[] pollbuffRWRead = new String[0];
    private int dimX = 0;
    private int dimY = 0;
    private String[] pollbuffRWWrite = new String[0];
    private int dimXW = 0;
    private int dimYW = 0;

    @Override
    public AttributeConfiguration getConfiguration() throws DevFailed {
	final AttributeConfiguration config = new AttributeConfiguration();
	config.setName("Poll_buffRW");
	config.setTangoType(TangoConst.Tango_DEV_STRING, AttrDataFormat.IMAGE);
	config.setWritable(AttrWriteType.READ_WRITE);
	return config;
    }

    @Override
    public AttributeValue getValue() throws DevFailed {
	return new AttributeValue(pollbuffRWRead, AttrQuality.ATTR_VALID, dimX, dimY);
    }

    @Override
    public void setValue(final AttributeValue value) throws DevFailed {
	pollbuffRWWrite = (String[]) value.getValue();

    }

    @Override
    public StateMachineBehavior getStateMachine() throws DevFailed {
	return null;
    }

    public String[] getPollbuffRWRead() {
	return pollbuffRWRead;
    }

    public void setPollbuffRWRead(final String[] pollbuffRWRead, final int dimX, final int dimY) {
	this.pollbuffRWRead = Arrays.copyOf(pollbuffRWRead, pollbuffRWRead.length);
	this.dimX = dimX;
	this.dimY = dimY;
    }

    public String[] getPollbuffRWWrite() {
	return pollbuffRWWrite;
    }

    public void setPollbuffRWWrite(final String[] pollbuffRWWrite, final int dimX, final int dimY) {
	this.pollbuffRWWrite = Arrays.copyOf(pollbuffRWWrite, pollbuffRWWrite.length);
	dimXW = dimX;
	dimYW = dimY;
    }

    @Override
    public AttributeValue getSetValue() throws DevFailed {
	return new AttributeValue(pollbuffRWWrite, AttrQuality.ATTR_VALID, dimXW, dimYW);
    }
}
