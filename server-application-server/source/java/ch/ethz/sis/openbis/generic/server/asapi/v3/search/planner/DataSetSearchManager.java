/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages detailed search with dataset search criteria.
 *
 * @author Viktor Kovtun
 */
public class DataSetSearchManager extends AbstractCompositeEntitySearchManager<DataSetSearchCriteria, DataSet, Long>
{

    public DataSetSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, Long> idsMapper)
    {
        super(searchDAO, authProvider, idsMapper);
    }

    protected Class<? extends AbstractCompositeSearchCriteria> getContainerSearchCriteriaClass()
    {
        return DataSetContainerSearchCriteria.class;
    }

    @Override
    protected Class<? extends AbstractCompositeSearchCriteria> getParentsSearchCriteriaClass()
    {
        return DataSetParentsSearchCriteria.class;
    }

    @Override
    protected Class<? extends AbstractCompositeSearchCriteria> getChildrenSearchCriteriaClass()
    {
        return DataSetChildrenSearchCriteria.class;
    }

    @Override
    protected DataSetSearchCriteria createEmptyCriteria(final boolean negated)
    {
        final DataSetSearchCriteria dataSetSearchCriteria = new DataSetSearchCriteria();
        if (negated)
        {
            dataSetSearchCriteria.negate();
        }
        return dataSetSearchCriteria;
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return getAuthProvider().getAuthorisedDatasets(ids, authorisationInformation);
    }

    private CodeSearchCriteria convertToCodeSearchCriterion(final PermIdSearchCriteria permIdSearchCriteria)
    {
        final CodeSearchCriteria codeSearchCriteria = new CodeSearchCriteria();
        codeSearchCriteria.setFieldValue(permIdSearchCriteria.getFieldValue());
        return codeSearchCriteria;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final DataSetSearchCriteria criteria, final AbstractCompositeSearchCriteria parentCriteria,
            final String idsColumnName)
    {
        final Class<? extends ISearchCriteria> parentsSearchCriteriaClass = getParentsSearchCriteriaClass();
        final Class<? extends ISearchCriteria> containerSearchCriteriaClass = getContainerSearchCriteriaClass();
        final Class<? extends ISearchCriteria> childrenSearchCriteriaClass = getChildrenSearchCriteriaClass();
        final List<? extends ISearchCriteria> parentsCriteria = getCriteria(criteria, parentsSearchCriteriaClass);
        final List<? extends ISearchCriteria> containerCriteria = getCriteria(criteria, containerSearchCriteriaClass);

        final Collection<? extends ISearchCriteria> childrenCriteria = getCriteria(criteria,
                childrenSearchCriteriaClass);
        final Collection<DataSetSearchCriteria> nestedCriteria = getCriteria(criteria, DataSetSearchCriteria.class);
        final Collection<ISearchCriteria> mainCriteria = getOtherCriteriaThan(criteria, parentsSearchCriteriaClass,
                childrenSearchCriteriaClass, containerSearchCriteriaClass, DataSetSearchCriteria.class);

        // Replacing perm ID search criteria with code search criteria, because for datasets perm ID is equivalent to code
        final Collection<ISearchCriteria> newMainCriteria = mainCriteria.stream().map(searchCriterion ->
                searchCriterion instanceof PermIdSearchCriteria
                        ? convertToCodeSearchCriterion((PermIdSearchCriteria) searchCriterion)
                        : searchCriterion
                ).collect(Collectors.toList());
        final Collection<ISearchCriteria> newParentsCriteria = parentsCriteria.stream()
                .map(searchCriterion -> searchCriterion instanceof PermIdSearchCriteria
                        ? convertToCodeSearchCriterion((PermIdSearchCriteria) searchCriterion)
                        : searchCriterion
                ).collect(Collectors.toList());
        final Collection<ISearchCriteria> newContainerCriteria = containerCriteria.stream()
                .map(searchCriterion -> searchCriterion instanceof PermIdSearchCriteria
                        ? convertToCodeSearchCriterion((PermIdSearchCriteria) searchCriterion)
                        : searchCriterion
                ).collect(Collectors.toList());
        final Collection<ISearchCriteria> newChildrenCriteria = childrenCriteria.stream()
                .map(searchCriterion -> searchCriterion instanceof PermIdSearchCriteria
                        ? convertToCodeSearchCriterion((PermIdSearchCriteria) searchCriterion)
                        : searchCriterion
                ).collect(Collectors.toList());

        final CompositeEntityCriteriaVo criteriaVo = new CompositeEntityCriteriaVo(newMainCriteria, newParentsCriteria,
                newChildrenCriteria, newContainerCriteria, nestedCriteria, criteria.getOperator(),
                criteria.isNegated());

        return super.doSearchForIDs(userId, criteriaVo, idsColumnName, TableMapper.DATA_SET, authorisationInformation);
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<DataSet> sortOptions) {
        return doSortIDs(ids, sortOptions, TableMapper.DATA_SET);
    }

}
