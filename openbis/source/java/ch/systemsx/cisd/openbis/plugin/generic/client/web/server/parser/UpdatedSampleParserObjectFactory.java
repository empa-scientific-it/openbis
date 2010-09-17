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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;

/**
 * A {@link NewSampleParserObjectFactory} extension for creating {@link NewSample} for batch update.
 * 
 * @author Piotr Buczek
 */
final class UpdatedSampleParserObjectFactory extends NewSampleParserObjectFactory
{
    private final SampleBatchUpdateDetails basicBatchUpdateDetails;

    UpdatedSampleParserObjectFactory(final SampleType sampleType,
            final IPropertyMapper propertyMapper, boolean identifierExpectedInFile,
            boolean allowExperiments)
    {
        super(sampleType, propertyMapper, identifierExpectedInFile, allowExperiments);
        this.basicBatchUpdateDetails = createBasicBatchUpdateDetails();
    }

    /**
     * Prepares details about which values should be updated in general taking into account only the
     * information about availability of columns in the file.
     */
    private SampleBatchUpdateDetails createBasicBatchUpdateDetails()
    {
        boolean updateExperiment = isColumnAvailable(UpdatedSample.EXPERIMENT);
        boolean updateParent = isColumnAvailable(UpdatedSample.PARENT);
        boolean updateContainer = isColumnAvailable(UpdatedSample.CONTAINER);
        return new SampleBatchUpdateDetails(updateExperiment, updateParent, updateContainer,
                getUnmatchedProperties());
    }

    //
    // AbstractParserObjectFactory
    //

    @Override
    public NewSample createObject(final String[] lineTokens) throws ParserException
    {
        final NewSample newSample = super.createObject(lineTokens);
        final SampleBatchUpdateDetails updateDetails = createBatchUpdateDetails(newSample);
        cleanUp(newSample);
        return new UpdatedSample(newSample, updateDetails);
    }

    //

    /**
     * Returns details about which values should be updated for the specified sample. If a cell was
     * left empty in the file the corresponding value will not be modified.
     */
    private SampleBatchUpdateDetails createBatchUpdateDetails(NewSample newSample)
    {
        final boolean updateExperiment =
                basicBatchUpdateDetails.isExperimentUpdateRequested()
                        && isNotEmpty(newSample.getExperimentIdentifier());
        final boolean updateParent =
                basicBatchUpdateDetails.isParentUpdateRequested()
                        && isNotEmpty(newSample.getParentIdentifier());
        final boolean updateContainer =
                basicBatchUpdateDetails.isContainerUpdateRequested()
                        && isNotEmpty(newSample.getContainerIdentifier());

        final Set<String> propertiesToUpdate = new HashSet<String>();
        for (IEntityProperty property : newSample.getProperties())
        {
            propertiesToUpdate.add(property.getPropertyType().getCode());
        }

        return new SampleBatchUpdateDetails(updateExperiment, updateParent, updateContainer,
                propertiesToUpdate);
    }

    /** Cleans properties and connections of the specified sample that are marked for deletion. */
    private void cleanUp(NewSample newSample)
    {
        if (isDeleteMark(newSample.getExperimentIdentifier()))
        {
            newSample.setExperimentIdentifier(null);
        }
        if (isDeleteMark(newSample.getParentIdentifier()))
        {
            newSample.setParentIdentifier(null);
        }
        if (isDeleteMark(newSample.getContainerIdentifier()))
        {
            newSample.setContainerIdentifier(null);
        }
        final List<IEntityProperty> updatedProperties = new ArrayList<IEntityProperty>();
        for (IEntityProperty property : newSample.getProperties())
        {
            if (isDeleteMark(property.getValue()) == false)
            {
                updatedProperties.add(property);
            }
        }
        newSample.setProperties(updatedProperties.toArray(new IEntityProperty[0]));
    }

    private static final String DELETE = "--DELETE--";

    private static boolean isNotEmpty(String value)
    {
        return StringUtils.isBlank(value) == false;
    }

    private static boolean isDeleteMark(String value)
    {
        return DELETE.equals(value);
    }

}
