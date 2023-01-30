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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author pkupczyk
 */
public abstract class UpdateObjectsOperationExecutor<UPDATE extends IObjectUpdate<? extends OBJECT_ID>, OBJECT_ID extends IObjectId>
        extends OperationExecutor<UpdateObjectsOperation<UPDATE>, UpdateObjectsOperationResult<? extends OBJECT_ID>> implements
        IUpdateObjectsOperationExecutor
{

}
