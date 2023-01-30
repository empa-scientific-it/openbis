package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import java.util.Date;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the material table.
 */
@Private
public class MaterialRecord extends CodeRecord
{
    public long maty_id;

    public long pers_id_registerer;

    public Date registration_timestamp;

    public Date modification_timestamp;
}
