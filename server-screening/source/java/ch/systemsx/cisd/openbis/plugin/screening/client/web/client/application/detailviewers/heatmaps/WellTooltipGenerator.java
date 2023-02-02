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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.PlateLayouterModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Generates tooltips for wells.
 * 
 * @author Piotr Buczek
 */
class WellTooltipGenerator implements HeatmapPresenter.IWellTooltipGenerator
{
    private static final String UNKNOWN_WELL_LABEL = "No well information available.";

    private static final String NEWLINE = "\n";

    private static final int MAX_DESCRIBED_FEATURES = 20;

    private final PlateLayouterModel model;

    private final IRealNumberRenderer realNumberRenderer;

    public WellTooltipGenerator(PlateLayouterModel model, IRealNumberRenderer realNumberRenderer)
    {
        this.model = model;
        this.realNumberRenderer = realNumberRenderer;
    }

    @Override
    public String generateTooltip(int rowIx, int colIx, CodeAndLabel distinguishedLabelOrNull)
    {
        WellData wellData = model.getWellMatrix()[rowIx][colIx];
        return generateShortDescription(wellData, distinguishedLabelOrNull);
    }

    private String generateShortDescription(WellData wellData, CodeAndLabel distinguishedLabelOrNull)
    {
        String tooltip = "";
        String labelOrNull = distinguishedLabelOrNull == null ? null : distinguishedLabelOrNull.getLabel();
        if (distinguishedLabelOrNull != null)
        {
            tooltip += generateOneFeatureDescription(wellData, labelOrNull, true);
        }

        tooltip += generateMetadataDescription(wellData);

        int allFeaturesNum = getNumberOfAllFeatures();
        int loadedFeaturesNum = getNumberOfLoadedFeatures(wellData);
        if (loadedFeaturesNum - (distinguishedLabelOrNull != null ? 1 : 0) > 0)
        {
            if (tooltip.length() == 0)
            {
                tooltip += getWellCodeDescription(wellData);
            } else
            {
                tooltip += NEWLINE; // separate metadata from the text below
            }
            int describedFeaturesNum = Math.min(MAX_DESCRIBED_FEATURES, loadedFeaturesNum);
            int fCounter = 0;
            for (String featureLabel : wellData.getFeatureLabels())
            {
                if (featureLabel.equals(labelOrNull) == false)
                {
                    tooltip += generateOneFeatureDescription(wellData, featureLabel, false);
                }
                fCounter++;
                if (fCounter == describedFeaturesNum)
                {
                    break;
                }
            }
            if (allFeaturesNum > describedFeaturesNum)
            {
                tooltip += "...";
            }
        }
        return StringUtils.isBlank(tooltip) ? UNKNOWN_WELL_LABEL : tooltip;
    }

    private int getNumberOfLoadedFeatures(WellData wellData)
    {
        return wellData.getFeatureLabels().size();
    }

    private int getNumberOfAllFeatures()
    {
        return model.getAllFeatureNames().size();
    }

    private String generateOneFeatureDescription(WellData wellData, String featureLabel,
            boolean distinguished)
    {
        FeatureValue value = wellData.tryGetFeatureValue(featureLabel);
        // if the value should be distinguished we show it even if it's null
        if (value == null && distinguished == false)
        {
            return "";
        }
        String textValue = (value == null ? "" : "" + renderValue(value));
        if (distinguished)
        {
            textValue = "<b>" + textValue + "</b>";
        }
        return featureLabel + ": " + textValue + NEWLINE;
    }

    private String renderValue(FeatureValue value)
    {
        if (value.isFloat())
        {
            return renderFloat(value.asFloat());
        } else
        {
            return value.toString();
        }
    }

    private String renderFloat(float value)
    {
        return realNumberRenderer.render(value);
    }

    private static String generateMetadataDescription(WellData wellData)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            return "";
        }
        String tooltip = getWellCodeDescription(metadata);

        List<IEntityProperty> properties = metadata.getWellSample().getProperties();
        Collections.sort(properties);
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            tooltip += NEWLINE + propertyType.getLabel() + ": " + getPropertyDisplayText(property);
            Material material = property.getMaterial();
            if (material != null
                    && material.getMaterialType().getCode()
                            .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
            {
                List<IEntityProperty> geneProperties = material.getProperties();
                for (IEntityProperty geneProperty : geneProperties)
                {
                    if (geneProperty.getPropertyType().getCode()
                            .equalsIgnoreCase(ScreeningConstants.GENE_SYMBOLS))
                    {
                        tooltip += " [" + geneProperty.tryGetAsString() + "]";
                    }
                }
            }
        }
        return tooltip + NEWLINE;
    }

    private static String getPropertyDisplayText(IEntityProperty property)
    {
        Material material = property.getMaterial();
        if (material != null)
        {
            return material.getCode() + " (" + material.getMaterialType().getCode() + ")";
        } else
        {
            return property.tryGetAsString();
        }
    }

    private static String getWellCodeDescription(WellData wellData)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        return metadata == null ? "" : getWellCodeDescription(metadata) + NEWLINE;
    }

    private static String getWellCodeDescription(WellMetadata metadata)
    {
        Sample wellSample = metadata.getWellSample();
        String sampleTypeCode = wellSample.getSampleType().getCode();
        return printFriendlyCode(sampleTypeCode) + ": " + wellSample.getSubCode();
    }

    // private
    static String printFriendlyCode(String code)
    {
        String[] tokens = code.split("_|-");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++)
        {
            if (sb.length() > 0)
            {
                sb.append(" ");
            }
            sb.append(capitalizeFirst(tokens[i]));
        }
        return sb.toString();
    }

    private static String capitalizeFirst(String value)
    {
        if (value == null || value.length() == 0)
        {
            return value;
        }
        return ("" + value.charAt(0)).toUpperCase() + value.substring(1).toLowerCase();
    }

}