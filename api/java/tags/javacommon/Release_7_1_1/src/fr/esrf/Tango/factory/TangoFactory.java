//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:	source code 
//
// $Author$
//
// Copyright (C) :      2004,2005,2006,2007,2008
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision$
//
// $Log$
// Revision 1.5  2008/10/10 11:38:00  pascal_verdier
// Headers changed for LGPL conformity.
//
//
//-======================================================================



package fr.esrf.Tango.factory;

import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.factory.DefaultTangoFactoryImpl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * 
 * @author BARBA-ROSSA
 *
 */
public class TangoFactory {
    public static final String FACTORY_PROPERTIES = "tango_factory.properties";
    public static final String TANGO_FACTORY = "TANGO_FACTORY";

    
    
    private static TangoFactory singleton = null;
    //private Properties properties =null;
    private ITangoFactory tangoFactory;
    private boolean isDefaultFactory = true;
    
    public TangoFactory()
    {
    	initTangoFactory();
    }

    /**
     * Load properties with impl specification and create instances
     *
     */
    public void initTangoFactory()
    {
    	// we get the properties with instance of objects
    	Properties	properties = getPropertiesFile();
    	if(properties == null || properties.size() == 0 || !properties.containsKey(TANGO_FACTORY))
    	{
    		tangoFactory = new DefaultTangoFactoryImpl();
    	}	
    	else
    	{
    		String factoryClassName = properties.getProperty(TANGO_FACTORY);
    		tangoFactory = (ITangoFactory)getObject(factoryClassName);
    		isDefaultFactory = false;
    	}
    }
    
    public static TangoFactory getSingleton()
    {
    	if(singleton == null)
			singleton = new TangoFactory();
    	return singleton;
    }

    public IConnectionDAO getConnectionDAO()
    {
    	return tangoFactory.getConnectionDAO();
    }
    
    public IDeviceProxyDAO getDeviceProxyDAO()
    {
    	return tangoFactory.getDeviceProxyDAO();
    }

    public IDatabaseDAO getDatabaseDAO()
    {
    	return tangoFactory.getDatabaseDAO();
    }    
    
    public IDeviceAttributeDAO getDeviceAttributeDAO()
    {
    	return tangoFactory.getDeviceAttributeDAO();
    }
    
    public IDeviceAttribute_3DAO getDeviceAttribute_3DAO()
    {
    	return tangoFactory.getDeviceAttribute_3DAO();
    }

    public IDeviceDataDAO getDeviceDataDAO()
    {
    	return tangoFactory.getDeviceDataDAO();
    }    

    public IDeviceDataHistoryDAO getDeviceDataHistoryDAO()
    {
    	return tangoFactory.getDeviceDataHistoryDAO();
    }
    
    public IApiUtilDAO getApiUtilDAO()
    {
    	return tangoFactory.getApiUtilDAO();
    }

    public IIORDumpDAO getIORDumpDAO()
    {
    	return tangoFactory.getIORDumpDAO();
    }    
    
    /**
     * We get the properties file which contains default properties
     * @return Properties
     */
    private static Properties getPropertiesFile()
    {
        try{
        	
        	// We use the class loader to load the properties file. 
        	// This compatible with unix and windows.
            InputStream	stream = TangoFactory.class.getClassLoader().getResourceAsStream(FACTORY_PROPERTIES);
    		Properties properties = new Properties();
    		
    		// We read the data in the properties file.
    		if(stream != null){
    			// We need to use a Buffered Input Stream to load the datas
    			BufferedInputStream	bufStream = new BufferedInputStream(stream);
            	properties.clear();
            	properties.load(bufStream);
    		}
    		return properties;
        }
    	catch(Exception e)
    	{
			e.printStackTrace();
    		return null;
    	}
    }

    /**
     * We instanciate the Component
     * @param className
     * @return Object
     */
    private static Object getObject(String className)
    {
    	try {
        	// we get the class coresponding to the life cycle name
			Class clazz = Class.forName(className);
			
			// we get the default constructor (with no parameter)
			Constructor contructor = clazz.getConstructor(new Class[]{});
			
			// we create an instance of the class using the constructor
			return contructor.newInstance(new Object[]{});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isDefaultFactory() {
		return isDefaultFactory;
	}

	public void setDefaultFactory(boolean isDefaultFactory) {
		this.isDefaultFactory = isDefaultFactory;
	}
}
