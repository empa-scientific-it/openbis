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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.ToStringComparator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SampleBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Headless system test counterpart to {@link SampleBrowserTest}. Not included tests:
 * <ul>
 * <li>testChangeColumnSettings()
 * <li>testExportMasterPlates()
 * <li>testExportCellPlates()
 * </ul>
 * 
 * @see SampleBrowserTest
 * @author Piotr Buczek
 */
@Test(groups = "system test")
public class SampleBrowsingTest extends GenericSystemTestCase
{
    private static final boolean DEBUG = false;

    private static final String DEFAULT_GROUP = "CISD";

    private static final String DEFAULT_PLATE_GEOMETRY_VALUE = "384_WELLS_16X24";

    @Test
    public void testListAllSamples()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getAllSampleType());

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(31, samples.getResultSet().getTotalLength());

        List<BasicEntityType> availableEntityTypes = new ArrayList<BasicEntityType>();
        availableEntityTypes.addAll(samples.getAvailableEntityTypes());
        Collections.sort(availableEntityTypes, new ToStringComparator());
        assertEquals(
                "[CELL_PLATE, CONTROL_LAYOUT, DILUTION_PLATE, DYNAMIC_PLATE, MASTER_PLATE, REINFECT_PLATE]",
                availableEntityTypes.toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        // Test that there are two samples displayed that have different types, and
        // have all properties even those that are assigned only to one of these types
        // (union of property values is displayed).

        // 'ORGANISM' is assigned only to 'CELL_PLATE' sample type
        Sample s1 = getSample(list, createSampleIdentifier("NEMO/CP-TEST-1"));
        assertEquals("CELL_PLATE", s1.getSampleType().getCode());
        assertEquals(5, s1.getProperties().size());
        checkUserProperty(s1.getProperties(), "ORGANISM", "HUMAN");

        // 'PLATE_GEOMETRY' is assigned only to 'CONTROL_LAYOUT' and 'MASTER PLATE' sample types
        Sample s2 = getSample(list, createSampleIdentifier("C1"));
        assertEquals("CONTROL_LAYOUT", s2.getSampleType().getCode());
        assertEquals(1, s2.getProperties().size());
        checkInternalProperty(s2.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);

        // test that 3 parents of a 'REINFECT_PLATE' are loaded
        Sample s3 = getSample(list, createSampleIdentifier("DEFAULT/RP1-A2X"));
        assertEquals("REINFECT_PLATE", s3.getSampleType().getCode());
        assertEquals("/CISD/DEFAULT/CP1-A2", s3.getGeneratedFrom().getIdentifier());
        assertEquals("/CISD/DP1-A", s3.getGeneratedFrom().getGeneratedFrom().getIdentifier());
        assertEquals("/CISD/MP1-MIXED", s3.getGeneratedFrom().getGeneratedFrom().getGeneratedFrom()
                .getIdentifier());
    }

    @Test
    public final void testListMasterPlates()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("MASTER_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(4, samples.getResultSet().getTotalLength());
        assertEquals("[MASTER_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample s2 = getSample(list, createSampleIdentifier("MP002-1"));
        checkInternalProperty(s2.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);
        assertNull(s2.getDeletion());
        assertNull(s2.getExperiment());
    }

    @Test
    public final void testListSharedMasterPlates()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeInstance(true);
        listCriteria.setSampleType(getSampleType("MASTER_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(1, samples.getResultSet().getTotalLength());
        assertEquals("[MASTER_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample s = getSample(list, createSharedSampleIdentifier("MP"));
        checkInternalProperty(s.getProperties(), "PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);
        assertNull(s.getDeletion());
        assertNull(s.getExperiment());
    }

    @Test
    public final void testListCellPlates()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("CELL_PLATE"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(12, samples.getResultSet().getTotalLength());
        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample s = getSample(list, createSampleIdentifier("NEMO/3VCP5"));
        // assertNotNull(s.getDeletion());
        assertEquals("/CISD/NEMO/EXP10", s.getExperiment().getIdentifier());
        assertEquals("/CISD/3V-125", s.getGeneratedFrom().getIdentifier());
        assertNull(s.getGeneratedFrom().getGeneratedFrom());
    }

    private void checkUserProperty(List<IEntityProperty> properties, String propertyCode,
            String propertyValue)
    {
        checkProperty(properties, false, propertyCode, propertyValue);
    }

    private void checkInternalProperty(List<IEntityProperty> properties, String propertyCode,
            String propertyValue)
    {
        checkProperty(properties, true, propertyCode, propertyValue);
    }

    private void checkProperty(List<IEntityProperty> properties, boolean managedInternally,
            String propertyCode, String propertyValue)
    {
        String fullPropertyCode = managedInternally ? "$" + propertyCode : propertyCode;
        for (IEntityProperty property : properties)
        {
            if (property.getPropertyType().getCode().equals(fullPropertyCode))
            {
                assertEquals(property.tryGetAsString(), propertyValue);
                return;
            }
        }
        fail("No property found with code " + fullPropertyCode);
    }

    private static String createSampleIdentifier(String sampleCode)
    {
        return createSampleIdentifier(DEFAULT_GROUP, sampleCode);
    }

    private static String createSharedSampleIdentifier(String sampleCode)
    {
        return createSampleIdentifier(null, sampleCode);
    }

    private static String createSampleIdentifier(String spaceCode, String sampleCode)
    {
        return "/" + (spaceCode == null ? "" : spaceCode + "/") + sampleCode;
    }

    private SampleType getAllSampleType()
    {
        SampleType result = new SampleType();
        result.setCode("(all)");
        return result;
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

    private static Sample getSample(GridRowModels<Sample> list, String identifier)
    {
        for (GridRowModel<Sample> gridRowModel : list)
        {
            Sample sample = gridRowModel.getOriginalObject();
            if (DEBUG)
            {
                System.out.println(sample.getIdentifier());
            }
            if (sample.getIdentifier().equals(identifier))
            {
                return sample;
            }
        }
        fail("No sample found for identifier " + identifier);
        return null; // satisfy compiler
    }

}
