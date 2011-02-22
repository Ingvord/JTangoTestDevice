package fr.soleil.TangoArchiving.ArchivingWatchApi.strategy.control.modes;

import fr.esrf.Tango.DevFailed;
import fr.soleil.TangoArchiving.ArchivingWatchApi.datasources.db.DBReaderFactory;
import fr.soleil.TangoArchiving.ArchivingWatchApi.datasources.db.IDBReader;
import fr.soleil.TangoArchiving.ArchivingWatchApi.devicelink.Warnable;
import fr.soleil.TangoArchiving.ArchivingWatchApi.dto.ArchivingAttribute;
import fr.soleil.TangoArchiving.ArchivingWatchApi.strategy.control.safetyperiod.ISaferPeriodCalculator;
import fr.soleil.TangoArchiving.ArchivingWatchApi.strategy.control.safetyperiod.SaferPeriodCalculatorFactory;
import fr.soleil.TangoArchiving.ArchivingWatchApi.tools.Tools;
import fr.soleil.TangoArchiving.ArchivingTools.Mode.ModePeriode;
import fr.soleil.TangoArchiving.ArchivingTools.Mode.TdbSpec;

/**
 * An implementation that looks how many records (if any) have been inserted since <i>f(period)</i> ago.
 * Where <i>f(period)</i> is the "safety period", meaning a time span longer than period to allow for network (or any other reason) delays.   
 * @author CLAISSE 
 */
public class TdbModeControllerByRecordCount extends TdbModeControllerAdapter 
{
    public TdbModeControllerByRecordCount() 
    {
        super();
    }

    /* (non-Javadoc)
     * @see archwatch.mode.controller.IModeController#controlPeriodicMode(fr.soleil.TangoArchiving.ArchivingTools.Mode.ModePeriode, archwatch.dto.Attribute)
     */
    protected int controlPeriodicMode(ModePeriode modeP, TdbSpec spec, ArchivingAttribute attr) throws DevFailed 
    {
        if ( modeP == null )
        {
            Tools.trace ( "TdbModeControllerByRecordCount/controlPeriodicMode/modeP == null!/for attribute|"+attr.getCompleteName()+"|" , Warnable.LOG_LEVEL_ERROR );
            return IModeController.CONTROL_FAILED;
        }
        
        int period = modeP.getPeriod ();
        long exportPeriod_millis = spec.getExportPeriod ();
        int exportPeriod = (int) exportPeriod_millis;
        
        ISaferPeriodCalculator saferPeriodCalculator = SaferPeriodCalculatorFactory.getCurrentImpl();
        int saferPeriod = saferPeriodCalculator.getSaferPeriod ( period + exportPeriod );
        //Tools.trace ( "TdbModeControllerByRecordCount/period|" + period + "|exportPeriod|" + exportPeriod + "|saferPeriod|" + saferPeriod , Warnable.LOG_LEVEL_DEBUG );
        
        String completeName = attr.getCompleteName ();
        IDBReader attributesReader = DBReaderFactory.getCurrentImpl ();
        
        int recordCount;
        try
        {
            recordCount = attributesReader.getRecordCount ( completeName , saferPeriod );
        }
        catch ( DevFailed e )
        {
            Tools.trace ( "FastMode3Controller/controlPeriodicMode/failed calling getRecordCount for attribute|"+completeName+"|" , e , Warnable.LOG_LEVEL_WARN );
            return IModeController.CONTROL_FAILED;
        }
        
        if ( recordCount > 0 )
        {
            return IModeController.CONTROL_OK;
            //WARNING CLA 28/06/06
            /*try
            {
                if ( attributesReader.isLastValueNull ( completeName ) ) 
                {
                    return IModeController.CONTROL_OK_BUT_VALUE_IS_NULL;
                }
                else
                {
                    return IModeController.CONTROL_OK;
                }
               
            }
            catch ( DevFailed e )
            {
                Tools.trace ( "FastMode3Controller/controlPeriodicMode/failed calling isLastValueNull for attribute|"+completeName+"|" , e , Warnable.LOG_LEVEL_WARN );
                return IModeController.CONTROL_OK_BUT_VALUE_MIGHT_BE_NULL;
            }*/
        }
        else
        {
            return IModeController.CONTROL_KO;    
        }
    }

}