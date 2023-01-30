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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.globalsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsExactlyValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKindCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchWildCardsCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.HibernateSearchDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * @author pkupczyk
 */
@Component
public class GlobalSearchExecutor implements IGlobalSearchExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public List<MatchingEntity> search(IOperationContext context, GlobalSearchCriteria criteria)
    {
        String userName = context.getSession().getUserName();
        String queryText = getQueryText(criteria);
        Collection<SearchableEntity> entityKinds = getEntityKinds(criteria);
        boolean wildCards = getWildCards(criteria);

        final List<MatchingEntity> list = new ArrayList<MatchingEntity>();

        if (queryText != null && queryText.trim().length() > 0)
        {
            for (final SearchableEntity entityKind : entityKinds)
            {
                HibernateSearchDataProvider dataProvider =
                        new HibernateSearchDataProvider(daoFactory);
                List<MatchingEntity> entities =
                        daoFactory.getHibernateSearchDAO().searchEntitiesByTerm(
                                userName, entityKind, queryText, dataProvider, wildCards, list.size(), Integer.MAX_VALUE);
                list.addAll(entities);
            }
        }

        return list;
    }

    private boolean getWildCards(GlobalSearchCriteria criteria)
    {
        for (ISearchCriteria subCriteria : criteria.getCriteria())
        {
            if (subCriteria instanceof GlobalSearchWildCardsCriteria)
            {
                return true;
            }
        }

        return false;
    }

    private String getQueryText(GlobalSearchCriteria criteria)
    {
        List<String> texts = new ArrayList<String>();

        for (ISearchCriteria subCriteria : criteria.getCriteria())
        {
            if (subCriteria instanceof GlobalSearchTextCriteria)
            {
                GlobalSearchTextCriteria textSubCriteria = (GlobalSearchTextCriteria) subCriteria;

                if (textSubCriteria.getFieldValue() instanceof StringContainsValue)
                {
                    texts.add(textSubCriteria.getFieldValue().getValue());
                } else if (textSubCriteria.getFieldValue() instanceof StringContainsExactlyValue)
                {
                    texts.add("\"" + textSubCriteria.getFieldValue().getValue() + "\"");
                }
            }
        }

        return StringUtils.join(texts, " ");
    }

    private Collection<SearchableEntity> getEntityKinds(GlobalSearchCriteria criteria)
    {
        GlobalSearchObjectKindCriteria lastObjectKindSubCriteria = null;

        for (ISearchCriteria subCriteria : criteria.getCriteria())
        {
            if (subCriteria instanceof GlobalSearchObjectKindCriteria)
            {
                lastObjectKindSubCriteria = (GlobalSearchObjectKindCriteria) subCriteria;
            }
        }

        if (lastObjectKindSubCriteria == null || lastObjectKindSubCriteria.getObjectKinds() == null
                || lastObjectKindSubCriteria.getObjectKinds().isEmpty())
        {
            return Arrays.asList(SearchableEntity.values());
        } else
        {
            List<GlobalSearchObjectKind> objectKinds = lastObjectKindSubCriteria.getObjectKinds();
            Collection<SearchableEntity> entityKinds = new HashSet<SearchableEntity>();

            for (GlobalSearchObjectKind objectKind : objectKinds)
            {
                SearchableEntity entityKind = null;

                switch (objectKind)
                {
                    case EXPERIMENT:
                        entityKind = SearchableEntity.EXPERIMENT;
                        break;
                    case SAMPLE:
                        entityKind = SearchableEntity.SAMPLE;
                        break;
                    case DATA_SET:
                        entityKind = SearchableEntity.DATA_SET;
                        break;
                    case MATERIAL:
                        entityKind = SearchableEntity.MATERIAL;
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported object kind " + objectKind);
                }

                entityKinds.add(entityKind);
            }

            return entityKinds;
        }
    }
}
