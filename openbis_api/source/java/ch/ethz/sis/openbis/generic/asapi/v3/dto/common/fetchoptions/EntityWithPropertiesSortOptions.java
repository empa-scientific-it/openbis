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

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter.MATCH_VALUE;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter.PREFIX_MATCH_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.fetchoptions.EntityWithPropertiesSortOptions")
public class EntityWithPropertiesSortOptions<OBJECT extends ICodeHolder & IPermIdHolder & IRegistrationDateHolder & IModificationDateHolder & IPropertiesHolder>
        extends EntitySortOptions<OBJECT>
{

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public static final String FETCHED_FIELDS_SCORE = "FETCHED_FIELDS_SCORE";

    @JsonIgnore
    public static final String TYPE = "TYPE";

    @JsonIgnore
    public static final String PROPERTY = "PROPERTY";

    @JsonIgnore
    public static final String PROPERTY_SCORE = "PROP_SCORE";

    @JsonIgnore
    public static final String ANY_PROPERTY_SCORE = "ANY_PR_SCORE";

    /**
     *
     * @return
     * @deprecated queries cannot specify sorting by score.
     */
    @Deprecated
    public SortOrder fetchedFieldsScore() {
        Map<SortParameter, String> parameters = new HashMap<>();
        parameters.put(SortParameter.FULL_MATCH_CODE_BOOST, 	"1000000");
        parameters.put(SortParameter.PARTIAL_MATCH_CODE_BOOST,  "100000");
        parameters.put(SortParameter.FULL_MATCH_PROPERTY_BOOST,  "10000");
        parameters.put(SortParameter.FULL_MATCH_TYPE_BOOST, 	   "1000");
        parameters.put(SortParameter.PARTIAL_MATCH_PROPERTY_BOOST, "100");
		return getOrCreateSortingWithParameters(FETCHED_FIELDS_SCORE, parameters);
    }

    public SortOrder getFetchedFieldsScore()
    {
        return getSorting(FETCHED_FIELDS_SCORE);
    }

    public SortOrder type()
    {
        return getOrCreateSorting(TYPE);
    }

    public SortOrder getType()
    {
        return getSorting(TYPE);
    }

    public SortOrder property(String propertyName)
    {
        return getOrCreateSorting(PROPERTY + propertyName);
    }

    public SortOrder getProperty(String propertyName)
    {
        return getSorting(PROPERTY + propertyName);
    }

    public SortOrder stringMatchPropertyScore(final String propertyName, final String propertyValue)
    {

        return getOrCreateSortingWithParameters(PROPERTY_SCORE + propertyName,
                Collections.singletonMap(MATCH_VALUE, propertyValue));
    }

    public SortOrder stringPrefixMatchPropertyScore(final String propertyName, final String propertyValue)
    {

        return getOrCreateSortingWithParameters(PROPERTY_SCORE + propertyName,
                Collections.singletonMap(PREFIX_MATCH_VALUE, propertyValue));
    }

    public SortOrder stringMatchAnyPropertyScore(final String propertyValue)
    {
        return getOrCreateSortingWithParameters(ANY_PROPERTY_SCORE,
                Collections.singletonMap(MATCH_VALUE, propertyValue));
    }

    public SortOrder stringPrefixMatchAnyPropertyScore(final String propertyValue)
    {
        return getOrCreateSortingWithParameters(ANY_PROPERTY_SCORE,
                Collections.singletonMap(PREFIX_MATCH_VALUE, propertyValue));
    }

}
