package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;

/**
 * A record object representing one row of the content_copies table.
 */
@Private
public class ContentCopyRecord extends CodeRecord
{
    public String edms_code;

    public String edms_label;

    public String edms_address;

    public String external_code;

    public String path;

    public String hash;

    public String repository_id;

    public Long edms_id;
}
