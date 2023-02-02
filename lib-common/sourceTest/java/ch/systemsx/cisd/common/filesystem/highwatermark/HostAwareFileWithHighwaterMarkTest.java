/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem.highwatermark;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;

/**
 * Test cases for the {@link HostAwareFileWithHighwaterMark}.
 * 
 * @author Christian Ribeaud
 */
public final class HostAwareFileWithHighwaterMarkTest
{

    @Test
    public final void testFromProperties()
    {
        boolean fail = true;
        try
        {
            HostAwareFileWithHighwaterMark.fromProperties(null, "");
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final String filePropertyKey = "file";
        try
        {
            HostAwareFileWithHighwaterMark.fromProperties(new Properties(), filePropertyKey);
            fail("");
        } catch (final ConfigurationFailureException e)
        {
        }
        final Properties properties = new Properties();
        final String path = "/my/path";
        properties.setProperty(filePropertyKey, path);
        HostAwareFileWithHighwaterMark fileWithHighwaterMark =
                HostAwareFileWithHighwaterMark.fromProperties(properties, filePropertyKey);
        // Default value is -1
        assertEquals(new File(path), fileWithHighwaterMark.getLocalFile());
        assertEquals("/my/path", fileWithHighwaterMark.getPath());
        assertEquals("/my/path", fileWithHighwaterMark.getPathDescription());
        assertEquals(-1, fileWithHighwaterMark.getHighwaterMark());
        // 100Kb
        properties.setProperty(filePropertyKey + HostAwareFileWithHighwaterMark.SEP
                + HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, "100");
        fileWithHighwaterMark =
                HostAwareFileWithHighwaterMark.fromProperties(properties, filePropertyKey);
        assertEquals(new File(path), fileWithHighwaterMark.getLocalFile());
        assertEquals(100, fileWithHighwaterMark.getHighwaterMark());
        // Meaningless value
        properties.setProperty(filePropertyKey + HostAwareFileWithHighwaterMark.SEP
                + HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, "notANumber");
        fileWithHighwaterMark =
                HostAwareFileWithHighwaterMark.fromProperties(properties, filePropertyKey);
        assertEquals(new File(path), fileWithHighwaterMark.getLocalFile());
        assertEquals(-1, fileWithHighwaterMark.getHighwaterMark());
    }

    @Test
    public final void testFromPropertiesWithHost()
    {
        final Properties properties = new Properties();
        final String hostFilePath = "localhost:/my/path";
        final String key = "key";
        properties.setProperty(key, hostFilePath);
        properties.setProperty(key + HostAwareFileWithHighwaterMark.SEP
                + HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, "123");
        final HostAwareFileWithHighwaterMark fileWithHighwaterMark =
                HostAwareFileWithHighwaterMark.fromProperties(properties, key);
        assertEquals(123L, fileWithHighwaterMark.getHighwaterMark());
        assertEquals("localhost", fileWithHighwaterMark.tryGetHost());
        assertEquals("/my/path", fileWithHighwaterMark.getPath());
        assertNull(fileWithHighwaterMark.tryGetRsyncModule());
        assertEquals("localhost:/my/path", fileWithHighwaterMark.getPathDescription());
    }

    @Test
    public final void testWindowsDrive() throws IOException
    {
        final String hostFilePath = "c:\\my\\path";
        final HostAwareFileWithHighwaterMark file =
                HostAwareFileWithHighwaterMark.create(hostFilePath, 123);
        assertEquals(123L, file.getHighwaterMark());
        assertNull(file.tryGetHost());
        assertNull(file.tryGetRsyncModule());
        assertEquals(new File(hostFilePath).getCanonicalFile(), file.getLocalFile());
    }

    @Test
    public final void testFromPropertiesWithHostAndRsyncModule()
    {
        final Properties properties = new Properties();
        final String hostFilePath = "localhost:my_module:/my/path";
        final String key = "key";
        properties.setProperty(key, hostFilePath);
        properties.setProperty(key + HostAwareFileWithHighwaterMark.SEP
                + HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, "123");
        final HostAwareFileWithHighwaterMark fileWithHighwaterMark =
                HostAwareFileWithHighwaterMark.fromProperties(properties, key);
        assertEquals(123L, fileWithHighwaterMark.getHighwaterMark());
        assertEquals("localhost", fileWithHighwaterMark.tryGetHost());
        assertEquals("/my/path", fileWithHighwaterMark.getPath());
        assertEquals("my_module", fileWithHighwaterMark.tryGetRsyncModule());
    }
}