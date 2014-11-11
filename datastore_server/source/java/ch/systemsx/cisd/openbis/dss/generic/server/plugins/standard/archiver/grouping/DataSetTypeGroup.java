package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

public class DataSetTypeGroup extends Grouper<DataSetType, DatasetListWithTotal>
{
    private static final long serialVersionUID = -6048320381482361970L;

    public DataSetTypeGroup()
    {
        super(DatasetListWithTotal.class);
    }
}