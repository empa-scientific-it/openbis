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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.fetchoptions.EntitySortOptions")
public class EntitySortOptions<OBJECT extends ICodeHolder & IPermIdHolder & IRegistrationDateHolder & IModificationDateHolder>
        extends SortOptions<OBJECT>
{

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public static final String CODE = "CODE";

    @JsonIgnore
    public static final String PERM_ID = "PERM_ID";

    @JsonIgnore
    public static final String MODIFICATION_DATE = "MODIFICATION_DATE";

    @JsonIgnore
    public static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    public SortOrder code()
    {
        return getOrCreateSorting(CODE);
    }

    public SortOrder getCode()
    {
        return getSorting(CODE);
    }

    public SortOrder permId()
    {
        return getOrCreateSorting(PERM_ID);
    }

    public SortOrder getPermId()
    {
        return getSorting(PERM_ID);
    }

    public SortOrder registrationDate()
    {
        return getOrCreateSorting(REGISTRATION_DATE);
    }

    public SortOrder getRegistrationDate()
    {
        return getSorting(REGISTRATION_DATE);
    }

    public SortOrder modificationDate()
    {
        return getOrCreateSorting(MODIFICATION_DATE);
    }

    public SortOrder getModificationDate()
    {
        return getSorting(MODIFICATION_DATE);
    }

}
