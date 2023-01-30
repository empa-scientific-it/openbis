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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.io.File;
import java.io.FileNotFoundException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

import static org.testng.AssertJUnit.*;

/**
 * Some basic test cases for {@link GlobalPropertiesLoader}.
 * 
 * @author Bernd Rinn
 */
public class GlobalPropertiesLoaderTest
{

    @Test
    public void testLoadGlobalPropertiesLoader() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#! GLOBAL_PROPERTIES_START\n#! a=A\n#!b = B\n#!  c= C");
        final GlobalProperties props = GlobalPropertiesLoader.load(f);
        assertEquals("A", props.get("a"));
        assertEquals("B", props.get("b"));
        assertEquals("C", props.get("c"));
        assertNull(props.tryGet("d"));
    }

    @Test
    public void testLoadGlobalPropertiesLoaderWithEnd() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#! GLOBAL_PROPERTIES_START  \n#! a=A\n#!b = B\n#!  c= C\n"
                + "#!GLOBAL_PROPERTIES_END\t\nUnrelated stuff\n#! d=D");
        final GlobalProperties props = GlobalPropertiesLoader.load(f);
        assertEquals("A", props.get("a"));
        assertEquals("B", props.get("b"));
        assertEquals("C", props.get("c"));
        assertNull(props.tryGet("d"));
    }

    @Test
    public void testLoadGlobalPropertiesLoaderIllegalLineFailed() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#! GLOBAL_PROPERTIES_START\n#! a=A\n#!b = B\n#!!!!");
        try
        {
            GlobalPropertiesLoader.load(f);
            fail("Didn't detect an illegal global property line.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Illegal global property line '#!!!!'", ex.getMessage());
        }
    }

    @Test
    public void testLoadGlobalPropertiesHeaderVariation1Loader() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#!GLOBAL_PROPERTIES_START\n#! a=A\n#!b = B\n#!  c= C");
        final GlobalProperties props = GlobalPropertiesLoader.load(f);
        assertEquals("A", props.get("a"));
        assertEquals("B", props.get("b"));
        assertEquals("C", props.get("c"));
        assertNull(props.tryGet("d"));
    }

    @Test
    public void testLoadGlobalPropertiesHeaderVariation2Loader() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#!\tGLOBAL_PROPERTIES_START\n#! a=A\n#!b = B\n#!  c= C");
        final GlobalProperties props = GlobalPropertiesLoader.load(f);
        assertEquals("A", props.get("a"));
        assertEquals("B", props.get("b"));
        assertEquals("C", props.get("c"));
        assertNull(props.tryGet("d"));
    }

    @Test
    public void testLoadGlobalPropertiesHeaderVariation3Loader() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#!  GLOBAL_PROPERTIES_START\n#! a=A\n#!b = B\n#!  c= C");
        final GlobalProperties props = GlobalPropertiesLoader.load(f);
        assertEquals("A", props.get("a"));
        assertEquals("B", props.get("b"));
        assertEquals("C", props.get("c"));
        assertNull(props.tryGet("d"));
    }

    @Test
    public void testLoadGlobalPropertiesDefineTwiceFailed() throws FileNotFoundException
    {
        final File f = new File("GlobalPropertiesLoaderTest.txt");
        f.deleteOnExit();
        FileUtilities.writeToFile(f, "#! GLOBAL_PROPERTIES_START\n#! a=A\n#!b = B\n#!  b= BB");
        try
        {
            GlobalPropertiesLoader.load(f);
            fail("Didn't detect that property 'b' was defined twice.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Property 'b' defined twice.", ex.getMessage());
        }
    }

}
