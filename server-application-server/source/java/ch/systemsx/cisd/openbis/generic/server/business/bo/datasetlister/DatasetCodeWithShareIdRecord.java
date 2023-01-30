package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the dataset table.
 */
@Private
public class DatasetCodeWithShareIdRecord extends CodeRecord
{
    public String share_id; // can be NULL in case of container(virtual) data sets
}
