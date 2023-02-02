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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.PathPrefixPrepender;

/**
 * Test cases for {@link PathPrefixPrepender}.
 * 
 * @author Christian Ribeaud
 */
public final class PathPrefixPrependerTest extends AbstractFileSystemTestCase
{

    private String prefixForAbsolutePathsOrNull;

    private String prefixForRelativePathsOrNull;

    private final File createDirectory(final String name)
    {
        final File dir = new File(workingDirectory, name);
        assert dir.mkdir();
        assertEquals(true, dir.isDirectory());
        return dir;
    }

    private final PathPrefixPrepender createPrepender()
    {
        final String absolute = "absolute";
        prefixForAbsolutePathsOrNull = workingDirectory.getAbsolutePath() + "/" + absolute;
        final String relative = "relative";
        prefixForRelativePathsOrNull = workingDirectory.getAbsolutePath() + "/" + relative;
        createDirectory(absolute);
        createDirectory(relative);
        return new PathPrefixPrepender(prefixForAbsolutePathsOrNull, prefixForRelativePathsOrNull);
    }

    @Test
    public final void testConstructor()
    {
        try
        {
            new PathPrefixPrepender((workingDirectory.getAbsolutePath() + "/absolute"),
                    workingDirectory.getAbsolutePath() + "/relative");
            fail("Given prefixes are not valid.");
        } catch (final ConfigurationFailureException ex)
        {
            // Nothing to do here.
        }
    }

    @DataProvider(name = "pathProvider")
    public Object[][] getPaths()
    {
        return new Object[][]
        {
                { "" },
                { "choubidou" },
                { "/choubidou" },
                { "/" } };
    }

    @Test(dataProvider = "pathProvider")
    public final void testWithNullPrefixes(final String path)
    {
        final PathPrefixPrepender prepender = new PathPrefixPrepender(null, null);
        assertEquals(path, prepender.addPrefixTo(path));
    }

    @Test
    public final void testAddPrefixTo()
    {
        final PathPrefixPrepender prepender = createPrepender();
        try
        {
            prepender.addPrefixTo(null);
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
        assertEquals(prefixForRelativePathsOrNull + "/", prepender.addPrefixTo(""));
        assertEquals(prefixForRelativePathsOrNull + "/choubidou", prepender
                .addPrefixTo("choubidou"));
        assertEquals(prefixForAbsolutePathsOrNull + "/choubidou", prepender
                .addPrefixTo("/choubidou"));
        assertEquals(prefixForAbsolutePathsOrNull + "/", prepender.addPrefixTo("/"));
    }
}