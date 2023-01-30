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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.DescriptorAccessionIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.DescriptorOntologyIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.DescriptorOntologyVersionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.PredicateAccessionIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.PredicateOntologyIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.PredicateOntologyVersionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ISearchEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.ISearchPropertyAssignmentExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.ISearchPropertyTypeExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSemanticAnnotationExecutor extends AbstractSearchObjectManuallyExecutor<SemanticAnnotationSearchCriteria, SemanticAnnotationPE>
        implements ISearchSemanticAnnotationExecutor
{

    @Autowired
    private ISearchEntityTypeExecutor searchEntityTypeExecutor;

    @Autowired
    private ISearchPropertyTypeExecutor searchPropertyTypeExecutor;

    @Autowired
    private ISearchPropertyAssignmentExecutor searchPropertyAssignmentExecutor;

    @Autowired
    private ISemanticAnnotationAuthorizationExecutor authorizationExecutor;

    @Override
    public List<SemanticAnnotationPE> search(IOperationContext context, SemanticAnnotationSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<SemanticAnnotationPE> listAll()
    {
        return daoFactory.getSemanticAnnotationDAO().listAllEntities();
    }

    @Override
    protected Matcher<SemanticAnnotationPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return new PermIdMatcher();
        } else if (criteria instanceof EntityTypeSearchCriteria)
        {
            return new EntityTypeMatcher();
        } else if (criteria instanceof PropertyTypeSearchCriteria)
        {
            return new PropertyTypeMatcher();
        } else if (criteria instanceof PropertyAssignmentSearchCriteria)
        {
            return new PropertyAssignmentMatcher();
        } else if (criteria instanceof PredicateOntologyIdSearchCriteria)
        {
            return new PredicateOntologyIdMatcher();
        } else if (criteria instanceof PredicateOntologyVersionSearchCriteria)
        {
            return new PredicateOntologyVersionMatcher();
        } else if (criteria instanceof PredicateAccessionIdSearchCriteria)
        {
            return new PredicateAccessionIdMatcher();
        } else if (criteria instanceof DescriptorOntologyIdSearchCriteria)
        {
            return new DescriptorOntologyIdMatcher();
        } else if (criteria instanceof DescriptorOntologyVersionSearchCriteria)
        {
            return new DescriptorOntologyVersionMatcher();
        } else if (criteria instanceof DescriptorAccessionIdSearchCriteria)
        {
            return new DescriptorAccessionIdMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, SemanticAnnotationPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof SemanticAnnotationPermId)
            {
                return object.getPermId().equals(((SemanticAnnotationPermId) id).getPermId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + criteria.getClass());
            }
        }

    }

    private class PermIdMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getPermId();
        }

    }

    private class PredicateOntologyIdMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getPredicateOntologyId();
        }

    }

    private class PredicateOntologyVersionMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getPredicateOntologyVersion();
        }

    }

    private class PredicateAccessionIdMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getPredicateAccessionId();
        }

    }

    private class DescriptorOntologyIdMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getDescriptorOntologyId();
        }

    }

    private class DescriptorOntologyVersionMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getDescriptorOntologyVersion();
        }

    }

    private class DescriptorAccessionIdMatcher extends StringFieldMatcher<SemanticAnnotationPE>
    {

        @Override
        protected String getFieldValue(SemanticAnnotationPE object)
        {
            return object.getDescriptorAccessionId();
        }

    }

    private class EntityTypeMatcher extends Matcher<SemanticAnnotationPE>
    {

        @Override
        public List<SemanticAnnotationPE> getMatching(IOperationContext context, List<SemanticAnnotationPE> objects, ISearchCriteria criteria)
        {
            List<EntityTypePE> entityTypeList = searchEntityTypeExecutor.search(context, (EntityTypeSearchCriteria) criteria);
            Set<EntityTypePE> entityTypeSet = new HashSet<EntityTypePE>(entityTypeList);

            List<SemanticAnnotationPE> matches = new ArrayList<SemanticAnnotationPE>();

            for (SemanticAnnotationPE object : objects)
            {
                if (object.getSampleType() != null && entityTypeSet.contains(object.getSampleType()))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

    }

    private class PropertyTypeMatcher extends Matcher<SemanticAnnotationPE>
    {

        @Override
        public List<SemanticAnnotationPE> getMatching(IOperationContext context, List<SemanticAnnotationPE> objects, ISearchCriteria criteria)
        {
            List<PropertyTypePE> propertyTypeList = searchPropertyTypeExecutor.search(context, (PropertyTypeSearchCriteria) criteria);
            Set<PropertyTypePE> propertyTypeSet = new HashSet<PropertyTypePE>(propertyTypeList);

            List<SemanticAnnotationPE> matches = new ArrayList<SemanticAnnotationPE>();

            for (SemanticAnnotationPE object : objects)
            {
                if (object.getPropertyType() != null && propertyTypeSet.contains(object.getPropertyType()))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

    }

    private class PropertyAssignmentMatcher extends Matcher<SemanticAnnotationPE>
    {

        @Override
        public List<SemanticAnnotationPE> getMatching(IOperationContext context, List<SemanticAnnotationPE> objects, ISearchCriteria criteria)
        {
            List<EntityTypePropertyTypePE> propertyAssignmentList =
                    searchPropertyAssignmentExecutor.search(context, (PropertyAssignmentSearchCriteria) criteria);
            Set<EntityTypePropertyTypePE> propertyAssignmentSet = new HashSet<EntityTypePropertyTypePE>(propertyAssignmentList);

            List<SemanticAnnotationPE> matches = new ArrayList<SemanticAnnotationPE>();

            for (SemanticAnnotationPE object : objects)
            {
                if (object.getSampleTypePropertyType() != null && propertyAssignmentSet.contains(object.getSampleTypePropertyType()))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

    }

}
