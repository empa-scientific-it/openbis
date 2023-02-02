/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class AnalysisSettingsTest extends AssertJUnit
{
    @Test
    public void testNoSettings()
    {
        AnalysisSettings analysisSettings = new AnalysisSettings(new Properties());

        assertEquals(true, analysisSettings.noAnalysisSettings());
    }

    @Test
    public void testInvalidSettings()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "TYPE1");
        try
        {
            new AnalysisSettings(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Invalid property '" + AnalysisSettings.KEY + "': missing ':' in 'TYPE1'.",
                    ex.getMessage());
        }
    }

    @Test
    public void testSettings()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY, "TYPE1:viewer1, TYPE2:viewer2");
        AnalysisSettings analysisSettings = new AnalysisSettings(properties);

        assertEquals("viewer1", analysisSettings.tryToGetReportingPluginKey(new DataSetBuilder().type("TYPE1").getDataSet()));
        assertEquals("viewer2", analysisSettings.tryToGetReportingPluginKey(new DataSetBuilder().type("TYPE2").getDataSet()));
        assertEquals(null, analysisSettings.tryToGetReportingPluginKey(new DataSetBuilder().type("TYPE3").getDataSet()));
        assertEquals(false, analysisSettings.noAnalysisSettings());
    }

}
