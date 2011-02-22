/*	Synchrotron Soleil 
 *  
 *   File          :  ScStr1Val.java
 *  
 *   Project       :  javaapi
 *  
 *   Description   :  
 *  
 *   Author        :  CLAISSE
 *  
 *   Original      :  7 mars 07 
 *  
 *   Revision:  					Author:  
 *   Date: 							State:  
 *  
 *   Log: ScStr1Val.java,v 
 *
 */
 /*
 * Created on 7 mars 07
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.TangoSnapshoting.SnapshotingApi.persistence.spring.dto;

import fr.soleil.TangoSnapshoting.SnapshotingApi.persistence.context.SnapshotPersistenceContext;
import fr.soleil.actiongroup.collectiveaction.onattributes.plugin.persistance.AnyAttribute;

public class ScStr1Val extends Val
{
    private String value;
    
    public ScStr1Val ()
    {
       
    }

    public ScStr1Val(AnyAttribute attribute, SnapshotPersistenceContext context) 
    {
        super ( attribute , context ) ;
        
        String [] val = attribute.getConvertedStringValuesTable ();
        this.value = val [0];
    }
    
    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
