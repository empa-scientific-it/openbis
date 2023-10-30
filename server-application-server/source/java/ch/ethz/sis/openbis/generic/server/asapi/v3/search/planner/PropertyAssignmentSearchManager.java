/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.IPropertyAssignmentSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROPERTY_TYPE_COLUMN;

/**
 * Manages detailed search with complex property assignment search criteria.
 *
 * @author Viktor Kovtun
 */
public class PropertyAssignmentSearchManager extends
        AbstractLocalSearchManager<PropertyAssignmentSearchCriteria, PropertyAssignment, Long>
{

    /** What property assignment table mapper to use based on the type of the parent criterion. */
    private static final Map<Class<? extends AbstractCompositeSearchCriteria>, TableMapper> TABLE_MAPPER_BY_CRITERIA = Map.of(
            SampleTypeSearchCriteria.class, TableMapper.SAMPLE_PROPERTY_ASSIGNMENT,
            ExperimentTypeSearchCriteria.class, TableMapper.EXPERIMENT_PROPERTY_ASSIGNMENT,
            DataSetTypeSearchCriteria.class, TableMapper.DATA_SET_PROPERTY_ASSIGNMENT,
            MaterialTypeSearchCriteria.class, TableMapper.MATERIAL_PROPERTY_ASSIGNMENT
    );

    private final IPropertyAssignmentSearchDAO assignmentsSearchDAO;

    public PropertyAssignmentSearchManager(final ISQLSearchDAO searchDAO,
            final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, Long> idsMapper, final IPropertyAssignmentSearchDAO assignmentsSearchDAO)
    {
        super(searchDAO, authProvider, idsMapper);
        this.assignmentsSearchDAO = assignmentsSearchDAO;
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final PropertyAssignmentSearchCriteria criteria,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        final TableMapper tableMapper = TABLE_MAPPER_BY_CRITERIA.get(parentCriteria.getClass());
        if (tableMapper == null)
        {
            throw new IllegalArgumentException(String.format("Table mapper not found for the parent criterion class %s.", parentCriteria.getClass()));
        }

        final String typeIdColumnName = tableMapper.getEntityTypesAttributeTypesTableEntityTypeIdField();
        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId,
                criteria, tableMapper, typeIdColumnName, authorisationInformation);

        final Set<Long> finalResults;
        // Very special case when property assignments should be linked with semantic annotations both directly and via attribute types
        if (isSampleTypeWithSemanticAnnotationsCriteria(parentCriteria, criteria))
        {
            final DummyCompositeSearchCriterion compositeSearchCriterion = new DummyCompositeSearchCriterion(
                    criteria.getCriteria(), criteria.getOperator());

            final Set<Long> propertyTypesIds = getSearchDAO().queryDBForIdsWithGlobalSearchMatchCriteria(userId,
                    compositeSearchCriterion, TableMapper.SEMANTIC_ANNOTATION, PROPERTY_TYPE_COLUMN, authorisationInformation);

            final Set<Long> assignmentIDsWithoutAnnotations = assignmentsSearchDAO.findAssignmentsWithoutAnnotations(
                    propertyTypesIds, typeIdColumnName);

            finalResults = new HashSet<>(mainCriteriaIntermediateResults);
            finalResults.addAll(assignmentIDsWithoutAnnotations);
        } else
        {
            finalResults = mainCriteriaIntermediateResults;
        }

        return filterIDsByUserRights(userId, authorisationInformation, finalResults);
    }

    private static boolean isSampleTypeWithSemanticAnnotationsCriteria(final AbstractCompositeSearchCriteria parentCriteria,
            final PropertyAssignmentSearchCriteria criteria)
    {
        return parentCriteria.getClass() == SampleTypeSearchCriteria.class
                && criteria.getCriteria().stream().anyMatch((subcriterion) -> subcriterion instanceof SemanticAnnotationSearchCriteria);
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<PropertyAssignment> sortOptions) {
        return doSortIDs(ids, sortOptions, TableMapper.SAMPLE_PROPERTY_ASSIGNMENT);
    }

    @Override
    protected AbstractCompositeSearchCriteria createEmptyCriteria(final boolean negated)
    {
        return new PropertyAssignmentSearchCriteria();
    }
}
