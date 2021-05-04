/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TextAttributeSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.search.DataSetSearchCriteria")
public class DataSetSearchCriteria extends AbstractDataSetSearchCriteria<DataSetSearchCriteria>
{

    private static final long serialVersionUID = 1L;

    public DataSetSearchCriteria()
    {
        super(DataSetSearchRelation.DATASET);
    }

    public DataSetParentsSearchCriteria withParents()
    {
        return with(new DataSetParentsSearchCriteria());
    }

    public DataSetChildrenSearchCriteria withChildren()
    {
        return with(new DataSetChildrenSearchCriteria());
    }

    public DataSetContainerSearchCriteria withContainer()
    {
        return with(new DataSetContainerSearchCriteria());
    }

    public DataSetSearchCriteria withSubcriteria()
    {
        return with(new DataSetSearchCriteria());
    }

    public TextAttributeSearchCriteria withTextAttribute()
    {
        return with(new TextAttributeSearchCriteria());
    }

    public DataSetSearchCriteria negate()
    {
        return (DataSetSearchCriteria) super.negate();
    }

}
