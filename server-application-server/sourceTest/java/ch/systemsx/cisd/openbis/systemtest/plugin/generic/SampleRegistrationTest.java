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

package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IdentifierExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
@Test(groups = "system test")
public class SampleRegistrationTest extends GenericSystemTestCase
{
    private static final String CELL_PLATE = "CELL_PLATE";

    private static final String DILUTION_PLATE = "DILUTION_PLATE";

    private static final String WELL = "WELL";

    private static final String CISD = "/CISD/";

    @Test
    public void testSimpleRegistration()
    {
        logIntoCommonClientService();

        NewSample sample = new NewSample();
        String identifier = "/cisd/" + commonClientService.generateCode("S-", EntityKind.SAMPLE);
        sample.setIdentifier(identifier);
        SampleType sampleType = new SampleType();
        sampleType.setCode(CELL_PLATE);
        sample.setSampleType(sampleType);
        sample.setProperties(new IEntityProperty[]
        { property("COMMENT", "test samplé") });
        // tested:
        // - ignore case
        // - support for both code and identifiers (with and without db instance)
        // - dealing with the same parent stated more than once
        String[] parents = new String[]
        { "c1", "C2", "/CISD/C3" };
        sample.setParentsOrNull(parents);
        genericClientService.registerSample("session", sample);

        Sample s = getSpaceSample(identifier);
        assertEquals(CELL_PLATE, s.getSampleType().getCode());
        List<IEntityProperty> properties = s.getProperties();
        assertEquals("COMMENT", properties.get(0).getPropertyType().getCode());
        assertEquals(StringEscapeUtils.escapeHtml4("test samplé"), properties.get(0).getValue());
        assertEquals(1, properties.size());
        assertEquals(3, s.getParents().size());
        assertEquals("[/CISD/C1, /CISD/C2, /CISD/C3]",
                Arrays.toString(IdentifierExtractor.extract(s.getParents()).toArray()));
    }

    @Test
    // translated broken test from GenericSampleRegistrationTest
    public void testRegisterGroupSampleWithParent()
    {
        logIntoCommonClientService();

        final NewSample sample = new NewSample();
        final String sampleCode = "dp4";
        final String identifier = CISD + sampleCode;
        sample.setIdentifier(identifier);
        final String parent = CISD + "C1";
        sample.setParentsOrNull(new String[]
        { parent });
        final SampleType sampleType = new SampleType();
        sampleType.setCode(DILUTION_PLATE);
        sample.setSampleType(sampleType);
        genericClientService.registerSample("session", sample);

        Sample s = getSpaceSample(identifier);
        assertEquals(1, s.getParents().size());
        assertEquals(parent, IdentifierExtractor.extract(s.getParents()).get(0));
    }

    @Test
    // translated broken test from GenericSampleRegistrationTest
    public void testRegisterGroupSampleWithContainer()
    {
        logIntoCommonClientService();

        final NewSample sample = new NewSample();
        final String sampleCode = "W12";
        final String simpleIdentifier = CISD + sampleCode;
        final String containerCode = "3V-125";
        final String containerIdentifier = CISD + containerCode;
        sample.setIdentifier(simpleIdentifier);
        sample.setContainerIdentifier(containerIdentifier);
        final SampleType sampleType = new SampleType();
        sampleType.setCode(WELL);
        sample.setSampleType(sampleType);
        genericClientService.registerSample("session", sample);

        final String fullIdentifier = containerIdentifier + ":" + sampleCode;
        Sample s = getContainedSample(containerIdentifier, fullIdentifier);
        assertEquals(0, s.getParents().size());
        assertEquals(containerIdentifier, s.getContainer().getIdentifier());
        assertEquals(sampleCode, s.getSubCode());
        assertEquals(containerCode + ":" + sampleCode, s.getCode());
        assertEquals(fullIdentifier, s.getIdentifier());
    }

    private Sample getSample(String sampleIdentifier, ListSampleCriteria listCriteria)
    {
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        GridRowModels<Sample> list = samples.getResultSet().getList();
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            System.err.println("SAMPLE:"+sample.getIdentifier()+ " "+sampleIdentifier);
            if (sample.getIdentifier().endsWith(sampleIdentifier.toUpperCase()))
            {
                return sample;
            }
        }
        fail("No sample of type found for identifier " + sampleIdentifier);
        return null; // satisfy compiler
    }

    private Sample getSpaceSample(String sampleIdentifier)
    {
        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        return getSample(sampleIdentifier, listCriteria);
    }

    private Sample getContainedSample(String containerIdentifier, String sampleIdentifier)
    {
        final Sample container = getSpaceSample(containerIdentifier);
        ListSampleCriteria listCriteria =
                ListSampleCriteria.createForContainer(TechId.create(container));
        return getSample(sampleIdentifier, listCriteria);
    }

}
