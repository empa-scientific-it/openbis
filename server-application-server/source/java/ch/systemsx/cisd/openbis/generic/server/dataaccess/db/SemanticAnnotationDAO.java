/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISemanticAnnotationDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * <i>Data Access Object</i> implementation for {@link SemanticAnnotationPE}.
 * 
 * @author pkupczyk
 */
public final class SemanticAnnotationDAO extends AbstractGenericEntityDAO<SemanticAnnotationPE> implements ISemanticAnnotationDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SemanticAnnotationDAO.class);

    public static final String ERROR_OWNER_CANNOT_BE_NULL_OR_MORE_THAN_ONE =
            "Semantic annotation has to be assigned either to entity type, property type or property assignment.";

    public static final String ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY = "Predicate of a semantic annotation has to be defined, i.e. predicate:\n"
            + "- ontology id cannot be null or empty\n"
            + "- ontology version cannot be null\n"
            + "- accession id cannot be null or empty\n";

    public static final String ERROR_DESCRIPTOR_CANNOT_BE_NULL_OR_EMPTY =
            "Descriptor of an entity type semantic annotation has to be defined, i.e. descriptor:\n"
                    + "- ontology id cannot be null or empty\n"
                    + "- ontology version cannot be null\n"
                    + "- accession id cannot be null or empty\n";

    public static final String ERROR_DESCRIPTOR_CAN_BE_NULL_OR_NON_EMPTY =
            "Descriptor of a property type or property assignment semantic annotation has to be either defined, i.e descriptor:\n"
                    + "- ontology id cannot be null or empty\n"
                    + "- ontology version cannot be null\n"
                    + "- accession id cannot be null or empty\n"
                    + "or the whole descriptor has to be null, i.e. descriptor:\n"
                    + "- ontology id has to be null\n"
                    + "- ontology version has to be null\n"
                    + "- accession id has to be null\n";

    SemanticAnnotationDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, SemanticAnnotationPE.class, historyCreator);
    }

    @Override
    public void createOrUpdate(SemanticAnnotationPE annotation)
    {
        validatePE(annotation);
        validateAnnotation(annotation);

        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(annotation);

        flushWithSqlExceptionHandling(template);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Created or updated semantic annotation '%s'.", annotation));
        }
    }

    @Override
    public List<SemanticAnnotationPE> findByIds(Collection<Long> ids)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SemanticAnnotationPE.class);
        criteria.add(Restrictions.in("id", ids));

        final List<SemanticAnnotationPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d semantic annotation(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<SemanticAnnotationPE> findByPermIds(Collection<String> permIds)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SemanticAnnotationPE.class);
        criteria.add(Restrictions.in("permId", permIds));

        final List<SemanticAnnotationPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d semantic annotation(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    private void validateAnnotation(SemanticAnnotationPE annotation)
    {
        int notNullCount = 0;

        if (annotation.getSampleType() != null)
        {
            notNullCount++;
        }
        if (annotation.getPropertyType() != null)
        {
            notNullCount++;
        }
        if (annotation.getSampleTypePropertyType() != null)
        {
            notNullCount++;
        }

        if (notNullCount != 1)
        {
            throw new UserFailureException(ERROR_OWNER_CANNOT_BE_NULL_OR_MORE_THAN_ONE);
        }

        if (isNullOrEmpty(annotation.getPredicateOntologyId()) || isNullOrEmpty(annotation.getPredicateAccessionId())
                || isNull(annotation.getPredicateOntologyVersion()))
        {
            throw new UserFailureException(ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY);
        }

        if (annotation.getSampleType() != null)
        {
            if (isNullOrEmpty(annotation.getDescriptorOntologyId()) || isNullOrEmpty(annotation.getDescriptorAccessionId())
                    || isNull(annotation.getDescriptorOntologyVersion()))
            {
                throw new UserFailureException(ERROR_DESCRIPTOR_CANNOT_BE_NULL_OR_EMPTY);
            }
        } else
        {
            if (isNull(annotation.getDescriptorOntologyId()) && isNull(annotation.getDescriptorAccessionId())
                    && isNull(annotation.getDescriptorOntologyVersion()))
            {
                return;
            } else if (isNullOrEmpty(annotation.getDescriptorOntologyId()) || isNullOrEmpty(annotation.getDescriptorAccessionId())
                    || isNull(annotation.getDescriptorOntologyVersion()))
            {
                throw new UserFailureException(ERROR_DESCRIPTOR_CAN_BE_NULL_OR_NON_EMPTY);
            }
        }
    }

    private boolean isNull(String value)
    {
        return value == null;
    }

    private boolean isNullOrEmpty(String value)
    {
        return value == null || value.trim().isEmpty();
    }

}
