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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.IRangeType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.RangeType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningDisplaySettings;

/**
 * @author Piotr Buczek
 */
public class ScreeningDisplaySettingsManager
{

    private static final String DELIMITER = ",";

    private final ScreeningDisplaySettings screeningSettings;

    @SuppressWarnings("deprecation")
    public ScreeningDisplaySettingsManager(IViewContext<?> viewContext)
    {
        DisplaySettingsManager displaySettingsManager = viewContext.getDisplaySettingsManager();
        ScreeningDisplaySettings settingsOrNull =
                (ScreeningDisplaySettings) displaySettingsManager
                        .tryGetTechnologySpecificSettings(viewContext.getTechnology());
        if (settingsOrNull == null)
        {
            settingsOrNull = new ScreeningDisplaySettings();
            displaySettingsManager.setTechnologySpecificSettings(viewContext.getTechnology(),
                    settingsOrNull);
        }
        screeningSettings = settingsOrNull;
    }

    // delegate

    @SuppressWarnings("deprecation")
    public List<String> tryGetDefaultChannels(String displayTypeID)
    {
        String channelListString = screeningSettings.getDefaultChannels().get(displayTypeID);
        if (channelListString != null)
        {
            return Arrays.asList(channelListString.split(","));
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public void setDefaultChannels(String displayTypeID, List<String> channels)
    {
        String channelListString = StringUtils.join(channels.toArray(new String[0]), DELIMITER);
        screeningSettings.getDefaultChannels().put(displayTypeID, channelListString);
    }

    @SuppressWarnings("deprecation")
    public void setDefaultAnalysisProcedure(String analysisProcedure)
    {
        screeningSettings.setDefaultAnalysisProcedure(analysisProcedure);
    }

    @SuppressWarnings("deprecation")
    public String getDefaultAnalysisProcedure()
    {
        return screeningSettings.getDefaultAnalysisProcedure();
    }

    public ImageResolution getDefaultResolution(String displayTypeId)
    {
        Map<String, ImageResolution> resolutions = screeningSettings.getDefaultResolutions();
        if (resolutions == null)
        {
            return null;
        } else
        {
            return resolutions.get(displayTypeId);
        }
    }

    public void setDefaultResolution(String displayTypeId, ImageResolution resolution)
    {
        Map<String, ImageResolution> resolutions = screeningSettings.getDefaultResolutions();
        if (resolutions == null)
        {
            resolutions = new HashMap<String, ImageResolution>();
            screeningSettings.setDefaultResolutions(resolutions);
        }
        resolutions.put(displayTypeId, resolution);
    }

    @SuppressWarnings("deprecation")
    public Map<String, String> getDefaultTransformationsForChannels(String displayTypeId)
    {
        Map<String, String> transformations =
                screeningSettings.getDefaultTransformations().get(displayTypeId);

        if (transformations == null)
        {
            transformations = new HashMap<String, String>();
            screeningSettings.getDefaultTransformations().put(displayTypeId, transformations);
        }

        return transformations;
    }

    public Integer getDefaultMovieDelay(String displayTypeId)
    {
        Map<String, Integer> delays = screeningSettings.getDefaultMovieDelays();

        if (delays != null && delays.get(displayTypeId) != null)
        {
            return delays.get(displayTypeId);
        } else
        {
            return 500;
        }
    }

    public void setDefaultMovieDelay(String displayTypeId, Integer delay)
    {
        Map<String, Integer> delays = screeningSettings.getDefaultMovieDelays();
        if (delays == null)
        {
            delays = new HashMap<String, Integer>();
            screeningSettings.setDefaultMovieDelays(delays);
        }
        delays.put(displayTypeId, delay);
    }

    public IRangeType getHeatMapRangeType(String featureCode)
    {
        Map<String, IRangeType> featureRangeTypes = screeningSettings.getDefaultFeatureRangeTypes();
        if (featureRangeTypes != null)
        {
            IRangeType rangeType = featureRangeTypes.get(featureCode);
            if (rangeType != null)
            {
                return rangeType;
            }
        }
        return RangeType.MIN_MAX;
    }

    public void setHeatMapRangeType(String featureCode, IRangeType rangeType)
    {
        Map<String, IRangeType> featureRangeTypes = screeningSettings.getDefaultFeatureRangeTypes();
        if (featureRangeTypes == null)
        {
            featureRangeTypes = new HashMap<String, IRangeType>();
            screeningSettings.setDefaultFeatureRangeTypes(featureRangeTypes);
        }
        featureRangeTypes.put(featureCode, rangeType);
    }

    @SuppressWarnings("deprecation")
    public Map<String, IntensityRange> getIntensityRangesForChannels(String displayTypeId)
    {
        Map<String, IntensityRange> intensityRangesForChannels =
                screeningSettings.getIntensityRangesForChannels().get(displayTypeId);

        if (intensityRangesForChannels == null)
        {
            intensityRangesForChannels = new HashMap<String, IntensityRange>();
            screeningSettings.getIntensityRangesForChannels().put(displayTypeId,
                    intensityRangesForChannels);
        }

        return intensityRangesForChannels;
    }
}
