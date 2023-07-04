/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Tests that dynamic properties are evaluated after certain save/update operations.
 * 
 * @author Piotr Buczek
 */
@Test(groups = "system test")
// NOTE: Standard propagation is overriden to provide more control over transaction handling.
// Transactions are not rolled back automatically after every method because there are dependencies
// between methods. DB cleanup is done after all methods are tested (see cleanup()).
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class DynamicPropertiesEvaluationTest extends GenericSystemTestCase
{
    private static final String CELL_PLATE = "CELL_PLATE";

    private static final String DESCRIPTION = "DESCRIPTION";

    private static final String NEW_SAMPLE_CODE = "NEW_SAMPLE_CODE";

    private static final String NEW_SAMPLE_IDENTIFIER = "/CISD/" + NEW_SAMPLE_CODE;

    private static long SLEEP_TIME = 1000; // 1s

    private static long MAX_RETRIES = 5;

    private TechId createdSampleId = null;

    private NewETPTAssignment createDynamicPropertyAssignment(final EntityKind entityKind,
            final String propertyTypeCode, String entityTypeCode, String script,
            Date modificationDate)
    {
        final boolean mandatory = false;
        final String defaultValue = null;
        final String section = null;
        final Long ordinal = 0L;
        final boolean dynamic = true;
        return new NewETPTAssignment(entityKind, propertyTypeCode, entityTypeCode, mandatory,
                defaultValue, section, ordinal, dynamic, false, modificationDate, script, false,
                false);
    }

    @BeforeMethod
    public void setUp()
    {
        logIntoCommonClientService();

        final EntityKind entityKind = EntityKind.SAMPLE;
        final String propertyTypeCode = DESCRIPTION;
        final String entityTypeCode = CELL_PLATE;
        final String script = "code_date";
        NewETPTAssignment assignment =
                createDynamicPropertyAssignment(entityKind, propertyTypeCode, entityTypeCode,
                        script, null);
        commonClientService.assignPropertyType(assignment);
    }

    @Test
    public void testRegisterDynamicPropertyAssignment()
    {
        final IDelegatedAction assertAction = new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    ListSampleCriteria listCriteria = new ListSampleCriteria();
                    listCriteria.setIncludeSpace(true);
                    listCriteria.setSpaceCode("CISD");
                    listCriteria.setSampleType(getSampleType(CELL_PLATE));

                    ResultSetWithEntityTypes<Sample> samples =
                            commonClientService.listSamples(new ListSampleDisplayCriteria(
                                    listCriteria));
                    assertTrue(samples.getResultSet().getTotalLength() > 0);
                    assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

                    GridRowModels<Sample> list = samples.getResultSet().getList();
                    for (GridRowModel<Sample> gridRowModel : list)
                    {
                        Sample sample = gridRowModel.getOriginalObject();
                        boolean found = false;
                        for (IEntityProperty property : sample.getProperties())
                        {
                            if (property.getPropertyType().getCode().equals(DESCRIPTION))
                            {
                                assertEquals(sample.getCode(),
                                        property.getStringValue().substring(0, sample.getCode().length()));
                                found = true;
                                break;
                            }
                        }
                        assertTrue(
                                "property " + DESCRIPTION + " not found for sample "
                                        + sample.getCode(), found);
                    }
                }
            };

        // properties should be evaluated asynchronously - check values after a few seconds
        check(SLEEP_TIME, MAX_RETRIES, "testRegisterDynamicPropertyAssignment", assertAction);
    }

    @Test(dependsOnMethods = "testRegisterDynamicPropertyAssignment")
    public void testCreateSampleWithDynamicProperty()
    {
        registerNewCellPlateSample();

        final IDelegatedAction assertAction = new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    Sample loadedSample = getSpaceSample(NEW_SAMPLE_IDENTIFIER);
                    createdSampleId = TechId.create(loadedSample);
                    boolean found = false;
                    for (IEntityProperty property : loadedSample.getProperties())
                    {
                        if (property.getPropertyType().getCode().equals(DESCRIPTION))
                        {
                            assertEquals(NEW_SAMPLE_CODE, loadedSample.getCode());
                            assertEquals(NEW_SAMPLE_CODE,
                                    property.getStringValue().substring(0, NEW_SAMPLE_CODE.length()));

                            found = true;
                            break;
                        }
                    }
                    assertTrue(
                            "property " + DESCRIPTION + " not found for sample "
                                    + loadedSample.getCode(), found);
                }
            };

        // properties should be evaluated asynchronously - check values after a few seconds
        check(SLEEP_TIME, MAX_RETRIES, "testCreateSampleWithDynamicProperty", assertAction);
    }

    void registerNewCellPlateSample()
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(NEW_SAMPLE_IDENTIFIER);
        final SampleType sampleType = new SampleType();
        sampleType.setCode(CELL_PLATE);
        newSample.setSampleType(sampleType);
        genericClientService.registerSample("session", newSample);
    }

    @Test(dependsOnMethods = "testCreateSampleWithDynamicProperty", groups = "broken")
    public void testUpdateDynamicPropertyAssignment()
    {
        final EntityKind entityKind = EntityKind.SAMPLE;
        final String propertyTypeCode = DESCRIPTION;
        final String entityTypeCode = CELL_PLATE;
        final String script = "date"; // different script
        Date modificationDate =
                getETPT(entityKind, propertyTypeCode, entityTypeCode).getModificationDate();
        NewETPTAssignment assignmentUpdates =
                createDynamicPropertyAssignment(entityKind, propertyTypeCode, entityTypeCode,
                        script, modificationDate);

        final Date dateBefore = new Date();
        commonClientService.updatePropertyTypeAssignment(assignmentUpdates);

        final IDelegatedAction assertAction = new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    final Date dateAfter = new Date();

                    ListSampleCriteria listCriteria = new ListSampleCriteria();
                    listCriteria.setIncludeSpace(true);
                    listCriteria.setSpaceCode("CISD");
                    listCriteria.setSampleType(getSampleType(CELL_PLATE));

                    ResultSetWithEntityTypes<Sample> samples =
                            commonClientService.listSamples(new ListSampleDisplayCriteria(
                                    listCriteria));
                    assertTrue(samples.getResultSet().getTotalLength() > 0);
                    assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

                    GridRowModels<Sample> list = samples.getResultSet().getList();
                    for (GridRowModel<Sample> gridRowModel : list)
                    {
                        Sample sample = gridRowModel.getOriginalObject();
                        boolean found = false;
                        for (IEntityProperty property : sample.getProperties())
                        {
                            if (property.getPropertyType().getCode().equals(propertyTypeCode))
                            {
                                assertTrue(dateBefore.getTime() < Long.parseLong(property
                                        .getStringValue()));
                                assertTrue(dateAfter.getTime() > Long
                                        .parseLong(property.getStringValue()));
                                found = true;
                                break;
                            }
                        }
                        assertTrue("property " + propertyTypeCode + " not found for sample "
                                + sample.getCode(), found);
                    }
                }
            };

        // properties should be evaluated asynchronously - check values after a few seconds
        check(SLEEP_TIME * 2, MAX_RETRIES, "testUpdateDynamicPropertyAssignment", assertAction);
    }

    @Test(dependsOnMethods = "testUpdateDynamicPropertyAssignment", groups = "broken")
    public void testUpdateSampleWithDynamicProperty()
    {
        testCreateSampleWithDynamicProperty();
        Sample oldSample = getSpaceSample(NEW_SAMPLE_IDENTIFIER);

        @SuppressWarnings("unchecked")
        SampleUpdates updates =
                new SampleUpdates("session", TechId.create(oldSample), Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST, null, null, oldSample.getVersion(),
                        oldSample.getIdentifier(), null, null);

        final Date dateBefore = new Date();
        genericClientService.updateSample(updates);

        final IDelegatedAction assertAction = new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    sleep(SLEEP_TIME);
                    final Date dateAfter = new Date();

                    Sample loadedSample = getSpaceSample(NEW_SAMPLE_IDENTIFIER);
                    createdSampleId = TechId.create(loadedSample);
                    boolean found = false;
                    try
                    {
                        for (IEntityProperty property : loadedSample.getProperties())
                        {
                            if (property.getPropertyType().getCode().equals(DESCRIPTION))
                            {
                                String[] dynPropertyValue = property.getStringValue().split("\\s+");

                                assertEquals(NEW_SAMPLE_CODE, dynPropertyValue[0]);

                                long dynPropTime = Long.parseLong(dynPropertyValue[1]);

                                assertTrue(dateBefore.getTime() < dynPropTime);
                                assertTrue(dateAfter.getTime() > dynPropTime);
                                found = true;
                                break;
                            }
                        }
                    } catch (NumberFormatException ex)
                    {
                        fail(ex.getMessage());
                    }
                    assertTrue(
                            "property " + DESCRIPTION + " not found for sample "
                                    + loadedSample.getCode(), found);
                }
            };

        // properties should be evaluated asynchronously - check values after a few seconds
        check(SLEEP_TIME * 2, MAX_RETRIES, "testUpdateSampleWithDynamicProperty", assertAction);
    }

    private SampleType getSampleType(String sampleTypeCode)
    {
        List<SampleType> sampleTypes = commonClientService.listSampleTypes();
        for (SampleType sampleType : sampleTypes)
        {
            if (sampleType.getCode().equals(sampleTypeCode))
            {
                return sampleType;
            }
        }
        fail("No sample type found with code " + sampleTypeCode);
        return null; // satisfy compiler
    }

    private Sample getSample(String sampleIdentifier, ListSampleCriteria listCriteria)
    {
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        GridRowModels<Sample> list = samples.getResultSet().getList();
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
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

    /**
     * Tries to perform given <var>assertAction</var> with specified number of <var>retries</var> waiting for specified time (in ms) before each
     * attempt. Only {@link AssertionError}s are handled by the method.
     */
    private void check(long timeToWait, long retries, String testName, IDelegatedAction assertAction)
    {
        for (int i = 0; i < retries; i++)
        {
            sleep(timeToWait);
            try
            {
                assertAction.execute();
            } catch (AssertionError ex)
            {
                System.err.println(String.format("%s: %d retry, assertion failed: \n\t %s",
                        testName, i + 1, ex.getMessage()));
                continue; // exception thrown - retry
            }
            return; // exception not thrown - success
        }
        assertAction.execute();
    }

    @AfterMethod
    public void cleanup()
    {
        commonClientService.unassignPropertyType(EntityKind.SAMPLE, DESCRIPTION, CELL_PLATE);
        if (createdSampleId != null)
        {
            commonClientService.deleteSample(createdSampleId, "test cleanup",
                    DeletionType.PERMANENT);
        }
    }
}
