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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.IRangeType;

/**
 * Screening specific display settings.
 * 
 * @author Piotr Buczek
 */
public class ScreeningDisplaySettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Map<String/* displayTypeID */, String/* channel name */> defaultChannels =
            new HashMap<String, String>();

    private Map<String, Map<String, String>> defaultTransformations =
            new HashMap<String, Map<String, String>>();

    private Map<String, ImageResolution> defaultResolutions;

    private Map<String, Integer> defaultMovieDelays;

    private Map<String, IRangeType> defaultFeatureRangeTypes;

    private String defaultAnalysisProcedure;

    private Map<String, Map<String, IntensityRange>> intensityRangesForChannels =
            new HashMap<String, Map<String, IntensityRange>>();

    /** @deprecated Should be used only by ScreeningDisplaySettingsManager. */
    @Deprecated
    public Map<String, String> getDefaultChannels()
    {
        return defaultChannels;
    }

    // for serialization

    @SuppressWarnings("unused")
    private void setDefaultChannels(Map<String, String> defaultChannels)
    {
        this.defaultChannels = defaultChannels;
    }

    /** @deprecated Should be used only by ScreeningDisplaySettingsManager. */
    @Deprecated
    public void setDefaultAnalysisProcedure(String defaultAnalysisProcedure)
    {
        this.defaultAnalysisProcedure = defaultAnalysisProcedure;
    }

    /**
     * Default analysis procedure for analysis result datasets.
     * 
     * @deprecated Should be used only by ScreeningDisplaySettingsManager.
     */
    @Deprecated
    public String getDefaultAnalysisProcedure()
    {
        return this.defaultAnalysisProcedure;
    }

    /** @deprecated Should be used only by ScreeningDisplaySettingsManager. */
    @Deprecated
    public Map<String, Map<String, String>> getDefaultTransformations()
    {
        if (defaultTransformations == null)
        {
            this.defaultTransformations = new HashMap<String, Map<String, String>>();
        }

        return defaultTransformations;
    }

    public Map<String, ImageResolution> getDefaultResolutions()
    {
        return defaultResolutions;
    }

    public void setDefaultResolutions(Map<String, ImageResolution> defaultResolutions)
    {
        this.defaultResolutions = defaultResolutions;
    }

    // for serialization

    @SuppressWarnings("unused")
    private void setDefaultTransformations(Map<String, Map<String, String>> defaultTransformations)
    {
        this.defaultTransformations = defaultTransformations;
    }

    public Map<String, Integer> getDefaultMovieDelays()
    {
        return defaultMovieDelays;
    }

    public void setDefaultMovieDelays(Map<String, Integer> defaultMovieDelays)
    {
        this.defaultMovieDelays = defaultMovieDelays;
    }

    /** @deprecated Should be used only by ScreeningDisplaySettingsManager. */
    @Deprecated
    public Map<String, Map<String, IntensityRange>> getIntensityRangesForChannels()
    {
        if (intensityRangesForChannels == null)
        {
            intensityRangesForChannels = new HashMap<String, Map<String, IntensityRange>>();
        }
        return intensityRangesForChannels;
    }

    @SuppressWarnings("unused")
    private void setIntensityRangesForChannels(
            Map<String, Map<String, IntensityRange>> intensityRangesForChannels)
    {
        this.intensityRangesForChannels = intensityRangesForChannels;
    }

    public Map<String, IRangeType> getDefaultFeatureRangeTypes()
    {
        return defaultFeatureRangeTypes;
    }

    public void setDefaultFeatureRangeTypes(Map<String, IRangeType> defaultFeatureRangeTypes)
    {
        this.defaultFeatureRangeTypes = defaultFeatureRangeTypes;
    }
}
