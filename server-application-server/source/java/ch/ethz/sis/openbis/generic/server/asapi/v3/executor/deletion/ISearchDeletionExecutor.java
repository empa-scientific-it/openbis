/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;

/**
 * @author pkupczyk
 */
public interface ISearchDeletionExecutor
{

    public List<Deletion> search(IOperationContext context, DeletionSearchCriteria criteria, DeletionFetchOptions fetchOptions);

}
