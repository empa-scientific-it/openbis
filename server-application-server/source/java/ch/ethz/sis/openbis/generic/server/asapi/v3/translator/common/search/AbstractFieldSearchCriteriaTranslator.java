/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractFieldSearchCriteriaTranslator extends AbstractSearchCriteriaTranslator
{

    private IDAOFactory daoFactory;

    private IObjectAttributeProviderFactory entityAttributeProviderFactory;

    public AbstractFieldSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        this.daoFactory = daoFactory;
        this.entityAttributeProviderFactory = entityAttributeProviderFactory;
    }

    protected DetailedSearchField getDetailedSearchField(SearchTranslationContext context, AbstractFieldSearchCriteria<?> criteria)
    {
        DetailedSearchField field;

        if (criteria.getFieldType().equals(SearchFieldType.PROPERTY))
        {
            field = DetailedSearchField.createPropertyField(criteria.getFieldName());
        } else if (criteria.getFieldType().equals(SearchFieldType.ATTRIBUTE))
        {
            IAttributeSearchFieldKind attribute = getEntityAttributeProvider(context).getAttribute(criteria);
            field = DetailedSearchField.createAttributeField(attribute);
        } else if (criteria.getFieldType().equals(SearchFieldType.ANY_FIELD))
        {
            field = DetailedSearchField.createAnyField(getEntityPropertyTypeDAO(context).listPropertyTypeCodes());
        } else if (criteria.getFieldType().equals(SearchFieldType.ANY_PROPERTY))
        {
            field = DetailedSearchField.createAnyPropertyField(getEntityPropertyTypeDAO(context).listPropertyTypeCodes());
        } else
        {
            throw new IllegalArgumentException("Unknown search field type: " + criteria.getFieldType());
        }

        return field;
    }

    protected IObjectAttributeProvider getEntityAttributeProvider(SearchTranslationContext context)
    {
        return getEntityAttributeProviderFactory().getProvider(context.peekObjectKind());
    }

    protected IEntityPropertyTypeDAO getEntityPropertyTypeDAO(SearchTranslationContext context)
    {
        EntityKind entityKind = getEntityPropertyKind(context.peekObjectKind());
        return getDaoFactory().getEntityPropertyTypeDAO(entityKind);
    }

    protected IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    protected IObjectAttributeProviderFactory getEntityAttributeProviderFactory()
    {
        return entityAttributeProviderFactory;
    }

    private EntityKind getEntityPropertyKind(SearchObjectKind objectKind)
    {
        switch (objectKind)
        {
            case EXPERIMENT:
                return EntityKind.EXPERIMENT;
            case SAMPLE:
                return EntityKind.SAMPLE;
            case DATA_SET:
                return EntityKind.DATA_SET;
            case MATERIAL:
                return EntityKind.MATERIAL;
            default:
                throw new IllegalArgumentException("Unknown object kind: " + objectKind);
        }
    }

}
