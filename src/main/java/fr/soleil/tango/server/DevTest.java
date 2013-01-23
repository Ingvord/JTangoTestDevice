package fr.soleil.tango.server;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.tango.DeviceState;
import org.tango.server.InvocationContext;
import org.tango.server.ServerManager;
import org.tango.server.annotation.AroundInvoke;
import org.tango.server.annotation.Attribute;
import org.tango.server.annotation.AttributeProperties;
import org.tango.server.annotation.Command;
import org.tango.server.annotation.Delete;
import org.tango.server.annotation.Device;
import org.tango.server.annotation.DeviceManagement;
import org.tango.server.annotation.DeviceProperty;
import org.tango.server.annotation.DynamicManagement;
import org.tango.server.annotation.Init;
import org.tango.server.annotation.State;
import org.tango.server.annotation.StateMachine;
import org.tango.server.attribute.AttributeValue;
import org.tango.server.cache.TangoCacheManager;
import org.tango.server.device.DeviceManager;
import org.tango.server.dynamic.DynamicManager;
import org.tango.utils.DevFailedUtils;

import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.DevEncoded;
import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevVarDoubleStringArray;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.tango.server.attribute.DevTestDynamicAttribute;
import fr.soleil.tango.server.attribute.DoubleAttrW;
import fr.soleil.tango.server.attribute.LongAttrW;
import fr.soleil.tango.server.attribute.PollbuffRW;
import fr.soleil.tango.server.attribute.StringAttrW;
import fr.soleil.tango.server.attribute.UCharAttr;
import fr.soleil.tango.server.attribute.ULong64Attr;
import fr.soleil.tango.server.attribute.ULong64AttrRW;
import fr.soleil.tango.server.attribute.ULong64AttrSpectrum;
import fr.soleil.tango.server.attribute.ULongAttr;
import fr.soleil.tango.server.attribute.ULongAttrRW;
import fr.soleil.tango.server.attribute.ULongAttrSpectrum;
import fr.soleil.tango.server.attribute.UShortAttr;
import fr.soleil.tango.server.attribute.UShortAttrSpectrum;
import fr.soleil.tango.server.attribute.UShortAttrW;
import fr.soleil.tango.server.attribute.UShortImageAttr;
import fr.soleil.tango.server.attribute.UShortImageAttrW;
import fr.soleil.tango.server.attribute.UShortSpecAttrW;
import fr.soleil.tango.server.command.IOSeqVecULongCmd;
import fr.soleil.tango.server.command.IOSeqVecUShortCmd;
import fr.soleil.tango.server.command.IOULongArrayCmd;
import fr.soleil.tango.server.command.IOULongCmd;
import fr.soleil.tango.server.command.IOUShortArrayCmd;
import fr.soleil.tango.server.command.IOUShortCmd;

/**
 * A Tango device to run test_suite for ESRF
 * 
 * @author $Author$
 * @version $Revision$
 */
@Device(deviceType = "TestDevice")
public class DevTest {

    private static final DeviceState[] deviceStates = DeviceState.values();
    private static final Logger logger = LoggerFactory.getLogger(DevTest.class);

    /* PROTECTED REGION ID(DevTest.static) ENABLED START */
    // put static variables here
    /* PROTECTED REGION END */

    private static final XLogger xlogger = XLoggerFactory.getXLogger(DevTest.class);

    /**
     * Starts the server.
     */
    public static void main(final String[] args) {
	ServerManager.getInstance().addClass(DevTest.class.getSimpleName(), DevTest.class);
	ServerManager.getInstance().start(args, DevTest.class.getSimpleName());
    }

    @DeviceManagement
    private DeviceManager deviceManager;

    @DeviceProperty(name = "tst_property")
    private String testProperty;

    /**
     * Attribute attr_asyn, DevDouble, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_asyn")
    private final double attrasyn = 0;

    /**
     * Attribute attr_asyn_except, DevDouble, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_asyn_except")
    private final double attrasynexcept = 0;

    /**
     * Attribute attr_asyn_to, DevDouble, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_asyn_to")
    private final double attrasynto = 0;

    /**
     * Attribute attr_asyn_write, DevLong, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "attr_asyn_write")
    private int attrasynwrite = 0;

    /**
     * Attribute attr_asyn_write_except, DevLong, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "attr_asyn_write_except")
    private int attrasynwriteexcept = 0;

    /**
     * Attribute attr_asyn_write_to, DevLong, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "attr_asyn_write_to")
    private int attrasynwriteto = 0;

    /**
     * Attribute attr_dq_db, DevDouble, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_dq_db")
    private final double attrdqdb = 0;

    /**
     * Attribute attr_dq_lo, DevLong, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_dq_lo")
    private final int attrdqlo = 0;

    /**
     * Attribute attr_dq_sh, DevShort, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_dq_sh")
    private final short attrdqsh = 0;

    /**
     * Attribute attr_dq_str, DevString, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_dq_str")
    private final String attrdqstr = "";
    /**
     * Attribute attr_no_alarm, DevLong, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_no_alarm")
    private final int attrnoalarm = 0;

    /**
     * Attribute attr_no_data, DevShort, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_no_data")
    private final short attrnodata = 0;

    /**
     * Attribute attr_wrong_size, DevLong, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_wrong_size")
    private final int attrwrongsize = 0;

    /**
     * Attribute attr_wrong_type, DevShort, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "attr_wrong_type")
    private final short attrwrongtype = 0;

    /**
     * Attribute Boolean_attr, DevBoolean, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Boolean_attr")
    private final boolean booleanattr = true;

    /**
     * Attribute Boolean_attr_w, DevBoolean, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "Boolean_attr_w", isMemorized = true)
    private boolean booleanattrw = false;

    /**
     * Attribute Boolean_ima_attr_rw, DevBoolean, Image, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Boolean_ima_attr_rw")
    private final boolean[][] booleanimaattrrw = new boolean[][] { { true, false } };

    /**
     * Attribute Boolean_spec_attr, DevBoolean, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Boolean_spec_attr")
    private final boolean[] booleanspecattr = new boolean[] { true, true, false, true, true };

    /**
     * Attribute Boolean_spec_attr_w, DevBoolean, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "Boolean_spec_attr_w")
    private boolean[] booleanspecattrw = new boolean[0];

    /**
     * Attribute Double_attr, DevDouble, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Double_attr")
    private final double doubleattr = 3.2;

    /**
     * Attribute Double_attr_w, DevDouble, Scalar, WRITE<br>
     * description:
     */
    // @Attribute(name = "Double_attr_w")
    // private double doubleattrw = 0;

    /**
     * Attribute Double_ima_attr, DevDouble, Image, READ<br>
     * description:
     */
    @Attribute(name = "Double_ima_attr")
    private final double[][] doubleimaattr = new double[][] { { 5.55, 6.66 } };

    /**
     * Attribute Double_spec_attr, DevDouble, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Double_spec_attr")
    private final double[] doublespecattr = new double[] { 1.11, 2.22 };

    /**
     * Attribute Double_spec_attr_w, DevDouble, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "Double_spec_attr_w")
    private double[] doublespecattrw = new double[0];

    /**
     * dynamic command and attribute management. Will be injected by the framework.
     */
    @DynamicManagement
    private DynamicManager dynamicManager;

    /**
     * Attribute Encoded_attr, DevEncoded, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Encoded_attr")
    private DevEncoded encodedattr = new DevEncoded("Which format?", new byte[] { 97, 98, 99, 100 });

    /**
     * Attribute Encoded_image, DevEncoded, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Encoded_image")
    private final DevEncoded encodedimage = new DevEncoded();

    /**
     * Attribute Event64_change_tst, DevLong64, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Event64_change_tst")
    private final long[] event64changetst = new long[0];

    /**
     * Attribute Event_change_tst, DevLong, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Event_change_tst")
    private final int[] eventchangetst = new int[0];

    /**
     * Attribute Event_quality_tst, DevDouble, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Event_quality_tst")
    private final double[] eventqualitytst = new double[0];

    /**
     * Attribute fast_actuator, DevDouble, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "fast_actuator")
    private double fastactuator = 0;

    /**
     * Attribute Float_attr, DevFloat, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Float_attr")
    private final float floatattr = 4.5f;

    /**
     * Attribute Float_attr_w, DevFloat, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "Float_attr_w")
    private float floatattrw = 0;

    /**
     * Attribute Float_ima_attr_w, DevFloat, Image, WRITE<br>
     * description:
     */
    @Attribute(name = "Float_ima_attr_w")
    private float[][] floatimaattrw = new float[8][8];

    /**
     * Attribute Float_spec_attr, DevFloat, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Float_spec_attr")
    private final float[] floatspecattr = new float[] { 4.5f, 8.5f, 16.5f };

    /**
     * Attribute Float_spec_attr_rw, DevFloat, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Float_spec_attr_rw")
    private final float[] floatspecattrrw = new float[] { 5.5f, 11.5f };

    /**
     * Attribute Float_spec_attr_w, DevFloat, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "Float_spec_attr_w")
    private float[] floatspecattrw = new float[0];

    private int iOPollArray2Num = 0;

    /**
     * Attribute Long64_attr_rw, DevLong64, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Long64_attr_rw")
    private long long64attrrw = 34359738368L;

    /**
     * Attribute Long64_spec_attr_rw, DevLong64, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Long64_spec_attr_rw")
    private final long[] long64specattrrw = new long[] { 1000, 10000, 100000 };

    /**
     * Attribute Long_attr, DevLong, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Long_attr")
    @AttributeProperties(minAlarm = "1000", maxAlarm = "1500")
    private int longattr = 1246;

    /**
     * Attribute Long_attr_w, DevLong, Scalar, WRITE<br>
     * description: Test description
     */
    // @Attribute(name = "Long_attr_w")
    // @AttributeProperties(description = "Test description", label = "Test label", displayUnit = "Et ta soeur",
    // standardUnit = "100", unit = "Kilogramme", format = "Tres long", minAlarm = "1", maxAlarm = "99999", minValue =
    // "0", maxValue = "100000")
    // private int longattrw = 0;

    /**
     * Attribute Long_attr_with_w, DevLong, Scalar, READ_WITH_WRITE<br>
     * description:
     */
    @Attribute(name = "Long_attr_with_w")
    private final int longattrwithw = 0;

    /**
     * Attribute Long_ima_attr, DevLong, Image, READ<br>
     * description:
     */
    @Attribute(name = "Long_ima_attr")
    private final int[][] longimaattr = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };

    /**
     * Attribute Long_spec_attr, DevLong, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Long_spec_attr")
    private final int[] longspecattr = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    /**
     * Attribute Long_spec_attr_rw, DevLong, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Long_spec_attr_rw")
    private int[] longspecattrrw = new int[0];

    /**
     * Attribute Long_spec_attr_w, DevLong, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "Long_spec_attr_w")
    private int[] longspecattrw = new int[0];

    /**
     * Attribute Poll_buff, DevString, Image, READ<br>
     * description:
     */
    @Attribute(name = "Poll_buff")
    private String[][] pollbuff = new String[0][0];

    // /**
    // * Attribute Poll_buffRW, DevString, Image, READ_WRITE<br>
    // * description:
    // */
    // @Attribute(name = "Poll_buffRW")
    // private String[] pollbuffRW = new String[0];

    /**
     * Attribute PollLong_attr, DevLong, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "PollLong_attr")
    private final int pollLongattr = 0;

    private int pollLongAttrNum = 0;

    private int pollStr1Even = 0;

    /**
     * Attribute PollString_spec_attr, DevString, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "PollString_spec_attr")
    private final String[] pollStringspecattr = new String[0];

    private int pollStringSpecAttrNum = 0;

    /**
     * Attribute Short_attr, DevShort, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Short_attr")
    private final short shortattr = 12;

    /**
     * Attribute Short_attr_rw, DevShort, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Short_attr_rw")
    private short shortattrrw = 66;

    /**
     * Attribute Short_attr_w, DevShort, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "Short_attr_w", isMemorized = true)
    private short shortattrw = 0;

    /**
     * Attribute Short_attr_w2, DevShort, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "Short_attr_w2")
    private short shortattrw2 = 0;

    /**
     * Attribute Short_attr_with_w, DevShort, Scalar, READ_WITH_WRITE<br>
     * description:
     */
    @Attribute(name = "Short_attr_with_w")
    private final short shortattrwithw = 44;

    /**
     * Attribute Short_ima_attr, DevShort, Image, READ<br>
     * description:
     */
    @Attribute(name = "Short_ima_attr")
    private final short[][] shortimaattr = new short[][] { { 40, 60 }, { 80, 100 } };

    /**
     * Attribute Short_ima_attr_rw, DevShort, Image, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Short_ima_attr_rw")
    private final short[][] shortimaattrrw = new short[][] { { 6, 7 }, { 8, 9 } };

    /**
     * Attribute Short_ima_attr_w, DevShort, Image, WRITE<br>
     * description:
     */
    @Attribute(name = "Short_ima_attr_w")
    private short[][] shortimaattrw = new short[0][0];

    /**
     * Attribute Short_spec_attr, DevShort, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "Short_spec_attr")
    private final short[] shortspecattr = new short[] { 10, 20, 30, 40 };

    /**
     * Attribute Short_spec_attr_rw, DevShort, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "Short_spec_attr_rw")
    private final short[] shortspecattrrw = new short[] { 8, 9 };

    /**
     * Attribute Short_spec_attr_w, DevShort, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "Short_spec_attr_w", maxDimX = 4)
    @AttributeProperties(maxValue = "100")
    private short[] shortspecattrw = new short[0];

    /**
     * Attribute slow_actuator, DevShort, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "slow_actuator")
    private short slowactuator = 0;

    /**
     * Attribute slow_actuator, DevShort, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "SlowAttr")
    private double slowAttr = 0;

    /**
     * The state of the device
     */
    @State
    private DevState state = DevState.ON;

    /**
     * Attribute State_attr_rw, DevState, Scalar, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "State_attr_rw")
    private DevState stateattrrw = DevState.FAULT;

    /**
     * Attribute State_spec_attr_rw, DevState, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "State_spec_attr_rw")
    private final DevState[] statespecattrrw = new DevState[] { DevState.ON, DevState.OFF };

    /**
     * The status of the device
     */
    // @Status
    // private String status = "";

    /**
     * Attribute String_attr, DevString, Scalar, READ<br>
     * description:
     */
    // @Attribute(name = "String_attr", isPolled = true, pollingPeriod = 250)
    @Attribute(name = "String_attr")
    private final String stringattr = "test_string";

    /**
     * Attribute String_attr_w, DevString, Scalar, WRITE<br>
     * description:
     */
    // @Attribute(name = "String_attr_w", isMemorized = true)
    // private String stringattrw = "";

    /**
     * Attribute String_attr_w2, DevString, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "String_attr_w2")
    private String stringattrw2 = "Not initialised";

    /**
     * Attribute String_ima_attr, DevString, Image, READ<br>
     * description:
     */
    @Attribute(name = "String_ima_attr")
    private final String[][] stringimaattr = new String[][] { { "Hello milky way" }, { "Hello moon" } };

    /**
     * Attribute String_ima_attr_rw, DevString, Image, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "String_ima_attr_rw")
    private String[][] stringimaattrrw = new String[0][0];

    /**
     * Attribute String_ima_attr_w, DevString, Image, WRITE<br>
     * description:
     */
    @Attribute(name = "String_ima_attr_w")
    private String[][] stringimaattrw = new String[0][0];

    /**
     * Attribute String_spec_attr, DevString, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "String_spec_attr", maxDimX = 2)
    @AttributeProperties(label = "Class label", unit = "Class unit", format = "Class format")
    private final String[] stringspecattr = new String[] { "Hello world", "Hello universe" };

    /**
     * Attribute String_spec_attr_rw, DevString, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "String_spec_attr_rw")
    private String[] stringspecattrrw = new String[0];

    /**
     * Attribute String_spec_attr_w, DevString, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "String_spec_attr_w")
    private String[] stringspecattrw = new String[0];

    /**
     * Attribute Sub_device_tst, DevBoolean, Scalar, READ<br>
     * description:
     */
    @Attribute(name = "Sub_device_tst")
    private final boolean subdevicetst = false;

    // /**
    // * Attribute UChar_attr, DevUChar, Scalar, READ<br>
    // * description:
    // */
    // @Attribute(name = "UChar_attr")
    // private final byte uCharattr = 88;

    /**
     * Attribute UChar_attr_w, DevUChar, Scalar, WRITE<br>
     * description:
     */
    @Attribute(name = "UChar_attr_w")
    private byte uCharattrw = 0;

    /**
     * Attribute UChar_spec_attr, DevUChar, Spectrum, READ<br>
     * description:
     */
    @Attribute(name = "UChar_spec_attr")
    private final byte[] uCharspecattr = new byte[] { 28, 45, (byte) 156, 34, (byte) 200, 12 };

    /**
     * Attribute UChar_spec_attr_rw, DevUChar, Spectrum, READ_WRITE<br>
     * description:
     */
    @Attribute(name = "UChar_spec_attr_rw")
    private final byte[] uCharspecattrrw = new byte[] { 22, 44, 66 };

    /**
     * Attribute UChar_spec_attr_w, DevUChar, Spectrum, WRITE<br>
     * description:
     */
    @Attribute(name = "UChar_spec_attr_w")
    private byte[] uCharspecattrw = new byte[0];
    private PollbuffRW pollbuffRW;
    private int[] iorray1;
    private boolean oEncodedEven;

    @Attribute(name = "Long64_attr")
    private final long long64Attr = 300;

    // /**
    // * Attribute ULong64_attr_rw, DevULong64, Scalar, READ_WRITE<br>
    // * description:
    // */
    // @Attribute(name = "ULong64_attr_rw")
    // private long uLong64attrrw = 49152;

    // /**
    // * Attribute ULong64_spec_attr_rw, DevULong64, Spectrum, READ_WRITE<br>
    // * description:
    // */
    // @Attribute(name = "ULong64_spec_attr_rw")
    // private long[] uLong64specattrrw = new long[] { 8888, 88888, 888888 };

    /* PROTECTED REGION ID(DevTest.private) ENABLED START */
    // put private variables here
    /* PROTECTED REGION END */

    // /**
    // * Attribute ULong_attr_rw, DevULong, Scalar, READ_WRITE<br>
    // * description:
    // */
    // @Attribute(name = "ULong_attr_rw")
    // private int uLongattrrw = (int) 3221225472L;

    /**
     * // * Attribute , DevULong, Spectrum, READ_WRITE<br>
     * // * description: //
     */
    // @Attribute(name = "")
    // private int[] uLongspecattrrw = new int[] { 2222, 22222, 222222 };

    // /**
    // * Attribute UShort_attr, DevUShort, Scalar, READ<br>
    // * description:
    // */
    // @Attribute(name = "UShort_attr")
    // private final short uShortattr = 111;
    // /**
    // * Attribute UShort_attr_w, DevUShort, Scalar, WRITE<br>
    // * description:
    // */
    // @Attribute(name = "UShort_attr_w")
    // private short uShortattrw = 0;
    /**
     * Attribute UShort_ima_attr_rw, DevUShort, Image, READ_WRITE<br>
     * description:
     */
    // @Attribute(name = "UShort_ima_attr_rw")
    // private short[][] uShortimaattrrw = new short[][] { { 2 }, { 3 } };
    // /**
    // * Attribute UShort_ima_attr_w, DevUShort, Image, WRITE<br>
    // * description:
    // */
    // @Attribute(name = "UShort_ima_attr_w")
    // private short[][] uShortimaattrw = new short[0][0];

    // /**
    // * Attribute UShort_spec_attr, DevUShort, Spectrum, READ<br>
    // * description:
    // */
    // @Attribute(name = "UShort_spec_attr")
    // private final short[] uShortspecattr = new short[] { 333, 444 };

    /**
     * Attribute UShort_spec_attr_w, DevUShort, Spectrum, WRITE<br>
     * description:
     */
    // @Attribute(name = "UShort_spec_attr_w")
    // private short[] uShortspecattrw = new short[0];

    // private short[] shortArrayWrite;
    // private short[] shortArrayRead = new short[] { 6, 7, 8, 9 };

    /**
     * Method called before and after command and attribute calls.
     */
    @AroundInvoke
    public final void aroundInvoke(final InvocationContext ctx) throws DevFailed {
	xlogger.entry(ctx);
	/* PROTECTED REGION ID(DevTest.aroundInvoke) ENABLED START */
	// put aroundInvoke code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Execute command "ChangeEncodedData". <br>
     * description:
     * 
     * @param changeEncodedDataIn
     *            void, void
     * @return void, void
     */
    @Command(name = "ChangeEncodedData", inTypeDesc = "void", outTypeDesc = "void")
    public void changeEncodedData() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.changeEncodedData) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "ChangeEncodedFormat". <br>
     * description:
     * 
     * @param changeEncodedFormatIn
     *            void, void
     * @return void, void
     */
    @Command(name = "ChangeEncodedFormat", inTypeDesc = "void", outTypeDesc = "void")
    public void changeEncodedFormat() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.changeEncodedFormat) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * all resources may be closed here. Collections may be also cleared.
     * 
     * @throws DevFailed
     */
    @Delete
    public final void deleteDevice() throws DevFailed {
	xlogger.entry();
	// dynamicManager.clearAll();
	/* PROTECTED REGION ID(DevTest.deleteDevice) ENABLED START */
	// put your device clearing code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Execute command "FileDb". <br>
     * description:
     * 
     * @param fileDbIn
     *            void, void
     * @return void, void
     */
    @Command(name = "FileDb", inTypeDesc = "void", outTypeDesc = "void")
    public void fileDb() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.fileDb) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Read attribute attr_asyn
     * 
     * @return attribute value
     */
    public double getAttrasyn() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getattr_asyn) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return attrasyn;
    }

    /**
     * Read attribute attr_asyn_except
     * 
     * @return attribute value
     */
    public double getAttrasynexcept() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getattr_asyn_except) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return attrasynexcept;
    }

    /**
     * Read attribute attr_asyn_to
     * 
     * @return attribute value
     */
    public double getAttrasynto() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getattr_asyn_to) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return attrasynto;
    }

    /**
     * Read attribute attr_dq_db
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public AttributeValue getAttrdqdb() throws DevFailed {
	xlogger.entry();
	final AttributeValue val = new AttributeValue();
	val.setQuality(AttrQuality.ATTR_VALID);
	val.setTime(System.currentTimeMillis());
	val.setValue(8.888d);
	xlogger.exit();
	return val;
    }

    /**
     * Read attribute attr_dq_lo
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public AttributeValue getAttrdqlo() throws DevFailed {
	xlogger.entry();
	final AttributeValue val = new AttributeValue();
	val.setQuality(AttrQuality.ATTR_ALARM);
	val.setTime(System.currentTimeMillis());
	val.setValue(7777);
	xlogger.exit();
	return val;
    }

    /**
     * Read attribute attr_dq_sh
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public AttributeValue getAttrdqsh() throws DevFailed {
	xlogger.entry();
	final AttributeValue val = new AttributeValue();
	val.setQuality(AttrQuality.ATTR_VALID);
	val.setTime(System.currentTimeMillis());
	val.setValue((short) 77);
	xlogger.exit();
	return val;
    }

    /**
     * Read attribute attr_dq_str
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public AttributeValue getAttrdqstr() throws DevFailed {
	xlogger.entry();
	final AttributeValue val = new AttributeValue();
	val.setQuality(AttrQuality.ATTR_ALARM);
	val.setTime(System.currentTimeMillis());
	val.setValue("Setting value date and quality");
	xlogger.exit();
	return val;
    }

    /**
     * Read attribute attr_no_alarm
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public int getAttrnoalarm() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getattr_no_alarm) ENABLED START */
	DevFailedUtils.throwDevFailed("API_AttrNoAlarm", "no alarm");
	/* PROTECTED REGION END */
	xlogger.exit();
	return attrnoalarm;
    }

    /**
     * Read attribute attr_no_data
     * 
     * @return attribute value
     */
    public AttributeValue getAttrnodata() {
	xlogger.entry();
	final AttributeValue av = new AttributeValue();
	av.setQuality(AttrQuality.ATTR_VALID);
	xlogger.exit();
	return av;
    }

    /**
     * Read attribute attr_wrong_size
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public AttributeValue getAttrwrongsize() throws DevFailed {
	xlogger.entry();

	final AttributeValue av = new AttributeValue(new int[1000][1000]);
	xlogger.exit();
	return av;
    }

    /**
     * Read attribute attr_wrong_type
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public AttributeValue getAttrwrongtype() throws DevFailed {
	xlogger.entry();
	final AttributeValue av = new AttributeValue(1024L);
	xlogger.exit();
	return av;
    }

    /**
     * Read attribute Boolean_attr
     * 
     * @return attribute value
     */
    public boolean getBooleanattr() {
	xlogger.entry();
	xlogger.exit();
	return booleanattr;
    }

    /**
     * Read attribute Boolean_ima_attr_rw
     * 
     * @return attribute value
     */
    public boolean[][] getBooleanimaattrrw() {
	xlogger.entry();
	xlogger.exit();
	return booleanimaattrrw;
    }

    /**
     * Read attribute Boolean_spec_attr
     * 
     * @return attribute value
     */
    public boolean[] getBooleanspecattr() {
	xlogger.entry();
	xlogger.exit();
	return booleanspecattr;
    }

    /**
     * Read attribute Double_attr
     * 
     * @return attribute value
     */
    public double getDoubleattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getDouble_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return doubleattr;
    }

    /**
     * Read attribute Double_ima_attr
     * 
     * @return attribute value
     */
    public double[][] getDoubleimaattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getDouble_ima_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return doubleimaattr;
    }

    /**
     * Read attribute Double_spec_attr
     * 
     * @return attribute value
     */
    public double[] getDoublespecattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getDouble_spec_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return doublespecattr;
    }

    /**
     * Read attribute Encoded_attr
     * 
     * @return attribute value
     */
    public DevEncoded getEncodedattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getEncoded_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();

	return encodedattr;
    }

    /**
     * Read attribute Encoded_image
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public DevEncoded getEncodedimage() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getEncoded_image) ENABLED START */
	// TODO still waiting for ESRF to provide the way to encode JPEG like in C++ API.
	// final BufferedImage bufferedImage = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);
	//
	// final int[] image = new int[256 * 256];
	// for (int i = 0; i < 256; i++) {
	// for (int j = 0; j < 256; j++) {
	// image[i + j * 256] = i;
	// }
	// }
	xlogger.exit();
	return new DevEncoded();
	// // System.out.println("getEncodedimage " + image[128 + 128 * 256]);
	//
	// for (int x = 0; x < 256; x++) {
	// for (int y = 0; y < 256; y++) {
	// // fix this based on your rastering order
	// final int c = image[y * 256 + x];
	// // all of the components set to the same will be gray
	// bufferedImage.setRGB(x, y, new Color(c, c, c).getRGB());
	// }
	// }
	//
	// // this writes the bufferedImage into a byte array called resultingBytes
	// final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
	// // final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
	// // final ImageWriter writer = iter.next();
	// try {
	// final JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(byteArrayOut);
	// final JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufferedImage);
	// param.setQuality(0.5F, true);
	// encoder.encode(bufferedImage, param);
	//
	// // ImageIO.write(bufferedImage, "jpeg", byteArrayOut);
	// // final File file = new File("C:/tmp/newimage.jpg");
	// // ImageIO.write(bufferedImage, "jpg", file);
	// } catch (final IOException e) {
	// DevFailedUtils.throwDevFailed(e);
	// }
	// final byte[] resultingBytes = byteArrayOut.toByteArray();
	// // System.out.println("getEncodedimage " + Arrays.toString(resultingBytes));
	// final DevEncoded encodedimage = new DevEncoded("JPEG_GRAY8", resultingBytes);

	// final JpegDecoder decode = new JpegDecoder(resultingBytes);
	// try {
	// final byte[][] r = decode.decode();
	// System.out.println(r.length);
	// System.out.println(r[0].length);
	// System.out.println(r[0][0]);
	// for (final byte[] element : r) {
	// System.out.println(Arrays.toString(element));
	// }
	// } catch (final IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	/* PROTECTED REGION END */

	// final byte[] byteImage = new byte[256 * 256];
	// final byte value = -100;
	// for (int i = 0; i < 256; i++) {
	// for (int j = 0; j < 256; j++) {
	// byteImage[i + j * 256] = value;
	//
	// // if (value == 127) {
	// // value = -128;
	// //
	// // } else {
	// // value++;
	// // }
	// }
	// }
	// byte[] r2 = null;
	// try {
	// r2 = new JpegEncoder().encode_gray8(byteImage, 256, 256);
	// } catch (final IOException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// return new DevEncoded(DevEncodedUtils.JPEG_GRAY8, r2);
	// return DevEncodedUtils.encodeJPEGGray8(image, 256, 256);
    }

    /**
     * Read attribute Event64_change_tst
     * 
     * @return attribute value
     */
    public long[] getEvent64changetst() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getEvent64_change_tst) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return event64changetst;
    }

    /**
     * Read attribute Event_change_tst
     * 
     * @return attribute value
     */
    public int[] getEventchangetst() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getEvent_change_tst) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return eventchangetst;
    }

    /**
     * Read attribute Event_quality_tst
     * 
     * @return attribute value
     */
    public double[] getEventqualitytst() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getEvent_quality_tst) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return eventqualitytst;
    }

    /**
     * Read attribute fast_actuator
     * 
     * @return attribute value
     */
    public double getFastactuator() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getfast_actuator) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return fastactuator;
    }

    /**
     * Read attribute Float_attr
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public float getFloatattr() {
	xlogger.entry();

	xlogger.exit();
	return floatattr;
    }

    /**
     * Read attribute Float_spec_attr
     * 
     * @return attribute value
     */
    public float[] getFloatspecattr() {
	xlogger.entry();
	xlogger.exit();
	return floatspecattr;
    }

    /**
     * Read attribute Float_spec_attr_rw
     * 
     * @return attribute value
     */
    public float[] getFloatspecattrrw() {
	xlogger.entry();
	xlogger.exit();
	return floatspecattrrw;
    }

    /**
     * Read attribute Long64_attr_rw
     * 
     * @return attribute value
     */
    public long getLong64attrrw() {
	xlogger.entry();
	xlogger.exit();
	return long64attrrw;
    }

    /**
     * Read attribute Long64_spec_attr_rw
     * 
     * @return attribute value
     */
    public long[] getLong64specattrrw() {
	xlogger.entry();
	xlogger.exit();
	return long64specattrrw;
    }

    /**
     * Read attribute Long_attr
     * 
     * @return attribute value
     */
    public int getLongattr() {
	xlogger.entry();
	xlogger.exit();
	return longattr;
    }

    /**
     * Read attribute Long_attr_with_w
     * 
     * @return attribute value
     */
    public int getLongattrwithw() {
	xlogger.entry();
	xlogger.exit();
	return 1246;
    }

    /**
     * Read attribute Long_ima_attr
     * 
     * @return attribute value
     */
    public int[][] getLongimaattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getLong_ima_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return longimaattr;
    }

    /**
     * Execute command "GetLongSize". <br>
     * description:
     * 
     * @param getLongSizeIn
     *            void, void
     * @return DevShort, Sizeof long (32 or 64 bits)
     */
    @Command(name = "GetLongSize", inTypeDesc = "void", outTypeDesc = "Sizeof long (32 or 64 bits)")
    public short getLongSize() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getLongSize) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return 0;
    }

    /**
     * Read attribute Long_spec_attr
     * 
     * @return attribute value
     */
    public int[] getLongspecattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getLong_spec_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return longspecattr;
    }

    /**
     * Read attribute Long_spec_attr_rw
     * 
     * @return attribute value
     */
    public int[] getLongspecattrrw() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getLong_spec_attr_rw) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return longspecattrrw;
    }

    /**
     * Read attribute Poll_buff
     * 
     * @return attribute value
     */
    public String[][] getPollbuff() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getPoll_buff) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return pollbuff;
    }

    // /**
    // * Read attribute Poll_buffRW
    // *
    // * @return attribute value
    // */
    // public String[] getPollbuffRW() {
    // xlogger.entry();
    // xlogger.exit();
    // return pollbuffRW;
    // }

    /**
     * Read attribute PollLong_attr
     * 
     * @return attribute value
     */
    public int getPollLongattr() {
	xlogger.entry();
	int tmpValue;
	pollLongAttrNum++;
	if (pollLongAttrNum % 2 == 0) {
	    tmpValue = 5555;
	} else {
	    tmpValue = 6666;
	}
	xlogger.exit();
	return tmpValue;
    }

    /**
     * Read attribute PollString_spec_attr
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public String[] getPollStringspecattr() throws DevFailed {
	xlogger.entry();
	String[] tmpValue = null;
	pollStringSpecAttrNum++;
	if (pollStringSpecAttrNum % 4 == 0) {
	    tmpValue = new String[] { "Hello world", "Hello universe" };
	} else if (pollStringSpecAttrNum % 4 == 1) {
	    tmpValue = new String[] { "Hello Grenoble" };
	} else if (pollStringSpecAttrNum % 4 == 2) {
	    System.out.println("########aa");
	    DevFailedUtils.throwDevFailed("aaaa", "bbb");
	} else {
	    System.out.println("########xx");
	    DevFailedUtils.throwDevFailed("xxx", "yyy");
	}
	System.out.println("########getPollStringspecattr " + Arrays.toString(tmpValue));
	xlogger.exit();
	return tmpValue;
    }

    /**
     * Read attribute Short_attr
     * 
     * @return attribute value
     */
    public short getShortattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getShort_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return shortattr;
    }

    /**
     * Read attribute Short_attr_rw
     * 
     * @return attribute value
     */
    public short getShortattrrw() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getShort_attr_rw) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return shortattrrw;
    }

    /**
     * Read attribute Short_attr_with_w
     * 
     * @return attribute value
     */
    public short getShortattrwithw() {
	xlogger.entry();
	xlogger.exit();
	return 44;
    }

    /**
     * Read attribute Short_ima_attr
     * 
     * @return attribute value
     */
    public short[][] getShortimaattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getShort_ima_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return shortimaattr;
    }

    /**
     * Read attribute Short_ima_attr_rw
     * 
     * @return attribute value
     * @throws DevFailed
     */
    public short[][] getShortimaattrrw() throws DevFailed {
	xlogger.entry();
	// final AttributeValue value = new AttributeValue();

	// value.setValue(shortArrayRead);
	xlogger.exit();
	return shortimaattrrw;
    }

    /**
     * Read attribute Short_spec_attr
     * 
     * @return attribute value
     */
    public short[] getShortspecattr() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getShort_spec_attr) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return shortspecattr;
    }

    /**
     * Read attribute Short_spec_attr_rw
     * 
     * @return attribute value
     */
    public short[] getShortspecattrrw() {
	xlogger.entry();
	xlogger.exit();
	return shortspecattrrw;
    }

    /**
     * Read attribute slow_actuator
     * 
     * @return attribute value
     */
    public short getSlowactuator() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getslow_actuator) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return slowactuator;
    }

    public DevState getState() throws DevFailed {
	/* PROTECTED REGION ID(DevTest.getState) ENABLED START */
	// put state code here
	/* PROTECTED REGION END */
	return state;
    }

    /**
     * Read attribute State_attr_rw
     * 
     * @return attribute value
     */
    public DevState getStateattrrw() {
	xlogger.entry();
	xlogger.exit();
	return stateattrrw;
    }

    /**
     * Read attribute State_spec_attr_rw
     * 
     * @return attribute value
     */
    public DevState[] getStatespecattrrw() {
	xlogger.entry();
	xlogger.exit();
	return statespecattrrw;
    }

    // public final String getStatus() {
    // return status;
    // }

    /**
     * Read attribute String_attr
     * 
     * @return attribute value
     */
    public String getStringattr() {
	xlogger.entry();
	xlogger.exit();
	return stringattr;
    }

    /**
     * Read attribute String_ima_attr
     * 
     * @return attribute value
     */
    public String[][] getStringimaattr() {
	xlogger.entry();
	xlogger.exit();
	return stringimaattr;
    }

    /**
     * Read attribute String_ima_attr_rw
     * 
     * @return attribute value
     */
    public String[][] getStringimaattrrw() {
	xlogger.entry();
	final String[][] argout = new String[][] { { "Alors la, pour une surprise" }, { "c'est une surprise" } };
	xlogger.exit();
	return argout;
    }

    /**
     * Read attribute String_spec_attr
     * 
     * @return attribute value
     */
    public String[] getStringspecattr() {
	xlogger.entry();
	xlogger.exit();
	return stringspecattr;
    }

    /**
     * Read attribute String_spec_attr_rw
     * 
     * @return attribute value
     */
    public String[] getStringspecattrrw() {
	xlogger.entry();
	xlogger.exit();
	return new String[] { "Thank's god", "It's friday" };
    }

    /**
     * Read attribute Sub_device_tst
     * 
     * @return attribute value
     */
    public boolean getSubdevicetst() {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.getSub_device_tst) ENABLED START */
	// put read attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return subdevicetst;
    }

    // /**
    // * Read attribute UChar_attr
    // *
    // * @return attribute value
    // */
    // public byte getUCharattr() {
    // xlogger.entry();
    // xlogger.exit();
    // return uCharattr;
    // }

    /**
     * Read attribute UChar_spec_attr
     * 
     * @return attribute value
     */
    public byte[] getUCharspecattr() {
	xlogger.entry();
	xlogger.exit();
	return uCharspecattr;
    }

    /**
     * Read attribute UChar_spec_attr_rw
     * 
     * @return attribute value
     */
    public byte[] getUCharspecattrrw() {
	xlogger.entry();
	xlogger.exit();
	return uCharspecattrrw;
    }

    /**
     * Read attribute ULong64_attr_rw
     * 
     * @return attribute value
     */
    // public long getULong64attrrw() {
    // xlogger.entry();
    // xlogger.exit();
    // return uLong64attrrw;
    // }

    // /**
    // * Read attribute ULong64_spec_attr_rw
    // *
    // * @return attribute value
    // */
    // public long[] getULong64specattrrw() {
    // xlogger.entry();
    // xlogger.exit();
    // return uLong64specattrrw;
    // }

    // /**
    // * Read attribute ULong_attr_rw
    // *
    // * @return attribute value
    // */
    // public int getULongattrrw() {
    // xlogger.entry();
    // xlogger.exit();
    // return uLongattrrw;
    // }

    // /**
    // * Read attribute
    // *
    // * @return attribute value
    // */
    // public int[] getULongspecattrrw() {
    // xlogger.entry();
    // xlogger.exit();
    // return uLongspecattrrw;
    // }

    // /**
    // * Read attribute UShort_attr
    // *
    // * @return attribute value
    // */
    // public short getUShortattr() {
    // xlogger.entry();
    // xlogger.exit();
    // return uShortattr;
    // }

    // /**
    // * Read attribute UShort_ima_attr_rw
    // *
    // * @return attribute value
    // */
    // public short[][] getUShortimaattrrw() {
    // xlogger.entry();
    // xlogger.exit();
    // return uShortimaattrrw;
    // }

    /**
     * Read attribute UShort_spec_attr
     * 
     * @return attribute value
     */
    // public short[] getUShortspecattr() {
    // xlogger.entry();
    // xlogger.exit();
    // return uShortspecattr;
    // }

    /**
     * Initialize the device.
     * 
     * @throws DevFailed
     */
    @Init(lazyLoading = false)
    public void initDevice() throws DevFailed {
	xlogger.entry();
	logger.debug("init");

	state = DevState.ON;
	// status = "The device is in ON state.";
	/* PROTECTED REGION ID(DevTest.initDevice) ENABLED START */
	// put your device initialization code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Execute command "IOAddAttribute". <br>
     * description:
     * 
     * @param iOAddAttributeIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOAddAttribute", inTypeDesc = "void", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOAddAttribute() throws DevFailed {
	xlogger.entry();
	dynamicManager.removeAttribute("Added_short_attr");
	dynamicManager.addAttribute(new DevTestDynamicAttribute());
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOAddOneElt". <br>
     * description:
     * 
     * @param iOAddOneEltIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOAddOneElt", inTypeDesc = "void", outTypeDesc = "void")
    public void iOAddOneElt() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOAddOneElt) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOArray1". <br>
     * description:
     * 
     * @param iOArray1In
     *            void, void
     * @return DevVarLongArray, An array allocated
     */
    @Command(name = "IOArray1", inTypeDesc = "void", outTypeDesc = "An array allocated")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int[] iOArray1() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return iorray1;
    }

    /**
     * Execute command "IOArray2". <br>
     * description:
     * 
     * @param iOArray2In
     *            void, void
     * @return DevVarShortArray, An array without copying
     */
    @Command(name = "IOArray2", inTypeDesc = "void", outTypeDesc = "An array without copying")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public short[] iOArray2() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new short[] { 100, 200 };
    }

    /**
     * Execute command "IOAttrThrowEx". <br>
     * description:
     * 
     * @param iOAttrThrowExIn
     *            DevVarShortArray, 2 elts : Attr code and throw except flag
     * @return void, void
     */
    @Command(name = "IOAttrThrowEx", inTypeDesc = "2 elts : Attr code and throw except flag", outTypeDesc = "void")
    public void iOAttrThrowEx(final short[] iOAttrThrowExIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOAttrThrowEx) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOAttrTrigPoll". <br>
     * description:
     * 
     * @param iOAttrTrigPollIn
     *            DevString, Attribute's name to trig polling
     * @return void, void
     */
    @Command(name = "IOAttrTrigPoll", inTypeDesc = "Attribute's name to trig polling", outTypeDesc = "void")
    public void iOAttrTrigPoll(final String iOAttrTrigPollIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOAttrTrigPoll) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOBool". <br>
     * description:
     * 
     * @param iOBoolIn
     *            DevBoolean, Number
     * @return DevBoolean, Not number
     */
    @Command(name = "IOBool", inTypeDesc = "Number", outTypeDesc = "Not number")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public boolean iOBool(final boolean iOBoolIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return !iOBoolIn;
    }

    /**
     * Execute command "IOChangeQuality". <br>
     * description:
     * 
     * @param iOChangeQualityIn
     *            DevShort, 0->VALID, 1->INVALID, 2->ALARM, 3->CHANGING
     * @return void, void
     */
    @Command(name = "IOChangeQuality", inTypeDesc = "0->VALID, 1->INVALID, 2->ALARM, 3->CHANGING", outTypeDesc = "void")
    public void iOChangeQuality(final short iOChangeQualityIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOChangeQuality) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOCharArray". <br>
     * description:
     * 
     * @param iOCharArrayIn
     *            DevVarCharArray, Array of char
     * @return DevVarCharArray, This reversed array
     */
    @Command(name = "IOCharArray", inTypeDesc = "Array of char", outTypeDesc = "This reversed array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public byte[] iOCharArray(final byte[] iOCharArrayIn) throws DevFailed {
	xlogger.entry();
	final int size = iOCharArrayIn.length;
	int j = size - 1;
	final byte[] iOCharArrayOut = new byte[size];
	for (int i = 0; i < size /* && j > 0 */; i++, j--) {
	    iOCharArrayOut[i] = iOCharArrayIn[j];
	}
	xlogger.exit();
	return iOCharArrayOut;
    }

    /**
     * Execute command "IODecValue". <br>
     * description:
     * 
     * @param iODecValueIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IODecValue", inTypeDesc = "void", outTypeDesc = "void")
    public void iODecValue() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iODecValue) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IODevByName". <br>
     * description:
     * 
     * @param iODevByNameIn
     *            DevString, device name
     * @return DevString, device name (returned by name()
     */
    @Command(name = "IODevByName", inTypeDesc = "device name", outTypeDesc = "device name (returned by name()")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iODevByName(final String iODevByNameIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	final String[] devices = ServerManager.getInstance().getDevicesOfClass("DevTest");
	if (!ArrayUtils.contains(devices, iODevByNameIn)) {
	    DevFailedUtils.throwDevFailed("API_DeviceNotFound", iODevByNameIn + " not found");
	}
	return iODevByNameIn;
    }

    /**
     * Execute command "IODevListByClass". <br>
     * description:
     * 
     * @param iODevListByClassIn
     *            DevString, class name
     * @return DevString, class first device name
     */
    @Command(name = "IODevListByClass", inTypeDesc = "class name", outTypeDesc = "class first device name")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iODevListByClass(final String iODevListByClassIn) throws DevFailed {
	xlogger.entry();
	String result = "";
	final String[] devices = ServerManager.getInstance().getDevicesOfClass(iODevListByClassIn);
	if (devices.length > 0) {
	    // return only family and domain
	    result = devices[0].substring(0, devices[0].lastIndexOf('/'));
	}
	xlogger.exit();
	return result;
    }

    /**
     * Execute command "IODouble". <br>
     * description:
     * 
     * @param iODoubleIn
     *            DevDouble, Number
     * @return DevDouble, Number * 2
     */
    @Command(name = "IODouble", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public double iODouble(final double iODoubleIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return iODoubleIn * 2.0;
    }

    /**
     * Execute command "IODoubleArray". <br>
     * description:
     * 
     * @param iODoubleArrayIn
     *            DevVarDoubleArray, Array of double
     * @return DevVarDoubleArray, This array * 2
     */
    @Command(name = "IODoubleArray", inTypeDesc = "Array of double", outTypeDesc = "This array * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public double[] iODoubleArray(final double[] iODoubleArrayIn) throws DevFailed {
	xlogger.entry();
	final int size = iODoubleArrayIn.length;
	final double[] iODoubleArrayOut = new double[size];
	for (int i = 0; i < size; i++) {
	    iODoubleArrayOut[i] = iODoubleArrayIn[i] * 2;
	}
	xlogger.exit();
	return iODoubleArrayOut;
    }

    /**
     * Execute command "IODoubleString". <br>
     * description:
     * 
     * @param iODoubleStringIn
     *            DevVarDoubleStringArray, Array of double and string
     * @return DevVarDoubleStringArray, This array of long * 2
     */
    @Command(name = "IODoubleString", inTypeDesc = "Array of double and string", outTypeDesc = "This array of long * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public DevVarDoubleStringArray iODoubleString(final DevVarDoubleStringArray iODoubleStringIn) throws DevFailed {
	xlogger.entry();
	final double[] dValueIn = iODoubleStringIn.dvalue;
	final int dSize = dValueIn.length;
	final String[] sValueIn = iODoubleStringIn.svalue;
	final double[] dValueOut = new double[dSize];
	final String[] sValueOut = sValueIn;
	for (int i = 0; i < dSize; i++) {
	    dValueOut[i] = dValueIn[i] * 2;
	}
	xlogger.exit();
	return new DevVarDoubleStringArray(dValueOut, sValueOut);
    }

    /**
     * Execute command "IODServDevice". <br>
     * description:
     * 
     * @param iODServDeviceIn
     *            void, void
     * @return DevString, dserver device name
     */
    @Command(name = "IODServDevice", inTypeDesc = "void", outTypeDesc = "dserver device name")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iODServDevice() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return ServerManager.getInstance().getAdminDeviceName();
    }

    /**
     * Execute command "IOExcept". <br>
     * description:
     * 
     * @param iOExceptIn
     *            void, Void
     * @return void, An exception
     */
    @Command(name = "IOExcept", inTypeDesc = "Void", outTypeDesc = "An exception")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOExcept() throws DevFailed {
	xlogger.entry();
	final DevError[] error = new DevError[] { new DevError("API_ThrowException", ErrSeverity.ERR, "This is a test",
		"IOExcept::execute") };

	xlogger.exit();
	throw new DevFailed("API_ThrowException", error);
    }

    /**
     * Execute command "IOExit". <br>
     * description:
     * 
     * @param iOExitIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOExit", inTypeDesc = "void", outTypeDesc = "void")
    public void iOExit() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOExit) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOFillPollBuffAttr". <br>
     * description:
     * 
     * @param iOFillPollBuffAttrIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOFillPollBuffAttr", inTypeDesc = "void", outTypeDesc = "void")
    public void iOFillPollBuffAttr() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOFillPollBuffAttr) ENABLED START */
	pollbuff = new String[][] { { "One_1", "Two_1" }, { "Three_1", "Four_1" } };
	deviceManager.triggerPolling("Poll_buff");
	pollbuff = new String[][] { { "One_2", "Two_2" }, { "Three_2", "Four_2" } };
	deviceManager.triggerPolling("Poll_buff");
	pollbuff = new String[][] { { "One_3", "Two_3" }, { "Three_3", "Four_3" } };
	deviceManager.triggerPolling("Poll_buff");

	pollbuffRW.setPollbuffRWRead(new String[] { "One_rd_1", "Two_rd_1", "Three_rd_1", "Four_rd_1" }, 2, 2);
	pollbuffRW.setPollbuffRWWrite(new String[] { "One_wr_1", "Two_wr_1" }, 2, 1);
	deviceManager.triggerPolling("Poll_buffRW");
	pollbuffRW.setPollbuffRWRead(new String[] { "One_rd_2", "Two_rd_2", "Three_rd_2", "Four_rd_2" }, 2, 2);
	pollbuffRW.setPollbuffRWWrite(new String[] { "One_wr_2", "Two_wr_2" }, 2, 1);
	deviceManager.triggerPolling("Poll_buffRW");
	pollbuffRW.setPollbuffRWRead(new String[] { "One_rd_3", "Two_rd_3", "Three_rd_3", "Four_rd_3", }, 2, 2);
	pollbuffRW.setPollbuffRWWrite(new String[] { "One_wr_3", "Two_wr_3" }, 2, 1);
	deviceManager.triggerPolling("Poll_buffRW");
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOFillPollBuffCmd". <br>
     * description:
     * 
     * @param iOFillPollBuffCmdIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOFillPollBuffCmd", inTypeDesc = "void", outTypeDesc = "void")
    public void iOFillPollBuffCmd() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOFillPollBuffCmd) ENABLED START */
	// put command code here
	iorray1 = new int[] { 10, 11, 12 };
	deviceManager.triggerPolling("IOArray1");
	iorray1 = new int[] { 11, 12, 13 };
	deviceManager.triggerPolling("IOArray1");
	iorray1 = new int[] { 12, 13, 14 };
	deviceManager.triggerPolling("IOArray1");
	iorray1 = new int[] { 10, 20, 30, 40 };
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOFillPollBuffEncodedAttr". <br>
     * description:
     * 
     * @param iOFillPollBuffEncodedAttrIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOFillPollBuffEncodedAttr", inTypeDesc = "void", outTypeDesc = "void")
    public void iOFillPollBuffEncodedAttr() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOFillPollBuffEncodedAttr) ENABLED START */
	encodedattr = new DevEncoded("First value", new byte[] { 22, 33 });
	deviceManager.triggerPolling("Encoded_attr");
	encodedattr = new DevEncoded("Second value", new byte[] { 33, 44 });
	deviceManager.triggerPolling("Encoded_attr");
	encodedattr = new DevEncoded("Third value", new byte[] { 44, 55 });
	deviceManager.triggerPolling("Encoded_attr");
	encodedattr = new DevEncoded("Which format?", new byte[] { 97, 98, 99, 100 });
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOFloat". <br>
     * description:
     * 
     * @param iOFloatIn
     *            DevFloat, Number
     * @return DevFloat, Number * 2
     */
    @Command(name = "IOFloat", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public float iOFloat(final float iOFloatIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return iOFloatIn * 2f;
    }

    /**
     * Execute command "IOFloatArray". <br>
     * description:
     * 
     * @param iOFloatArrayIn
     *            DevVarFloatArray, Array of float
     * @return DevVarFloatArray, This array * 2
     */
    @Command(name = "IOFloatArray", inTypeDesc = "Array of float", outTypeDesc = "This array * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public float[] iOFloatArray(final float[] iOFloatArrayIn) throws DevFailed {
	xlogger.entry();
	final int size = iOFloatArrayIn.length;
	final float[] iOFloatArrayOut = new float[size];
	for (int i = 0; i < size; i++) {
	    iOFloatArrayOut[i] = iOFloatArrayIn[i] * 2;
	}
	xlogger.exit();
	return iOFloatArrayOut;
    }

    @Command(name = "OEncoded")
    public DevEncoded oEncoded(final DevEncoded argin) {
	DevEncoded result;
	oEncodedEven = !oEncodedEven;
	if (oEncodedEven) {
	    result = new DevEncoded("Odd - OEncoded format", new byte[] { 11, 21 });
	} else {
	    result = new DevEncoded("Even - OEncoded format", new byte[] { 10, 20, 30, 40 });
	}
	return result;
    }

    @Command(name = "PollingPoolTst")
    public String[] pollingPoolTst() {
	final int poolSize = TangoCacheManager.getPoolSize();
	final List<String> pollDevices = TangoCacheManager.getPolledDevices();
	String[] result;
	if (pollDevices.size() == 0) {
	    result = new String[0];
	} else if (pollDevices.size() == poolSize) {
	    result = pollDevices.toArray(new String[pollDevices.size()]);
	} else {

	    final List<Set<String>> ordered = new LinkedList<Set<String>>();
	    int i = 1;
	    int j = 1;
	    final int devicePerThread = (int) Math.ceil((double) pollDevices.size() / (double) poolSize);
	    for (final String pollDevice : pollDevices) {
		if (i > ordered.size()) {
		    final Set<String> thread = new LinkedHashSet<String>();
		    ordered.add(thread);
		}
		final Set<String> thread = ordered.get(i - 1);
		thread.add(pollDevice);
		if (j >= devicePerThread) {
		    i++;
		    j = 0;
		}
		j++;
		if (i > poolSize) {
		    i = 1;
		}
	    }
	    i = 0;
	    result = new String[ordered.size()];
	    for (final Set<String> set : ordered) {
		System.out.println(set);
		final StringBuilder sb = new StringBuilder();
		for (final Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
		    final String string = iterator.next();
		    sb.append(string);
		    if (iterator.hasNext()) {
			sb.append(",");
		    }
		}
		result[i++] = sb.toString();
	    }
	}
	return result;
    }

    /**
     * Execute command "IOGetCbExecuted". <br>
     * description:
     * 
     * @param iOGetCbExecutedIn
     *            void, void
     * @return DevLong, Number of times the CB has been executed
     */
    @Command(name = "IOGetCbExecuted", inTypeDesc = "void", outTypeDesc = "Number of times the CB has been executed")
    public int iOGetCbExecuted() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOGetCbExecuted) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return 0;
    }

    /**
     * Execute command "IOIncValue". <br>
     * description:
     * 
     * @param iOIncValueIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOIncValue", inTypeDesc = "void", outTypeDesc = "void")
    public void iOIncValue() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOIncValue) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOInitWAttr". <br>
     * description:
     * 
     * @param iOInitWAttrIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOInitWAttr", inTypeDesc = "void", outTypeDesc = "void")
    public void iOInitWAttr() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOInitWAttr) ENABLED START */
	shortattrrw = 10;
	final LongAttrW attr = (LongAttrW) dynamicManager.getAttribute("Long_attr_w");
	attr.setLongattrw(100);

	final DoubleAttrW attrD = (DoubleAttrW) dynamicManager.getAttribute("Double_attr_w");
	attrD.setDoubleValue(1.1);

	final StringAttrW attrS = (StringAttrW) dynamicManager.getAttribute("String_attr_w");
	attrS.setStringValue("Init");

	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOLong". <br>
     * description:
     * 
     * @param iOLongIn
     *            DevLong, Number
     * @return DevLong, Number * 2
     */
    @Command(name = "IOLong", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int iOLong(final int iOLongIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return iOLongIn * 2;
    }

    /**
     * Execute command "IOLong64". <br>
     * description:
     * 
     * @param iOLong64In
     *            DevLong64, Number
     * @return DevLong64, Number * 2
     */
    @Command(name = "IOLong64", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public long iOLong64(final long iOLong64In) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return iOLong64In * 2;
    }

    /**
     * Execute command "IOLongArray". <br>
     * description:
     * 
     * @param iOLongArrayIn
     *            DevVarLongArray, Array of long
     * @return DevVarLongArray, This array * 2
     */
    @Command(name = "IOLongArray", inTypeDesc = "Array of long", outTypeDesc = "This array * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int[] iOLongArray(final int[] iOLongArrayIn) throws DevFailed {
	xlogger.entry();
	final int size = iOLongArrayIn.length;
	final int[] iOLongArrayOut = new int[size];
	for (int i = 0; i < size; i++) {
	    iOLongArrayOut[i] = iOLongArrayIn[i] * 2;
	}
	xlogger.exit();
	return iOLongArrayOut;
    }

    /**
     * Execute command "IOLongString". <br>
     * description:
     * 
     * @param iOLongStringIn
     *            DevVarLong64Array, Array of long and string
     * @return DevVarLong64Array, This array of long * 2
     */
    @Command(name = "IOLongString", inTypeDesc = "Array of long and string", outTypeDesc = "This array of long * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public DevVarLongStringArray iOLongString(final DevVarLongStringArray iOLongStringIn) throws DevFailed {
	xlogger.entry();
	final int[] lValueIn = iOLongStringIn.lvalue;
	final int lSize = lValueIn.length;
	final String[] sValueIn = iOLongStringIn.svalue;
	final int[] lValueOut = new int[lSize];
	final String[] sValueOut = sValueIn;
	for (int i = 0; i < lSize; i++) {
	    lValueOut[i] = lValueIn[i] * 2;
	}
	xlogger.exit();
	return new DevVarLongStringArray(lValueOut, sValueOut);
    }

    /**
     * Execute command "IOPollArray2". <br>
     * description:
     * 
     * @param iOPollArray2In
     *            void, void
     * @return DevVarShortArray, An array without copying changing at each call
     */
    @Command(name = "IOPollArray2", inTypeDesc = "void", outTypeDesc = "An array without copying changing at each call")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public short[] iOPollArray2() throws DevFailed {
	xlogger.entry();
	final short[] argout = new short[2];
	iOPollArray2Num++;

	if (iOPollArray2Num % 2 == 0) {
	    argout[0] = 100;
	    argout[1] = 200;
	} else {
	    argout[0] = 300;
	    argout[1] = 400;
	}
	xlogger.exit();
	return argout;
    }

    /**
     * Execute command "IOPollStr1". <br>
     * description:
     * 
     * @param iOPollStr1In
     *            void, void
     * @return DevString, Just a string dynamically allocated changing at each call
     */
    @Command(name = "IOPollStr1", inTypeDesc = "void", outTypeDesc = "Just a string dynamically allocated changing at each call")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iOPollStr1() throws DevFailed {
	xlogger.entry();
	String argout = "";
	pollStr1Even++;
	// System.out.println(pollStr1Even % 3);
	if (pollStr1Even % 3 == 0) {
	    argout = "Even value from IOPollStr1";
	} else if (pollStr1Even % 3 == 1) {
	    argout = "Odd value from IOPollStr1";
	} else {
	    DevFailedUtils.throwDevFailed("qqq", "www");
	}
	xlogger.exit();
	return argout;
    }

    /**
     * Execute command "IOPushDevEncodedEvent". <br>
     * description:
     * 
     * @param iOPushDevEncodedEventIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOPushDevEncodedEvent", inTypeDesc = "void", outTypeDesc = "void")
    public void iOPushDevEncodedEvent() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOPushDevEncodedEvent) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOPushEvent". <br>
     * description:
     * 
     * @param iOPushEventIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOPushEvent", inTypeDesc = "void", outTypeDesc = "void")
    public void iOPushEvent() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOPushEvent) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IORegClassSig". <br>
     * description:
     * 
     * @param iORegClassSigIn
     *            DevLong, Signal number
     * @return void, void
     */
    @Command(name = "IORegClassSig", inTypeDesc = "Signal number", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iORegClassSig(final int iORegClassSigIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iORegClassSig) ENABLED START */
	DevTestSignalHandler.install(iORegClassSigIn);
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IORegSig". <br>
     * description:
     * 
     * @param iORegSigIn
     *            DevLong, Signal number
     * @return void, void
     */
    @Command(name = "IORegSig", inTypeDesc = "Signal number", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iORegSig(final int iORegSigIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iORegSig) ENABLED START */
	DevTestSignalHandler.install(iORegSigIn);
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IORegSigOwn". <br>
     * description:
     * 
     * @param iORegSigOwnIn
     *            DevLong, Signal number
     * @return void, void
     */
    @Command(name = "IORegSigOwn", inTypeDesc = "Signal number", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iORegSigOwn(final int iORegSigOwnIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iORegSigOwn) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IORemoveAttribute". <br>
     * description:
     * 
     * @param iORemoveAttributeIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IORemoveAttribute", inTypeDesc = "void", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iORemoveAttribute() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iORemoveAttribute) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IORemoveOneElt". <br>
     * description:
     * 
     * @param iORemoveOneEltIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IORemoveOneElt", inTypeDesc = "void", outTypeDesc = "void")
    public void iORemoveOneElt() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iORemoveOneElt) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOReThrow". <br>
     * description:
     * 
     * @param iOReThrowIn
     *            DevVarLong64Array, Error facility
     * @return void, An exception
     */
    @Command(name = "IOReThrow", inTypeDesc = "Error facility", outTypeDesc = "An exception")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOReThrow(final DevVarLongStringArray iOReThrowIn) throws DevFailed {
	xlogger.entry();
	final int[] lValue = iOReThrowIn.lvalue;
	final String[] sValue = iOReThrowIn.svalue;
	final int lSize = lValue.length;
	final int sSize = sValue.length;
	final DevError[] errors = new DevError[lSize > sSize ? sSize : lSize];
	for (int i = 0; i < errors.length; i++) {
	    errors[i] = new DevError(sValue[i], ErrSeverity.from_int(lValue[i]), "test", "test");
	}
	xlogger.exit();
	throw new DevFailed(errors);
    }

    /**
     * Execute command "IOSeqVecChar". <br>
     * description:
     * 
     * @param iOSeqVecCharIn
     *            DevVarCharArray, Input char array
     * @return DevVarCharArray, Output char array
     */
    @Command(name = "IOSeqVecChar", inTypeDesc = "Input char array", outTypeDesc = "Output char array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public byte[] iOSeqVecChar(final byte[] iOSeqVecCharIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSeqVecChar) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return iOSeqVecCharIn;
    }

    /**
     * Execute command "IOSeqVecDouble". <br>
     * description:
     * 
     * @param iOSeqVecDoubleIn
     *            DevVarDoubleArray, Input double array
     * @return DevVarDoubleArray, Output double array
     */
    @Command(name = "IOSeqVecDouble", inTypeDesc = "Input double array", outTypeDesc = "Output double array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public double[] iOSeqVecDouble(final double[] iOSeqVecDoubleIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSeqVecDouble) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return iOSeqVecDoubleIn;
    }

    /**
     * Execute command "IOSeqVecFloat". <br>
     * description:
     * 
     * @param iOSeqVecFloatIn
     *            DevVarFloatArray, Input float array
     * @return DevVarFloatArray, Output float array
     */
    @Command(name = "IOSeqVecFloat", inTypeDesc = "Input float array", outTypeDesc = "Output float array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public float[] iOSeqVecFloat(final float[] iOSeqVecFloatIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSeqVecFloat) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return iOSeqVecFloatIn;
    }

    /**
     * Execute command "IOSeqVecLong". <br>
     * description:
     * 
     * @param iOSeqVecLongIn
     *            DevVarLongArray, Input long array
     * @return DevVarLongArray, Output long array
     */
    @Command(name = "IOSeqVecLong", inTypeDesc = "Input long array", outTypeDesc = "Output long array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int[] iOSeqVecLong(final int[] iOSeqVecLongIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSeqVecLong) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return iOSeqVecLongIn;
    }

    /**
     * Execute command "IOSeqVecShort". <br>
     * description:
     * 
     * @param iOSeqVecShortIn
     *            DevVarShortArray, Input short array
     * @return DevVarShortArray, Output short array
     */
    @Command(name = "IOSeqVecShort", inTypeDesc = "Input short array", outTypeDesc = "Output short array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public short[] iOSeqVecShort(final short[] iOSeqVecShortIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSeqVecShort) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return iOSeqVecShortIn;
    }

    /**
     * Execute command "IOSeqVecString". <br>
     * description:
     * 
     * @param iOSeqVecStringIn
     *            DevVarStringArray, Input string array
     * @return DevVarStringArray, Output string array
     */
    @Command(name = "IOSeqVecString", inTypeDesc = "Input string array", outTypeDesc = "Output string array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String[] iOSeqVecString(final String[] iOSeqVecStringIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSeqVecString) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return iOSeqVecStringIn;
    }

    /**
     * Execute command "IOSeqVecULong". <br>
     * description:
     * 
     * @param iOSeqVecULongIn
     *            DevVarULongArray, Input unsigned long array
     * @return DevVarULongArray, Output unsigned long array
     */
    // @Command(name = "IOSeqVecULong", inTypeDesc = "Input unsigned long array", outTypeDesc =
    // "Output unsigned long array")
    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM})
    // public int[] iOSeqVecULong(final int[] iOSeqVecULongIn) throws DevFailed {
    // xlogger.entry();
    // /* PROTECTED REGION ID(DevTest.iOSeqVecULong) ENABLED START */
    // // put command code here
    // /* PROTECTED REGION END */
    // xlogger.exit();
    // return iOSeqVecULongIn;
    // }

    /**
     * Execute command "IOSeqVecUShort". <br>
     * description:
     * 
     * @param iOSeqVecUShortIn
     *            DevVarUShortArray, Input unsigned short array
     * @return DevVarUShortArray, Output unsigned short array
     */
    // @Command(name = "IOSeqVecUShort", inTypeDesc = "Input unsigned short array", outTypeDesc =
    // "Output unsigned short array")
    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM})
    // public short[] iOSeqVecUShort(final short[] iOSeqVecUShortIn) throws DevFailed {
    // xlogger.entry();
    // /* PROTECTED REGION ID(DevTest.iOSeqVecUShort) ENABLED START */
    // // put command code here
    // /* PROTECTED REGION END */
    // xlogger.exit();
    // return iOSeqVecUShortIn;
    // }

    /**
     * Execute command "IOSetAttr". <br>
     * description:
     * 
     * @param iOSetAttrIn
     *            DevLong, New attr value
     * @return void, void
     */
    @Command(name = "IOSetAttr", inTypeDesc = "New attr value", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF })
    public void iOSetAttr(final int iOSetAttrIn) throws DevFailed {
	xlogger.entry();
	longattr = iOSetAttrIn;
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOSetWAttrLimit". <br>
     * description:
     * 
     * @param iOSetWAttrLimitIn
     *            DevVarDoubleArray, arr[0]==0 -> min_value, arr[0]==1 -> max_value, arr[1]=limit
     * @return void, void
     */
    @Command(name = "IOSetWAttrLimit", inTypeDesc = "arr[0]==0 -> min_value, arr[0]==1 -> max_value, arr[1]=limit", outTypeDesc = "void")
    public void iOSetWAttrLimit(final double[] iOSetWAttrLimitIn) throws DevFailed {
	xlogger.entry();
	final double value = iOSetWAttrLimitIn[0];
	final String limit = Double.toString(iOSetWAttrLimitIn[1]);
	final DeviceProxy proxy = new DeviceProxy("");
	final AttributeInfo info = proxy.get_attribute_info("Double_attr_w");
	if (value == 0) {
	    info.min_value = limit;
	} else if (value == 1) {
	    info.max_value = limit;
	}
	proxy.set_attribute_info(new AttributeInfo[] { info });
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOShort". <br>
     * description:
     * 
     * @param iOShortIn
     *            DevShort, Number
     * @return DevShort, Number * 2
     */
    @Command(name = "IOShort", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    public short iOShort(final short iOShortIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return (short) (iOShortIn * 2);
    }

    /**
     * Execute command "IOShortArray". <br>
     * description:
     * 
     * @param iOShortArrayIn
     *            DevVarShortArray, Array of short
     * @return DevVarShortArray, This array * 2
     */
    @Command(name = "IOShortArray", inTypeDesc = "Array of short", outTypeDesc = "This array * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public short[] iOShortArray(final short[] iOShortArrayIn) throws DevFailed {
	xlogger.entry();
	final int size = iOShortArrayIn.length;
	final short[] iOShortArrayOut = new short[size];
	for (int i = 0; i < size; i++) {
	    iOShortArrayOut[i] = (short) (iOShortArrayIn[i] * 2);
	}
	xlogger.exit();
	return iOShortArrayOut;
    }

    /**
     * Execute command "IOShortSleep". <br>
     * description:
     * 
     * @param iOShortSleepIn
     *            DevVarShortArray, Input short and sleeping time in sec
     * @return DevShort, Output short (in * 2)
     */
    @Command(name = "IOShortSleep", inTypeDesc = "Input short and sleeping time in sec", outTypeDesc = "Output short (in * 2)")
    public short iOShortSleep(final short[] iOShortSleepIn) throws DevFailed {
	xlogger.entry();
	final short outValue = (short) (iOShortSleepIn[0] * 2);
	final short sleepTime = iOShortSleepIn[1];
	try {
	    Thread.sleep(sleepTime);
	} catch (final InterruptedException e) {
	    throw new DevFailed(e.toString(), null);
	}
	xlogger.exit();
	return outValue;
    }

    /**
     * Execute command "IOSleep". <br>
     * description:
     * 
     * @param iOSleepIn
     *            DevUShort, sleeping time
     * @return void, void
     */
    @Command(name = "IOSleep", inTypeDesc = "sleeping time", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOSleep(final short iOSleepIn) throws DevFailed {
	xlogger.entry();
	try {
	    Thread.sleep(iOSleepIn);
	} catch (final InterruptedException e) {
	    throw new DevFailed(e.toString(), null);
	}
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOSleepExcept". <br>
     * description:
     * 
     * @param iOSleepExceptIn
     *            DevShort, Sleep time (sec)
     * @return void, An exception
     */
    @Command(name = "IOSleepExcept", inTypeDesc = "Sleep time (sec)", outTypeDesc = "An exception")
    public void iOSleepExcept(final short iOSleepExceptIn) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	throw new DevFailed(new DevError[] { new DevError("aaa", ErrSeverity.ERR, "This is a test",
		"IOSleepExcept::execute") });
    }

    /**
     * Execute command "IOStartPoll". <br>
     * description:
     * 
     * @param iOStartPollIn
     *            void, Void
     * @return DevLong, Constant number set to 11
     */
    @Command(name = "IOStartPoll", inTypeDesc = "Void", outTypeDesc = "Constant number set to 11")
    public int iOStartPoll() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return 11;
    }

    /**
     * Execute command "IOState". <br>
     * description:
     * 
     * @param iOStateIn
     *            DevState, New device state
     * @return void, void
     */
    @Command(name = "IOState", inTypeDesc = "New device state", outTypeDesc = "void")
    public void iOState(final DevState iOStateIn) throws DevFailed {
	xlogger.entry();
	setState(iOStateIn);
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOStr1". <br>
     * description:
     * 
     * @param iOStr1In
     *            void, void
     * @return DevString, Just a string dynamically allocated
     */
    @Command(name = "IOStr1", inTypeDesc = "void", outTypeDesc = "Just a string dynamically allocated")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iOStr1() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return "Hello from IOStr1";
    }

    /**
     * Execute command "IOStr2". <br>
     * description:
     * 
     * @param iOStr2In
     *            void, void
     * @return DevString, A constant string
     */
    @Command(name = "IOStr2", inTypeDesc = "void", outTypeDesc = "A constant string")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iOStr2() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return "Hello from IOStr2";
    }

    /**
     * Execute command "IOStrArray". <br>
     * description:
     * 
     * @param iOStrArrayIn
     *            void, void
     * @return DevVarStringArray, A string array
     */
    @Command(name = "IOStrArray", inTypeDesc = "void", outTypeDesc = "A string array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String[] iOStrArray() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new String[] { "First string from dev_string_array", "Second string from dev_string_array" };
    }

    /**
     * Execute command "IOString". <br>
     * description:
     * 
     * @param iOStringIn
     *            DevString, Word
     * @return DevString, the pallindrome is
     */
    @Command(name = "IOString", inTypeDesc = "Word", outTypeDesc = "the pallindrome is")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String iOString(final String iOStringIn) throws DevFailed {
	xlogger.entry();
	StringBuffer buffer = new StringBuffer(iOStringIn);
	buffer = buffer.reverse();
	xlogger.exit();
	return buffer.toString();
    }

    /**
     * Execute command "IOStringArray". <br>
     * description:
     * 
     * @param iOStringArrayIn
     *            DevVarStringArray, Array of string
     * @return DevVarStringArray, This reversed array
     */
    @Command(name = "IOStringArray", inTypeDesc = "Array of string", outTypeDesc = "This reversed array ")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public String[] iOStringArray(final String[] iOStringArrayIn) throws DevFailed {
	xlogger.entry();
	final int size = iOStringArrayIn.length;
	int j = size - 1;
	final String[] iOStringArrayOut = new String[size];
	for (int i = 0; i < size; i++, j--) {
	    iOStringArrayOut[i] = iOStringArrayIn[j];
	}
	xlogger.exit();
	return iOStringArrayOut;
    }

    /**
     * Execute command "IOStruct". <br>
     * description:
     * 
     * @param iOStructIn
     *            void, void
     * @return DevVarLong64Array, A structure type
     */
    @Command(name = "IOStruct", inTypeDesc = "void", outTypeDesc = "A structure type")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public DevVarLongStringArray iOStruct() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new DevVarLongStringArray(new int[] { 1000, 2000 }, new String[] { "First string from dev_struct",
		"Second string from dev_struct" });
    }

    /**
     * Execute command "IOSubscribeEvent". <br>
     * description:
     * 
     * @param iOSubscribeEventIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOSubscribeEvent", inTypeDesc = "void", outTypeDesc = "void")
    public void iOSubscribeEvent() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOSubscribeEvent) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOTempl". <br>
     * description:
     * 
     * 
     * @return void, void
     */
    @Command(name = "IOTempl", inTypeDesc = "void", outTypeDesc = "void")
    public void iOTempl() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOTemplIn". <br>
     * description: print the in
     * 
     * @return void, void
     */
    @Command(name = "IOTemplIn", inTypeDesc = "the long to print", outTypeDesc = "void")
    public void iOTemplIn(final int in) throws DevFailed {
	xlogger.entry();
	// System.out.println(in);
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOTemplInOut". <br>
     * 
     * @return
     * 
     * 
     * @return double, in in*2
     */
    @Command(name = "IOTemplInOut", inTypeDesc = "double", outTypeDesc = "double array")
    public double[] iOTemplInOut(final double in) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new double[] { in, in * 2 };
    }

    /**
     * Execute command "IOTemplInOutState". <br>
     * 
     * @return
     * 
     * 
     * @return double, in in*2
     */
    @Command(name = "IOTemplInOutState", inTypeDesc = "double", outTypeDesc = "double array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM, DeviceState.CLOSE,
	    DeviceState.DISABLE, DeviceState.EXTRACT, DeviceState.INIT, DeviceState.INSERT, DeviceState.MOVING,
	    DeviceState.OPEN, DeviceState.RUNNING, DeviceState.STANDBY, DeviceState.UNKNOWN })
    public double[] iOTemplInOutState(final double in) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new double[] { in, in * 2 };
    }

    /**
     * Execute command "IOTemplInState". <br>
     * description: print the in
     * 
     * @return void, void
     */
    @Command(name = "IOTemplInState", inTypeDesc = "the long to print", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM, DeviceState.CLOSE,
	    DeviceState.DISABLE, DeviceState.EXTRACT, DeviceState.INIT, DeviceState.INSERT, DeviceState.MOVING,
	    DeviceState.OPEN, DeviceState.RUNNING, DeviceState.STANDBY, DeviceState.UNKNOWN })
    public void iOTemplInState(final int in) throws DevFailed {
	xlogger.entry();
	// System.out.println(in);
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOTemplOut". <br>
     * 
     * @return
     * 
     * 
     * @return long[], { 10, 20, 30, 40 }
     */
    @Command(name = "IOTemplOut", inTypeDesc = "void", outTypeDesc = "long array")
    public int[] iOTemplOut() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new int[] { 10, 20, 30, 40 };
    }

    /**
     * Execute command "IOTemplOutState". <br>
     * 
     * @return
     * 
     * 
     * @return long[], { 10, 20, 30, 40 }
     */
    @Command(name = "IOTemplOutState", inTypeDesc = "void", outTypeDesc = "long array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM, DeviceState.CLOSE,
	    DeviceState.DISABLE, DeviceState.EXTRACT, DeviceState.INIT, DeviceState.INSERT, DeviceState.MOVING,
	    DeviceState.OPEN, DeviceState.RUNNING, DeviceState.STANDBY, DeviceState.UNKNOWN })
    public int[] iOTemplOutState() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return new int[] { 10, 20, 30, 40 };
    }

    /**
     * Execute command "IOTemplState". <br>
     * description:
     * 
     * 
     * @return void, void
     */
    @Command(name = "IOTemplState", inTypeDesc = "void", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM, DeviceState.CLOSE,
	    DeviceState.DISABLE, DeviceState.EXTRACT, DeviceState.INIT, DeviceState.INSERT, DeviceState.MOVING,
	    DeviceState.OPEN, DeviceState.RUNNING, DeviceState.STANDBY, DeviceState.UNKNOWN })
    public void iOTemplState() throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOThrow". <br>
     * description:
     * 
     * @param iOThrowIn
     *            DevVarLong64Array, Error facility
     * @return void, An exception
     */
    @Command(name = "IOThrow", inTypeDesc = "Error facility", outTypeDesc = "An exception")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOThrow(final DevVarLongStringArray iOThrowIn) throws DevFailed {
	xlogger.entry();
	final int[] lValue = iOThrowIn.lvalue;
	final String[] sValue = iOThrowIn.svalue;
	final DevError[] errors = new DevError[1];
	errors[0] = new DevError(sValue[0], ErrSeverity.from_int(lValue[0]), "test", "test");
	xlogger.exit();
	throw new DevFailed(errors);
    }

    /**
     * Execute command "IOTrigPoll". <br>
     * description:
     * 
     * @param iOTrigPollIn
     *            DevString, Command's name to trig polling
     * @return void, void
     */
    @Command(name = "IOTrigPoll", inTypeDesc = "Command's name to trig polling", outTypeDesc = "void")
    public void iOTrigPoll(final String iOTrigPollIn) throws DevFailed {
	xlogger.entry();
	deviceManager.triggerPolling(iOTrigPollIn);
	// final DeviceData dd = new DeviceData();
	// dd.insert(new String[] { MDC.get("deviceName"), iOTrigPollIn });
	// new DeviceProxy(ServerManager.getInstance().getAdminDeviceName()).command_inout("TriggerPolling", dd);

	xlogger.exit();
    }

    /**
     * Execute command "IOULong". <br>
     * description:
     * 
     * @param iOULongIn
     *            DevULong, Number
     * @return DevULong, Number * 2
     */
    // @Command(name = "IOULong", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    // public int iOULong(final int iOULongIn) throws DevFailed {
    // xlogger.entry();
    // xlogger.exit();
    // return iOULongIn * 2;
    // }

    /**
     * Execute command "IOULong64". <br>
     * description:
     * 
     * @param iOULong64In
     *            DevULong64, Number
     * @return DevULong64, Number * 2
     */
    @Command(name = "IOULong64", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public long iOULong64(final long iOULong64In) throws DevFailed {
	xlogger.entry();
	xlogger.exit();
	return iOULong64In * 2;
    }

    // /**
    // * Execute command "IOULongArray". <br>
    // * description:
    // *
    // * @param iOULongArrayIn
    // * DevVarULongArray, Array of unsigned long
    // * @return DevVarULongArray, This array * 2
    // */
    // @Command(name = "IOULongArray", inTypeDesc = "Array of unsigned long", outTypeDesc = "This array * 2")
    // public int[] iOULongArray(final int[] iOULongArrayIn) throws DevFailed {
    // xlogger.entry();
    // final int size = iOULongArrayIn.length;
    // final int[] iOULongArrayOut = new int[size];
    // for (int i = 0; i < size; i++) {
    // iOULongArrayOut[i] = iOULongArrayIn[i] * 2;
    // }
    // xlogger.exit();
    // return iOULongArrayOut;
    // }

    /**
     * Execute command "IOUnregClassSig". <br>
     * description:
     * 
     * @param iOUnregClassSigIn
     *            DevLong, Signal number
     * @return void, void
     */
    @Command(name = "IOUnregClassSig", inTypeDesc = "Signal number", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOUnregClassSig(final int iOUnregClassSigIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOUnregClassSig) ENABLED START */
	// not possible to remove a signal handler in java
	DevTestSignalHandler.checkSignal(iOUnregClassSigIn);
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOUnregSig". <br>
     * description:
     * 
     * @param iOUnregSigIn
     *            DevLong, Signal number
     * @return void, void
     */
    @Command(name = "IOUnregSig", inTypeDesc = "Signal number", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOUnregSig(final int iOUnregSigIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOUnregSig) ENABLED START */
	// not possible to remove a signal handler in java
	DevTestSignalHandler.checkSignal(iOUnregSigIn);
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Execute command "IOUnSubscribeEvent". <br>
     * description:
     * 
     * @param iOUnSubscribeEventIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOUnSubscribeEvent", inTypeDesc = "void", outTypeDesc = "void")
    public void iOUnSubscribeEvent() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.iOUnSubscribeEvent) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    @Command(name = "SetGetProperties")
    public String[] setGetProperties() throws DevFailed {
	final String[] result = new String[20];
	final DeviceProxy me = new DeviceProxy(MDC.get("deviceName"));
	/*
	 * attr.get_properties(multi_prop); multi_prop.label = "Test_label"; multi_prop.description =
	 * "Test_description"; multi_prop.unit = "Test_description"; multi_prop.standard_unit = "Test_standard_unit";
	 * multi_prop.display_unit = "Test_display_unit"; multi_prop.format = "Test_format"; multi_prop.min_value = "0";
	 * multi_prop.max_value = "200"; multi_prop.min_alarm = "10"; multi_prop.max_alarm = "190";
	 * multi_prop.min_warning = "20"; multi_prop.max_warning = "180"; multi_prop.delta_t = "5"; multi_prop.delta_val
	 * = "10"; multi_prop.event_period = "300"; multi_prop.archive_period = "400"; multi_prop.rel_change =
	 * "0.2,0.3"; multi_prop.abs_change = "40,50"; multi_prop.archive_rel_change = "0.6,0.7";
	 * multi_prop.archive_abs_change = "80,90";
	 */
	// Double_attr
	final AttributeInfoEx infoBackup = me.get_attribute_info_ex("Double_attr_w");
	AttributeInfoEx info = me.get_attribute_info_ex("Double_attr_w");
	info.label = "Test_label";
	info.description = "Test_description";
	info.unit = "Test_description";
	info.standard_unit = "Test_standard_unit";
	info.display_unit = "Test_display_unit";
	info.format = "Test_format";
	info.min_value = "0";
	info.max_value = "200";
	info.alarms.min_alarm = "10";
	info.alarms.max_alarm = "190";
	info.alarms.min_warning = "20";
	info.alarms.max_warning = "180";
	info.alarms.delta_t = "5";
	info.alarms.delta_val = "10";
	info.events.per_event.period = "300";
	info.events.arch_event.period = "400";
	info.events.ch_event.rel_change = "0.2,0.3";
	info.events.ch_event.abs_change = "40,50";
	info.events.arch_event.rel_change = "0.6,0.7";
	info.events.arch_event.abs_change = "80,90";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Double_attr_w");
	int i = 0;
	result[i++] = info.label;
	result[i++] = info.description;
	result[i++] = info.unit;
	result[i++] = info.standard_unit;
	result[i++] = info.display_unit;
	result[i++] = info.format;
	result[i++] = info.min_value;
	result[i++] = info.max_value;
	result[i++] = info.alarms.min_alarm;
	result[i++] = info.alarms.max_alarm;
	result[i++] = info.alarms.min_warning;
	result[i++] = info.alarms.max_warning;
	result[i++] = info.alarms.delta_t;
	result[i++] = info.alarms.delta_val;
	result[i++] = info.events.per_event.period;
	result[i++] = info.events.arch_event.period;
	result[i++] = info.events.ch_event.rel_change;
	result[i++] = info.events.ch_event.abs_change;
	result[i++] = info.events.arch_event.rel_change;
	result[i++] = info.events.arch_event.abs_change;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	return result;
    }

    @Command(name = "SetGetRanges")
    public String[] setGetRanges() throws DevFailed {
	final String[] result = new String[27];
	final DeviceProxy me = new DeviceProxy(MDC.get("deviceName"));
	// Double_attr
	AttributeInfoEx infoBackup = me.get_attribute_info_ex("Double_attr_w");
	AttributeInfoEx info = me.get_attribute_info_ex("Double_attr_w");
	info.min_value = "-1111.11";
	info.max_value = "1111.11";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Double_attr_w");
	result[0] = "Double_attr_w";
	result[1] = info.min_value;
	result[2] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Float_attr
	infoBackup = me.get_attribute_info_ex("Float_attr_w");
	info = me.get_attribute_info_ex("Float_attr_w");
	info.min_value = "-888.88";
	info.max_value = "888.88";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Float_attr_w");
	result[3] = "Float_attr_w";
	result[4] = info.min_value;
	result[5] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Long_attr
	infoBackup = me.get_attribute_info_ex("Long_attr_w");
	info = me.get_attribute_info_ex("Long_attr_w");
	info.min_value = "900";
	info.max_value = "1600";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Long_attr_w");
	result[6] = "Long_attr_w";
	result[7] = info.min_value;
	result[8] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Long64_attr
	infoBackup = me.get_attribute_info_ex("Long64_attr_rw");
	info = me.get_attribute_info_ex("Long64_attr_rw");
	info.min_value = "-100000";
	info.max_value = "100000";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Long64_attr_rw");
	result[9] = "Long64_attr_rw";
	result[10] = info.min_value;
	result[11] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Short_attr
	infoBackup = me.get_attribute_info_ex("Short_attr_w");
	info = me.get_attribute_info_ex("Short_attr_w");
	info.min_value = "-6000";
	info.max_value = "6000";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Short_attr_w");
	result[12] = "Short_attr_w";
	result[13] = info.min_value;
	result[14] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// UChar_attr
	infoBackup = me.get_attribute_info_ex("UChar_attr_w");
	info = me.get_attribute_info_ex("UChar_attr_w");
	info.min_value = "0";
	info.max_value = "250";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("UChar_attr_w");
	result[15] = "UChar_attr_w";
	result[16] = info.min_value;
	result[17] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// ULong_attr_rw
	infoBackup = me.get_attribute_info_ex("ULong_attr_rw");
	info = me.get_attribute_info_ex("ULong_attr_rw");
	info.min_value = "0";
	info.max_value = "888888";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("ULong_attr_rw");
	result[18] = "ULong_attr_rw";
	result[19] = info.min_value;
	result[20] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// ULong64_attr
	infoBackup = me.get_attribute_info_ex("ULong64_attr_rw");
	info = me.get_attribute_info_ex("ULong64_attr_rw");
	info.min_value = "0";
	info.max_value = "99999999";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("ULong64_attr_rw");
	result[21] = "ULong64_attr_rw";
	result[22] = info.min_value;
	result[23] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// UShort_attr
	infoBackup = me.get_attribute_info_ex("UShort_attr_w");
	info = me.get_attribute_info_ex("UShort_attr_w");
	info.min_value = "0";
	info.max_value = "40000";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("UShort_attr_w");
	result[24] = "UShort_attr_w";
	result[25] = info.min_value;
	result[26] = info.max_value;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	return result;
    }

    @Command(name = "SetGetAlarms")
    public String[] setGetAlarms() throws DevFailed {
	final String[] result = new String[45];
	final DeviceProxy me = new DeviceProxy(MDC.get("deviceName"));
	// Double_attr
	AttributeInfoEx infoBackup = me.get_attribute_info_ex("Double_attr");
	AttributeInfoEx info = me.get_attribute_info_ex("Double_attr");
	info.alarms.min_alarm = "-999.99";
	info.alarms.max_alarm = "-888.88";
	info.alarms.min_warning = "888.88";
	info.alarms.max_warning = "999.99";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Double_attr");
	result[0] = "Double_attr";
	result[1] = info.alarms.min_alarm;
	result[2] = info.alarms.max_alarm;
	result[3] = info.alarms.min_warning;
	result[4] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Float_attr
	infoBackup = me.get_attribute_info_ex("Float_attr");
	info = me.get_attribute_info_ex("Float_attr");
	info.alarms.min_alarm = "-777.77";
	info.alarms.max_alarm = "-666.66";
	info.alarms.min_warning = "666.66";
	info.alarms.max_warning = "777.77";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Float_attr");
	result[5] = "Float_attr";
	result[6] = info.alarms.min_alarm;
	result[7] = info.alarms.max_alarm;
	result[8] = info.alarms.min_warning;
	result[9] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Long_attr
	infoBackup = me.get_attribute_info_ex("Long_attr");
	info = me.get_attribute_info_ex("Long_attr");
	info.alarms.min_alarm = "1000";
	info.alarms.max_alarm = "1100";
	info.alarms.min_warning = "1400";
	info.alarms.max_warning = "1500";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Long_attr");
	result[10] = "Long_attr";
	result[11] = info.alarms.min_alarm;
	result[12] = info.alarms.max_alarm;
	result[13] = info.alarms.min_warning;
	result[14] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Long64_attr
	infoBackup = me.get_attribute_info_ex("Long64_attr");
	info = me.get_attribute_info_ex("Long64_attr");
	info.alarms.min_alarm = "-90000";
	info.alarms.max_alarm = "-80000";
	info.alarms.min_warning = "80000";
	info.alarms.max_warning = "90000";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Long64_attr");
	result[15] = "Long64_attr";
	result[16] = info.alarms.min_alarm;
	result[17] = info.alarms.max_alarm;
	result[18] = info.alarms.min_warning;
	result[19] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// Short_attr
	infoBackup = me.get_attribute_info_ex("Short_attr");
	info = me.get_attribute_info_ex("Short_attr");
	info.alarms.min_alarm = "-5000";
	info.alarms.max_alarm = "-4000";
	info.alarms.min_warning = "4000";
	info.alarms.max_warning = "5000";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("Short_attr");
	result[20] = "Short_attr";
	result[21] = info.alarms.min_alarm;
	result[22] = info.alarms.max_alarm;
	result[23] = info.alarms.min_warning;
	result[24] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// UChar_attr
	infoBackup = me.get_attribute_info_ex("UChar_attr");
	info = me.get_attribute_info_ex("UChar_attr");
	info.alarms.min_alarm = "1";
	info.alarms.max_alarm = "2";
	info.alarms.min_warning = "230";
	info.alarms.max_warning = "240";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("UChar_attr");
	result[25] = "UChar_attr";
	result[26] = info.alarms.min_alarm;
	result[27] = info.alarms.max_alarm;
	result[28] = info.alarms.min_warning;
	result[29] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// ULong_attr
	infoBackup = me.get_attribute_info_ex("ULong_attr");
	info = me.get_attribute_info_ex("ULong_attr");
	info.alarms.min_alarm = "1";
	info.alarms.max_alarm = "2";
	info.alarms.min_warning = "666666";
	info.alarms.max_warning = "777777";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("ULong_attr");
	result[30] = "ULong_attr";
	result[31] = info.alarms.min_alarm;
	result[32] = info.alarms.max_alarm;
	result[33] = info.alarms.min_warning;
	result[34] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// ULong64_attr
	infoBackup = me.get_attribute_info_ex("ULong64_attr");
	info = me.get_attribute_info_ex("ULong64_attr");
	info.alarms.min_alarm = "1";
	info.alarms.max_alarm = "2";
	info.alarms.min_warning = "77777777";
	info.alarms.max_warning = "88888888";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("ULong64_attr");
	result[35] = "ULong64_attr";
	result[36] = info.alarms.min_alarm;
	result[37] = info.alarms.max_alarm;
	result[38] = info.alarms.min_warning;
	result[39] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	// UShort_attr
	infoBackup = me.get_attribute_info_ex("UShort_attr");
	info = me.get_attribute_info_ex("UShort_attr");
	info.alarms.min_alarm = "1";
	info.alarms.max_alarm = "2";
	info.alarms.min_warning = "20000";
	info.alarms.max_warning = "30000";
	me.set_attribute_info(new AttributeInfoEx[] { info });
	info = me.get_attribute_info_ex("UShort_attr");
	result[40] = "UShort_attr";
	result[41] = info.alarms.min_alarm;
	result[42] = info.alarms.max_alarm;
	result[43] = info.alarms.min_warning;
	result[44] = info.alarms.max_warning;
	me.set_attribute_info(new AttributeInfoEx[] { infoBackup });
	return result;

    }

    // /**
    // * Execute command "IOUShort". <br>
    // * description:
    // *
    // * @param iOUShortIn
    // * DevUShort, Number
    // * @return DevUShort, Number * 2
    // */
    // @Command(name = "IOUShort", inTypeDesc = "Number", outTypeDesc = "Number * 2")
    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    // public short iOUShort(final short iOUShortIn) throws DevFailed {
    // xlogger.entry();
    // xlogger.exit();
    // return (short) (iOUShortIn * 2);
    // }

    // /**
    // * Execute command "IOUShortArray". <br>
    // * description:
    // *
    // * @param iOUShortArrayIn
    // * DevVarUShortArray, Array of unsigned short
    // * @return DevVarUShortArray, This array * 2
    // */
    // @Command(name = "IOUShortArray", inTypeDesc = "Array of unsigned short", outTypeDesc = "This array * 2")
    // @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    // public short[] iOUShortArray(final short[] iOUShortArrayIn) throws DevFailed {
    // xlogger.entry();
    // final int size = iOUShortArrayIn.length;
    // final short[] iOUShortArrayOut = new short[size];
    // for (int i = 0; i < size; i++) {
    // iOUShortArrayOut[i] = (short) (iOUShortArrayIn[i] * 2);
    // }
    // xlogger.exit();
    // return iOUShortArrayOut;
    // }

    /**
     * Execute command "IOVoid". <br>
     * description:
     * 
     * @param iOVoidIn
     *            void, void
     * @return void, void
     */
    @Command(name = "IOVoid", inTypeDesc = "void", outTypeDesc = "void")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public void iOVoid() throws DevFailed {
	xlogger.entry();
	// Does nothing
	xlogger.exit();
	return;
    }

    /**
     * Execute command "OLong". <br>
     * description:
     * 
     * @param oLongIn
     *            void, void
     * @return DevLong, A long
     */
    @Command(name = "OLong", inTypeDesc = "void", outTypeDesc = "A long")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int oLong() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.oLong) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return 0;
    }

    /**
     * Execute command "OLongArray". <br>
     * description:
     * 
     * @param oLongArrayIn
     *            void, void
     * @return DevVarLongArray, A long array
     */
    @Command(name = "OLongArray", inTypeDesc = "void", outTypeDesc = "A long array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int[] oLongArray() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.oLongArray) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return new int[0];
    }

    /**
     * Execute command "OLongString". <br>
     * description:
     * 
     * @param oLongStringIn
     *            void, void
     * @return DevVarLong64Array, A unsigned long and string array
     */
    @Command(name = "OLongString", inTypeDesc = "void", outTypeDesc = "A unsigned long and string array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public long[] oLongString() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.oLongString) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return new long[0];
    }

    /**
     * Execute command "OULong". <br>
     * description:
     * 
     * @param oULongIn
     *            void, void
     * @return DevULong, An unsigned long
     */
    @Command(name = "OULong", inTypeDesc = "void", outTypeDesc = "An unsigned long")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int oULong() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.oULong) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return 0;
    }

    /**
     * Execute command "OULongArray". <br>
     * description:
     * 
     * @param oULongArrayIn
     *            void, void
     * @return DevVarULongArray, A unsigned long array
     */
    @Command(name = "OULongArray", inTypeDesc = "void", outTypeDesc = "A unsigned long array")
    @StateMachine(deniedStates = { DeviceState.FAULT, DeviceState.OFF, DeviceState.ALARM })
    public int[] oULongArray() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.oULongArray) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return new int[0];
    }

    /**
     * Execute command "PushDataReady". <br>
     * description:
     * 
     * @param pushDataReadyIn
     *            DevVarLong64Array, s[0] = attribute name, l[0] = user counter
     * @return void, void
     */
    @Command(name = "PushDataReady", inTypeDesc = "s[0] = attribute name, l[0] = user counter", outTypeDesc = "void")
    public void pushDataReady(final long[] pushDataReadyIn) throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DevTest.pushDataReady) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return;
    }

    /**
     * Write attribute attr_asyn_write
     * 
     * @param attrasynwrite
     *            value to write
     */
    public void setAttrasynwrite(final int attrasynwrite) {
	xlogger.entry();
	this.attrasynwrite = attrasynwrite;
	/* PROTECTED REGION ID(DevTest.setattr_asyn_write) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute attr_asyn_write_except
     * 
     * @param attrasynwriteexcept
     *            value to write
     */
    public void setAttrasynwriteexcept(final int attrasynwriteexcept) {
	xlogger.entry();
	this.attrasynwriteexcept = attrasynwriteexcept;
	/* PROTECTED REGION ID(DevTest.setattr_asyn_write_except) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute attr_asyn_write_to
     * 
     * @param attrasynwriteto
     *            value to write
     */
    public void setAttrasynwriteto(final int attrasynwriteto) {
	xlogger.entry();
	this.attrasynwriteto = attrasynwriteto;
	/* PROTECTED REGION ID(DevTest.setattr_asyn_write_to) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute Boolean_attr_w
     * 
     * @param booleanattrw
     *            value to write
     */
    public void setBooleanattrw(final boolean booleanattrw) {
	xlogger.entry();
	this.booleanattrw = booleanattrw;
	xlogger.exit();
    }

    /**
     * Write attribute Boolean_ima_attr_rw
     * 
     * @param booleanimaattrrw
     *            value to write
     */
    public void setBooleanimaattrrw(final boolean[][] booleanimaattrrw) {
	xlogger.entry();
	// this.booleanimaattrrw = booleanimaattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute Boolean_spec_attr_w
     * 
     * @param booleanspecattrw
     *            value to write
     */
    public void setBooleanspecattrw(final boolean[] booleanspecattrw) {
	xlogger.entry();
	this.booleanspecattrw = Arrays.copyOf(booleanspecattrw, booleanspecattrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute Double_attr_w
     * 
     * @param doubleattrw
     *            value to write
     */
    // public void setDoubleattrw(final double doubleattrw) {
    // xlogger.entry();
    // this.doubleattrw = doubleattrw;
    // /* PROTECTED REGION ID(DevTest.setDouble_attr_w) ENABLED START */
    // // put write attribute code here
    // /* PROTECTED REGION END */
    // xlogger.exit();
    // }

    /**
     * Write attribute Double_spec_attr_w
     * 
     * @param doublespecattrw
     *            value to write
     */
    public void setDoublespecattrw(final double[] doublespecattrw) {
	xlogger.entry();
	this.doublespecattrw = Arrays.copyOf(doublespecattrw, doublespecattrw.length);
	xlogger.exit();
    }

    public void setDynamicManager(final DynamicManager dynamicManager) throws DevFailed {
	this.dynamicManager = dynamicManager;
	dynamicManager.addAttribute(new UShortAttrW());
	dynamicManager.addAttribute(new LongAttrW());
	dynamicManager.addAttribute(new DoubleAttrW());
	dynamicManager.addAttribute(new StringAttrW());
	dynamicManager.addAttribute(new UShortAttr());
	dynamicManager.addAttribute(new UCharAttr());
	dynamicManager.addAttribute(new ULongAttrRW());
	dynamicManager.addAttribute(new ULong64AttrRW());
	dynamicManager.addAttribute(new UShortAttrSpectrum());
	dynamicManager.addAttribute(new ULongAttrSpectrum());
	dynamicManager.addAttribute(new ULong64AttrSpectrum());
	dynamicManager.addAttribute(new UShortSpecAttrW());
	dynamicManager.addAttribute(new UShortImageAttr());
	dynamicManager.addAttribute(new UShortImageAttrW());
	dynamicManager.addAttribute(new ULongAttr());
	dynamicManager.addAttribute(new ULong64Attr());
	pollbuffRW = new PollbuffRW();
	dynamicManager.addAttribute(pollbuffRW);

	dynamicManager.addCommand(new IOSeqVecUShortCmd());
	dynamicManager.addCommand(new IOSeqVecULongCmd());
	dynamicManager.addCommand(new IOUShortCmd());
	dynamicManager.addCommand(new IOULongCmd());
	dynamicManager.addCommand(new IOUShortArrayCmd());
	dynamicManager.addCommand(new IOULongArrayCmd());
    }

    // /**
    // * Write attribute Encoded_attr
    // *
    // * @param encodedattr
    // * value to write
    // */
    // public void setEncodedattr(final DevEncoded encodedattr) {
    // xlogger.entry();
    // this.encodedattr = encodedattr;
    // /* PROTECTED REGION ID(DevTest.setEncoded_attr) ENABLED START */
    // // put write attribute code here
    // /* PROTECTED REGION END */
    // xlogger.exit();
    // }

    /**
     * Write attribute fast_actuator
     * 
     * @param fastactuator
     *            value to write
     */
    public void setFastactuator(final double fastactuator) {
	xlogger.entry();
	this.fastactuator = fastactuator;
	/* PROTECTED REGION ID(DevTest.setfast_actuator) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute Float_attr_w
     * 
     * @param floatattrw
     *            value to write
     * @throws DevFailed
     */
    public void setFloatattrw(final float floatattrw) throws DevFailed {
	xlogger.entry();
	if (Float.isNaN(floatattrw)) {
	    DevFailedUtils.throwDevFailed("Nan not allowed");
	}
	this.floatattrw = floatattrw;
	xlogger.exit();
    }

    /**
     * Write attribute Float_ima_attr_w
     * 
     * @param floatimaattrw
     *            value to write
     */
    public void setFloatimaattrw(final float[][] floatimaattrw) {
	xlogger.entry();
	this.floatimaattrw = Arrays.copyOf(floatimaattrw, floatimaattrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute Float_spec_attr_rw
     * 
     * @param floatspecattrrw
     *            value to write
     */
    public void setFloatspecattrrw(final float[] floatspecattrrw) {
	xlogger.entry();
	// this.floatspecattrrw = floatspecattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute Float_spec_attr_w
     * 
     * @param floatspecattrw
     *            value to write
     */
    public void setFloatspecattrw(final float[] floatspecattrw) {
	xlogger.entry();
	this.floatspecattrw = Arrays.copyOf(floatspecattrw, floatspecattrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute Long64_attr_rw
     * 
     * @param long64attrrw
     *            value to write
     */
    public void setLong64attrrw(final long long64attrrw) {
	xlogger.entry();
	this.long64attrrw = long64attrrw;
	xlogger.exit();
    }

    /**
     * Write attribute Long64_spec_attr_rw
     * 
     * @param long64specattrrw
     *            value to write
     */
    public void setLong64specattrrw(final long[] long64specattrrw) {
	xlogger.entry();
	// this.long64specattrrw = long64specattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute Long_attr_w
     * 
     * @param longattrw
     *            value to write
     */
    // public void setLongattrw(final int longattrw) {
    // xlogger.entry();
    // this.longattrw = longattrw;
    // xlogger.exit();
    // }

    /**
     * Write attribute Long_attr_with_w
     * 
     * @param longattrwithw
     *            value to write
     */
    public void setLongattrwithw(final int longattrwithw) {
	xlogger.entry();
	// DO NOTHING
	xlogger.exit();
    }

    /**
     * Write attribute Long_spec_attr_rw
     * 
     * @param longspecattrrw
     *            value to write
     */
    public void setLongspecattrrw(final int[] longspecattrrw) {
	xlogger.entry();
	this.longspecattrrw = Arrays.copyOf(longspecattrrw, longspecattrrw.length);
	/* PROTECTED REGION ID(DevTest.setLong_spec_attr_rw) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute Long_spec_attr_w
     * 
     * @param longspecattrw
     *            value to write
     */
    public void setLongspecattrw(final int[] longspecattrw) {
	xlogger.entry();
	this.longspecattrw = Arrays.copyOf(longspecattrw, longspecattrw.length);
	xlogger.exit();
    }

    // /**
    // * Write attribute Poll_buffRW
    // *
    // * @param pollbuffRW
    // * value to write
    // */
    // public void setPollbuffRW(final String[] pollbuffRW) {
    // xlogger.entry();
    // this.pollbuffRW = pollbuffRW;
    // /* PROTECTED REGION ID(DevTest.setPoll_buffRW) ENABLED START */
    // // put write attribute code here
    // /* PROTECTED REGION END */
    // xlogger.exit();
    // }

    /**
     * Write attribute Short_attr_rw
     * 
     * @param rw
     *            value to write
     */
    public void setShortattrrw(final short shortattrrw) {
	xlogger.entry();
	this.shortattrrw = shortattrrw;
	/* PROTECTED REGION ID(DevTest.setShort_attr_rw) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute Short_attr_w
     * 
     * @param shortattrw
     *            value to write
     */
    public void setShortattrw(final short shortattrw) {
	xlogger.entry();
	this.shortattrw = shortattrw;
	/* PROTECTED REGION ID(DevTest.setShort_attr_w) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute Short_attr_w2
     * 
     * @param shortattrw2
     *            value to write
     */
    public void setShortattrw2(final short shortattrw2) {
	xlogger.entry();
	this.shortattrw2 = shortattrw2;
	/* PROTECTED REGION ID(DevTest.setShort_attr_w2) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute Short_attr_with_w
     * 
     * @param shortattrwithw
     *            value to write
     */
    public void setShortattrwithw(final short shortattrwithw) {
	xlogger.entry();
	// DO NOTHING
	xlogger.exit();
    }

    /**
     * Write attribute Short_ima_attr_rw
     * 
     * @param shortimaattrrw
     *            value to write
     */
    public void setShortimaattrrw(final short[][] shortimaattrrw) {
	xlogger.entry();
	// shortArrayWrite = (short[]) org.tango.utils.ArrayUtils.from2DArrayToArray(shortimaattrrw);
	// shortArrayRead = ArrayUtils.addAll(shortArrayRead, shortArrayWrite);
	// this.shortimaattrrw = shortimaattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute Short_ima_attr_w
     * 
     * @param shortimaattrw
     *            value to write
     */
    public void setShortimaattrw(final short[][] shortimaattrw) {
	xlogger.entry();
	this.shortimaattrw = Arrays.copyOf(shortimaattrw, shortimaattrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute Short_spec_attr_rw
     * 
     * @param shortspecattrrw
     *            value to write
     */
    public void setShortspecattrrw(final short[] shortspecattrrw) {
	xlogger.entry();
	// this.shortspecattrrw = shortspecattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute Short_spec_attr_w
     * 
     * @param shortspecattrw
     *            value to write
     */
    public void setShortspecattrw(final short[] shortspecattrw) {
	xlogger.entry();
	this.shortspecattrw = Arrays.copyOf(shortspecattrw, shortspecattrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute slow_actuator
     * 
     * @param slowactuator
     *            value to write
     */
    public void setSlowactuator(final short slowactuator) {
	xlogger.entry();
	this.slowactuator = slowactuator;
	/* PROTECTED REGION ID(DevTest.setslow_actuator) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    public void setState(final DevState state) {
	this.state = state;
    }

    /**
     * Write attribute State_attr_rw
     * 
     * @param stateattrrw
     *            value to write
     */
    public void setStateattrrw(final DevState stateattrrw) {
	xlogger.entry();
	this.stateattrrw = stateattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute State_spec_attr_rw
     * 
     * @param statespecattrrw
     *            value to write
     */
    public void setStatespecattrrw(final DevState[] statespecattrrw) {
	xlogger.entry();
	// this.statespecattrrw = statespecattrrw;
	xlogger.exit();
    }

    // public void setStatus(final String status) {
    // this.status = status;
    // }

    /**
     * Write attribute String_attr_w
     * 
     * @param stringattrw
     *            value to write
     */
    // public void setStringattrw(final String stringattrw) {
    // xlogger.entry();
    // this.stringattrw = stringattrw;
    // xlogger.exit();
    // }

    /**
     * Write attribute String_attr_w2
     * 
     * @param stringattrw2
     *            value to write
     */
    public void setStringattrw2(final String stringattrw2) {
	xlogger.entry();
	this.stringattrw2 = stringattrw2;
	/* PROTECTED REGION ID(DevTest.setString_attr_w2) ENABLED START */
	// put write attribute code here
	/* PROTECTED REGION END */
	xlogger.exit();
    }

    /**
     * Write attribute String_ima_attr_rw
     * 
     * @param stringimaattrrw
     *            value to write
     */
    public void setStringimaattrrw(final String[][] stringimaattrrw) {
	xlogger.entry();
	this.stringimaattrrw = stringimaattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute String_ima_attr_w
     * 
     * @param stringimaattrw
     *            value to write
     */
    public void setStringimaattrw(final String[][] stringimaattrw) {
	xlogger.entry();
	this.stringimaattrw = stringimaattrw;
	xlogger.exit();
    }

    /**
     * Write attribute String_spec_attr_rw
     * 
     * @param stringspecattrrw
     *            value to write
     */
    public void setStringspecattrrw(final String[] stringspecattrrw) {
	xlogger.entry();
	this.stringspecattrrw = Arrays.copyOf(stringspecattrrw, stringspecattrrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute String_spec_attr_w
     * 
     * @param stringspecattrw
     *            value to write
     */
    public void setStringspecattrw(final String[] stringspecattrw) {
	xlogger.entry();
	this.stringspecattrw = Arrays.copyOf(stringspecattrw, stringspecattrw.length);
	xlogger.exit();
    }

    /**
     * Write attribute UChar_attr_w
     * 
     * @param uCharattrw
     *            value to write
     */
    public void setUCharattrw(final byte uCharattrw) {
	xlogger.entry();
	this.uCharattrw = uCharattrw;
	xlogger.exit();
    }

    /**
     * Write attribute UChar_spec_attr_rw
     * 
     * @param uCharspecattrrw
     *            value to write
     */
    public void setUCharspecattrrw(final byte[] uCharspecattrrw) {
	xlogger.entry();
	// this.uCharspecattrrw = uCharspecattrrw;
	xlogger.exit();
    }

    /**
     * Write attribute UChar_spec_attr_w
     * 
     * @param uCharspecattrw
     *            value to write
     */
    public void setUCharspecattrw(final byte[] uCharspecattrw) {
	xlogger.entry();
	this.uCharspecattrw = Arrays.copyOf(uCharspecattrw, uCharspecattrw.length);
	xlogger.exit();
    }

    // /**
    // * Write attribute ULong64_attr_rw
    // *
    // * @param uLong64attrrw
    // * value to write
    // */
    // public void setULong64attrrw(final long uLong64attrrw) {
    // xlogger.entry();
    // this.uLong64attrrw = uLong64attrrw;
    // xlogger.exit();
    // }

    // /**
    // * Write attribute ULong64_spec_attr_rw
    // *
    // * @param uLong64specattrrw
    // * value to write
    // */
    // public void setULong64specattrrw(final long[] uLong64specattrrw) {
    // xlogger.entry();
    // this.uLong64specattrrw = uLong64specattrrw;
    // xlogger.exit();
    // }

    // /**
    // * Write attribute ULong_attr_rw
    // *
    // * @param uLongattrrw
    // * value to write
    // */
    // public void setULongattrrw(final int uLongattrrw) {
    // xlogger.entry();
    // this.uLongattrrw = uLongattrrw;
    // if (uLongattrrw > 1000) {
    // this.uLongattrrw = 1111;
    // }
    // xlogger.exit();
    // }

    // /**
    // * Write attribute
    // *
    // * @param uLongspecattrrw
    // * value to write
    // */
    // public void setULongspecattrrw(final int[] uLongspecattrrw) {
    // xlogger.entry();
    // this.uLongspecattrrw = uLongspecattrrw;
    // xlogger.exit();
    // }

    // /**
    // * Write attribute UShort_attr_w
    // *
    // * @param uShortattrw
    // * value to write
    // */
    // public void setUShortattrw(final short uShortattrw) {
    // xlogger.entry();
    // this.uShortattrw = uShortattrw;
    // xlogger.exit();
    // }

    // /**
    // * Write attribute UShort_ima_attr_rw
    // *
    // * @param uShortimaattrrw
    // * value to write
    // */
    // public void setUShortimaattrrw(final short[][] uShortimaattrrw) {
    // xlogger.entry();
    // this.uShortimaattrrw = uShortimaattrrw;
    // xlogger.exit();
    // }

    /**
     * Write attribute UShort_ima_attr_w
     * 
     * @param uShortimaattrw
     *            value to write
     */
    // public void setUShortimaattrw(final short[][] uShortimaattrw) {
    // xlogger.entry();
    // this.uShortimaattrw = uShortimaattrw;
    // /* PROTECTED REGION ID(DevTest.setUShort_ima_attr_w) ENABLED START */
    // // put write attribute code here
    // /* PROTECTED REGION END */
    // xlogger.exit();
    // }

    /**
     * Write attribute UShort_spec_attr_w
     * 
     * @param uShortspecattrw
     *            value to write //
     */
    // public void setUShortspecattrw(final short[] uShortspecattrw) {
    // xlogger.entry();
    // this.uShortspecattrw = uShortspecattrw;
    // xlogger.exit();
    // }

    /**
     * Execute command "SubDeviceTst". <br>
     * description:
     * 
     * @param subDeviceTstIn
     *            void, void
     * @return DevBoolean, true = sub device connected
     */
    @Command(name = "SubDeviceTst", inTypeDesc = "void", outTypeDesc = "true = sub device connected")
    public boolean subDeviceTst() throws DevFailed {
	xlogger.entry();
	/* PROTECTED REGION ID(DeTest.subDeviceTst) ENABLED START */
	// put command code here
	/* PROTECTED REGION END */
	xlogger.exit();
	return false;
    }

    public void setTestProperty(final String testProperty) {
	this.testProperty = testProperty;
    }

    // public void setCmdPollProperty(final String cmdPollProperty) {
    // this.cmdPollProperty = cmdPollProperty;
    // }
    //
    // public void setPollProperty(final String pollProperty) {
    // this.pollProperty = pollProperty;
    // }
    //
    // public void setSubDevicesProperty(final String subDevicesProperty) {
    // this.subDevicesProperty = subDevicesProperty;
    // }

    public double getSlowAttr() {
	return slowAttr;
    }

    public void setSlowAttr(final double slowAttr) {
	this.slowAttr = slowAttr;
    }

    public long getLong64Attr() {
	return long64Attr;
    }

    public void setDeviceManager(final DeviceManager deviceManager) {
	this.deviceManager = deviceManager;
    }

}