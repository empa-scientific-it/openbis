/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.global.fetchoptions.GlobalSearchObjectSortOptions")
public class GlobalSearchObjectSortOptions extends SortOptions<GlobalSearchObject>
{

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public static final String SCORE = "SCORE";

    @JsonIgnore
    public static final String OBJECT_KIND = "OBJECT_KIND";

    @JsonIgnore
    public static final String OBJECT_PERM_ID = "OBJECT_PERM_ID";

    @JsonIgnore
    public static final String OBJECT_IDENTIFIER = "OBJECT_IDENTIFIER";

    public SortOrder score()
    {
        return getOrCreateSorting(SCORE);
    }

    public SortOrder objectKind()
    {
        return getOrCreateSorting(OBJECT_KIND);
    }

    public SortOrder objectPermId()
    {
        return getOrCreateSorting(OBJECT_PERM_ID);
    }

    public SortOrder objectIdentifier()
    {
        return getOrCreateSorting(OBJECT_IDENTIFIER);
    }

}