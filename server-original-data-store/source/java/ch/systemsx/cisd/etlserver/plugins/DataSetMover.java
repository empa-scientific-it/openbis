/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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