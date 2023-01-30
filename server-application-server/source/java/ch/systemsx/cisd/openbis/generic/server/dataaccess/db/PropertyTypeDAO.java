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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * <i>Data Access Object</i> implementation for {@link PropertyTypePE}.
 * 
 * @author Christian Ribeaud
 */
final class PropertyTypeDAO extends AbstractGenericEntityDAO<PropertyTypePE> implements
        IPropertyTypeDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PropertyTypeDAO.class);

    PropertyTypeDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, PropertyTypePE.class, historyCreator);
    }

    //
    // IPropertyTypeDAO
    //

    @Override
    public final PropertyTypePE tryFindPropertyTypeByCode(final String code)
            throws DataAccessException
    {
        assert code != null : "Unspecified property type code";

        final String mangledCode = CodeConverter.tryToDatabase(code);
        final boolean internalNamespace = CodeConverter.isInternalNamespace(code);
        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("select pt from %s pt where pt.simpleCode = ? "
                                + "and pt.managedInternally = ?",
                                PropertyTypePE.class.getSimpleName()),
                        toArray(mangledCode, internalNamespace)));
        final PropertyTypePE entity = tryFindEntity(list, "property type", code);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), code, entity));
        }
        return entity;
    }

    @Override
    public List<PropertyTypePE> listAllPropertyTypes()
    {
        final Criteria criteria = currentSession().createCriteria(PropertyTypePE.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<PropertyTypePE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d property types(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<PropertyTypePE> listAllPropertyTypesWithRelations()
    {
        final Criteria criteria = currentSession().createCriteria(PropertyTypePE.class);
        criteria.setFetchMode("materialTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setFetchMode("sampleTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setFetchMode("experimentTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setFetchMode("dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<PropertyTypePE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d property types(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public final List<PropertyTypePE> listPropertyTypes() throws DataAccessException
    {
        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s v where v.managedInternally = false", PropertyTypePE.class
                                .getSimpleName())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d property types(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public final List<DataTypePE> listDataTypes() throws DataAccessException
    {
        final List<DataTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s dt", DataTypePE.class.getSimpleName())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d data types(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public final DataTypePE getDataTypeByCode(final DataTypeCode code) throws DataAccessException
    {
        assert code != null : "Unspecified entity data type.";

        final List<DataTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s dt where dt.code = ?", DataTypePE.class
                                .getSimpleName()),
                        toArray(code)));
        final DataTypePE entity = getEntity(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), code, entity));
        }
        return entity;
    }

    @Override
    public final void createPropertyType(final PropertyTypePE propertyType)
            throws DataAccessException
    {
        assert propertyType != null : "Unspecified property type.";
        validatePE(propertyType);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(propertyType);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: property type '%s'.", propertyType));
        }
    }

    @Override
    public void delete(PropertyTypePE entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";
        int assignmentsSize =
                entity.getDataSetTypePropertyTypes().size()
                        + entity.getExperimentTypePropertyTypes().size()
                        + entity.getMaterialTypePropertyTypes().size()
                        + entity.getSampleTypePropertyTypes().size();
        if (assignmentsSize > 0)
        {
            String errorMsgFormat =
                    "Property Type '%s' cannot be deleted because entity types are assigned.";
            throw new DataIntegrityViolationException(String.format(errorMsgFormat, entity
                    .getCode()));
        }
        super.delete(entity);
    }

}
