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

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Screening specific display settings.
 * 
 * @author Piotr Buczek
 */
public class ScreeningDisplaySettings implements ISerializable
{
    private static final long serialVersionUID = 1L;

    private Map<String/* displayTypeID */, String/* channel name */> defaultChannels =
            new HashMap<String, String>();

    private String defaultAnalysisProcedure;

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
}
