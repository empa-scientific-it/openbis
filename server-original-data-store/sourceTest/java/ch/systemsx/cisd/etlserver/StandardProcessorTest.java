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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.IOException;

import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.PathPrefixPrepender;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Test cases for the {@link StandardProcessor}.
 * 
 * @author Christian Ribeaud
 */
public final class StandardProcessorTest extends AbstractFileSystemTestCase
{
    private static final String PREFIX_FOR_RELATIVE_PATH = "rel";

    private static final String PREFIX_FOR_ABSOLUTE_PATH = null;

    private static final String FINISHED_FILE_NAME_TEMPLATE = ".MARKER_is_finished_{0}";

    private static final String PARAMETERS_FILE_NAME = "parameters";

    private IProcessor processor;

    private Mockery context;

    private IFileFactory fileFactory;

    private PathPrefixPrepender pathPrefixPrepender;

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        fileFactory = context.mock(IFileFactory.class);
        pathPrefixPrepender = createPathPrefixPrepender();
        processor = createStandardProcessor();
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final PathPrefixPrepender createPathPrefixPrepender()
    {
        final File file = new File(workingDirectory, PREFIX_FOR_RELATIVE_PATH);
        assertEquals(true, file.mkdir());
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
        return new PathPrefixPrepender(PREFIX_FOR_ABSOLUTE_PATH, file.getAbsolutePath());
    }

    private final IProcessor createStandardProcessor()
    {
        return new StandardProcessor(fileFactory, StorageFormat.PROPRIETARY, pathPrefixPrepender,
                PARAMETERS_FILE_NAME, FINISHED_FILE_NAME_TEMPLATE, "_");
    }

    @Test
    public final void testInitiateProcessingWithNullParameters()
    {
        try
        {
            processor.initiateProcessing(null, null, null);
            fail("Null parameters not allowed here.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

}