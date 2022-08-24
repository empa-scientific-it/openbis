/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.pat.create.CreatePersonalAccessTokensOperationResult")
public class CreatePersonalAccessTokensOperationResult extends CreateObjectsOperationResult<PersonalAccessTokenPermId>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private CreatePersonalAccessTokensOperationResult()
    {
    }

    public CreatePersonalAccessTokensOperationResult(List<PersonalAccessTokenPermId> objectIds)
    {
        super(objectIds);
    }
}
