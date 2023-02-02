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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.WellTooltipGenerator.printFriendlyCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.PlateLayouterModel;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ObjectCreationUtilForTests;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Tests of {@link WellTooltipGenerator} class.
 * 
 * @author Tomasz Pylak
 */
public class WellTooltipGeneratorTest extends AssertJUnit
{

    private static final String FEATURE_X_DESC = "FeatureX: 1.0\n";

    private static final String FEATURE_Y_DESC = "FeatureY: 2.0\n";

    private static final String ALL_FEATURES_DESC = FEATURE_X_DESC + FEATURE_Y_DESC;

    private static final String WELL_A2 = "A2";

    private static final String WELL_B3 = "B3";

    private static final String METADATA_EXPECTED_DESC_A2 = "Well: " + WELL_A2 + "\n"
            + "someValue: 42\n";

    @Test
    // wells with metadata and no feature vectors
    public void testMetadataTooltip()
    {
        PlateLayouterModel model = createPlateModelWithMetadata();
        String desc = tryGenerateShortDescription(model, 0, 1, null);
        assertEquals(METADATA_EXPECTED_DESC_A2, desc);
    }

    private static String tryGenerateShortDescription(PlateLayouterModel model, int rowIx,
            int colIx, String featureLabelOrNull)
    {
        CodeAndLabel distinguishedLabelOrNull =
                featureLabelOrNull == null ? null : new CodeAndLabel(null, featureLabelOrNull);
        return new WellTooltipGenerator(model, createDummyRealNumberRenderer()).generateTooltip(
                rowIx, colIx, distinguishedLabelOrNull);
    }

    private static IRealNumberRenderer createDummyRealNumberRenderer()
    {
        return new IRealNumberRenderer()
            {
                @Override
                public String render(float value)
                {
                    return "" + value;
                }
            };
    }

    @Test
    // wells with metadata and feature vectors
    public void testTooltipWithMetadataAndFeatureVectors()
    {
        PlateLayouterModel model = createPlateModelWithMetadata();
        model.setFeatureVectorDataset(createFeatureVectorDataset());

        // no feature distingushed
        String desc = tryGenerateShortDescription(model, 0, 1, null);
        assertEquals(METADATA_EXPECTED_DESC_A2 + "\n" + ALL_FEATURES_DESC, desc);

        // one feature distingushed
        desc = tryGenerateShortDescription(model, 0, 1, "FeatureY");
        assertEquals("FeatureY: <b>2.0</b>\n" + METADATA_EXPECTED_DESC_A2 + "\n" + FEATURE_X_DESC,
                desc);

    }

    @Test
    // no metadata for the wells, feature vectors present
    public void testTooltipWithFeatureVectors()
    {
        PlateLayouterModel model = new PlateLayouterModel(createEmptyPlateMetadata(), null);
        model.setFeatureVectorDataset(createFeatureVectorDataset());

        String desc = tryGenerateShortDescription(model, 0, 1, null);
        assertEquals(ALL_FEATURES_DESC, desc);

        desc = tryGenerateShortDescription(model, 0, 1, "FeatureX");
        assertEquals("FeatureX: <b>1.0</b>\n" + "\n" + FEATURE_Y_DESC, desc);

    }

    @Test
    // no metadata for the wells, many feature vectors present (not all can be shown)
    public void testTooltipWithManyFeatureVectors()
    {
        PlateLayouterModel model = new PlateLayouterModel(createEmptyPlateMetadata(), null);
        model.setFeatureVectorDataset(createLargeFeatureVectorDataset());

        String desc = tryGenerateShortDescription(model, 0, 1, null);
        AssertionUtil.assertStarts("Feature0: 0.0", desc);
        AssertionUtil.assertEnds("Feature19: 19.0\n" + "...", desc);

        desc = tryGenerateShortDescription(model, 0, 1, "Feature4");
        AssertionUtil.assertStarts("Feature4: <b>4.0</b>", desc);
        AssertionUtil.assertEnds("Feature19: 19.0\n" + "...", desc);
    }

    @Test
    public void testPrintFriendlyCode()
    {
        assertEquals("", printFriendlyCode(""));
        assertEquals("A", printFriendlyCode("A"));
        assertEquals("Ab", printFriendlyCode("AB"));
        assertEquals("This Is Not A Joke", printFriendlyCode("THIs-iS_NOT_a_JOKE"));
    }

    // 10 features for well A2
    private static FeatureVectorDataset createLargeFeatureVectorDataset()
    {
        int size = 40;
        CodeAndLabel[] featureNames = new CodeAndLabel[size];
        FeatureValue[] featureValues = new FeatureValue[size];
        for (int i = 0; i < size; i++)
        {
            final String label = "Feature" + i;
            final FeatureValue featureValue = FeatureValue.createFloat(i);
            featureNames[i] = new CodeAndLabel(label, label);
            featureValues[i] = featureValue;
        }
        List<FeatureVectorValues> features = new ArrayList<FeatureVectorValues>();
        features.add(new FeatureVectorValues(null, getLocation(WELL_A2), null, featureNames,
                featureValues));
        return new FeatureVectorDataset(createDatasetReference(), features,
                Arrays.asList(featureNames));
    }

    private static FeatureVectorDataset createFeatureVectorDataset()
    {

        String[] featureLabels =
        { "FeatureX", "FeatureY" };
        CodeAndLabel[] codesAndLabels =
                new CodeAndLabel[]
                { new CodeAndLabel(featureLabels[0], featureLabels[0]),
                        new CodeAndLabel(featureLabels[1], featureLabels[1]) };

        List<FeatureVectorValues> features = new ArrayList<FeatureVectorValues>();
        features.add(new FeatureVectorValues(null, getLocation(WELL_A2), null, codesAndLabels,
                new FeatureValue[]
                { FeatureValue.createFloat(1), FeatureValue.createFloat(2) }));
        features.add(new FeatureVectorValues(null, getLocation(WELL_B3), null, codesAndLabels,
                new FeatureValue[]
                { FeatureValue.createFloat(-1), FeatureValue.createFloat(-2) }));
        return new FeatureVectorDataset(createDatasetReference(), features,
                Arrays.asList(codesAndLabels));
    }

    private static DatasetReference createDatasetReference()
    {
        return new DatasetReference(0, null, null, null, null, null, null, null, null, null, null);
    }

    private static PlateMetadata createEmptyPlateMetadata()
    {
        List<WellMetadata> wells = new ArrayList<WellMetadata>();
        Sample plate = createSample("plate");
        return new PlateMetadata(plate, wells, 16, 24);
    }

    private static PlateLayouterModel createPlateModelWithMetadata()
    {
        return new PlateLayouterModel(createNonEmptyPlateMetadata(), null);
    }

    private static PlateMetadata createNonEmptyPlateMetadata()
    {
        List<WellMetadata> wells = new ArrayList<WellMetadata>();
        Sample plate = createSample("plate");
        wells.add(createWellMetadata(WELL_A2, plate));
        wells.add(createWellMetadata(WELL_B3, plate));
        return new PlateMetadata(plate, wells, 16, 24);
    }

    private static WellMetadata createWellMetadata(String wellCode, Sample plate)
    {
        WellMetadata well = new WellMetadata();
        Sample wellSample = createSample(null, "someValue", 42);
        wellSample.setSubCode(wellCode);
        wellSample.setContainer(plate);
        WellLocation loc = getLocation(wellCode);
        well.setWellSample(wellSample, loc);
        return well;
    }

    private static WellLocation getLocation(String wellCode)
    {
        return ScreeningUtils.tryCreateLocationFromMatrixCoordinate(wellCode);
    }

    private static Sample createSample(String code, String propertyLabel, int value)
    {
        Sample sample = createSample(code);
        EntityProperty property =
                ObjectCreationUtilForTests.createIntProperty(propertyLabel, value);
        sample.setProperties(Arrays.<IEntityProperty> asList(property));
        return sample;
    }

    private static Sample createSample(String code)
    {
        Sample sample = new Sample();
        sample.setCode(code);
        SampleType sampleType = new SampleType();
        sampleType.setCode("WELL");
        sample.setSampleType(sampleType);
        return sample;
    }
}
