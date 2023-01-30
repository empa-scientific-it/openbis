/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.builder.ExternalDataBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class RegisterDataSetTest extends BaseTest
{
    private Sample sampleWithExperiment;

    private Sample spaceSampleWithoutExperiment;

    private Sample sharedSample;

    private Experiment experiment;

    @BeforeMethod
    void createFixture() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        experiment = create(anExperiment().inProject(project));

        sampleWithExperiment = create(aSample().inExperiment(experiment));
        spaceSampleWithoutExperiment = create(aSample().inSpace(space));
        sharedSample = create(aSample());
    }

    @Test
    public void testRegisterDataSetForAnExperiment() throws Exception
    {
        AbstractExternalData dataSet = create(aDataSet().inExperiment(experiment));

        assertThat(dataSet, is(inExperiment(experiment)));
    }

    @Test
    public void testRegisterContainerWithNonExistingDataSetForAnExperimentFails() throws Exception
    {
        try
        {
            create(aDataSet().asContainer().withComponents("DS-101")
                    .inExperiment(experiment));
            fail("UserFailureExcpetion expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown data set code 'DS-101'", ex.getMessage());
        }
    }

    @Test
    public void testRegisterContainerDataSetForAnExperiment() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().withType("UNKNOWN").inExperiment(experiment));
        AbstractExternalData component2 = create(aDataSet().withType("UNKNOWN").inExperiment(experiment));
        List<String> componentDSCodes = new ArrayList<String>();
        componentDSCodes.add(component1.getCode());
        componentDSCodes.add(component2.getCode());

        AbstractExternalData dataSet = create(aDataSet().asContainer().withComponents(component1.getCode(), component2.getCode())
                .inExperiment(experiment));

        assertThat(dataSet, is(inExperiment(experiment)));
        List<AbstractExternalData> components = dataSet.tryGetAsContainerDataSet().getContainedDataSets();
        List<String> codes = Code.extractCodes(components);

        assertEquals(componentDSCodes.toString(), codes.toString());
        assertEquals("UNKNOWN", components.get(0).getDataSetType().getCode());
        assertThat(components.get(0), is(inExperiment(experiment)));
        assertThat(components.get(1), is(inExperiment(experiment)));
        assertThat(components.size(), equalTo(2));
    }

    @Test
    public void testRegisterDataSetForADeletedExperiment() throws Exception
    {
        commonServer.deleteExperiments(systemSessionToken, Arrays.asList(new TechId(experiment)),
                "t", DeletionType.TRASH);
        createWithExpectedError(aDataSet().inExperiment(experiment),
                "Unknown experiment '" + experiment.getIdentifier() + "'.");
    }

    @Test
    public void testRegisterDataSetAsContainerForADeletedDataSetFails() throws Exception
    {
        AbstractExternalData componentDataSet = create(aDataSet().asContainer().inExperiment(experiment));
        assertThat(componentDataSet, is(inExperiment(experiment)));

        commonServer.deleteDataSets(systemSessionToken, Arrays.asList(componentDataSet.getCode()),
                "t", DeletionType.TRASH, true);

        try
        {
            create(aDataSet().asContainer().withComponents(componentDataSet.getCode()).inExperiment(experiment));
            fail("UserFailureExcpetion expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown data set code '" + componentDataSet.getCode() + "'", ex.getMessage());
        }
    }

    @Test
    public void testRegisterDataSetForASampleWithExperiment() throws Exception
    {
        AbstractExternalData dataSet = create(aDataSet().inSample(sampleWithExperiment));

        assertThat(dataSet, is(inSample(sampleWithExperiment)));
        assertThat(dataSet, is(inExperiment(experiment)));
    }

    @Test
    public void testRegisterContainerDataSetForASampleWithExperiment() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().withType("UNKNOWN").inSample(sampleWithExperiment));
        AbstractExternalData component2 = create(aDataSet().withType("UNKNOWN").inSample(sampleWithExperiment));
        List<String> componentDSCodes = new ArrayList<String>();
        componentDSCodes.add(component1.getCode());
        componentDSCodes.add(component2.getCode());

        AbstractExternalData dataSet = create(aDataSet().asContainer().withComponents(component1.getCode(), component2.getCode())
                .inSample(sampleWithExperiment));

        assertThat(dataSet, is(inSample(sampleWithExperiment)));
        assertThat(dataSet, is(inExperiment(experiment)));
        List<AbstractExternalData> components = dataSet.tryGetAsContainerDataSet().getContainedDataSets();
        List<String> codes = Code.extractCodes(components);
        assertEquals(componentDSCodes.toString(), codes.toString());
        assertEquals("UNKNOWN", components.get(0).getDataSetType().getCode());
        assertThat(components.get(0), is(inSample(sampleWithExperiment)));
        assertThat(components.get(0), is(inExperiment(experiment)));
        assertThat(components.get(1), is(inSample(sampleWithExperiment)));
        assertThat(components.get(1), is(inExperiment(experiment)));
        assertThat(components.size(), equalTo(2));
    }

    @Test
    public void testRegisterDataSetForADeletedSampleWithExperiment() throws Exception
    {
        commonServer.deleteSamples(systemSessionToken, Arrays.asList(new TechId(sampleWithExperiment)),
                "t", DeletionType.TRASH);
        createWithExpectedError(aDataSet().inSample(sampleWithExperiment),
                "No sample could be found with given identifier '" + sampleWithExperiment.getIdentifier() + "'.");
    }

    @Test
    public void testRegisterDataSetForASampleWithDeletedExperiment() throws Exception
    {
        commonServer.deleteExperiments(systemSessionToken, Arrays.asList(new TechId(experiment)),
                "t", DeletionType.TRASH);
        createWithExpectedError(aDataSet().inSample(sampleWithExperiment),
                "No sample could be found with given identifier '" + sampleWithExperiment.getIdentifier() + "'.");
    }

    @Test
    public void testRegisterDataSetForASampleWithoutExperiment() throws Exception
    {
        AbstractExternalData dataSet = create(aDataSet().withType("NO-EXP-TYPE").inSample(spaceSampleWithoutExperiment));

        assertThat(dataSet, is(inSample(spaceSampleWithoutExperiment)));
    }

    @Test
    public void testRegisterContainerDataSetAndItsComponentsForASampleWithoutExperiment() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().withType("NEXP-TYPE").inSample(spaceSampleWithoutExperiment));
        AbstractExternalData component2 = create(aDataSet().withType("NEXP-TYPE").inSample(spaceSampleWithoutExperiment));
        List<String> componentDSCodes = new ArrayList<String>();
        componentDSCodes.add(component1.getCode());
        componentDSCodes.add(component2.getCode());

        AbstractExternalData dataSet = create(aDataSet().withType("NEXP-CONTAINER-TYPE")
                .asContainer().withComponents(component1.getCode(), component2.getCode()).inSample(spaceSampleWithoutExperiment));

        assertThat(dataSet, is(inSample(spaceSampleWithoutExperiment)));
        List<AbstractExternalData> components = dataSet.tryGetAsContainerDataSet().getContainedDataSets();
        List<String> codes = Code.extractCodes(components);
        assertEquals(componentDSCodes.toString(), codes.toString());
        assertEquals("NEXP-TYPE", components.get(0).getDataSetType().getCode());
        assertThat(components.get(0), is(inSample(spaceSampleWithoutExperiment)));
        assertThat(components.get(1), is(inSample(spaceSampleWithoutExperiment)));
        assertThat(components.size(), equalTo(2));
    }

    @Test
    public void testRegisterContainerDataSetWithExistingComponentsForASampleWithoutExperiment() throws Exception
    {
        AbstractExternalData componentDataSet = create(aDataSet().inSample(sampleWithExperiment));
        AbstractExternalData dataSet = create(aDataSet().withType("NEXP-CONTAINER-TYPE")
                .asContainer().withComponent(componentDataSet).inSample(spaceSampleWithoutExperiment));

        assertThat(dataSet, is(inSample(spaceSampleWithoutExperiment)));
        List<AbstractExternalData> components = dataSet.tryGetAsContainerDataSet().getContainedDataSets();
        List<String> codes = Code.extractCodes(components);
        assertEquals("[" + componentDataSet.getCode() + "]", codes.toString());
        assertEquals(componentDataSet.getDataSetType().getCode(), components.get(0).getDataSetType().getCode());
        assertThat(components.get(0), is(inSample(sampleWithExperiment)));
        assertThat(components.get(0), is(inExperiment(experiment)));
        assertThat(components.size(), equalTo(1));
    }

    @Test
    public void testRegisterDataSetForASampleWithoutExperimentButWrongDataSetType() throws Exception
    {
        createWithExpectedError(aDataSet().inSample(spaceSampleWithoutExperiment),
                "Data set can not be registered because no experiment found for sample '"
                        + spaceSampleWithoutExperiment.getIdentifier() + "'.");
    }

    @Test
    public void testRegisterDataSetForASharedSample() throws Exception
    {
        createWithExpectedError(aDataSet().withType("NEXP-TYPE").inSample(sharedSample),
                "Data set can not be registered because sample '" + sharedSample.getIdentifier()
                        + "' is a shared sample.");
    }

    @Test
    public void testRegisterDataSetForSampleWithoutExperimentViaPerformEntityOperation() throws Exception
    {
        NewExternalData newDataSet = aDataSet().withType("NO-EXP-TYPE2")
                .inSample(spaceSampleWithoutExperiment).get();
        AtomicEntityOperationDetails operationDetails = new EntityOperationBuilder().dataSet(newDataSet).create();

        AtomicEntityOperationResult operationResult = etlService.performEntityOperations(systemSessionToken, operationDetails);

        assertThat((int) operationResult.getDataSetsCreatedCount(), equalTo(1));
        AbstractExternalData dataSet = etlService.tryGetDataSet(systemSessionToken, newDataSet.getCode());
        assertThat(dataSet, is(inSample(spaceSampleWithoutExperiment)));
    }

    private void createWithExpectedError(ExternalDataBuilder dataSetBuilder, String expectedErrorMessage)
    {
        try
        {
            create(dataSetBuilder);
            fail("UserFailureExcpetion expected");
        } catch (UserFailureException ex)
        {
            assertEquals(expectedErrorMessage, ex.getMessage());
        }
    }

}
