package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.plugins.AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class MultiDataSetDeletionMaintenanceTask
        extends AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent {

    static final String LAST_SEEN_EVENT_ID_FILE = "last-seen-event-id-file";

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);
        String eventIdFileName = PropertyUtils.getMandatoryProperty(properties, LAST_SEEN_EVENT_ID_FILE);
        lastSeenEventIdFile = new File(eventIdFileName);
    }


    @Override
    protected void execute(List<DeletedDataSet> datasetCodes)
    {
        List<MultiDataSetArchiverContainerDTO> container = findArchivesWithDeletedDataSets(datasetCodes);
    }

    private List<MultiDataSetArchiverContainerDTO> findArchivesWithDeletedDataSets(List<DeletedDataSet> datasetCodes)
    {
        IMultiDataSetArchiverReadonlyQueryDAO dao = getReadonlyQuery();
        String[] dataSetCodes = datasetCodes.stream().map(DeletedDataSet::getCode).toArray(String[]::new);
        return dao.listContainersWithDataSets(dataSetCodes);
    }

    private IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        return MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
    }
}
