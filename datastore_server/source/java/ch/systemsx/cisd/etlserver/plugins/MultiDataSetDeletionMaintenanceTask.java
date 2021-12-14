package ch.systemsx.cisd.etlserver.plugins;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

import java.util.List;

public class MultiDataSetDeletionMaintenanceTask
        extends AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent {

    @Override
    protected void execute(List<DeletedDataSet> datasetCodes)
    {
    }
}
