//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TANGO clent/server API.
//
// $Author$
//
//
// Revision 3.22  2007/08/07 06:42:28  pascal_verdier
// Take care for extractState() and extractDevState() methods.
//
// $Log$
// Revision 1.2  2007/11/22 07:53:46  pascal_verdier
// Chenge extractDevStateArray management.
//
// Revision 1.1  2007/08/23 09:41:20  ounsy
// Add default impl for tangorb
//
// Revision 3.21  2007/06/07 09:23:56  pascal_verdier
// extractBoolean() added (was removed unfornutatly).
//
// Revision 3.20  2007/05/29 08:11:15  pascal_verdier
// Long64, ULong64, ULong, UShort and DevState attributes added.
//
// Revision 3.19  2007/01/10 10:11:07  pascal_verdier
// DevFailed message syntax fixed.
//
// Revision 3.18  2006/09/19 13:25:29  pascal_verdier
// Access control management added.
//
// Revision 3.17  2006/03/20 13:05:53  pascal_verdier
// extractCharArray() method added.
//
// Revision 3.16  2005/12/02 09:52:32  pascal_verdier
// java import have been optimized.
//
// Revision 3.15  2005/11/29 05:32:02  pascal_verdier
// Bug in getType() method for UCHAR fixed.
//
// Revision 3.14  2005/05/18 12:46:22  pascal_verdier
// getType() method added.
//
// Revision 3.13  2004/12/16 10:16:44  pascal_verdier
// Missing TANGO 5 features added.
//
// Revision 3.12  2004/12/09 13:41:14  pascal_verdier
// New Attribute types added (insert and extract methods).
//
// Revision 3.11  2004/12/09 12:13:30  pascal_verdier
// If read attribute failed WrongData exception is thrown for all get methods.
//
// Revision 3.10  2004/12/07 09:30:29  pascal_verdier
// Exception classes inherited from DevFailed added.
//
// Revision 3.9  2004/11/05 12:38:56  pascal_verdier
// Bug on DeviceAttribute.extract fixed.
//
// Revision 3.8  2004/10/11 12:23:27  pascal_verdier
// Example in header modified.
//
// Revision 3.7  2004/09/17 07:57:03  pascal_verdier
// Attribute for Devive_3Impl (Tango 5) implemented.
//
// Revision 3.6  2004/08/17 08:36:39  pascal_verdier
// An exception is now thrown if quality factor is invalid.
//
// Revision 3.5  2004/03/12 13:15:23  pascal_verdier
// Using JacORB-2.1
//
// Revision 3.0  2003/04/29 08:03:29  pascal_verdier
// Asynchronous calls added.
// Logging related methods.
// little bugs fixed.
//
// Revision 2.0  2003/01/09 14:00:37  verdier
// jacORB is now the ORB used.
//
// Revision 1.8  2002/06/26 09:02:17  verdier
// tested with atkpanel on a TACO device
//
// Revision 1.7  2002/04/09 12:21:51  verdier
// IDL 2 implemented.
//
// Revision 1.6  2002/01/09 12:18:15  verdier
// TACO signals can be read as TANGO attribute.
//
// Revision 1.5  2001/12/10 14:19:42  verdier
// TACO JNI Interface added.
// URL syntax used for connection.
// Connection on device without database added.
//
// Revision 1.4  2001/07/04 14:06:05  verdier
// Attribute management added.
//
//
// Copyright 2001 by European Synchrotron Radiation Facility, Grenoble, France
//               All Rights Reversed
//-======================================================================

package fr.esrf.TangoApi;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;


/**
 *	Class Description:
 *	This class manage data object for Tango device attribute access.
 *	<Br><Br>
 *	<Br><b> Usage example: </b> <Br>
 *	<ul><i>
 *		DeviceAttribute	devattr = dev.read_attribute("Current"); <Br>
 *		if (devattr.hasFailed())<Br>
 *		{<ul>
 *			Except.print_exception(devattr.getErrStack());
 *		</ul>}
 *		else <Br>
 *		{<ul>
 *			double	current = devattr.extractDouble(); <Br>
 *			System.out.println("Current : " + current);
 *		</ul>}
 *	</ul></i>
 *
 * @author  verdier
 * @version  $Revision$
 */

public class DeviceAttributeDAODefaultImpl implements IDeviceAttributeDAO
{
	AttributeValue_3	attrval = new AttributeValue_3();

	
	//===========================================
	/**
	 * Build a DeviceAttribute IDL object
	 *
	 *	@param name		Attribute name.
	 */
	//===========================================
	private void buildAttributeValueObject(String name)
	{
		attrval.name    = name;
		attrval.quality = AttrQuality.ATTR_VALID;
		attrval.time    = new TimeVal();
		attrval.r_dim   = new AttributeDim();
		attrval.w_dim   = new AttributeDim();
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		attrval.w_dim.dim_x   = 0;
		attrval.w_dim.dim_y   = 0;
		try {
			attrval.value = ApiUtil.get_orb().create_any();
		} catch (DevFailed e) {}
		
		long	now = System.currentTimeMillis();
		attrval.time.tv_sec  = (int) (now/1000);
		attrval.time.tv_usec = (int) (now-attrval.time.tv_sec*1000)*1000;
		attrval.time.tv_nsec = 0;
		attrval.err_list     = null;
	}

	public DeviceAttributeDAODefaultImpl() 
	{
	}
	
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param attrval	AttributeValue_3 IDL object.
	 */
	//===========================================
	public void init(AttributeValue_3 attrval)
	{
		this.attrval = attrval;
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param attrval	AttributeValue IDL object.
	 */
	//===========================================
	public  void init(AttributeValue attrval)
	{
		this.attrval.value         = attrval.value;
		this.attrval.name          = attrval.name;
		this.attrval.quality       = attrval.quality;
		this.attrval.time          = attrval.time;
		this.attrval.r_dim         = new AttributeDim();
		this.attrval.w_dim         = new AttributeDim();
		this.attrval.r_dim.dim_x   = attrval.dim_x;
		this.attrval.r_dim.dim_y   = attrval.dim_y;
		this.attrval.w_dim.dim_x   = 0;
		this.attrval.w_dim.dim_y   = 0;
		this.attrval.err_list      = null;
	}


	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 */
	//===========================================
	public  void init(String name)
	{
		buildAttributeValueObject(name);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public  void init(String name, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		this.attrval.r_dim.dim_x   = dim_x;
		this.attrval.r_dim.dim_y   = dim_y;
	}

	
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public  void init(String name, boolean value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, DevState value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, boolean[] value, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(value, dim_x, dim_y);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, byte value)
	{
		buildAttributeValueObject(name);
		insert_uc(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, byte[] value, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert_uc(value, dim_x, dim_y);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, short value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param values	Attribute values.
	 *	@param dim_x	array dimention in X	
	 *	@param dim_y	array dimention in Y	
	 */
	//===========================================
	public void init(String name, short[] values, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(values, dim_x, dim_y);
	}

	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, int value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param values		Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void init(String name, int[] values, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(values, dim_x, dim_y);
	}

	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, long value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param values		Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void init(String name, long[] values, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(values, dim_x, dim_y);
	}

	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, float value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param values		Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void init(String name, float[] values, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(values, dim_x, dim_y);
	}

	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name		Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, double value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param values		Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void init(String name, double[] values, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(values, dim_x, dim_y);
	}

	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param value	Attribute value.
	 */
	//===========================================
	public void init(String name, String value)
	{
		buildAttributeValueObject(name);
		insert(value);
	}
	//===========================================
	/**
	 *	DeviceAttribute class constructor.
	 *
	 *	@param name	Attribute name.
	 *	@param values	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void init(String name, String[] values, int dim_x, int dim_y)
	{
		buildAttributeValueObject(name);
		insert(values, dim_x, dim_y);
	}

	//===========================================
	//===========================================
	public boolean hasFailed()
	{
		return (attrval.err_list!=null && attrval.err_list.length>0);
	}
	//===========================================
	/**
	 *	Returns the attribute errors list
	 */
	//===========================================
	public DevError[] getErrStack()
	{
		return attrval.err_list;
	}
	//===========================================
	/**
	 *	Set the AttributeValue internal object with input one.
	 *
	 *	@param val	AttributeValue_3 input object
	 */
	//===========================================
	public void setAttributeValue(AttributeValue_3 attrval)
	{
		this.attrval = attrval;
	}
	//===========================================
	/**
	 *	Set the AttributeValue internal object with input one.
	 *
	 *	@param val	AttributeValue input object
	 */
	//===========================================
	public void setAttributeValue(AttributeValue attrval)
	{
		this.attrval.value         = attrval.value;
		this.attrval.name          = attrval.name;
		this.attrval.quality       = attrval.quality;
		this.attrval.time          = attrval.time;
		this.attrval.r_dim.dim_x   = attrval.dim_x;
		this.attrval.r_dim.dim_y   = attrval.dim_y;
		this.attrval.w_dim.dim_x   = 0;
		this.attrval.w_dim.dim_y   = 0;
		this.attrval.err_list      = null;
	}


	//===========================================
	//	Insert methods
	//===========================================

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(DevState argin)
	{
		DevState[]	values = new DevState[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarStateArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 */
	//===========================================
	public void insert(DevState[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarStateArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(DevState[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarStateArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(boolean argin)
	{
		boolean[]	values = new boolean[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarBooleanArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 */
	//===========================================
	public void insert(boolean[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarBooleanArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(boolean[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarBooleanArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_uc(byte argin)
	{
		byte[]	values = new byte[1];
		attrval.r_dim.dim_x   = 1;
		values[0] = argin;
		attrval.r_dim.dim_y   = 0;
		DevVarCharArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_uc(short argin)
	{
		byte[]	values = new byte[1];
		values[0] = (byte)(argin & 0xFF);
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarCharArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_uc(short[] argin)
	{
		byte[]	values = new byte[argin.length];
		for (int i=0 ; i<argin.length ; i++)
			values[i] = (byte)(argin[i] & 0xFF);
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarCharArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x	nb data.in x direction
	 *	@param dim_y	nb data.in y direction
	 */
	//===========================================
	public void insert_uc(short[] argin, int dim_x, int dim_y)
	{
		byte[]	values = new byte[argin.length];
		for (int i=0 ; i<argin.length ; i++)
			values[i] = (byte)(argin[i] & 0xFF);
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarCharArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_uc(byte[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarCharArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(short argin)
	{
		short[]	values = new short[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarShortArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(short[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarShortArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(short[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarShortArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_us(short argin)
	{
		short[]	values = new short[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarUShortArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_us(int argin)
	{
		short[]	values = new short[1];
		values[0] = (short)(argin & 0xFFFF);
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarUShortArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_us(short[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarUShortArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_us(int[] argin)
	{
		short[]	values = new short[argin.length];
		for (int i=0 ; i<argin.length ; i++)
			values[i] = (short)(argin[i] & 0xFFFF);
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarUShortArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_us(short[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarUShortArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_us(int[] argin, int dim_x, int dim_y)
	{
		short[]	values = new short[argin.length];
		for (int i=0 ; i<argin.length ; i++)
			values[i] = (short)(argin[i] & 0xFFFF);
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarUShortArrayHelper.insert(attrval.value, values);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(int argin)
	{
		int[]	values = new int[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarLongArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(int[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarLongArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(int[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarLongArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(long argin)
	{
		long[]	values = new long[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarLong64ArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(long[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarLong64ArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(long[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarLong64ArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_ul(int argin)
	{
		int[]	values = new int[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarULongArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values as unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_ul(long argin)
	{
		int[]	values = new int[1];
		values[0] = (int)(argin & 0xFFFFFFFF);
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarULongArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_ul(int[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarULongArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_ul(long[] argin)
	{
		int[]	values = new int[argin.length];
		for (int i=0 ; i<argin.length ; i++)
			values[i] = (int)(argin[i] & 0xFFFFFFFF);
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarULongArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_ul(int[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarULongArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute valuesas unsigned.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_ul(long[] argin, int dim_x, int dim_y)
	{
		int[]	values = new int[argin.length];
		for (int i=0 ; i<argin.length ; i++)
			values[i] = (int)(argin[i] & 0xFFFFFFFF);
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarULongArrayHelper.insert(attrval.value, values);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_u64(long argin)
	{
		long[]	values = new long[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarULong64ArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert_u64(long[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarULong64ArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert_u64(long[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarULong64ArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(float argin)
	{
		float[]	values = new float[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarFloatArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(float[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarFloatArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(float[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarFloatArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(double argin)
	{
		double[]	values = new double[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarDoubleArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(double[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarDoubleArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(double[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarDoubleArrayHelper.insert(attrval.value, argin);
	}

	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 */
	//===========================================
	public void insert(String argin)
	{
		String[]	values = new String[1];
		values[0] = argin;
		attrval.r_dim.dim_x   = 1;
		attrval.r_dim.dim_y   = 0;
		DevVarStringArrayHelper.insert(attrval.value, values);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 */
	//===========================================
	public void insert(String[] argin)
	{
		attrval.r_dim.dim_x   = argin.length;
		attrval.r_dim.dim_y   = 0;
		DevVarStringArrayHelper.insert(attrval.value, argin);
	}
	//===========================================
	/**
	 *	Insert method for attribute values.
	 *
	 *	@param argin	Attribute values.
	 *	@param dim_x array dimention in X	
	 *	@param dim_y array dimention in Y	
	 */
	//===========================================
	public void insert(String[] argin, int dim_x, int dim_y)
	{
		attrval.r_dim.dim_x   = dim_x;
		attrval.r_dim.dim_y   = dim_y;
		DevVarStringArrayHelper.insert(attrval.value, argin);
	}


	//===========================================
	/**
	 *	Throws exception if err_list not null.
	 *
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	private void manageExceptions(String method_name) throws DevFailed
	{
		if (attrval.err_list!=null)
			if (attrval.err_list.length>0)
				throw new WrongData(attrval.err_list);
		if (attrval.quality==AttrQuality.ATTR_INVALID)
			Except.throw_wrong_data_exception("AttrQuality_ATTR_INVALID",
							"Attribute quality factor is INVALID",
							"DeviceAttribute." + method_name + "()");
	}

	//===========================================
	/**
	 *	extract method for an DevState Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public DevState[] extractDevStateArray() throws DevFailed
	{
		manageExceptions("extractDevStateArray()");
		try{
			if (isArray())
					return DevVarStateArrayHelper.extract(attrval.value);
			else
			{
				 // It is used for state attribute
					return new DevState[] { DevStateHelper.extract(attrval.value)};
			}
		}
		catch(org.omg.CORBA.BAD_PARAM e)
		{
			Except.throw_wrong_data_exception(e.toString(),
						"Exception catched : " + e.toString() +"\n" +
						"Maybe the attribute value has not been initialized",
						"DeviceAttribute.extractDevStateArray()");
		}
		return new DevState[0];//	never used
	}
	//===========================================
	/**
	 *	extract method for an DevState.
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public DevState extractDevState() throws DevFailed
	{
		manageExceptions("extractDevState");
		 if (isArray())
		 {
			 // It is used for an attribute of DevState enum.
			 DevState[]	array = extractDevStateArray();
			 return array[0];
		 }
		 else
		 {
			 // It is used for state attribute
			 return DevStateHelper.extract(attrval.value);
		 }
	}
	//===========================================
	/**
	 *	extract method for an boolean.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public boolean extractBoolean() throws DevFailed
	{
		manageExceptions("extractBoolean()");
		return DevVarBooleanArrayHelper.extract(attrval.value)[0];
	}
	//===========================================
	/**
	 *	extract method for an boolean Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public boolean[] extractBooleanArray() throws DevFailed
	{
		manageExceptions("extractBooleanArray()");
		return DevVarBooleanArrayHelper.extract(attrval.value);
	}
	//===========================================
	/**
	 *	extract method for an unsigned char.
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public short extractUChar() throws DevFailed
	{
		manageExceptions("extractUChar");
		short[]	array = extractUCharArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for an unsigned char Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public short[] extractUCharArray() throws DevFailed
	{
		manageExceptions("extractUCharArray()");

		byte[]	argout = DevVarCharArrayHelper.extract(attrval.value);
		short[]	val = new short[argout.length];
		short	mask = 0xFF;
		for (int i=0 ; i<argout.length ; i++)
			val[i] = (short)(mask & argout[i]);
		return val;
	}
	//===========================================
	/**
	 *	extract method for an unsigned char Array as a char array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public byte[] extractCharArray() throws DevFailed
	{
		manageExceptions("extractCharArray()");

		return DevVarCharArrayHelper.extract(attrval.value);
	}
	//===========================================
	/**
	 *	extract method for a short.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public short extractShort() throws DevFailed
	{
		manageExceptions("xtractShort()");
		short[]	array = extractShortArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for a short Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public short[] extractShortArray() throws DevFailed
	{
		manageExceptions("extractShortArray");
		return DevVarShortArrayHelper.extract(attrval.value);
	}

	//===========================================
	/**
	 *	extract method for an unsigned short.
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public int extractUShort() throws DevFailed
	{
		manageExceptions("extractUShort");
		int[]	array = extractUShortArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for an unsigned short Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public int[] extractUShortArray() throws DevFailed
	{
		manageExceptions("extractUShortArray");
		short[]	argout = DevVarUShortArrayHelper.extract(attrval.value);
		int[]	val = new int[argout.length];
		for (int i=0 ; i<argout.length ; i++)
			val[i] = 0xFFFF & argout[i];
		return val;
	}

	//===========================================
	/**
	 *	extract method for a long.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public int extractLong() throws DevFailed
	{
		manageExceptions("extractLong");
		int[]	array = extractLongArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for a long Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public int[] extractLongArray() throws DevFailed
	{
		manageExceptions("extractLongArray");
		return DevVarLongArrayHelper.extract(attrval.value);
	}

	//===========================================
	/**
	 *	extract method for a unsigned long.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public long extractULong() throws DevFailed
	{
		manageExceptions("extractULong");
		int[]	array = DevVarULongArrayHelper.extract(attrval.value);
		long	mask = (long) 0x7fffffff;
		mask += ((long)1 << 31);
		return (mask & (long)array[0]);
	}

	//===========================================
	/**
	 *	extract method for a unsigned long.array
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public long[] extractULongArray() throws DevFailed
	{
		manageExceptions("extractULong");
		int[]	array = DevVarULongArrayHelper.extract(attrval.value);
		long	mask = (long) 0x7fffffff;
		mask += ((long)1 << 31);
		long[]	result = new long[array.length];
		for (int i=0 ; i<array.length ; i++)
			result[i] = (mask & (long)array[i]);
		return result;
	}
	//===========================================
	/**
	 *	extract method for a long.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public long extractLong64() throws DevFailed
	{
		manageExceptions("extractLong64");
		return extractLong64Array()[0];
	}
	//===========================================
	/**
	 *	extract method for a long Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public long[] extractLong64Array() throws DevFailed
	{
		manageExceptions("extractLong64Array");
		return DevVarLong64ArrayHelper.extract(attrval.value);
	}

	//===========================================
	/**
	 *	extract method for a long.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public long extractULong64() throws DevFailed
	{
		manageExceptions("extractULong64");
		return extractULong64Array()[0];
	}
	//===========================================
	/**
	 *	extract method for a long Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public long[] extractULong64Array() throws DevFailed
	{
		manageExceptions("extractULong64Array");
		return DevVarULong64ArrayHelper.extract(attrval.value);
	}

	//===========================================
	/**
	 *	extract method for a float.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public float extractFloat() throws DevFailed
	{
		manageExceptions("extractFloat");
		float[]	array = extractFloatArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for a float Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public float[] extractFloatArray() throws DevFailed
	{
		manageExceptions("extractFloatArray");
		return DevVarFloatArrayHelper.extract(attrval.value);
	}
	//===========================================
	/**
	 *	extract method for a double.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public double extractDouble() throws DevFailed
	{
		manageExceptions("extractDouble");
		double[]	array = extractDoubleArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for a double Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public double[] extractDoubleArray() throws DevFailed
	{
		manageExceptions("extractDoubleArray");
		return DevVarDoubleArrayHelper.extract(attrval.value);
	}
	//===========================================
	/**
	 *	extract method for a DevState (state attribute).
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public DevState extractState() throws DevFailed
	{
		// It is used for state attribute
		// and kept for backward compatibility
		return extractDevState();
	}

	//===========================================
	/**
	 *	extract method for a String.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public String extractString() throws DevFailed
	{
		manageExceptions("extractString");
		String[]	array = extractStringArray();
		return array[0];
	}
	//===========================================
	/**
	 *	extract method for a double Array.
	 *
	 *	@return	the extracted value.
	 *	@throws	DevFailed in case of read_attribute failed
	 *				or if AttrQuality is ATTR_INVALID.
	 */
	//===========================================
	public String[] extractStringArray() throws DevFailed
	{
		manageExceptions("extractStringArray");
		return DevVarStringArrayHelper.extract(attrval.value);
	}
	
	//===========================================
	//===========================================
	public boolean isArray() throws DevFailed 
	{ 
		boolean retval = true; 
		    
		try 
		{ 
			TypeCode        tc = attrval.value.type(); 
			TypeCode        tc_alias = tc.content_type(); 
			TypeCode        tc_seq   = tc_alias.content_type(); 
		} 
		catch(org.omg.CORBA.TypeCodePackage.BadKind e) 
		{ 
			//System.out.println(e); 
			retval = false; 
		} 
		return retval; 
	} 
	//=========================================== 
	//=========================================== 
	
	
	//===========================================
	/**
	 *	Return attribute quality
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public AttrQuality getQuality() throws DevFailed
	{
		manageExceptions("getQuality");
		return attrval.quality;
	}
	//===========================================
	/**
	 *	Return attribute time value.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public TimeVal getTimeVal() throws DevFailed
	{
		manageExceptions("getTimeVal");
		return attrval.time;
	}
	//===========================================
	/**
	 *	Return attribute time value in seconds since EPOCH.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public long getTimeValSec() throws DevFailed
	{
		manageExceptions("getTimeValSec");
		return (long)attrval.time.tv_sec;
	}
	//===========================================
	/**
	 *	Return attribute time value in seconds since EPOCH.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public long getTimeValMillisSec() throws DevFailed
	{
		manageExceptions("getTimeValMillisSec");
		return ((long)attrval.time.tv_sec*1000L) +
				(attrval.time.tv_usec/1000L);
	}
	//===========================================
	/**
	 *	Return attribute name.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public String getName() throws DevFailed
	{
		return attrval.name;
	}
	//===========================================
	//===========================================
	private int DIM_MINI(int x)
	{
		return (x==0) ? 1 : x;
	}
	//===========================================
	/**
	 *	Return number of data read.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public int getNbRead() throws DevFailed
	{
		manageExceptions("getNbRead");
		return attrval.r_dim.dim_x * DIM_MINI(attrval.r_dim.dim_y);
	}
	//===========================================
	/**
	 *	Return number of data written.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public int getNbWritten() throws DevFailed
	{
		manageExceptions("getNbWritten");
		return attrval.w_dim.dim_x * DIM_MINI(attrval.w_dim.dim_y);
	}
	//===========================================
	/**
	 *	Return attribute dim_x.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public int getDimX() throws DevFailed
	{
		manageExceptions("getDimX");
		return attrval.r_dim.dim_x;
	}
	//===========================================
	/**
	 *	Return attribute dim_y.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public int getDimY() throws DevFailed
	{
		manageExceptions("getDimY");
		return attrval.r_dim.dim_y;
	}
	//===========================================
	/**
	 *	Return attribute written dim_x.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public int getWrittenDimX() throws DevFailed
	{
		manageExceptions("getWrittenDimX");
		return attrval.w_dim.dim_x;
	}
	//===========================================
	/**
	 *	Return attribute written dim_y.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public int getWrittenDimY() throws DevFailed
	{
		manageExceptions("getWrittenDimY");
		return attrval.w_dim.dim_y;
	}
	//===========================================
	/**
	 *	Return AttributeValue IDL object.
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public AttributeValue getAttributeValueObject_2()
	{
		AttributeValue	attrval = new AttributeValue();
		attrval.value   = this.attrval.value;   
		attrval.name    = this.attrval.name;
		attrval.quality = this.attrval.quality;
		attrval.time    = this.attrval.time;
		attrval.dim_x   = this.attrval.r_dim.dim_x;
		attrval.dim_y   = this.attrval.r_dim.dim_y;

		return attrval;
	}
	//===========================================
	/**
	 *	Return AttributeValue IDL object.
	 */
	//===========================================
	public AttributeValue_3 getAttributeValueObject_3()
	{
		return attrval;
	}
	
	//===========================================
	/**
	 *	return time in milliseconds since 1/1/70
	 *	@throws	DevFailed in case of read_attribute failed
	 */
	//===========================================
	public long getTime() throws DevFailed
	{
		manageExceptions("getTime");
		return  (long)attrval.time.tv_sec * 1000 + attrval.time.tv_usec/1000;
	}
	//===========================================
	//===========================================
	public int getType() throws DevFailed
	{
		int	type = -1;
		try {
			TypeCode	tc = attrval.value.type();
			//	Special case for test
			if (tc.kind().value()==TCKind._tk_enum)
				return TangoConst.Tango_DEV_STATE;
			
			TypeCode	tc_alias = tc.content_type();
			TypeCode	tc_seq   = tc_alias.content_type();
			TCKind		kind = tc_seq.kind();
			switch(kind.value())
			{
			case TCKind._tk_void:
				type = TangoConst.Tango_DEV_VOID;
				break;
			case TCKind._tk_boolean:
				type = TangoConst.Tango_DEV_BOOLEAN;
				break;
			case TCKind._tk_char:
				type = TangoConst.Tango_DEV_CHAR;
				break;
			case TCKind._tk_octet:
				type = TangoConst.Tango_DEV_UCHAR;
				break;
			case TCKind._tk_short:
				type = TangoConst.Tango_DEV_SHORT;
				break;
			case TCKind._tk_ushort:
				type = TangoConst.Tango_DEV_USHORT;
				break;
			case TCKind._tk_long:
				type = TangoConst.Tango_DEV_LONG;
				break;
			case TCKind._tk_ulong:
				type = TangoConst.Tango_DEV_ULONG;
				break;
			case TCKind._tk_longlong:
				type = TangoConst.Tango_DEV_LONG64;
				break;
			case TCKind._tk_ulonglong:
				type = TangoConst.Tango_DEV_ULONG64;
				break;
			case TCKind._tk_float:
				type = TangoConst.Tango_DEV_FLOAT;
				break;
			case TCKind._tk_double:
				type = TangoConst.Tango_DEV_DOUBLE;
				break;
			case TCKind._tk_string:
				type = TangoConst.Tango_DEV_STRING;
				break;
			case TCKind._tk_enum: 
				type = TangoConst.Tango_DEV_STATE; 
				break; 
			default: 
				Except.throw_exception("AttributeTypeNotSupported", 
				"Attribute Type (" + kind.value() + ") Not Supported", 
				"DeviceAttribute.getType()"); 
			}
		}
		catch(org.omg.CORBA.TypeCodePackage.BadKind e)
		{
			Except.throw_exception("Api_TypeCodePackage.BadKind",
						"Bad or unknown type ",
						"DeviceAttribute.getType()");
		}
		return type;
	}
}
