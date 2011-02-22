package fr.soleil.tango.util.serialized;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.HashMap;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.ApiUtilDAOWebImpl;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DatabaseDAOWebImpl;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceAttributeDAOWebImpl;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceDataDAOWebImpl;
import fr.esrf.TangoApi.DeviceDataHistory;
import fr.esrf.TangoApi.DeviceDataHistoryDAOWebImpl;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceProxyDAOWebImpl;
import fr.soleil.util.parameter.FileParameterLoaderImpl;
import fr.soleil.util.parameter.ParameterManager;
import fr.soleil.util.serialized.WebReflectRequest;
import fr.soleil.util.serialized.WebRequest;
import fr.soleil.util.serialized.WebResponse;
import fr.soleil.util.serialized.WebServerClient;
import fr.soleil.util.serialized.serializer.WebSerializer;
import fr.soleil.util.serialized.serializer.WebSerializerObject;
import fr.soleil.util.serialized.serializer.WebSerializerObjectArray;

public class WebServerClientUtil
{
	private static WebServerClient m_defaultWebServerClient = null;
	private static HashMap<Object, String> m_storedObjectMap = null;
	private static long lCount = 0;
	public static void setDefaultClient(WebServerClient defaultWebServerClient)
	{
		m_defaultWebServerClient = defaultWebServerClient;
	}
	
	/**
	 * Build a the default WebServerclient from properties file parameter.
	 *
	 */
	public static void buildDefaultClient()
	{
		try
		{
			// we get the parameterLoader
			if(ParameterManager.getLoader() == null)
				ParameterManager.setLoader(new FileParameterLoaderImpl());
			
			// We get the parameter url from properties
			String strURL = ParameterManager.getStringParameter("", "SERVER_URL");
			System.out.println("Server URL : " + strURL);
			// temp : in the final version we store it in a properties file or we use the preference api to store it.
			URL url = new URL(strURL);
			WebServerClient webServerClient = new WebServerClient(url);
			WebServerClientUtil.setDefaultClient(webServerClient);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the default WebServerClient
	 * @return WebServerClient
	 */
	public static WebServerClient getDefaultClient()
	{
		if(m_defaultWebServerClient == null)
			buildDefaultClient();
		return m_defaultWebServerClient;
	}

	/**
	 * We a response from the WebServer
	 * @param storedObject : this object have an instance in the WebServer. We pass this instance to get the id and send it to the webserver
	 * @param strMethod
	 * @param aMethodParam
	 * @return Object
	 * @throws Exception
	 */
	public static Object getResponse(Object storedObject, Object[] aActionParam, String strMethod, Object[] aMethodParam, Class[] aMethodParamClass) throws DevFailed
	{
		try
		{
			String objectID =  getStoredObjectID(storedObject);

			String strAction = storedObject.getClass().getName();
			
			// temp : need to create a special exception
			if(objectID == null)
			{
				// if we don't find the class, we pass the constructor information for the server 
				// we get the name of the class
				// special case : if the class ifs a child of DeviceProxy we send the DeviceProxy type.
				// fr.esrf.TangoApi.DeviceProxy
				return getResponse(storedObject, strAction, aActionParam, strMethod, aMethodParam, aMethodParamClass);
			}
			WebRequest request = new WebRequest();
			WebReflectRequest reflectRequest = new WebReflectRequest(objectID, strAction, strMethod, getMethodParam(aMethodParam), aMethodParamClass);
//			request.setArguments(new Object[]{strAction, null, strMethod, getMethodParam(aMethodParam), objectID, aMethodParamClass});
			request.setArguments(new Object[]{reflectRequest});
			WebResponse response = getDefaultClient().getObject(request);
			if(response != null && response.getResult() != null && response.getResult().length > 0)
			{
				Object result =  response.getResult()[0];
				
				if( (result instanceof WebSerializerObject) || (result instanceof WebSerializerObjectArray))                                      
				{
					result = WebSerializer.rebuild(result);
					
					storeObject(response.getResult()[0], result);
				}
				return result;
			}
		}
		catch(DevFailed e)
		{
			throw e;
		}			
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}	
	
	/**
	 * Store an object in the storedMap 
	 * @param webResponse : the object returned by the server
	 * @param result : the result returned to User Interface
	 */
	public static void storeObject(Object webResponse, Object result)
	{
		if(webResponse instanceof WebSerializerObjectArray )
		{
			WebSerializerObject tempSerial = null;
			WebSerializerObjectArray tempArray = (WebSerializerObjectArray)webResponse;
			if( tempArray.getWebSerializerObject() == null)
				return;
			
			
			Object tempResultObject = null;
			for(int i=0;i < tempArray.getWebSerializerObject().length;i++)
			{
				// if the webResponse is an array the result, is an array too...
				tempResultObject = Array.get(result, i); // we get the element at the index.
				storeObject(tempArray.getWebSerializerObject()[i], tempResultObject); // we store the element of the array
			}
			
		}
		
		if(webResponse instanceof WebSerializerObject)
		{
			String objectID = ((WebSerializerObject)webResponse).getObjectID();
			if(objectID == null)
				return;
			
			// TEMP -------------------------
			Object daoImpl = getDAOImpl(result);

			getStoredObjectMap().put(daoImpl.hashCode(), objectID);
			
			// TEMP -------------------------
		}
	}
	
	public static Object getDAOImpl(Object result)
	{
		if(result instanceof DeviceAttribute)
		{
			return ((DeviceAttribute)result).getDeviceattributeDAO();
		}
		if(result instanceof DeviceData)
		{
			return((DeviceData)result).getDevicedataDAO();
		}
		if(result instanceof DeviceDataHistory)
		{
			return ((DeviceDataHistory)result).getDeviceedatahistoryDAO();
		}
		if(result instanceof DeviceProxy)
		{
			return  ((DeviceProxy)result).getDeviceProxy();
		}			
		if(result instanceof Database)
		{
			return ((Database)result).getDatabaseDAO();
		}
		if(result instanceof ApiUtil)
		{
			return ((ApiUtil)result).getApiUtilDAO();
		}		
		return result;
	}
	
	/**
	 * Return a stored Object if it was stored in the Server. 
	 * @param result
	 * @return
	 */
	public static String getStoredObjectID(Object result)
	{
		Object tempResult = getDAOImpl(result);
		
		if(tempResult != null)
			if(getStoredObjectMap().containsKey(tempResult.hashCode()))
				return getStoredObjectMap().get(tempResult.hashCode());
		return null;
	} 
	
	/**
	 * We control if parameters will be present in the storage. if true we replace the object by the reference. 
	 * @param aMethodParam
	 * @return
	 */
	public static Object[] getMethodParam(Object[] aMethodParam)
	{
		Object[] newObject = new Object[aMethodParam.length];
		String tempKey = null;
		WebSerializerObject webSerialObject = null;
		Object[] tempClassParam = null;
		for(int i = 0;i < aMethodParam.length;i++)
		{
			tempKey = getStoredObjectID(aMethodParam[i]);
			if(tempKey != null)
			{
				webSerialObject = new WebSerializerObject(aMethodParam[i].getClass().getName());
				webSerialObject.setObjectID(tempKey);
				newObject[i] = webSerialObject; // temp.
			}
			else
			{
				tempKey = createObjectID();
				// we control if the type is not a non serializable object
				tempClassParam = getClassParam(aMethodParam[i]);
				if(tempClassParam != null)
				{
					
					webSerialObject = new WebSerializerObject(aMethodParam[i].getClass().getName());
					webSerialObject.setObjectID(tempKey);
					webSerialObject.addAttribute("aActionParam", tempClassParam);
					newObject[i] = webSerialObject; // temp.
				}
				else
					newObject[i] = aMethodParam[i];
			}
		}
		return newObject;
	}
	
	// temp : we get the constructor parameter to send to the server
	public static Object[] getClassParam(Object object)
	{
		Object impl = null;
		if(object instanceof DeviceAttribute)
		{
			impl = ((DeviceAttribute)object).getDeviceattributeDAO();
			return ((DeviceAttributeDAOWebImpl)impl).getClassParam();
		}
		if(object instanceof DeviceData)
		{
			impl = ((DeviceData)object).getDevicedataDAO();
			return ((DeviceDataDAOWebImpl)impl).getClassParam();
		}
		if(object instanceof DeviceDataHistory)
		{
			impl = ((DeviceDataHistory)object).getDeviceedatahistoryDAO();
			return ((DeviceDataHistoryDAOWebImpl)impl).getClassParam();
		}
		if(object instanceof DeviceProxy)
		{
			impl = ((DeviceProxy)object).getDeviceProxy();
			return ((DeviceProxyDAOWebImpl)impl).getClassParam();
		}			
		if(object instanceof Database)
		{
			impl = ((Database)object).getDatabaseDAO();
			return ((DatabaseDAOWebImpl)impl).getClassParam();
		}
		if(object instanceof ApiUtil)
		{
			impl = ((ApiUtil)object).getApiUtilDAO();
			return ((ApiUtilDAOWebImpl)impl).getClassParam();
		}
		return null;
	} 
	
	/**
	 * Remove an object from the storage
	 * @param objectToRemove
	 */
	public static void removeObject(Object objectToRemove)
	{
		try
		{
			String objectID = getStoredObjectID(objectToRemove);
			//System.out.println("WebServerClientUtil.removeObject : " + objectID + " : " + objectToRemove );
			if(objectID != null)
			{
				// we remove the object from the server storage
				WebRequest request = new WebRequest();
				WebReflectRequest reflectRequest = new WebReflectRequest();
				reflectRequest.setAction("Storage.remove");
	//			request.setArguments(new Object[]{"Storage.remove", (Object[])null, (String)null, (Object[])null, objectID, (Class[])null});
				request.setArguments(new Object[]{reflectRequest});
				getDefaultClient().getObject(request);
				
				// we remove the reference from the client storage
				getStoredObjectMap().remove(objectToRemove.hashCode());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param strAction
	 * @param aActionParam
	 * @param strMethod
	 * @param aMethodParam
	 * @return
	 * @throws DevFailed
	 * @deprecated
	 */
	public static Object getResponse(String strAction,Object[] aActionParam, String strMethod, Object[] aMethodParam)  throws DevFailed
	{
		return getResponse(null, strAction, aActionParam, strMethod, aMethodParam, null);
	}	
	
	public static Object getResponse(Object callObject, String strAction,Object[] aActionParam, String strMethod, Object[] aMethodParam, Class[] aMethodParamClass)  throws DevFailed
	{
		try
		{
			WebRequest request = new WebRequest();
			WebReflectRequest reflectRequest = new WebReflectRequest(strAction, aActionParam, strMethod, getMethodParam(aMethodParam), aMethodParamClass);
			//request.setArguments(new Object[]{strAction, aActionParam, strMethod, getMethodParam(aMethodParam), (String)null, aMethodParamClass});
			request.setArguments(new Object[]{reflectRequest});
			
			WebResponse response = getDefaultClient().getObject(request);
			if(response != null && response.getResult() != null && response.getResult().length > 0)
			{
				Object result =  response.getResult()[0];
				
				if( (result instanceof WebSerializerObject) || (result instanceof WebSerializerObjectArray))                                      
				{
					result = WebSerializer.rebuild(result);
					storeObject(response.getResult()[0], result);
				}
				// if the second argument of the result array is not null we need to store the object
				Object returnedCallObject = response.getResult()[1];
				if(callObject != null &&  returnedCallObject != null)
				{
					storeObject(returnedCallObject, callObject);
				}
				
				
				return result;
			}
		}
		catch(DevFailed e)
		{
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public synchronized static String createObjectID()
	{
		lCount ++;
		return Long.toString(System.currentTimeMillis()) + Long.toString(lCount);
	}
	
	public static HashMap<Object, String> getStoredObjectMap() {
		if(m_storedObjectMap== null)
			m_storedObjectMap = new HashMap<Object, String>();
		return m_storedObjectMap;
	}

	public static void setStoredObjectMap(HashMap<Object, String> objectMap) {
		m_storedObjectMap = objectMap;
	}

	protected void finalize() throws Throwable {
		super.finalize();
	}	
	
}
