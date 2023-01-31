package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IChecksumProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;

/**
 * Implementation of {@link IDataSetMover}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetMover implements IDataSetMover
{
    private final IEncapsulatedOpenBISService service;

    private final IShareIdManager manager;

    public DataSetMover(IEncapsulatedOpenBISService service, IShareIdManager shareIdManager)
    {
        this.service = service;
        manager = shareIdManager;
    }

    @Override
    public void moveDataSetToAnotherShare(File dataSetDirInStore, File share,
            IChecksumProvider checksumProvider, ISimpleLogger logger)
    {
        SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share, service, manager,
                checksumProvider, logger);
    }
}