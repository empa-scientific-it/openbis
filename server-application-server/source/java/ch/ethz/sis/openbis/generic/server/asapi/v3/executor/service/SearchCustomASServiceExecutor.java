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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;

/**
 * @author pkupczyk
 */
@Component
public class SearchCustomASServiceExecutor extends AbstractSearchObjectManuallyExecutor<CustomASServiceSearchCriteria, CustomASService>
        implements ISearchCustomASServiceExecutor
{

    @Autowired
    private ICustomASServiceProvider serviceProvider;

    @Autowired
    private ICustomASServiceAuthorizationExecutor authorizationExecutor;

    @Override
    public List<CustomASService> search(IOperationContext context, CustomASServiceSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<CustomASService> listAll()
    {
        return serviceProvider.getCustomASServices();
    }

    @Override
    protected Matcher<CustomASService> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<CustomASService>() {
                @Override
                protected String getCodeOf(CustomASService object)
                {
                    return object.getCode().getPermId();
                }};
        }
        throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
    }

    private class CodeMatcher extends StringFieldMatcher<CustomASService>
    {

        @Override
        protected String getFieldValue(CustomASService object)
        {
            return object.getCode().getPermId();
        }

    }

}
