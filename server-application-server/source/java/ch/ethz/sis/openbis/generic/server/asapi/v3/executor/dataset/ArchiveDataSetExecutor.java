/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class ArchiveDataSetExecutor extends AbstractArchiveUnarchiveDataSetExecutor implements IArchiveDataSetExecutor
{

    @Autowired
    private IDataSetAuthorizationExecutor authorizationExecutor;

    @Override
    public void archive(final IOperationContext context, final List<? extends IDataSetId> dataSetIds, final DataSetArchiveOptions options)
    {
        doArchiveUnarchive(context, dataSetIds, options, new IArchiveUnarchiveAction()
            {
                @Override
                public void execute(IDataSetTable dataSetTable)
                {
                    dataSetTable.archiveDatasets(options.isRemoveFromDataStore(), options.getOptions());
                }
            });
    }

    @Override
    protected void assertAuthorization(IOperationContext context, IDataSetId dataSetId, DataPE dataSet)
    {
        authorizationExecutor.canArchive(context, dataSetId, dataSet);
    }

}
