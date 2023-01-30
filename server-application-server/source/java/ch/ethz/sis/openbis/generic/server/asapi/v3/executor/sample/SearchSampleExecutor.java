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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;

/**
 * @author pkupczyk
 */
@Component
public class SearchSampleExecutor extends AbstractSearchObjectExecutor<SampleSearchCriteria, Long> implements ISearchSampleExecutor
{
    
    @Autowired
    private ISampleAuthorizationExecutor authorizationExecutor;

    @Override
    protected List<Long> doSearch(IOperationContext context, DetailedSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        
        SampleSearchManager searchManager =
                new SampleSearchManager(daoFactory.getHibernateSearchDAO(), businessObjectFactory.createSampleLister(context.getSession()));

        Collection<Long> sampleIds =
                searchManager.searchForSampleIDs(context.getSession().getUserName(), criteria);

        return new ArrayList<Long>(sampleIds);
    }

}
