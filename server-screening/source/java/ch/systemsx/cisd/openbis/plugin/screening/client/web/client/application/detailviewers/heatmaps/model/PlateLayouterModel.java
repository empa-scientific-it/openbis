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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.PlateLayouter;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.IRangeType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureList;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores the model of {@link PlateLayouter}. Contains some logic to change the model.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayouterModel
{
    private DatasetReference datasetReference; // currently shown dataset

    private final WellData[][] wellMatrix;

    private final List<WellData> wellList; // the same wells as in the matrix

    private final Sample plateSample;

    private final ScreeningDisplaySettingsManager displaySettingsManager;

    // --- internal dynamix state

    private ImageDatasetEnrichedReference imageDatasetOrNull;

    // names of all features
    private List<CodeAndLabel> allFeatureNames = new ArrayList<CodeAndLabel>();

    // labels of loaded features
    private Set<String> availableFeatureLabels = new LinkedHashSet<String>();

    // labels of loaded vocabulary features
    private Set<String> vocabularyFeatureLabels = new HashSet<String>();

    // lists of features
    private List<FeatureList> featureLists = new ArrayList<FeatureList>();

    private CodeAndLabel chosenFeature;

    private IRangeType rangeType;

    // ---

    public PlateLayouterModel(PlateMetadata plateMetadata, ScreeningDisplaySettingsManager displaySettingsManager)
    {
        this.displaySettingsManager = displaySettingsManager;
        this.plateSample = plateMetadata.getPlate();
        this.wellMatrix = createWellMatrix(plateMetadata);
        this.wellList = asList(wellMatrix);
    }

    public DatasetReference tryGetDatasetReference()
    {
        return datasetReference;
    }

    public WellData[][] getWellMatrix()
    {
        return wellMatrix;
    }

    public List<WellData> getWellList()
    {
        return wellList;
    }

    public ImageDatasetEnrichedReference tryGetImageDataset()
    {
        return imageDatasetOrNull;
    }

    public void setImageDataset(ImageDatasetEnrichedReference imageDataset)
    {
        this.imageDatasetOrNull = imageDataset;
    }

    public List<CodeAndLabel> getAllFeatureNames()
    {
        return allFeatureNames;
    }

    public List<FeatureList> getFeatureLists()
    {
        return featureLists;
    }

    public boolean isVocabularyFeature()
    {
        return vocabularyFeatureLabels.contains(chosenFeature.getLabel());
    }

    /**
     * Sets chosen feature.
     * 
     * @return <code>true</code> if chosen feature is already available.
     */
    public boolean setChosenFeature(CodeAndLabel chosenFeature)
    {
        this.chosenFeature = chosenFeature;
        setRangeType(displaySettingsManager.getHeatMapRangeType(chosenFeature.getCode()));
        return availableFeatureLabels.contains(chosenFeature.getLabel());
    }

    public CodeAndLabel getChosenFeature()
    {
        return chosenFeature;
    }

    public void setRangeType(IRangeType rangeType)
    {
        this.rangeType = rangeType;
        if (chosenFeature != null)
        {
            displaySettingsManager.setHeatMapRangeType(chosenFeature.getCode(), rangeType);
        }
    }

    public IRangeType getRangeType()
    {
        return rangeType;
    }

    // --- some logic

    public MinMaxAndRange calculateRange()
    {
        IRangeCalculator calculator = RangeCalculatorFactory.create(rangeType);
        List<Float> data = new ArrayList<Float>();
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        String featureKey = chosenFeature.getLabel();
        for (WellData wellData : wellList)
        {
            Float value = Utils.tryAsFloatFeature(wellData, featureKey);
            if (value != null && Float.isNaN(value) == false && Float.isInfinite(value) == false)
            {
                data.add(value);
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        return new MinMaxAndRange(min, max, calculator.calculate(data));
    }

    public List<String> extractUniqueVocabularyTerms()
    {
        Set<String> uniqueValues = new HashSet<String>();
        for (WellData well : wellList)
        {
            String term = Utils.tryAsVocabularyFeature(well, chosenFeature.getLabel());
            if (term != null)
            {
                uniqueValues.add(term);
            }
        }
        List<String> result = new ArrayList<String>(uniqueValues);
        Collections.sort(result);
        return result;

    }

    public void setFeatureVectorDataset(FeatureVectorDataset featureVectorDatasetOrNull)
    {
        cleanFeatureVectors();
        this.datasetReference = null;
        if (featureVectorDatasetOrNull != null)
        {
            this.datasetReference = featureVectorDatasetOrNull.getDatasetReference();
            this.allFeatureNames.addAll(featureVectorDatasetOrNull.getFeatureNames());
            if (featureVectorDatasetOrNull.getFeatureLists() != null)
            {
                this.featureLists.addAll(featureVectorDatasetOrNull.getFeatureLists());
            }
            List<? extends FeatureVectorValues> features =
                    featureVectorDatasetOrNull.getDatasetFeatures();
            if (features.isEmpty() == false)
            {
                // NOTE: for each feature vector in the dataset this set is the same
                this.vocabularyFeatureLabels = extractVocabularyFeatureLabels(features.get(0));
            }
            for (FeatureVectorValues featureVector : features)
            {
                WellLocation loc = featureVector.getWellLocation();
                WellData wellData = tryGetWellData(loc);
                if (wellData != null)
                {
                    for (Entry<String, FeatureValue> entry : featureVector.getFeatureMap()
                            .entrySet())
                    {
                        String featureLabel = entry.getKey();
                        availableFeatureLabels.add(featureLabel);
                        FeatureValue value = entry.getValue();
                        wellData.addFeatureValue(featureLabel, value);
                    }
                }
            }
        }
    }

    private void cleanFeatureVectors()
    {
        this.vocabularyFeatureLabels.clear();
        this.availableFeatureLabels.clear();
        this.allFeatureNames.clear();
        this.featureLists.clear();
        for (WellData well : wellList)
        {
            well.resetFeatureValues();
        }
    }

    // add new feature to those already loaded
    public void updateFeatureVectorDataset(FeatureVectorDataset featureVectorDataset)
    {
        assert datasetReference.getCode().equals(
                featureVectorDataset.getDatasetReference().getCode());

        List<? extends FeatureVectorValues> features = featureVectorDataset.getDatasetFeatures();
        if (features.isEmpty() == false)
        {
            // NOTE: for each feature vector in the dataset this set is the same
            this.vocabularyFeatureLabels.addAll(extractVocabularyFeatureLabels(features.get(0)));
        }
        for (FeatureVectorValues featureVector : features)
        {
            WellLocation loc = featureVector.getWellLocation();
            WellData wellData = tryGetWellData(loc);
            if (wellData != null && wellData.isFullyLoaded() == false)
            {
                for (Entry<String, FeatureValue> entry : featureVector.getFeatureMap().entrySet())
                {
                    String featureLabel = entry.getKey();
                    availableFeatureLabels.add(featureLabel);
                    FeatureValue value = entry.getValue();
                    wellData.addFeatureValue(featureLabel, value);
                }
            }
        }
    }

    public void resetFeatureValues(WellData wellData)
    {
        wellData.resetFeatureValues();
    }

    // set all well's features
    public void updateWellFeatureValues(FeatureVectorValues wellFeatureVectorValues)
    {
        assert datasetReference.getCode().equals(wellFeatureVectorValues.getDataSetCode());

        final WellLocation loc = wellFeatureVectorValues.getWellLocation();
        final WellData wellData = tryGetWellData(loc);
        if (wellData != null)
        {
            final Map<String, FeatureValue> allValues = wellFeatureVectorValues.getFeatureMap();
            wellData.setAllFeatureValues(allValues);
        }
    }

    private static Set<String> extractVocabularyFeatureLabels(FeatureVectorValues featureVector)
    {
        final Set<String> result = new HashSet<String>();
        for (Entry<String, FeatureValue> entry : featureVector.getFeatureMap().entrySet())
        {
            String featureLabel = entry.getKey();
            FeatureValue featureValue = entry.getValue();
            if (featureValue.isVocabularyTerm())
            {
                result.add(featureLabel);
            }
        }
        return result;
    }

    private WellData tryGetWellData(WellLocation loc)
    {
        int rowIx = loc.getRow() - 1;
        int colIx = loc.getColumn() - 1;
        if (rowIx < wellMatrix.length && colIx < wellMatrix[rowIx].length)
        {
            return wellMatrix[rowIx][colIx];
        } else
        {
            // can happen if the plate geometry is not in sync with the feature vector database
            return null;
        }
    }

    // ------------------------

    // Elements will NOT contain null even if well is empty.
    private static WellData[][] createWellMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] matrix = createEmptyWellMatrix(plateMetadata);
        List<WellMetadata> wells = plateMetadata.getWells();
        for (WellMetadata well : wells)
        {
            WellLocation location = well.tryGetLocation();
            if (location != null)
            {
                WellData wellData = matrix[location.getRow() - 1][location.getColumn() - 1];
                wellData.setMetadata(well);
            }
        }
        return matrix;
    }

    private static WellData[][] createEmptyWellMatrix(PlateMetadata plateMetadata)
    {
        WellData[][] data = new WellData[plateMetadata.getRowsNum()][plateMetadata.getColsNum()];
        Experiment experiment = plateMetadata.getPlate().getExperiment();
        for (int row = 0; row < data.length; row++)
        {
            for (int col = 0; col < data[row].length; col++)
            {
                data[row][col] = new WellData(new WellLocation(row + 1, col + 1), experiment);
            }
        }
        return data;
    }

    private static <T> List<T> asList(T[][] matrix)
    {
        List<T> result = new ArrayList<T>();
        for (int row = 0; row < matrix.length; row++)
        {
            for (int col = 0; col < matrix[row].length; col++)
            {
                result.add(matrix[row][col]);
            }
        }
        return result;
    }

    public Sample getPlateSample()
    {
        return plateSample;
    }

}