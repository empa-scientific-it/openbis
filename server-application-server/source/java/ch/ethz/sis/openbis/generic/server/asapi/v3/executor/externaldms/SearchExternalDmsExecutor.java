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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.AddressSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.LabelSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchExternalDmsExecutor extends AbstractSearchObjectManuallyExecutor<ExternalDmsSearchCriteria, ExternalDataManagementSystemPE>
        implements ISearchExternalDmsExecutor
{

    @Autowired
    private IExternalDmsAuthorizationExecutor authorizationExecutor;

    @Override
    public List<ExternalDataManagementSystemPE> search(IOperationContext context, ExternalDmsSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<ExternalDataManagementSystemPE> listAll()
    {
        return daoFactory.getExternalDataManagementSystemDAO().listExternalDataManagementSystems();
    }

    @Override
    protected Matcher<ExternalDataManagementSystemPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<ExternalDataManagementSystemPE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<ExternalDataManagementSystemPE>();
        } else if (criteria instanceof LabelSearchCriteria)
        {
            return new LabelMatcher();
        } else if (criteria instanceof AddressSearchCriteria)
        {
            return new AddressMatcher();
        } else if (criteria instanceof ExternalDmsTypeSearchCriteria)
        {
            return new ExternalDmsTypeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }
}
