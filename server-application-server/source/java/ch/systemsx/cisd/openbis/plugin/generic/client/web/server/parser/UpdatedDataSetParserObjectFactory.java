/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedDataSet;

/**
 * A {@link AbstractParserObjectFactory} extension for creating {@link NewDataSet}.
 * 
 * @author Izabela Adamczyk
 */
public final class UpdatedDataSetParserObjectFactory extends
        AbstractParserObjectFactory<NewDataSet>
{

    private final DataSetBatchUpdateDetails basicBatchUpdateDetails;

    public UpdatedDataSetParserObjectFactory(final IPropertyMapper propertyMapper)
    {
        super(NewDataSet.class, propertyMapper);
        this.basicBatchUpdateDetails = createBasicBatchUpdateDetails();
    }

    @Override
    public final NewDataSet createObject(final String[] lineTokens) throws ParserException
    {
        final NewDataSet newDataSet = super.createObject(lineTokens);
        setProperties(newDataSet, lineTokens);

        final DataSetBatchUpdateDetails updateDetails = createBatchUpdateDetails(newDataSet);
        cleanUp(newDataSet);

        return new UpdatedDataSet(newDataSet, updateDetails);
    }

    private final void setProperties(final NewDataSet newObject, final String[] lineTokens)
    {
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        for (final String unmatchedProperty : getUnmatchedProperties())
        {
            final IPropertyModel propertyModel = tryGetPropertyModel(unmatchedProperty);
            final String propertyDefault = tryGetPropertyDefault(unmatchedProperty);
            final String propertyValue =
                    getPropertyValue(lineTokens, propertyModel, propertyDefault);
            if (isNotEmpty(propertyValue))
            {
                final IEntityProperty property = new EntityProperty();
                property.setPropertyType(createPropertyType(unmatchedProperty));
                property.setValue(propertyValue);
                properties.add(property);
            }
        }
        newObject.setProperties(properties.toArray(IEntityProperty.EMPTY_ARRAY));
    }

    private final PropertyType createPropertyType(final String propertyTypeCode)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        return propertyType;
    }

    /**
     * Prepares details about which values should be updated in general taking into account only the information about availability of columns in the
     * file.
     */
    private DataSetBatchUpdateDetails createBasicBatchUpdateDetails()
    {
        DataSetBatchUpdateDetails details = new DataSetBatchUpdateDetails();
        details.setExperimentUpdateRequested(isColumnAvailable(NewDataSet.EXPERIMENT));
        details.setSampleUpdateRequested(isColumnAvailable(NewDataSet.SAMPLE));
        details.setParentsUpdateRequested(isColumnAvailable(NewDataSet.PARENTS));
        details.setContainerUpdateRequested(isColumnAvailable(NewDataSet.CONTAINER));
        details.setFileFormatUpdateRequested(isColumnAvailable(NewDataSet.FILE_FORMAT));
        return details;
    }

    /**
     * Returns details about which values should be updated for the specified data set. If a cell was left empty in the file the corresponding value
     * will not be modified.
     */
    private DataSetBatchUpdateDetails createBatchUpdateDetails(NewDataSet dataSet)
    {
        DataSetBatchUpdateDetails details = new DataSetBatchUpdateDetails();

        details.setExperimentUpdateRequested(basicBatchUpdateDetails.isExperimentUpdateRequested()
                && isNotEmpty(dataSet.getExperimentIdentifier()));

        details.setSampleUpdateRequested(basicBatchUpdateDetails.isSampleUpdateRequested()
                && isNotEmpty(dataSet.getSampleIdentifierOrNull()));

        details.setParentsUpdateRequested(basicBatchUpdateDetails.isParentsUpdateRequested()
                && isNotEmpty(dataSet.getParentsIdentifiersOrNull()));

        details.setContainerUpdateRequested(basicBatchUpdateDetails.isContainerUpdateRequested()
                && isNotEmpty(dataSet.getContainerIdentifierOrNull()));

        details.setFileFormatUpdateRequested(basicBatchUpdateDetails.isFileFormatUpdateRequested()
                && isNotEmpty(dataSet.getFileFormatOrNull()));

        final Set<String> propertiesToUpdate = new HashSet<String>();
        for (IEntityProperty property : dataSet.getProperties())
        {
            propertiesToUpdate.add(property.getPropertyType().getCode());
        }
        details.setPropertiesToUpdate(propertiesToUpdate);

        return details;
    }

    /** Cleans fields of the specified data set that are marked for deletion or are empty. */
    private void cleanUp(NewDataSet newDataSet)
    {
        if (isDeletionMark(newDataSet.getExperimentIdentifier()))
        {
            throw new ParserException(NewDataSet.EXPERIMENT
                    + " column is required and cannot be marked for deletetion.");
        } else if (isEmpty(newDataSet.getExperimentIdentifier()))
        {
            newDataSet.setExperimentIdentifier(null);
        }
        if (isDeletionMark(newDataSet.getSampleIdentifierOrNull())
                || isEmpty(newDataSet.getSampleIdentifierOrNull()))
        {
            newDataSet.setSampleIdentifierOrNull(null);
        }
        if (isDeletionMark(newDataSet.getParentsIdentifiersOrNull())
                || isEmpty(newDataSet.getParentsIdentifiersOrNull()))
        {
            newDataSet.setParentsIdentifiersOrNull((String) null);
        }
        if (isDeletionMark(newDataSet.getContainerIdentifierOrNull())
                || isEmpty(newDataSet.getContainerIdentifierOrNull()))
        {
            newDataSet.setContainerIdentifierOrNull(null);
        }
        if (isDeletionMark(newDataSet.getFileFormatOrNull())
                || isEmpty(newDataSet.getFileFormatOrNull()))
        {
            newDataSet.setFileFormatOrNull(null);
        }

        final List<IEntityProperty> updatedProperties = new ArrayList<IEntityProperty>();
        for (IEntityProperty property : newDataSet.getProperties())
        {
            if (isDeletionMark(property.getStringValue()) == false)
            {
                updatedProperties.add(property);
            }
        }
        newDataSet.setProperties(updatedProperties.toArray(IEntityProperty.EMPTY_ARRAY));
    }

    @Override
    protected final boolean ignoreUnmatchedProperties()
    {
        return true;
    }

}
