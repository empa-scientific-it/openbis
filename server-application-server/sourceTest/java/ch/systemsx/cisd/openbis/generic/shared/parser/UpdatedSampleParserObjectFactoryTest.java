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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.parser.DefaultPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;

/**
 * Test cases for corresponding {@link UpdatedSampleParserObjectFactory} class.
 * 
 * @author Piotr Buczek
 */
public final class UpdatedSampleParserObjectFactoryTest
{

    private final static UpdatedSampleParserObjectFactory createUpdatedSampleParserObjectFactory(
            IPropertyMapper propertyMapper, boolean expectIdentifierInFile)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode("SAMPLE_TYPE");
        final UpdatedSampleParserObjectFactory parserObjectFactory =
                new UpdatedSampleParserObjectFactory(sampleType, propertyMapper,
                        expectIdentifierInFile, true);
        return parserObjectFactory;
    }

    private final static String PROPERTY_1 = "prop1";

    private final static String PROPERTY_2 = "prop2";

    private final static IPropertyMapper createPropertyMapper()
    {
        final String[] properties = new String[]
        { Identifier.IDENTIFIER_COLUMN, NewSample.CONTAINER, PROPERTY_1, PROPERTY_2 };
        final DefaultPropertyMapper propertyMapper = new DefaultPropertyMapper(properties, null);
        return propertyMapper;
    }

    @DataProvider
    private final static Object[][] getLineTokens()
    {
        return new Object[][]
        {
                { new String[]
                { "", "", "", "" }, 0, 0 },
                { new String[]
                { "id1", "cont1", "1", "hello" }, 2, 2 },
                { new String[]
                { "id1", "", "1", "" }, 1, 1 },
                { new String[]
                { "", "cont1", "", "hello" }, 1, 1 },
                { new String[]
                { "id1", "cont1", "--DELETE--", "__DELETE__" }, 0, 2 },
                { new String[]
                { "__DELETE__", "--DELETE--", "--DELETE--", "hello" }, 1, 2 } };
    }

    @Test(dataProvider = "getLineTokens")
    public final void testCreateObject(final String[] lineTokens, final int numberOfProperties,
            final int numOfPropertiestoUpdate)
    {
        final String identifierToken = lineTokens[0].toUpperCase();
        final String containerToken = lineTokens[1].toUpperCase();
        final String p1Token = lineTokens[2];
        final String p2Token = lineTokens[3];
        final UpdatedSampleParserObjectFactory parserObjectFactory =
                createUpdatedSampleParserObjectFactory(createPropertyMapper(), true);
        final UpdatedSample objectCreated =
                (UpdatedSample) parserObjectFactory.createObject(lineTokens);
        // assert that all NewSample properties are set properly
        assertEquals(identifierToken, objectCreated.getIdentifier());
        assertEquals(StringUtils.isEmpty(containerToken) || "--DELETE--".equals(containerToken)
                || "__DELETE__".equals(containerToken) ? null : containerToken,
                objectCreated.getContainerIdentifier());
        final IEntityProperty[] properties = objectCreated.getProperties();
        assertEquals(numberOfProperties, properties.length);
        int index = 2;
        for (final IEntityProperty sampleProperty : properties)
        {
            sampleProperty.getValue().equals(lineTokens[index++]);
        }
        // assert that SampleBatchUpdateDetails contains proper data
        final SampleBatchUpdateDetails batchUpdateDetails = objectCreated.getBatchUpdateDetails();
        final Set<String> propertiesToUpdate = batchUpdateDetails.getPropertiesToUpdate();
        assertEquals(numOfPropertiestoUpdate, propertiesToUpdate.size());
        assertEquals(StringUtils.isBlank(p1Token) == false, propertiesToUpdate.contains(PROPERTY_1));
        assertEquals(StringUtils.isBlank(p2Token) == false, propertiesToUpdate.contains(PROPERTY_2));
        assertEquals(StringUtils.isBlank(containerToken) == false,
                batchUpdateDetails.isContainerUpdateRequested());
        assertFalse(batchUpdateDetails.isParentsUpdateRequested());
        assertFalse(batchUpdateDetails.isExperimentUpdateRequested());
    }
}
