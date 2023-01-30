/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewSample}.
 * 
 * @author Christian Ribeaud
 */
class NewSampleParserObjectFactory extends AbstractParserObjectFactory<NewSample>
{
    private final SampleType sampleType;

    private final boolean identifierExpectedInFile;

    private final boolean allowExperiments;

    NewSampleParserObjectFactory(final SampleType sampleType, final IPropertyMapper propertyMapper,
            boolean identifierExpectedInFile, boolean allowExperiments)
    {
        super(NewSample.class, propertyMapper);
        this.sampleType = sampleType;
        this.identifierExpectedInFile = identifierExpectedInFile;
        this.allowExperiments = allowExperiments;
    }

    private final PropertyType createPropertyType(final String propertyTypeCode)
    {
        final PropertyType propertyType = new PropertyType();
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType.setCode(propertyTypeCode);
        propertyType.setDataType(dataType);
        return propertyType;
    }

    private final void setProperties(final NewSample newSample, final String[] lineTokens)
    {
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        for (final String unmatchedProperty : getUnmatchedProperties())
        {
            final IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
            final String propertyDefault = tryGetPropertyDefault(unmatchedProperty);
            final String propertyValue =
                    getPropertyValue(lineTokens, propertyModel, propertyDefault);
            if (StringUtils.isEmpty(propertyValue) == false)
            {
                final IEntityProperty property = new EntityProperty();
                property.setPropertyType(createPropertyType(unmatchedProperty));
                property.setValue(propertyValue);
                properties.add(property);
            }
        }
        newSample.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
    }

    //
    // AbstractParserObjectFactory
    //

    @Override
    protected final boolean ignoreUnmatchedProperties()
    {
        return true;
    }

    @Override
    public NewSample createObject(final String[] lineTokens) throws ParserException
    {
        final NewSample newSample = super.createObject(lineTokens);
        if (identifierExpectedInFile && newSample.getIdentifier() == null)
        {
            throw new ParserException("Mandatory column '" + Identifier.IDENTIFIER_COLUMN
                    + "' is missing.");
        }
        if (identifierExpectedInFile == false && newSample.getIdentifier() != null)
        {
            throw new ParserException("Requested automatical generation of codes. Column '"
                    + Identifier.IDENTIFIER_COLUMN + "' should be removed from the file.");
        }
        if (allowExperiments == false && newSample.getExperimentIdentifier() != null)
        {
            throw new ParserException("Column '" + NewSample.EXPERIMENT
                    + "' should be removed from the file.");
        }
        newSample.setSampleType(sampleType);
        setProperties(newSample, lineTokens);
        if (newSample.getParentsOrNull() != null)
        {
            List<String> parents = new ArrayList<String>();
            for (String parent : newSample.getParentsOrNull())
            {
                String trimmedOrNull = StringUtils.trimToNull(parent);
                if (trimmedOrNull != null)
                {
                    parents.add(trimmedOrNull);
                }
            }
            if (parents.size() == 0)
            {
                newSample.setParentsOrNull(null);
            } else
            {
                newSample.setParentsOrNull(parents.toArray(new String[0]));
            }
        }
        newSample.setExperimentIdentifier(StringUtils.trimToNull(newSample
                .getExperimentIdentifier()));
        return newSample;
    }
}
