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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TextAttributeSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.sample.search.SampleSearchCriteria")
public class SampleSearchCriteria extends AbstractSampleSearchCriteria<SampleSearchCriteria>
{

    private static final long serialVersionUID = 1L;

    public SampleSearchCriteria()
    {
        super(SampleSearchRelation.SAMPLE);
    }

    public SampleParentsSearchCriteria withParents()
    {
        return with(new SampleParentsSearchCriteria());
    }

    public SampleChildrenSearchCriteria withChildren()
    {
        return with(new SampleChildrenSearchCriteria());
    }

    public SampleContainerSearchCriteria withContainer()
    {
        return with(new SampleContainerSearchCriteria());
    }

    public SampleSearchCriteria withSubcriteria()
    {
        return with(new SampleSearchCriteria());
    }

    public TextAttributeSearchCriteria withTextAttribute()
    {
        return with(new TextAttributeSearchCriteria());
    }

    @Override
    public SampleSearchCriteria negate()
    {
        return (SampleSearchCriteria) super.negate();
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

}
