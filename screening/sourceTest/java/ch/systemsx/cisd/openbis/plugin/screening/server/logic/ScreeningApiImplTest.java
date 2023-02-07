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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Franz-Josef Elmer
 */
public class ScreeningApiImplTest extends AbstractServerTestCase
{
    private static final String SERVER_DOWNLOAD_URL = "server-url/"
            + GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

    private static final String SERVER_URL = "server-url";

    private IScreeningBusinessObjectFactory screeningBOFactory;

    private ScreeningApiImpl screeningApi;

    @BeforeMethod
    public void beforeMethod()
    {
        screeningBOFactory = context.mock(IScreeningBusinessObjectFactory.class);
        screeningApi =
                new ScreeningApiImpl(session, screeningBOFactory, daoFactory,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()));
    }

    @Test
    public void testListPlateWellsFromAugmentedCode()
    {
        final String identifier = "/SPACE/PLATE";
        final PlateIdentifier pi = PlateIdentifier.createFromAugmentedCode(identifier);
        final SamplePE plate = createSamplePE();

        final RecordingMatcher<ListOrSearchSampleCriteria> listerCriteria =
                new RecordingMatcher<ListOrSearchSampleCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).getDAOFactory();
                    one(screeningBOFactory).createSampleBO(session);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(SampleIdentifierFactory.parse(identifier));
                    one(sampleBO).getSample();
                    will(returnValue(plate));

                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(listerCriteria));
                    Sample w1 = createWellSample("w1", "A01");
                    Sample w2 = createWellSample("w2", "A02");
                    will(returnValue(Arrays.asList(w1, w2)));
                }
            });

        List<WellIdentifier> wells = screeningApi.listPlateWells(pi);
        assertEquals(plate.getId(), listerCriteria.recordedObject().getContainerSampleIds()
                .iterator().next());
        assertEquals(2, wells.size());
        assertEquals(wellIdentifier(pi, "w1", 1, 1), wells.get(0));
        assertEquals(wellIdentifier(pi, "w2", 1, 2), wells.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlateWellsFromPermId()
    {
        final String permId = "PLATE_PERM_ID";
        final PlateIdentifier pi = PlateIdentifier.createFromPermId(permId);
        final SamplePE plate = createSamplePE();

        final RecordingMatcher<ListOrSearchSampleCriteria> listerCriteria =
                new RecordingMatcher<ListOrSearchSampleCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).getDAOFactory();
                    one(screeningBOFactory).createSampleBO(session);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySamplePermId(permId);
                    one(sampleBO).getSample();
                    will(returnValue(plate));

                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(listerCriteria));
                    Sample w1 = createWellSample("w1", "A01");
                    Sample w2 = createWellSample("w2", "A02");
                    will(returnValue(Arrays.asList(w1, w2)));
                }
            });

        List<WellIdentifier> wells = screeningApi.listPlateWells(pi);

        assertEquals(plate.getId(), listerCriteria.recordedObject().getContainerSampleIds()
                .iterator().next());
        assertEquals(2, wells.size());
        assertEquals(wellIdentifier(pi, "w1", 1, 1), wells.get(0));
        assertEquals(wellIdentifier(pi, "w2", 1, 2), wells.get(1));
        context.assertIsSatisfied();
    }

    private static HashSet<Long> asSet(long id)
    {
        return new HashSet<Long>(Arrays.asList(id));
    }

    @Test
    public void testListImageDatasets()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, "384_WELLS_16X24");
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(asSet(1)));
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1),
                            imageAnalysisDataSet(p1, "2", 2))));
                }
            });

        List<ImageDatasetReference> dataSets = screeningApi.listImageDatasets(Arrays.asList(pi1));
        assertEquals(1, dataSets.size());
        assertEquals("1", dataSets.get(0).getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(0)
                .getPlateGeometry());
        assertEquals(new Date(100), dataSets.get(0).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(0).getDatastoreServerUrl());
        // FIXME this check doesn't work because of space code (as well as 2 other cases below)
        // assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(pi1.getPlateCode(), dataSets.get(0).getPlate().getPlateCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testListRawImageDatasets()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, "384_WELLS_16X24");
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(asSet(1)));
                    will(returnValue(Arrays.asList(imageRawDataSet(p1, "1", 1),
                            imageRawDataSet(p1, "2", 2), imageAnalysisDataSet(p1, "3", 3))));
                }
            });

        List<ImageDatasetReference> dataSets =
                screeningApi.listRawImageDatasets(Arrays.asList(pi1));
        assertEquals(2, dataSets.size());
        assertEquals("1", dataSets.get(0).getDatasetCode());
        assertEquals("2", dataSets.get(1).getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(0)
                .getPlateGeometry());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(1)
                .getPlateGeometry());
        assertEquals(new Date(100), dataSets.get(0).getRegistrationDate());
        assertEquals(new Date(200), dataSets.get(1).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(0).getDatastoreServerUrl());
        assertEquals(SERVER_URL, dataSets.get(1).getDatastoreServerUrl());
        // assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(pi1.getPlateCode(), dataSets.get(0).getPlate().getPlateCode());
        assertEquals(pi1.getPlateCode(), dataSets.get(1).getPlate().getPlateCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testListSegmentationImageDatasets()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, "384_WELLS_16X24");
                    will(returnValue(Arrays.asList(p1)));

                    allowing(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(asSet(1)));
                    final AbstractExternalData rawImage = imageRawDataSet(p1, "2", 2);
                    AbstractExternalData imageSegmentationDataSet =
                            imageSegmentationDataSet(p1, "3", 3, rawImage);
                    AbstractExternalData imageAnalysisDataSet = imageAnalysisDataSet(p1, "4", 4);
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1), rawImage,
                            imageSegmentationDataSet, imageAnalysisDataSet)));

                    one(datasetLister).listByParentTechIds(Arrays.asList(1l, 2l));
                    will(returnValue(Arrays.asList(imageSegmentationDataSet, imageAnalysisDataSet)));

                    one(datasetLister).listParentIds(Arrays.asList(3l));
                    HashMap<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
                    result.put(3l, Collections.singleton(2l));
                    will(returnValue(result));
                }
            });

        List<ImageDatasetReference> dataSets =
                screeningApi.listSegmentationImageDatasets(Arrays.asList(pi1));
        assertEquals(1, dataSets.size());
        assertEquals("3", dataSets.get(0).getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), dataSets.get(0)
                .getPlateGeometry());
        assertEquals(new Date(300), dataSets.get(0).getRegistrationDate());
        assertEquals(SERVER_URL, dataSets.get(0).getDatastoreServerUrl());
        assertEquals("2", dataSets.get(0).getParentImageDatasetReference().getDatasetCode());
        assertEquals(SERVER_URL, dataSets.get(0).getParentImageDatasetReference()
                .getDatastoreServerUrl());
        // assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(pi1.getPlateCode(), dataSets.get(0).getPlate().getPlateCode());
        context.assertIsSatisfied();
    }

    private static PlateIdentifier createSharedPlate(String plateCode)
    {
        return PlateIdentifier.createFromAugmentedCode("/" + plateCode);
    }

    @Test
    public void testListImageDatasetsWithMissingPlateGeometry()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, null);
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(asSet(1)));
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1))));
                }
            });

        try
        {
            screeningApi.listImageDatasets(Arrays.asList(pi1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample '/p1' has no property " + ScreeningConstants.PLATE_GEOMETRY,
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithMissingUnderscore()
    {
        assertListImageDatasetsFailsFor("2X4");
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithMissingX()
    {
        assertListImageDatasetsFailsFor("abc_2.4");
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithWidthNotANumber()
    {
        assertListImageDatasetsFailsFor("abc_aX4");
        context.assertIsSatisfied();
    }

    @Test
    public void testListImageDatasetsWithPlateGeometryWithHeightNotANumber()
    {
        assertListImageDatasetsFailsFor("abc_2Xb");
        context.assertIsSatisfied();
    }

    @Test
    public void testListFeatureVectorDatasets()
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, "384_WELLS_16X24");
                    will(returnValue(Arrays.asList(p1)));

                    exactly(2).of(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    long imageDatasetId = 1;
                    one(datasetLister).listBySampleIds(
                            with(new HashSet<Long>(asSet(imageDatasetId))));
                    will(returnValue(Arrays.asList(
                            imageDataSet(p1, "" + imageDatasetId, imageDatasetId),
                            imageAnalysisDataSet(p1, "2", 2))));

                    exactly(2).of(datasetLister).listByParentTechIds(Arrays.asList(imageDatasetId));
                    long analysisDatasetId = 3;
                    will(returnValue(Arrays.asList(imageAnalysisDataSet(null, ""
                            + analysisDatasetId, analysisDatasetId))));

                    exactly(2).of(datasetLister).listParentIds(Arrays.asList(analysisDatasetId));
                    Map<Long, Set<Long>> parentToChildrenMap = new HashMap<Long, Set<Long>>();
                    parentToChildrenMap.put(analysisDatasetId,
                            new HashSet<Long>(Arrays.asList(imageDatasetId)));
                    will(returnValue(parentToChildrenMap));
                }
            });

        List<FeatureVectorDatasetReference> dataSets =
                screeningApi.listFeatureVectorDatasets(Arrays.asList(pi1));

        assertEquals(2, dataSets.size());

        FeatureVectorDatasetReference firstDataset = dataSets.get(0);
        assertEquals("2", firstDataset.getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), firstDataset.getPlateGeometry());
        assertEquals(new Date(200), firstDataset.getRegistrationDate());
        assertEquals(SERVER_URL, firstDataset.getDatastoreServerUrl());
        // assertEquals(pi1, dataSets.get(0).getPlate());
        assertEquals(pi1.getPlateCode(), firstDataset.getPlate().getPlateCode());

        FeatureVectorDatasetReference secondDataset = dataSets.get(1);
        assertEquals("3", secondDataset.getDatasetCode());
        assertEquals(Geometry.createFromRowColDimensions(16, 24), secondDataset.getPlateGeometry());
        assertEquals(new Date(300), secondDataset.getRegistrationDate());
        assertEquals(SERVER_URL, secondDataset.getDatastoreServerUrl());
        assertEquals(pi1.getPlateCode(), secondDataset.getPlate().getPlateCode());
        // assertEquals(pi1, dataSets.get(1).getPlate());
        context.assertIsSatisfied();
    }

    private void assertListImageDatasetsFailsFor(final String plateGeometry)
    {
        final PlateIdentifier pi1 = createSharedPlate("p1");
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    one(sampleLister).list(with(any(ListOrSearchSampleCriteria.class)));
                    Sample p1 = plateSample(pi1, plateGeometry);
                    will(returnValue(Arrays.asList(p1)));

                    one(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                    one(datasetLister).listBySampleIds(with(asSet(1)));
                    will(returnValue(Arrays.asList(imageDataSet(p1, "1", 1))));
                }
            });

        try
        {
            screeningApi.listImageDatasets(Arrays.asList(pi1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Invalid property " + ScreeningConstants.PLATE_GEOMETRY + ": "
                    + plateGeometry.toUpperCase(), ex.getMessage());
        }
    }

    private Sample plateSample(PlateIdentifier plateIdentifier, String plateGeometryOrNull)
    {
        Sample sample = new Sample();
        sample.setId((long) 1);
        sample.setCode(plateIdentifier.getPlateCode());
        sample.setIdentifier(plateIdentifier.toString());
        if (plateGeometryOrNull != null)
        {
            IEntityProperty property = new EntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(ScreeningConstants.PLATE_GEOMETRY);
            property.setPropertyType(propertyType);
            VocabularyTerm term = new VocabularyTerm();
            term.setCode(plateGeometryOrNull.toUpperCase());
            property.setVocabularyTerm(term);
            sample.setProperties(Arrays.asList(property));
        } else
        {
            sample.setProperties(new ArrayList<IEntityProperty>());
        }
        return sample;
    }

    private AbstractExternalData imageDataSet(Sample sample, String code, long id)
    {
        AbstractExternalData dataSet = createDataSet(sample, code, id);
        dataSet.setDataSetType(dataSetType("HCS_IMAGE"));
        dataSet.setExperiment(new Experiment());
        dataSet.getExperiment().setProject(new Project());
        dataSet.getExperiment().getProject().setSpace(new Space());
        return dataSet;
    }

    private AbstractExternalData imageRawDataSet(Sample sample, String code, long id)
    {
        AbstractExternalData dataSet = createDataSet(sample, code, id);
        dataSet.setDataSetType(dataSetType("HCS_IMAGE_RAW"));
        dataSet.setExperiment(new Experiment());
        dataSet.getExperiment().setProject(new Project());
        dataSet.getExperiment().getProject().setSpace(new Space());
        return dataSet;
    }

    private AbstractExternalData imageSegmentationDataSet(Sample sample, String code, long id,
            AbstractExternalData parent)
    {
        AbstractExternalData dataSet = createDataSet(sample, code, id);
        dataSet.setDataSetType(dataSetType("HCS_IMAGE_SEGMENTATION"));
        dataSet.setParents(Collections.singleton(parent));
        dataSet.setExperiment(new Experiment());
        dataSet.getExperiment().setProject(new Project());
        dataSet.getExperiment().getProject().setSpace(new Space());
        return dataSet;
    }

    private AbstractExternalData imageAnalysisDataSet(Sample sample, String code, long id)
    {
        AbstractExternalData dataSet = createDataSet(sample, code, id);
        dataSet.setDataSetType(dataSetType("HCS_IMAGE_ANALYSIS_DATA"));
        dataSet.setExperiment(new Experiment());
        dataSet.getExperiment().setProject(new Project());
        dataSet.getExperiment().getProject().setSpace(new Space());
        return dataSet;
    }

    private PhysicalDataSet createDataSet(Sample sample, String code, long id)
    {
        PhysicalDataSet dataSet = new PhysicalDataSet();
        dataSet.setId(id);
        dataSet.setCode(code);
        dataSet.setSample(sample);
        DataStore dataStore = new DataStore();
        dataStore.setDownloadUrl(SERVER_DOWNLOAD_URL);
        dataStore.setHostUrl(SERVER_URL);
        dataSet.setDataStore(dataStore);
        dataSet.setRegistrationDate(new Date(Long.parseLong(code) * 100));
        return dataSet;
    }

    private DataSetType dataSetType(String code)
    {
        DataSetType type = new DataSetType();
        type.setCode(code);
        return type;
    }

    private static SamplePE createSamplePE()
    {
        SamplePE sample = new SamplePE();
        sample.setId(1L);
        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setContainerHierarchyDepth(1);
        sampleType.setGeneratedFromHierarchyDepth(1);
        sampleType.setListable(false);
        sampleType.setAutoGeneratedCode(false);
        sampleType.setShowParentMetadata(false);
        sampleType.setSubcodeUnique(false);
        sample.setSampleType(sampleType);
        return sample;
    }

    private static Sample createWellSample(String permId, String subCode)
    {
        Sample result = new Sample();
        result.setPermId(permId);
        result.setSubCode(subCode);
        return result;
    }

    private static WellIdentifier wellIdentifier(PlateIdentifier plate, String wellPermId, int row,
            int col)
    {
        return new WellIdentifier(plate, new WellPosition(row, col), wellPermId);
    }

}
