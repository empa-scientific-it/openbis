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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.AddressSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.LabelSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author anttil
 */
@JsonObject("as.dto.dataset.search.ExternalDmsSearchCriteria")
public class ExternalDmsSearchCriteria extends AbstractObjectSearchCriteria<IExternalDmsId>
{

    private static final long serialVersionUID = 1L;

    public CodeSearchCriteria withCode()
    {
        return with(new CodeSearchCriteria());
    }

    public CodesSearchCriteria withCodes()
    {
        return with(new CodesSearchCriteria());
    }

    public LabelSearchCriteria withLabel()
    {
        return with(new LabelSearchCriteria());
    }

    public AddressSearchCriteria withAddress()
    {
        return with(new AddressSearchCriteria());
    }

    public ExternalDmsTypeSearchCriteria withType()
    {
        return with(new ExternalDmsTypeSearchCriteria());
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("EXTERNAL_DMS");
        return builder;
    }

}
