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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ISearchEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation.ISearchSemanticAnnotationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class SearchPropertyAssignmentExecutor extends AbstractSearchObjectManuallyExecutor<PropertyAssignmentSearchCriteria, EntityTypePropertyTypePE>
        implements ISearchPropertyAssignmentExecutor
{

    @Autowired
    private ISearchEntityTypeExecutor searchEntityTypeExecutor;

    @Autowired
    private ISearchPropertyTypeExecutor searchPropertyTypeExecutor;

    @Autowired
    private ISearchSemanticAnnotationExecutor searchSemanticAnnotationExecutor;

    @Override
    protected List<EntityTypePropertyTypePE> listAll()
    {
        List<EntityTypePropertyTypePE> propertyAssignments = new ArrayList<EntityTypePropertyTypePE>();
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL).listEntityPropertyTypes());
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT).listEntityPropertyTypes());
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.SAMPLE).listEntityPropertyTypes());
        propertyAssignments.addAll(daoFactory.getEntityPropertyTypeDAO(EntityKind.DATA_SET).listEntityPropertyTypes());
        return propertyAssignments;
    }

    @Override
    protected Matcher<EntityTypePropertyTypePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof IdsSearchCriteria<?>)
        {
            return new IdsMatcher();
        } else if (criteria instanceof EntityTypeSearchCriteria)
        {
            return new EntityTypeMatcher();
        } else if (criteria instanceof PropertyTypeSearchCriteria)
        {
            return new PropertyTypeMatcher();
        } else if (criteria instanceof SemanticAnnotationSearchCriteria)
        {
            return new SemanticAnnotationMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends Matcher<EntityTypePropertyTypePE>
    {

        @SuppressWarnings("unchecked")
        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> objects, ISearchCriteria criteria)
        {
            IPropertyAssignmentId id = ((IdSearchCriteria<IPropertyAssignmentId>) criteria).getId();

            if (id == null)
            {
                return objects;
            } else
            {
                IdsSearchCriteria<IPropertyAssignmentId> idsCriteria = new IdsSearchCriteria<IPropertyAssignmentId>();
                idsCriteria.thatIn(Arrays.asList(id));
                return new IdsMatcher().getMatching(context, objects, idsCriteria);
            }
        }

    }

    private class IdsMatcher extends Matcher<EntityTypePropertyTypePE>
    {

        @SuppressWarnings("unchecked")
        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> objects, ISearchCriteria criteria)
        {
            Collection<IPropertyAssignmentId> ids = ((IdsSearchCriteria<IPropertyAssignmentId>) criteria).getFieldValue();

            if (ids != null && false == ids.isEmpty())
            {
                for (IPropertyAssignmentId id : ids)
                {
                    if (id instanceof PropertyAssignmentPermId)
                    {
                        PropertyAssignmentPermId permId = (PropertyAssignmentPermId) id;
                        checkEntityTypeId(permId.getEntityTypeId());
                        checkPropertyTypeId(permId.getPropertyTypeId());
                    } else
                    {
                        throw new IllegalArgumentException("Unknown id: " + id.getClass());
                    }
                }

                Set<IPropertyAssignmentId> idsSet = new HashSet<IPropertyAssignmentId>(ids);
                List<EntityTypePropertyTypePE> matches = new ArrayList<EntityTypePropertyTypePE>();

                for (EntityTypePropertyTypePE object : objects)
                {
                    IEntityTypeId entityTypeId = new EntityTypePermId(object.getEntityType().getCode(),
                            EntityKindConverter.convert(object.getEntityType().getEntityKind()));
                    IPropertyTypeId propertyTypeId = new PropertyTypePermId(object.getPropertyType().getCode());
                    IPropertyAssignmentId propertyAssignmentId = new PropertyAssignmentPermId(entityTypeId, propertyTypeId);

                    if (idsSet.contains(propertyAssignmentId))
                    {
                        matches.add(object);
                    }
                }

                return matches;
            } else
            {
                return new ArrayList<EntityTypePropertyTypePE>();
            }
        }

    }

    private class EntityTypeMatcher extends Matcher<EntityTypePropertyTypePE>
    {

        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> objects, ISearchCriteria criteria)
        {
            List<EntityTypePE> entityTypeList = searchEntityTypeExecutor.search(context, (EntityTypeSearchCriteria) criteria);
            Set<EntityTypePE> entityTypeSet = new HashSet<EntityTypePE>(entityTypeList);

            List<EntityTypePropertyTypePE> matching = new ArrayList<EntityTypePropertyTypePE>();
            for (EntityTypePropertyTypePE object : objects)
            {
                if (entityTypeSet.contains(object.getEntityType()))
                {
                    matching.add(object);
                }
            }

            return matching;
        }

    }

    private class PropertyTypeMatcher extends Matcher<EntityTypePropertyTypePE>
    {

        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> objects, ISearchCriteria criteria)
        {
            List<PropertyTypePE> propertyTypeList = searchPropertyTypeExecutor.search(context, (PropertyTypeSearchCriteria) criteria);
            Set<PropertyTypePE> propertyTypeSet = new HashSet<PropertyTypePE>(propertyTypeList);

            List<EntityTypePropertyTypePE> matching = new ArrayList<EntityTypePropertyTypePE>();
            for (EntityTypePropertyTypePE object : objects)
            {
                if (propertyTypeSet.contains(object.getPropertyType()))
                {
                    matching.add(object);
                }
            }

            return matching;
        }

    }

    private class SemanticAnnotationMatcher extends Matcher<EntityTypePropertyTypePE>
    {
        @Override
        public List<EntityTypePropertyTypePE> getMatching(IOperationContext context, List<EntityTypePropertyTypePE> assignments,
                ISearchCriteria criteria)
        {
            List<EntityTypePropertyTypePE> sampleAssignments = getSampleAssignments(assignments);

            List<SemanticAnnotationPE> annotations =
                    searchSemanticAnnotationExecutor.search(context, (SemanticAnnotationSearchCriteria) criteria);

            Set<PropertyTypePE> propertyTypesWithMatchingAnnotations =
                    getPropertyTypesWithMatchingAnnotations(context, sampleAssignments, annotations);
            Set<EntityTypePropertyTypePE> assignmentsWithMatchingAnnotations =
                    getAssignmentsWithMatchingAnnotations(context, sampleAssignments, annotations);
            Set<EntityTypePropertyTypePE> assignmentsWithoutAnyAnnotations = getAssignmentsWithoutAnyAnnotations(context, sampleAssignments);
            Set<EntityTypePropertyTypePE> matching = new HashSet<EntityTypePropertyTypePE>();

            matching.addAll(assignmentsWithMatchingAnnotations);

            for (EntityTypePropertyTypePE assignmentWithoutAnyAnnotations : assignmentsWithoutAnyAnnotations)
            {
                if (propertyTypesWithMatchingAnnotations.contains(assignmentWithoutAnyAnnotations.getPropertyType()))
                {
                    matching.add(assignmentWithoutAnyAnnotations);
                }
            }

            return new ArrayList<EntityTypePropertyTypePE>(matching);
        }

        private List<EntityTypePropertyTypePE> getSampleAssignments(List<EntityTypePropertyTypePE> assignments)
        {
            List<EntityTypePropertyTypePE> sampleAssignments = new ArrayList<EntityTypePropertyTypePE>();

            for (EntityTypePropertyTypePE assignment : assignments)
            {
                if (EntityKind.SAMPLE.equals(assignment.getEntityType().getEntityKind()))
                {
                    sampleAssignments.add(assignment);
                }
            }

            return sampleAssignments;
        }

        private Set<PropertyTypePE> getPropertyTypesWithMatchingAnnotations(IOperationContext context, List<EntityTypePropertyTypePE> assignments,
                List<SemanticAnnotationPE> annotations)
        {
            Set<PropertyTypePE> propertyTypesSet = new HashSet<PropertyTypePE>();
            Set<PropertyTypePE> propertyTypesWithAnnotations = new HashSet<PropertyTypePE>();

            for (EntityTypePropertyTypePE assignment : assignments)
            {
                propertyTypesSet.add(assignment.getPropertyType());
            }

            for (SemanticAnnotationPE annotation : annotations)
            {
                if (annotation.getPropertyType() != null && propertyTypesSet.contains(annotation.getPropertyType()))
                {
                    propertyTypesWithAnnotations.add(annotation.getPropertyType());
                }
            }

            return propertyTypesWithAnnotations;
        }

        private Set<EntityTypePropertyTypePE> getAssignmentsWithMatchingAnnotations(IOperationContext context,
                List<EntityTypePropertyTypePE> assignments,
                List<SemanticAnnotationPE> annotations)
        {
            Set<EntityTypePropertyTypePE> assignmentsSet = new HashSet<EntityTypePropertyTypePE>(assignments);
            Set<EntityTypePropertyTypePE> assignmentsWithAnnotations = new HashSet<EntityTypePropertyTypePE>();

            for (SemanticAnnotationPE annotation : annotations)
            {
                if (annotation.getSampleTypePropertyType() != null && assignmentsSet.contains(annotation.getSampleTypePropertyType()))
                {
                    assignmentsWithAnnotations.add(annotation.getSampleTypePropertyType());
                }
            }

            return assignmentsWithAnnotations;
        }

        private Set<EntityTypePropertyTypePE> getAssignmentsWithoutAnyAnnotations(IOperationContext context,
                List<EntityTypePropertyTypePE> assignments)
        {
            List<SemanticAnnotationPE> allAnnotations = searchSemanticAnnotationExecutor.search(context, new SemanticAnnotationSearchCriteria());
            Set<EntityTypePropertyTypePE> assignmentsWithoutAnyAnnotations = new HashSet<EntityTypePropertyTypePE>(assignments);

            for (SemanticAnnotationPE annotation : allAnnotations)
            {
                if (annotation.getSampleTypePropertyType() != null)
                {
                    assignmentsWithoutAnyAnnotations.remove(annotation.getSampleTypePropertyType());
                }
            }

            return assignmentsWithoutAnyAnnotations;
        }
    }

    private void checkEntityTypeId(IEntityTypeId entityTypeId)
    {
        if (entityTypeId == null)
        {
            throw new UserFailureException("Property assignment entity type id cannot be null");
        }

        if (false == entityTypeId instanceof EntityTypePermId)
        {
            throw new IllegalArgumentException("Unknown id: " + entityTypeId.getClass());
        }

        EntityTypePermId entityTypePermId = (EntityTypePermId) entityTypeId;

        if (entityTypePermId.getPermId() == null)
        {
            throw new UserFailureException("Entity type perm id cannot be null");
        }

        if (entityTypePermId.getEntityKind() == null)
        {
            throw new UserFailureException("Entity type entity kind cannot be null");
        }
    }

    private void checkPropertyTypeId(IPropertyTypeId propertyTypeId)
    {
        if (propertyTypeId == null)
        {
            throw new UserFailureException("Property assignment property type id cannot be null");
        }

        if (false == propertyTypeId instanceof PropertyTypePermId)
        {
            throw new IllegalArgumentException("Unknown id: " + propertyTypeId.getClass());
        }

        PropertyTypePermId propertyTypePermId = (PropertyTypePermId) propertyTypeId;

        if (propertyTypePermId.getPermId() == null)
        {
            throw new UserFailureException("Property type perm id cannot be null");
        }
    }

}
