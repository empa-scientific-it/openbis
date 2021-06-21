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

import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SampleBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewerTest;

/**
 * Headless system test counterpart to {@link GenericSampleViewerTest}. Not included:
 * <ul>
 * <li>loading of sample children
 * <li>loading of sample components
 * <li>detailed test of indirectly connected data sets
 * </ul>
 * 
 * @see SampleBrowserTest
 * @author Piotr Buczek
 */
@Test(groups = "system test")
public class SampleDetailsTest extends GenericSystemTestCase
{

    private static final String CISD_ID_PREFIX = "/CISD/";

    private static final String DEFAULT_SPACE = "CISD";

    private static final String PERMLINK_TEMPLATE =
            "http://localhost/openbis/index.html?viewMode=SIMPLE#entity=SAMPLE&permId=%s";

    private static final String CONTROL_LAYOUT_EXAMPLE = "CL1";

    private static final String CELL_PLATE_EXAMPLE = "3VCP5";

    private static final String CELL_PLATE_EXAMPLE_EXPERIMENT_ID = "/CISD/NEMO/EXP10";

    private static final String CELL_PLATE_WITH_DATA_EXAMPLE = "CP-TEST-2";

    private static final String CELL_PLATE_WITH_DATA_EXAMPLE_ID = CISD_ID_PREFIX
            + CELL_PLATE_WITH_DATA_EXAMPLE;

    private static final String CELL_PLATE_WITH_DATA_EXAMPLE_EXPERIMENT_ID = "/CISD/NOE/EXP-TEST-2";

    private static final String CONTROL_LAYOUT_EXAMPLE_PERM_ID = "200811050919915-8";

    private static final String CELL_PLATE_EXAMPLE_PERM_ID = "200811050946559-979";

    private static final String CONTROL_LAYOUT_EXAMPLE_PERMLINK = StringEscapeUtils
            .escapeHtml4(String.format(PERMLINK_TEMPLATE, CONTROL_LAYOUT_EXAMPLE_PERM_ID));

    private static final String CELL_PLATE_EXAMPLE_PERMLINK = StringEscapeUtils.escapeHtml4(String
            .format(PERMLINK_TEMPLATE, CELL_PLATE_EXAMPLE_PERM_ID));

    private static final String DIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092159222-2";

    @Test
    public void testGetMasterPlateDetails()
    {
        logIntoCommonClientService();

        ListSampleCriteria listCriteria = new ListSampleCriteria();
        listCriteria.setIncludeSpace(true);
        listCriteria.setSpaceCode("CISD");
        listCriteria.setSampleType(getSampleType("CONTROL_LAYOUT"));

        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));
        assertEquals(6, samples.getResultSet().getTotalLength());
        assertEquals("[CONTROL_LAYOUT]", samples.getAvailableEntityTypes().toString());

        GridRowModels<Sample> list = samples.getResultSet().getList();

        Sample sListed = getSample(list, createSampleIdentifier(CONTROL_LAYOUT_EXAMPLE));
        Sample sDetails =
                genericClientService.getSampleGenerationInfo(TechId.create(sListed)).getParent();
        assertEquals(sListed.getIdentifier(), sDetails.getIdentifier());
        assertEquals(sListed.getPermId(), sDetails.getPermId());
        assertEquals(CONTROL_LAYOUT_EXAMPLE_PERM_ID, sDetails.getPermId());
        assertEquals(sListed.getPermlink(), sDetails.getPermlink());
        assertEquals(CONTROL_LAYOUT_EXAMPLE_PERMLINK, sDetails.getPermlink());
        assertEquals(sListed.getSampleType(), sDetails.getSampleType());
        assertEquals("CONTROL_LAYOUT", sDetails.getSampleType().getCode());
        assertEquals(sListed.getRegistrator().getUserId(), sDetails.getRegistrator().getUserId());
        assertEquals("test", sDetails.getRegistrator().getUserId());
        assertEquals(sListed.getProperties().size(), sDetails.getProperties().size());
        checkInternalProperty(sDetails.getProperties(), "PLATE_GEOMETRY", "384_WELLS_16X24");
        checkUserProperty(sDetails.getProperties(), "DESCRIPTION", "test control layout");
    }

    @Test
    public void testGetCellPlateDetails()
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

        Sample sListed = getSample(list, createSampleIdentifier("NEMO/" + CELL_PLATE_EXAMPLE));
        Sample sDetails =
                genericClientService.getSampleGenerationInfo(TechId.create(sListed)).getParent();
        assertEquals(sListed.getIdentifier(), sDetails.getIdentifier());
        assertEquals(sListed.getPermId(), sDetails.getPermId());
        assertEquals(CELL_PLATE_EXAMPLE_PERM_ID, sDetails.getPermId());
        assertEquals(sListed.getPermlink(), sDetails.getPermlink());
        assertEquals(CELL_PLATE_EXAMPLE_PERMLINK, sDetails.getPermlink());
        assertEquals(sListed.getSampleType(), sDetails.getSampleType());
        assertEquals("CELL_PLATE", sDetails.getSampleType().getCode());
        assertEquals(sListed.getRegistrator().getUserId(), sDetails.getRegistrator().getUserId());
        assertEquals("test", sDetails.getRegistrator().getUserId());

        assertEquals(sListed.getExperiment().getIdentifier(), sDetails.getExperiment()
                .getIdentifier());
        assertEquals(CELL_PLATE_EXAMPLE_EXPERIMENT_ID, sDetails.getExperiment().getIdentifier());

        assertEquals(1, sDetails.getParents().size());
        final Sample parent = sDetails.getParents().iterator().next();
        assertEquals("3V-125", parent.getCode());

        assertEquals(sListed.getProperties().size(), sDetails.getProperties().size());

    }

    @Test
    public void testGetSampleDataSets()
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

        Sample sample = getSample(list, createSampleIdentifier("NOE/" + CELL_PLATE_WITH_DATA_EXAMPLE));

        // directly connected
        boolean showOnlyDirectlyConnected = true;

        final TypedTableResultSet<AbstractExternalData> directlyConnectedResults =
                commonClientService.listSampleDataSets(TechId.create(sample),
                        DefaultResultSetConfig
                                .<String, TableModelRowWithObject<AbstractExternalData>> createFetchAll(),
                        showOnlyDirectlyConnected);

        assertEquals(1, directlyConnectedResults.getResultSet().getTotalLength());
        final PhysicalDataSet directlyConnectedDataSet =
                getDataSet(directlyConnectedResults.getResultSet().getList(),
                        DIRECTLY_CONNECTED_DATA_SET_CODE).tryGetAsDataSet();
        DataSetExpectations.checkThat(directlyConnectedDataSet)
                .hasCode(DIRECTLY_CONNECTED_DATA_SET_CODE)
                .hasSampleWithIdentifier("/CISD/NOE/CP-TEST-2")
                .hasExperimentWithIdentifier(CELL_PLATE_WITH_DATA_EXAMPLE_EXPERIMENT_ID)
                .hasFileFormatType("3VPROPRIETARY").hasLocation("a/2");

        // indirectly connected
        showOnlyDirectlyConnected = false;
        TypedTableResultSet<AbstractExternalData> indirectlyConnectedResults =
                commonClientService.listSampleDataSets(TechId.create(sample),
                        DefaultResultSetConfig
                                .<String, TableModelRowWithObject<AbstractExternalData>> createFetchAll(),
                        showOnlyDirectlyConnected);

        assertEquals(6, indirectlyConnectedResults.getResultSet().getTotalLength());

        // the directly connected data set should still be retrieved
        PhysicalDataSet directlyConnectedDataSet2 =
                getDataSet(directlyConnectedResults.getResultSet().getList(),
                        DIRECTLY_CONNECTED_DATA_SET_CODE).tryGetAsDataSet();
        DataSetExpectations
                .checkThat(directlyConnectedDataSet2)
                .hasCode(directlyConnectedDataSet.getCode())
                .hasSampleWithIdentifier(directlyConnectedDataSet.getSample().getIdentifier())
                .hasExperimentWithIdentifier(
                        directlyConnectedDataSet.getExperiment().getIdentifier())
                .hasFileFormatType(directlyConnectedDataSet.getFileFormatType().getCode())
                .hasLocation(directlyConnectedDataSet.getDataSetLocation());

        final String indirectlyConnectedDataSetCode1 = "20081105092159111-1";
        PhysicalDataSet indirectlyConnectedDataSetThroughChildSample =
                getDataSet(indirectlyConnectedResults.getResultSet().getList(),
                        indirectlyConnectedDataSetCode1).tryGetAsDataSet();
        DataSetExpectations.checkThat(indirectlyConnectedDataSetThroughChildSample)
                .hasCode(indirectlyConnectedDataSetCode1)
                .hasSampleWithIdentifier(CISD_ID_PREFIX + "NEMO/CP-TEST-1")
                .hasExperimentWithIdentifier("/CISD/NEMO/EXP-TEST-1").hasFileFormatType("TIFF")
                .hasLocation("a/1");

        final String indirectlyConnectedDataSetCode2 = "20081105092259000-9";
        PhysicalDataSet indirectlyConnectedDataSetThroughChildDataSet =
                getDataSet(indirectlyConnectedResults.getResultSet().getList(),
                        indirectlyConnectedDataSetCode2).tryGetAsDataSet();
        DataSetExpectations.checkThat(indirectlyConnectedDataSetThroughChildDataSet)
                .hasCode(indirectlyConnectedDataSetCode2).hasNoSample()
                .hasExperimentWithIdentifier("/CISD/DEFAULT/EXP-REUSE").hasFileFormatType("XML")
                .hasLocation("xml/result-9");
    }

    private static class DataSetExpectations
    {
        public static DataSetExpectations checkThat(PhysicalDataSet dataSet)
        {
            return new DataSetExpectations(dataSet);
        }

        private PhysicalDataSet dataSet;

        private DataSetExpectations(PhysicalDataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public DataSetExpectations hasCode(String expectedCode)
        {
            assertEquals(expectedCode, dataSet.getCode());
            return this;
        }

        public DataSetExpectations hasSampleWithIdentifier(String expectedSampleIdentifier)
        {
            assertEquals(expectedSampleIdentifier, dataSet.getSample().getIdentifier());
            return this;
        }

        public DataSetExpectations hasNoSample()
        {
            assertEquals(null, dataSet.getSample());
            return this;
        }

        public DataSetExpectations hasExperimentWithIdentifier(String expectedExperimentIdentifier)
        {
            assertEquals(expectedExperimentIdentifier, dataSet.getExperiment().getIdentifier());
            return this;
        }

        public DataSetExpectations hasFileFormatType(String expectedFileFormatTypeCode)
        {
            assertEquals(expectedFileFormatTypeCode, dataSet.getFileFormatType().getCode());
            return this;
        }

        public DataSetExpectations hasLocation(String expectedLocation)
        {
            assertEquals(expectedLocation, dataSet.getLocation());
            return this;
        }
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
        return createSampleIdentifier(DEFAULT_SPACE, sampleCode);
    }

    private static String createSampleIdentifier(String spaceCode, String sampleCode)
    {
        return "/" + (spaceCode == null ? "" : spaceCode + "/") + sampleCode;
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

    private static final boolean DEBUG = false;

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

    private static AbstractExternalData getDataSet(
            GridRowModels<TableModelRowWithObject<AbstractExternalData>> list, String identifier)
    {
        for (GridRowModel<TableModelRowWithObject<AbstractExternalData>> gridRowModel : list)
        {
            AbstractExternalData externalData = gridRowModel.getOriginalObject().getObjectOrNull();
            if (DEBUG)
            {
                System.out.println(externalData.getIdentifier());
            }
            if (externalData.getIdentifier().equals(identifier))
            {
                return externalData;
            }
        }
        fail("No data set found for identifier " + identifier);
        return null; // satisfy compiler
    }

}
