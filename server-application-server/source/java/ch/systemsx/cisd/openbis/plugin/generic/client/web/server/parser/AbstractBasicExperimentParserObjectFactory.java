/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewBasicExperiment}.
 *
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
public abstract class AbstractBasicExperimentParserObjectFactory<T extends NewBasicExperiment>
        extends AbstractParserObjectFactory<T>
{
    /**
     * @param beanClass
     * @param propertyMapper
     */
    protected AbstractBasicExperimentParserObjectFactory(Class<T> beanClass,
            IPropertyMapper propertyMapper)
    {
        super(beanClass, propertyMapper);
    }

    private final PropertyType createPropertyType(final String propertyTypeCode)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        return propertyType;
    }

    private final void setProperties(final T newExperiment, final String[] lineTokens)
    {
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        for (final String unmatchedProperty : getUnmatchedProperties())
        {
            final IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
            final String propertyDefault = tryGetPropertyDefault(unmatchedProperty);
            final String propertyValue =
                    getPropertyValue(lineTokens, propertyModel, propertyDefault);
            if (StringUtils.isBlank(propertyValue) == false)
            {
                PropertyType propertyType = createPropertyType(unmatchedProperty);
                if (isDeletionMark(propertyValue))
                {
                    final IEntityProperty property = new EntityProperty();
                    property.setPropertyType(propertyType);
                    property.setValue(null);
                    properties.add(property);
                } else
                {
                    String[] splitted = propertyValue.split(",");
                    for (String v : splitted)
                    {
                        final IEntityProperty property = new EntityProperty();
                        property.setPropertyType(propertyType);
                        property.setValue(v);
                        properties.add(property);
                    }
                }
            }
        }
        newExperiment.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
    }

    @Override
    protected final boolean ignoreUnmatchedProperties()
    {
        return true;
    }

    @Override
    public T createObject(final String[] lineTokens) throws ParserException
    {
        final T newExperiment = super.createObject(lineTokens);
        setProperties(newExperiment, lineTokens);
        return newExperiment;
    }
}
