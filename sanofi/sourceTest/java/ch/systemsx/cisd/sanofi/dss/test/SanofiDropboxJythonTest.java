/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.sanofi.dss.test;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.eodsql.MockDataSet;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * <pre>
 * Things not tested
 * - skip well creation when plate library already exists
 * - skip material creation for preexisting materials
 * - error cases
 * </pre>
 * 
 * @author Kaloyan Enimanev
 */
public class SanofiDropboxJythonTest extends AbstractJythonDataSetHandlerTest
{
    private static final String PLATE_CODE = "plateCode";

    private static final String LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE";

    private static final String EXPERIMENT_RECIPIENTS_PROPNAME = "EMAIL_RECIPIENTS";

    private static final String[] EXPERIMENT_RECIPIENTS = new String[]
        { "admin@sanofi.com", "mickey@mouse.org" };

    private static final String MATERIAL_TYPE = "COMPOUND";
    
    private static final String POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL";

    private static final String NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL";

    private static final String COMPOUND_WELL_TYPE = "COMPOUND_WELL";

    private static final String COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M";

    private static final String COMPOUND_WELL_MATERIAL_PROPNAME = "COMPOUND";

    private static final String DATASET_DIR_NAME = "batchNr_plateCode.variant_2011.06.28";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("HCS_IMAGE_RAW");

    private static final String EXPERIMENT_IDENTIFIER = "/SANOFI/PROJECT/EXP";
    private static final String PLATE_IDENTIFIER = "/SANOFI/TEST-PLATE";

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();
    }

    @Test
    public void testHappyCaseWithLibraryCreation() throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties = createThreadPropertiesRelativeToScriptsFolder("sanofi-dropbox.py");
        createHandler(properties, false, true);
        createData();

        final String libraryTemplate = "1.45\t\tH\n0.12\t0.002\tL";
        final Sample plate = createPlate(libraryTemplate, "6_WELLS_2X3");
        setUpPlateSearchExpectations(plate);
        setUpLibraryTemplateExpectations(plate);

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));
        queryResult.add(createQueryResult("B1"));
        queryResult.add(createQueryResult("B2"));

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        final RecordingMatcher<ListMaterialCriteria> materialCriteria =
                new RecordingMatcher<ListMaterialCriteria>();
        final RecordingMatcher<String> email = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)),
                            with(anything()));
                    will(returnValue(queryResult));

                    one(openBisService).listMaterials(with(materialCriteria), with(equal(true)));
                    will(returnValue(Collections.emptyList()));
                    
                    exactly(5).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), DATASET_DIR_NAME));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(openBisService).getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(EXPERIMENT_RECIPIENTS)));
                }
            });

        handler.handle(markerFile);

        assertEquals(MATERIAL_TYPE, materialCriteria.recordedObject().tryGetMaterialType()
                .getCode());
        assertEquals(true, queryResult.hasCloseBeenInvoked());

        List<NewSample> registeredSamples =
                atomicatOperationDetails.recordedObject().getSampleRegistrations();

        assertEquals(5, registeredSamples.size());
        assertAllSamplesHaveContainer(registeredSamples, plate.getIdentifier());
        assertCompoundWell(registeredSamples, "A1", "1.45");
        assertPositiveControl(registeredSamples, "A3");
        assertCompoundWell(registeredSamples, "B1", "0.12");
        assertCompoundWell(registeredSamples, "B2", "0.002");
        assertNegativeControl(registeredSamples, "B3");

        List<? extends NewExternalData> dataSetsRegistered =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations();
        assertEquals(1, dataSetsRegistered.size());

        NewExternalData dataSet = dataSetsRegistered.get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        AssertionUtil
                .assertContains(
                        "New data for the plate <a href='https://bwl27.sanofi-aventis.com:8443/openbis#entity=SAMPLE"
                                + "&sample_type=PLATE&action=SEARCH&code=TEST-PLATE'>TEST-PLATE</a> has been registered.",
                        email.recordedObject());
        context.assertIsSatisfied();
    }

    private void assertAllSamplesHaveContainer(List<NewSample> newSamples,
            String containerIdentifier)
    {
        for (NewSample newSample : newSamples)
        {
            assertEquals(containerIdentifier, newSample.getContainerIdentifier());
        }
    }

    private NewSample findByWellCode(List<NewSample> newSamples, String wellCode)
    {
        for (NewSample newSample : newSamples)
        {
            if (newSample.getIdentifier().endsWith(":" + wellCode))
            {
                return newSample;
            }
        }
        throw new RuntimeException("Failed to find sample registration for well " + wellCode);
    }

    private void assertNegativeControl(List<NewSample> newSamples, String wellCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(NEGATIVE_CONTROL_TYPE, newSample.getSampleType().getCode());
        assertEquals(0, newSample.getProperties().length);
    }

    private void assertPositiveControl(List<NewSample> newSamples, String wellCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(POSITIVE_CONTROL_TYPE, newSample.getSampleType().getCode());
        assertEquals(0, newSample.getProperties().length);
    }

    private void assertCompoundWell(List<NewSample> newSamples, String wellCode,
            String concentration)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(COMPOUND_WELL_TYPE, newSample.getSampleType().getCode());

        IEntityProperty concentrationProp =
                EntityHelper.tryFindProperty(newSample.getProperties(),
                        COMPOUND_WELL_CONCENTRATION_PROPNAME);
        assertNotNull(concentrationProp);
        assertEquals("Invalid concentration value for well '" + wellCode + "': ", concentration,
                concentrationProp.tryGetAsString());

        String materialCode = getMaterialCodeByWellCode(wellCode);
        MaterialIdentifier materialIdentifier = new MaterialIdentifier(materialCode, MATERIAL_TYPE);

        IEntityProperty wellMaterialProp =
                EntityHelper.tryFindProperty(newSample.getProperties(),
                        COMPOUND_WELL_MATERIAL_PROPNAME);
        assertNotNull(wellMaterialProp);
        assertEquals("Invalid material found in well '" + wellCode + "': ",
                materialIdentifier.print(), wellMaterialProp.tryGetAsString());

    }

    private void setUpPlateSearchExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    SearchCriteria sc = new SearchCriteria();
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                            "PLATE"));
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                            PLATE_CODE));
                    oneOf(openBisService).searchForSamples(sc);

                    will(returnValue(Arrays.asList(plate)));
                }
            });
    }

    private void setUpLibraryTemplateExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    final String identifierString = plate.getExperiment().getIdentifier();
                    ExperimentIdentifier identifier =
                            ExperimentIdentifierFactory.parse(identifierString);
                    oneOf(openBisService).tryToGetExperiment(identifier);
                    will(returnValue(plate.getExperiment()));
                }
            });
    }

    private void createData() throws IOException
    {
        File dataDirectory = new File("./sourceTest/examples/" + DATASET_DIR_NAME);
        FileUtils.copyDirectoryToDirectory(dataDirectory, workingDirectory);
        incomingDataSetFile = new File(workingDirectory, dataDirectory.getName());

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + dataDirectory.getName());
        FileUtilities.writeToFile(markerFile, "");
    }

    private Sample createPlate(String libraryTemplate, String plateGeometry)
    {
        ExperimentBuilder experimentBuilder = new ExperimentBuilder();
        experimentBuilder.identifier(EXPERIMENT_IDENTIFIER);
        experimentBuilder.property(LIBRARY_TEMPLATE_PROPNAME, libraryTemplate);
        String recipients = StringUtils.join(Arrays.asList(EXPERIMENT_RECIPIENTS), ",");
        experimentBuilder.property(EXPERIMENT_RECIPIENTS_PROPNAME, recipients);

        SampleBuilder sampleBuilder = new SampleBuilder();
        sampleBuilder.experiment(experimentBuilder.getExperiment());
        sampleBuilder.identifier(PLATE_IDENTIFIER);
        sampleBuilder.property(ScreeningConstants.PLATE_GEOMETRY, plateGeometry);

        final Sample plate = sampleBuilder.getSample();
        return plate;
    }

    private Map<String, Object> createQueryResult(String wellCode)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("WELL_CODE", wellCode);
        result.put("MATERIAL_CODE", getMaterialCodeByWellCode(wellCode));
        result.put("ABASE_COMPOUND_ID", wellCode + "_compound_id");
        result.put("ABASE_COMPOUND_BATCH_ID", wellCode + "_compound_batch_id");
        return result;
    }

    private String getMaterialCodeByWellCode(String wellCode)
    {
        return wellCode + "_material_code";
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/sanofi-dropbox/";
    }
}