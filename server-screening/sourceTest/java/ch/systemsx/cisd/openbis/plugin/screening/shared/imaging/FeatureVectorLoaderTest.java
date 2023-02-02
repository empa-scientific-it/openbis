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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Tests of {@link FeatureVectorLoader}.
 * 
 * @author Franz-Josef Elmer
 */
public class FeatureVectorLoaderTest extends AssertJUnit
{
    private static final int EXPERIMENT_ID = 42;

    private static final String DATA_SET_CODE1 = "ds1";

    private static final String DATA_SET_CODE2 = "ds2";

    private static final String DATA_SET_CODE3 = "ds3";

    private Mockery context;

    private IMetadataProvider service;

    private IImagingReadonlyQueryDAO dao;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IMetadataProvider.class);
        dao = context.mock(IImagingReadonlyQueryDAO.class);
    }

    @AfterMethod
    public final void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoFiltering()
    {
        long[] dataSetIDs = new long[]
        { 1, 2, 3 };
        String[][] featureCodesPerDataset = new String[][]
        {
                { "<A>a", "<B>b" },
                { "<B>beta", "c" },
                { "<B>b" } };
        prepareLoadFeatures(dataSetIDs, null, featureCodesPerDataset);

        FeatureVectorLoader builder = createBuilder();
        builder.addFeatureVectorsOfDataSetsOrDie(Arrays.asList(DATA_SET_CODE1, DATA_SET_CODE2,
                DATA_SET_CODE3));
        List<CodeAndLabel> codesAndLabels = builder.getCodesAndLabels();
        List<FeatureTableRow> rows = builder.createFeatureTableRows();

        assertEquals("[<A> a, <B> b, <B> beta, <C> c]", codesAndLabels.toString());
        assertFeatureTableRow(DATA_SET_CODE1, "A1", "/s/S1", "1.5, 11.5, NaN, NaN", rows.get(0));
        assertFeatureTableRow(DATA_SET_CODE1, "A2", "/s/S1", "0.5, 10.5, NaN, NaN", rows.get(1));
        assertFeatureTableRow(DATA_SET_CODE2, "A1", "/s/S2", "NaN, NaN, 2.5, 12.5", rows.get(2));
        assertFeatureTableRow(DATA_SET_CODE2, "A2", "/s/S2", "NaN, NaN, 1.5, 11.5", rows.get(3));
        assertFeatureTableRow(DATA_SET_CODE3, "A1", "/s/S3", "NaN, 3.5, NaN, NaN", rows.get(4));
        assertFeatureTableRow(DATA_SET_CODE3, "A2", "/s/S3", "NaN, 2.5, NaN, NaN", rows.get(5));
        assertEquals(6, rows.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testFiltering()
    {
        long[] dataSetIDs = new long[]
        { 1, 2, 3 };
        String[][] featureCodesPerDataset = new String[][]
        {
                { "<A>a", "b" },
                { "<B>beta", "c" },
                { "b" } };
        prepareLoadFeatures(dataSetIDs, "B", featureCodesPerDataset);

        FeatureVectorLoader builder = createBuilder("B");
        builder.addFeatureVectorsOfDataSetsOrDie(Arrays.asList(DATA_SET_CODE1, DATA_SET_CODE2,
                DATA_SET_CODE3));
        List<CodeAndLabel> codesAndLabels = builder.getCodesAndLabels();
        List<FeatureTableRow> rows = builder.createFeatureTableRows();

        assertEquals("[<B> b, <B> beta]", codesAndLabels.toString());
        assertFeatureTableRow(DATA_SET_CODE1, "A1", "/s/S1", "11.5, NaN", rows.get(0));
        assertFeatureTableRow(DATA_SET_CODE1, "A2", "/s/S1", "10.5, NaN", rows.get(1));
        assertFeatureTableRow(DATA_SET_CODE2, "A1", "/s/S2", "NaN, 2.5", rows.get(2));
        assertFeatureTableRow(DATA_SET_CODE2, "A2", "/s/S2", "NaN, 1.5", rows.get(3));
        assertFeatureTableRow(DATA_SET_CODE3, "A1", "/s/S3", "3.5, NaN", rows.get(4));
        assertFeatureTableRow(DATA_SET_CODE3, "A2", "/s/S3", "2.5, NaN", rows.get(5));
        assertEquals(6, rows.size());
        context.assertIsSatisfied();
    }

    private void prepareLoadFeatures(long[] dataSetIDs, String filteredCodeOrNull,
            String[][] featureCodesPerDataset)
    {
        Geometry geometry = Geometry.createFromCartesianDimensions(2, 1);
        prepareListDatasets(dataSetIDs);
        prepareListContainers(dataSetIDs, geometry);
        prepareGetFeatureDefinitions(dataSetIDs, featureCodesPerDataset);
        prepareGetFeatureVocabularyTerms(dataSetIDs);
        prepareAddFeatureVectors(dataSetIDs, filteredCodeOrNull, geometry, featureCodesPerDataset);
    }

    private void prepareAddFeatureVectors(final long dataSetIDs[], final String filteredCodeOrNull,
            final Geometry geometry, final String[]... featureHeadersPerDataset)
    {
        context.checking(new Expectations()
            {
                {
                    List<ImgFeatureValuesDTO> values = new ArrayList<ImgFeatureValuesDTO>();
                    List<Long> listedFeatureDefIds = new ArrayList<Long>();
                    long featureId = 0;
                    int datasetIx = 0;

                    for (String[] featureHeaders : featureHeadersPerDataset)
                    {
                        long dataSetId = dataSetIDs[datasetIx++];
                        long datasetFeatureIx = 0;
                        for (String featureHeader : featureHeaders)
                        {
                            String featureCode = CodeAndLabelUtil.create(featureHeader).getCode();
                            if (filteredCodeOrNull == null
                                    || filteredCodeOrNull.equals(featureCode))
                            {
                                listedFeatureDefIds.add(featureId);

                                PlateFeatureValues matrixValues = new PlateFeatureValues(geometry);
                                matrixValues.setForWellLocation(dataSetId + 10 * datasetFeatureIx
                                        + 0.5f, 1, 1);
                                matrixValues.setForWellLocation(dataSetId + 10 * datasetFeatureIx
                                        - 0.5f, 1, 2);

                                ImgFeatureValuesDTO value =
                                        new ImgFeatureValuesDTO(0.0, 0.0, matrixValues, featureId);
                                values.add(value);
                            }
                            featureId++;
                            datasetFeatureIx++;
                        }
                    }
                    one(dao).getFeatureValues(asPrimitiveArray(listedFeatureDefIds));
                    will(returnValue(values));
                }

            });
    }

    private static long[] asPrimitiveArray(List<Long> values)
    {
        long[] array = new long[values.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = values.get(i);
        }
        return array;
    }

    private void prepareListContainers(final long[] dataSetIDs, final Geometry geometry)
    {
        context.checking(new Expectations()
            {
                {
                    long[] containerIds = new long[dataSetIDs.length];
                    List<ImgContainerDTO> containers = new ArrayList<ImgContainerDTO>();

                    for (int i = 0; i < dataSetIDs.length; i++)
                    {
                        long id = dataSetIDs[i];
                        containerIds[i] = getContainerId(id);
                        String samplePermID = "s" + containerIds[i];
                        ImgContainerDTO container =
                                new ImgContainerDTO(samplePermID, geometry.getNumberOfRows(),
                                        geometry.getNumberOfColumns(), EXPERIMENT_ID);
                        container.setId(containerIds[i]);
                        containers.add(container);

                        one(service).tryGetSampleIdentifier(samplePermID);
                        will(returnValue(createSpaceIdentifier(id)));
                    }

                    one(dao).listContainersByIds(containerIds);
                    will(returnValue(containers));
                }

            });
    }

    private void prepareListDatasets(final long[] dataSetIDs)
    {
        context.checking(new Expectations()
            {
                {
                    String[] permIDs = new String[dataSetIDs.length];
                    List<ImgAnalysisDatasetDTO> dataSets = new ArrayList<ImgAnalysisDatasetDTO>();

                    for (int i = 0; i < dataSetIDs.length; i++)
                    {
                        long id = dataSetIDs[i];
                        permIDs[i] = "ds" + id;

                        ImgAnalysisDatasetDTO dataSet =
                                new ImgAnalysisDatasetDTO(permIDs[i], getContainerId(id));
                        dataSet.setId(id);
                        dataSets.add(dataSet);

                        one(service).tryGetContainedDatasets(dataSet.getPermId());
                        will(returnValue(Collections.emptyList()));
                    }

                    one(dao).listAnalysisDatasetsByPermId(permIDs);
                    will(returnValue(dataSets));
                }
            });
    }

    private void prepareGetFeatureDefinitions(final long[] dataSetIDs,
            final String[]... featureCodesPerDataset)
    {
        context.checking(new Expectations()
            {
                {
                    List<ImgFeatureDefDTO> defs = new ArrayList<ImgFeatureDefDTO>();
                    int datasetIx = 0;
                    int featureId = 0;
                    for (String[] featureCodes : featureCodesPerDataset)
                    {
                        long dataSetID = dataSetIDs[datasetIx];
                        for (String featureCode : featureCodes)
                        {
                            CodeAndLabel codeAndTitle = CodeAndLabelUtil.create(featureCode);
                            String title = codeAndTitle.getLabel();
                            String code = codeAndTitle.getCode();
                            ImgFeatureDefDTO def =
                                    new ImgFeatureDefDTO(title, code, title, dataSetID);
                            def.setId(featureId++);
                            defs.add(def);
                        }
                        datasetIx++;
                    }

                    one(dao).listFeatureDefsByDataSetIds(dataSetIDs);
                    will(returnValue(defs));
                }
            });
    }

    private void prepareGetFeatureVocabularyTerms(final long[] dataSetIDs)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).listFeatureVocabularyTermsByDataSetId(dataSetIDs);
                    will(returnValue(new ArrayList<ImgFeatureVocabularyTermDTO>()));
                }
            });
    }

    private static long getContainerId(long datasetId)
    {
        return datasetId + 100;
    }

    private SampleIdentifier createSpaceIdentifier(long datasetId)
    {
        return new SampleIdentifier(new SpaceIdentifier("s"), "S" + datasetId);
    }

    private void assertFeatureTableRow(String expectedDataSetCode, String expectedWell,
            String expectedPlate, String expectedValues, FeatureTableRow row)
    {
        assertEquals(expectedDataSetCode, row.getDataSetCode());
        String rowLetter =
                PlateUtils.translateRowNumberIntoLetterCode(row.getWellLocation().getRow());
        assertEquals(expectedWell, rowLetter + row.getWellLocation().getColumn());
        assertEquals(expectedPlate, row.getPlateIdentifier().toString());
        assertEquals(expectedValues, render(row.getFeatureValues()));
    }

    private String render(FeatureValue[] values)
    {
        StringBuilder builder = new StringBuilder();
        for (FeatureValue value : values)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private FeatureVectorLoader createBuilder(String... featureCodes)
    {
        return new FeatureVectorLoader(Arrays.asList(featureCodes), dao, service);
    }

}
