package fr.soleil.util.serialized.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Store Object which cannot be serialized or rebuild such as Corba's ANY object
 * @author BARBA-ROSSA
 *
 */
public class WebObjectStorage implements IWebObjectStorage {
	
private String m_strSessionID = null;
private HashMap<String, Object> m_map = null;
private long counter = 0;

public WebObjectStorage(String sessionID)
{
	m_map = new HashMap<String, Object>();
	counter = 0;
	m_strSessionID = sessionID;
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#addObject(java.lang.Object)
 */
public String addObject(Object object)
{
	// we get the object id
	String objectID = getObjectID();
	
	// we put the object in the storage
	m_map.put(objectID, object);
	return objectID;
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#addObject(java.lang.String, java.lang.Object)
 */
public void addObject(String objectID, Object object)
{
	
	// we put the object in the storage
	m_map.put(objectID, object);
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#getObject(java.lang.String)
 */
public Object getObject(String objectID)
{
	if(m_map.containsKey(objectID))
		return m_map.get(objectID);
	return null;
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#removeObject(java.lang.String)
 */
public void removeObject(String objectID)
{
	if(m_map.containsKey(objectID))
		m_map.remove(objectID);
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#isObjectID(java.lang.String)
 */
public boolean isObjectID(String objectID)
{
	return m_map.containsKey(objectID);
}

private synchronized String getObjectID()
{
	counter++;
	return Long.toString(counter) +  Long.toString(System.currentTimeMillis());
} 

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#getMap()
 */
public HashMap<String, Object> getMap() 
{
	return m_map;
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#setMap(java.util.HashMap)
 */
public void setMap(HashMap<String, Object> map) 
{
	m_map = map;
}


protected void finalize() throws Throwable {
	System.out.println("WebObjectStorage.remove : " + m_strSessionID );
	super.finalize();
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#getSessionID()
 */
public String getSessionID() {
	return m_strSessionID;
}

/* (non-Javadoc)
 * @see fr.soleil.util.serialized.serializer.IWebStorageObject#setSessionID(java.lang.String)
 */
public void setSessionID(String sessionID) {
	m_strSessionID = sessionID;
}


}
