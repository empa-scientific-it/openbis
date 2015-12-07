/*

 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IUpdateDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IUpdateEntityExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetMethodExecutor extends AbstractUpdateMethodExecutor<DataSetUpdate> implements IUpdateDataSetMethodExecutor
{

    @Autowired
    private IUpdateDataSetExecutor updateExecutor;

    @Override
    protected IUpdateEntityExecutor<DataSetUpdate> getUpdateExecutor()
    {
        return updateExecutor;
    }

}
