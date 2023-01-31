/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Utility class for creating SampleDataSetPair objects from a text file.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleDataSetPairParserObjectFactory extends AbstractParserObjectFactory<SampleDataSetPair>
{

    private static final String DATASET_PREFIX = "d_";

    private static final String SAMPLE_PREFIX = "s_";

    /**
     * Clients use the factory factory to make a SampleDataSetPairParserObjectFactory.
     */
    public static IParserObjectFactoryFactory<SampleDataSetPair> createFactoryFactory(
            final SampleType sampleType, final DataSetType dataSetType, DataSetKind dataSetKind)
    {
        IParserObjectFactoryFactory<SampleDataSetPair> factoryFactory =
                new IParserObjectFactoryFactory<SampleDataSetPair>()
                    {
                        @Override
                        public final IParserObjectFactory<SampleDataSetPair> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            return new SampleDataSetPairParserObjectFactory(sampleType,
                                    dataSetType, dataSetKind, propertyMapper);
                        }
                    };

        return factoryFactory;
    }

    private final SampleType sampleType;

    private final DataSetType dataSetType;

    private final DataSetKind dataSetKind;

    /**
     * Clients should use the factory factory to create the factory.
     * 
     * @param propertyMapper
     */
    private SampleDataSetPairParserObjectFactory(SampleType sampleType, DataSetType dataSetType, DataSetKind dataSetKind,
            IPropertyMapper propertyMapper)
    {
        super(SampleDataSetPair.class, propertyMapper);
        this.sampleType = sampleType;
        this.dataSetType = dataSetType;
        this.dataSetKind = dataSetKind;
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

    private final void setProperties(final SampleDataSetPair newSampleDataSet,
            final String[] lineTokens)
    {
        final List<IEntityProperty> sampleProperties = new ArrayList<IEntityProperty>();
        final List<NewProperty> dataSetProperties = new ArrayList<NewProperty>();
        for (final String unmatchedProperty : getUnmatchedProperties())
        {
            IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
            String propertyDefault = tryGetPropertyDefault(unmatchedProperty);
            String propertyValue = getPropertyValue(lineTokens, propertyModel, propertyDefault);
            if (StringUtils.isEmpty(propertyValue))
            {
                continue;
            }

            if (unmatchedProperty.startsWith(SAMPLE_PREFIX))
            {
                addEntityPropertyToList(SAMPLE_PREFIX, sampleProperties, unmatchedProperty,
                        propertyValue);
            } else if (unmatchedProperty.startsWith(DATASET_PREFIX))
            {
                addNewPropertyToList(DATASET_PREFIX, dataSetProperties, unmatchedProperty,
                        propertyValue);
            }

        }
        newSampleDataSet.setSampleProperties(sampleProperties.toArray(IEntityProperty.EMPTY_ARRAY));
        newSampleDataSet.setDataSetProperties(dataSetProperties);
    }

    private void addEntityPropertyToList(String prefix, final List<IEntityProperty> list,
            final String unmatchedProperty, String propertyValue)
    {
        IEntityProperty property = new EntityProperty();
        property.setPropertyType(createPropertyType(unmatchedProperty.substring(prefix.length())));
        property.setValue(propertyValue);
        list.add(property);
    }

    private void addNewPropertyToList(String prefix, final List<NewProperty> list,
            final String unmatchedProperty, String propertyValue)
    {
        NewProperty property = new NewProperty();
        property.setPropertyCode(unmatchedProperty.substring(prefix.length()));
        property.setValue(propertyValue);
        list.add(property);
    }

    //
    // AbstractParserObjectFactory
    //

    @Override
    protected boolean ignoreUnmatchedProperties()
    {
        return true;
    }

    @Override
    public SampleDataSetPair createObject(final String[] lineTokens) throws ParserException
    {
        boolean allTokensAreEmpty = true;
        for (String token : lineTokens)
        {
            if (token.trim().length() > 0)
            {
                allTokensAreEmpty = false;
                break;
            }
        }

        // Skip empty lines
        if (allTokensAreEmpty)
        {
            return null;
        }

        final SampleDataSetPair newSampleDataSet = super.createObject(lineTokens);
        newSampleDataSet.setTokens(lineTokens);
        setProperties(newSampleDataSet, lineTokens);

        cleanUpNewSample(newSampleDataSet);
        cleanUpDataSetInformation(newSampleDataSet);
        return newSampleDataSet;
    }

    private void cleanUpNewSample(final SampleDataSetPair newSampleDataSet)
    {
        NewSample newSample = newSampleDataSet.getNewSample();
        newSample.setSampleType(sampleType);
        newSample
                .setContainerIdentifier(StringUtils.trimToNull(newSample.getContainerIdentifier()));

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
    }

    private void cleanUpDataSetInformation(final SampleDataSetPair newSampleDataSet)
    {
        DataSetInformation dataSetInformation = newSampleDataSet.getDataSetInformation();
        dataSetInformation.setDataSetType(dataSetType);
        dataSetInformation.setDataSetKind(dataSetKind);
    }
}
