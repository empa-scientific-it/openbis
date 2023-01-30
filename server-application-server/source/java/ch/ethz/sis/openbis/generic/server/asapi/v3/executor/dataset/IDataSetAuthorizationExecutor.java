/*
 * Copyright 2016 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IObjectAuthorizationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
public interface IDataSetAuthorizationExecutor extends IObjectAuthorizationExecutor
{

    void canCreate(IOperationContext context, DataPE dataSet);

    void canUpdate(IOperationContext context, IDataSetId id, DataPE dataSet);

    void canDelete(IOperationContext context, IDataSetId id, DataPE dataSet);

    void canGet(IOperationContext context);

    void canSearch(IOperationContext context);

    void canArchive(IOperationContext context, IDataSetId id, DataPE dataSet);

    void canUnarchive(IOperationContext context, IDataSetId id, DataPE dataSet);

    void canLock(IOperationContext context, IDataSetId dataSetId, DataPE dataSet);

    void canUnlock(IOperationContext context, IDataSetId dataSetId, DataPE dataSet);

    void canFreeze(IOperationContext context, DataPE dataSet);
}
