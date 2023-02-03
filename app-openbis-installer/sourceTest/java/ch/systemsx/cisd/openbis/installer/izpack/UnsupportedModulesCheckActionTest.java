/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.installer.izpack;

import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.ENABLED_TECHNOLOGIES_KEY;

import java.io.File;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.InstallData;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;

/**
 * @author Franz-Josef Elmer
 */
public class UnsupportedModulesCheckActionTest extends AbstractFileSystemTestCase
{
    private File corePluginsFolder;

    private File corePluginsProperties;

    private AbstractUIHandler handler;

    private Mockery context;

    private File previousInstallDir;

    @Override
    @BeforeMethod
    public void setUp()
    {
        corePluginsFolder = new File(workingDirectory, Utils.CORE_PLUGINS_PATH);
        corePluginsFolder.mkdirs();
        corePluginsProperties = new File(workingDirectory, Utils.CORE_PLUGINS_PROPERTIES_PATH);
        corePluginsProperties.getParentFile().mkdirs();
        context = new Mockery();
        handler = context.mock(AbstractUIHandler.class);
        previousInstallDir = GlobalInstallationContext.installDir;
        GlobalInstallationContext.installDir = workingDirectory;
    }

    @AfterMethod
    public void tearDown()
    {
        GlobalInstallationContext.installDir = previousInstallDir;
        FileUtilities.deleteRecursively(corePluginsFolder);
        corePluginsProperties.delete();
    }

    @Test
    public void testEnabledUnsupportedModules()
    {
        // Given
        FileUtilities.writeToFile(corePluginsProperties, ENABLED_TECHNOLOGIES_KEY + "= shared, scree.*, proteomics");
        UnsupportedModulesCheckAction action = new UnsupportedModulesCheckAction();
        Properties variables = new Properties();
        InstallData data = new InstallData(variables, new VariableSubstitutorImpl(new Properties()));
        RecordingMatcher<String> titleMatcher = new RecordingMatcher<String>();
        RecordingMatcher<String> contentMatcher = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    allowing(handler).emitErrorAndBlockNext(with(titleMatcher), with(contentMatcher));
                }
            });

        // When
        action.executeAction(data, handler);

        // Then
        assertEquals("Unsupported Module", titleMatcher.recordedObject());
        assertEquals("The following modules are no longer supported: [screening, proteomics]\n"
                + "They were deprecated and have been removed.\n"
                + "Please follow up evaluating if you need these modules and take an informed decision\n"
                + "before disabling them because you may loose access to data and functionality.",
                contentMatcher.recordedObject());
    }

    @Test
    public void testNotEnabledUnsupportedModules()
    {
        // Given
        FileUtilities.writeToFile(corePluginsProperties, ENABLED_TECHNOLOGIES_KEY + "= shared, microscopy");
        UnsupportedModulesCheckAction action = new UnsupportedModulesCheckAction();
        Properties variables = new Properties();
        InstallData data = new InstallData(variables, new VariableSubstitutorImpl(new Properties()));
        RecordingMatcher<String> titleMatcher = new RecordingMatcher<String>();
        RecordingMatcher<String> contentMatcher = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    allowing(handler).emitErrorAndBlockNext(with(titleMatcher), with(contentMatcher));
                }
            });

        // When
        action.executeAction(data, handler);

        // Then
        assertEquals("[]", titleMatcher.getRecordedObjects().toString());
        assertEquals("[]", contentMatcher.getRecordedObjects().toString());
    }

}
