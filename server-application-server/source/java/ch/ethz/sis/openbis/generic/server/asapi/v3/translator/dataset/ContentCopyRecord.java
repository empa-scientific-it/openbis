package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseRecord;

public class ContentCopyRecord extends ObjectBaseRecord
{
    public String externalCode;

    public String path;

    public String gitCommitHash;

    public String gitRepositoryId;
}
