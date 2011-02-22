//+======================================================================
// $Source$
//
// Project:   Tango
//
// Description:  java source code for the TANGO clent/server API.
//
// $Author$
//
// $Revision$
//
// $Log$
// Revision 1.4  2007/08/23 08:32:57  ounsy
// updated change from api/java
//
// Revision 1.2  2004/03/19 10:24:35  ounsy
// Modification of the overall Java event client Api for synchronization with tango C++ Release 4
//
// Revision 1.1  2004/03/08 11:43:23  pascal_verdier
// *** empty log message ***
//
//
// Copyleftt 2003 by Synchrotron Soleil, France
//-======================================================================
/*
 * ITangoChangeListener.java
 *
 * Created on September 22, 2003, 3:45 PM
 */

package fr.esrf.TangoApi.events;


import java.util.EventListener;

/**
 *
 * @author  pascal_verdier
 */
public interface ITangoChangeListener extends EventListener {
    public void change( TangoChangeEvent e);        
}
