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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Methods to manipulate on entity types and create user-frendly labels out of them.
 * 
 * @author Tomasz Pylak
 */
public class EntityTypeLabelUtils
{

    public final static List<String> createDatasetLabels(List<? extends DatasetReference> datasetReferences,
            boolean withFileType)
    {
        return createDatasetLabels(datasetReferences, withFileType, false);
    }

    public final static List<String> createDatasetLabels(List<? extends DatasetReference> datasetReferences,
            boolean withFileType, boolean useAnalysisProcedureInPlaceOfType)
    {
        List<String> labels = new ArrayList<String>(datasetReferences.size());
        for (DatasetReference datasetReference : datasetReferences)
        {
            labels.add(createDatasetLabel(datasetReference, withFileType, null, true, useAnalysisProcedureInPlaceOfType));
        }
        return labels;
    }

    public final static List<String> createDatasetLabelsForFeatureVectors(
            List<DatasetReference> featureVectors, boolean withFileType)
    {
        Map<String, Integer> labelsSet = new HashMap<String, Integer>(featureVectors.size());
        List<String> labels = new ArrayList<String>(featureVectors.size());
        for (DatasetReference reference : featureVectors)
        {
            String label =
                    createDatasetLabel(reference, withFileType, reference.getAnalysisProcedure(),
                            false);

            Integer count = 1;
            if (labelsSet.containsKey(label))
            {
                count = labelsSet.get(label) + 1;
            }
            labelsSet.put(label, count);

            labels.add(label);
        }

        // detect duplicated labels
        int i = 0;
        for (DatasetReference reference : featureVectors)
        {
            String label = labels.get(i);
            if (labelsSet.get(label) > 1)
            {
                labels.set(
                        i,
                        createDatasetLabel(reference, withFileType,
                                reference.getAnalysisProcedure(), true));
            }
            i++;
        }

        return labels;
    }

    /**
     * Creates user friendly labels, e.g.<br>
     * Raw (TIFF), 2011-05-30, 12378612873681-12312<br>
     * Overview (JPG), 2011-05-30, 12378612873681-12312<br>
     * <br>
     * Features, 2011-05-30, 12378612873681-12312<br>
     * Metadata, 2011-05-30, 12378612873681-12312<br>
     * <br>
     * Hcs analysis cell classifications (MAT), 2011-05-30, 12378612873681-12312<br>
     */
    public static String createDatasetLabel(DatasetReference datasetReference,
            boolean withFileType, String analysisProcedure, boolean withDatasetCode)
    {
        return createDatasetLabel(datasetReference, withFileType, analysisProcedure, withDatasetCode, false);
    }

    public static String createDatasetLabel(DatasetReference datasetReference,
            boolean withFileType, String analysisProcedure, boolean withDatasetCode, boolean useAnalysisProcedureInteadOfDataSetType)
    {
        String registrationDate = renderDate(datasetReference);
        return createDatasetLabel(datasetReference, withFileType, registrationDate,
                analysisProcedure, withDatasetCode, useAnalysisProcedureInteadOfDataSetType);
    }

    // private, just for tests
    static String createDatasetLabel(DatasetReference datasetReference, boolean withFileType,
            String registrationDate, String analysisProcedure, boolean withDatasetCode, boolean useAnalysisProcedureInteadOfDataSetType)
    {
        String typeLabel;
        if (useAnalysisProcedureInteadOfDataSetType)
        {
            typeLabel = datasetReference.getAnalysisProcedure();
            if (typeLabel == null)
            {
                typeLabel = "Unspecified Analysis Procedure";
            }
        } else
        {
            typeLabel = getDatasetUserFriendlyTypeCode(datasetReference);
        }
        String fileType =
                withFileType && datasetReference.getFileTypeCode() != null ? " ("
                        + datasetReference.getFileTypeCode() + ")" : "";
        String analysisProcedureRendered =
                StringUtils.isBlank(analysisProcedure) ? "" : analysisProcedure + ", ";

        String additionalLabelPart = datasetReference.getLabelText() == null ? "" : (datasetReference.getLabelText() + ", ");

        return typeLabel + fileType + ", " + additionalLabelPart + analysisProcedureRendered + registrationDate
                + (withDatasetCode ? ", " + datasetReference.getCode() : "");
    }

    private static String renderDate(DatasetReference datasetReference)
    {
        return DateRenderer.renderDate(datasetReference.getRegistrationDate(),
                BasicConstant.DATE_PATTERN);
    }

    private static String getDatasetUserFriendlyTypeCode(DatasetReference datasetReference)
    {
        String datasetTypeCode = getDatasetTypeCode(datasetReference);
        String label =
                tryWithoutPrefix(datasetTypeCode,
                        ScreeningConstants.HCS_ANALYSIS_DATASET_TYPE_PREFIX);
        if (StringUtils.isBlank(label))
        {
            label =
                    tryWithoutPrefix(datasetTypeCode,
                            ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX
                                    + ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER);
        }

        if (StringUtils.isBlank(label))
        {
            label =
                    tryWithoutPrefix(datasetTypeCode,
                            ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX);
        }
        if (StringUtils.isBlank(label))
        {
            label = tryWithoutPrefix(datasetTypeCode, ScreeningConstants.HCS_DATASET_TYPE_PREFIX);
        }
        if (StringUtils.isBlank(label))
        {
            label = datasetTypeCode;
        }
        if (label.startsWith("_"))
        {
            label = label.substring(1);
        }
        return formatAsTitle(label, true);
    }

    private static String tryWithoutPrefix(String datasetTypeCode, String prefix)
    {
        if (datasetTypeCode.startsWith(prefix))
        {
            return datasetTypeCode.substring(prefix.length());
        } else
        {
            return null;
        }
    }

    private static String getDatasetTypeCode(DatasetReference datasetReference)
    {
        return datasetReference.getEntityType().getCode();
    }

    /**
     * Changes capitalization and replaces '_' wit ' '. Letters are made small, first letter gets capitalized if captalizeFirstLetter is true, e.g.
     * 'COdE_XYZ' is changed to 'Code Xyz'.
     */
    public static String formatAsTitle(String text, boolean captalizeFirstLetter)
    {
        String label;
        if (captalizeFirstLetter)
        {
            label = ("" + text.charAt(0)).toUpperCase() + text.substring(1).toLowerCase();
        } else
        {
            label = text.toLowerCase();
        }
        return label.replace('_', ' ');
    }

}
