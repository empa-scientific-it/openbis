/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;

/**
 * @author pkupczyk
 */
@Component
public class SearchDeletionExecutor implements ISearchDeletionExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IDeletionAuthorizationExecutor authorizationExecutor;

    @Override
    public List<Deletion> search(IOperationContext context, DeletionSearchCriteria criteria, DeletionFetchOptions fetchOptions)
    {
        authorizationExecutor.canSearch(context);

        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null");
        }

        List<Deletion> deletions = null;

        if (fetchOptions.hasDeletedObjects())
        {
            deletions = listWithDeletedObjects(context);
        } else
        {
            deletions = listWithoutDeletedObjects(context);
        }

        if (criteria.getCriteria() != null && false == criteria.getCriteria().isEmpty())
        {
            deletions = new SearchDeletions(deletions).search(context, criteria);
        }

        if (deletions == null)
        {
            return Collections.emptyList();
        } else
        {
            Collections.sort(deletions, new Comparator<Deletion>()
                {
                    @Override
                    public int compare(Deletion d1, Deletion d2)
                    {
                        return d1.getRegistrationDate().compareTo(d2.getRegistrationDate());
                    }
                });
            return deletions;
        }
    }

    private List<Deletion> listWithDeletedObjects(IOperationContext context)
    {
        IDeletionTable deletionTable = businessObjectFactory.createDeletionTable(context.getSession());
        deletionTable.load(true);
        return deletionTable.getDeletions();
    }

    private List<Deletion> listWithoutDeletedObjects(IOperationContext context)
    {
        IDeletionTable deletionTable = businessObjectFactory.createDeletionTable(context.getSession());
        deletionTable.load(false);
        return deletionTable.getDeletions();
    }

    private class SearchDeletions extends AbstractSearchObjectManuallyExecutor<DeletionSearchCriteria, Deletion>
    {

        private List<Deletion> allDeletions;

        public SearchDeletions(List<Deletion> allDeletions)
        {
            this.allDeletions = allDeletions;
        }

        @Override
        protected List<Deletion> listAll()
        {
            return allDeletions;
        }

        @Override
        protected Matcher<Deletion> getMatcher(ISearchCriteria criteria)
        {
            if (criteria instanceof IdSearchCriteria<?>)
            {
                return new IdMatcher();
            } else
            {
                throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
            }
        }

        private class IdMatcher extends SimpleFieldMatcher<Deletion>
        {

            @Override
            protected boolean isMatching(IOperationContext context, Deletion object, ISearchCriteria criteria)
            {
                Object id = ((IdSearchCriteria<?>) criteria).getId();

                if (id == null)
                {
                    return true;
                } else if (id instanceof DeletionTechId)
                {
                    DeletionTechId techId = (DeletionTechId) id;
                    return object.getId().equals(techId.getTechId());
                } else
                {
                    throw new IllegalArgumentException("Unknown id: " + id.getClass());
                }
            }

        }

    }

}
