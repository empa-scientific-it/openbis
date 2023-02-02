package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.Location} instead
 * 
 * @author Jakub Straszewski
 * @deprecated
 */
@Deprecated
public class Location extends ch.systemsx.cisd.openbis.dss.etl.dto.api.Location
{

    public Location(int row, int column)
    {
        super(row, column);
    }
}