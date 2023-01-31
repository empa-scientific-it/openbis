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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IDataSetPathsInfoFeeder;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;

/**
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathsInfoFeederTest extends AbstractFileSystemTestCase
{
    private static final String ROOT_PATH = DatabaseBasedDataSetPathsInfoFeederTest.class.getName();

    private Mockery context;

    private IPathsInfoDAO dao;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(IPathsInfoDAO.class);
    }

    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testWithChecksum()
    {
        final File dir = new File(workingDirectory, "dir");
        dir.mkdirs();
        final File file1 = new File(dir, "hello.txt");
        FileUtilities.writeToFile(file1, "hello world");
        final File file2 = new File(dir, "read.me");
        FileUtilities.writeToFile(file2, "nothing to read");
        new File(dir, "dir").mkdirs();
        final File file3 = new File(workingDirectory, "read.me");
        FileUtilities.writeToFile(file3, "hello reader");
        context.checking(new Expectations()
            {
                {
                    one(dao).createDataSet("ds-1", "a/b/c/");
                    will(returnValue(42L));

                    one(dao).createDataSetFile(with(42L), with(new IsNull<Long>()), with(""),
                            with(ROOT_PATH), with(38L), with(true), 
                            with(new IsNull<Integer>()), with(new IsNull<String>()), with(any(Date.class)));
                    will(returnValue(100L));

                    one(dao).createDataSetFile(with(42L), with(100L), with("dir"), with("dir"),
                            with(26L), with(true), with(new IsNull<Integer>()), with(new IsNull<String>()), 
                            with(any(Date.class)));
                    will(returnValue(101L));

                    one(dao).createDataSetFile(with(42L), with(101L), with("dir/dir"), with("dir"),
                            with(0L), with(true), with(new IsNull<Integer>()), with(new IsNull<String>()), 
                            with(any(Date.class)));
                    will(returnValue(104L));

                    one(dao).createDataSetFiles(
                            with(equal(Arrays.asList(
                                    new PathEntryDTO(42L, 101L, "dir/hello.txt", "hello.txt", 11L,
                                            222957957, null, false, new Date(file1.lastModified())),
                                    new PathEntryDTO(42L, 101L, "dir/read.me", "read.me", 15L,
                                            1246790599, null, false, new Date(file2.lastModified())),
                                    new PathEntryDTO(42L, 100L, "read.me", "read.me", 12L,
                                            (int) 3188708281L, null, false,
                                            new Date(file2.lastModified()))))));

                    one(dao).commit();
                }
            });

        IDataSetPathsInfoFeeder feeder =
                new DatabaseBasedDataSetPathsInfoFeeder(dao,
                        new DefaultFileBasedHierarchicalContentFactory(), true, null);
        feeder.addPaths("ds-1", "a/b/c/", workingDirectory);
        feeder.commit();
    }

    @Test
    public void testWithOutChecksum()
    {
        final File dir = new File(workingDirectory, "dir");
        dir.mkdirs();
        final File file1 = new File(dir, "hello.txt");
        FileUtilities.writeToFile(file1, "hello world");
        final File file2 = new File(dir, "read.me");
        FileUtilities.writeToFile(file2, "nothing to read");
        new File(dir, "dir").mkdirs();
        final File file3 = new File(workingDirectory, "read.me");
        FileUtilities.writeToFile(file3, "hello reader");
        context.checking(new Expectations()
            {
                {
                    one(dao).createDataSet("ds-1", "a/b/c/");
                    will(returnValue(42L));

                    one(dao).createDataSetFile(with(42L), with(new IsNull<Long>()), with(""),
                            with(ROOT_PATH), with(38L), with(true), 
                            with(new IsNull<Integer>()), with(new IsNull<String>()), with(any(Date.class)));
                    will(returnValue(100L));

                    one(dao).createDataSetFile(with(42L), with(100L), with("dir"), with("dir"),
                            with(26L), with(true), with(new IsNull<Integer>()), with(new IsNull<String>()), 
                            with(any(Date.class)));
                    will(returnValue(101L));

                    one(dao).createDataSetFile(with(42L), with(101L), with("dir/dir"), with("dir"),
                            with(0L), with(true), with(new IsNull<Integer>()), with(new IsNull<String>()), 
                            with(any(Date.class)));
                    will(returnValue(104L));

                    one(dao).createDataSetFiles(
                            with(equal(Arrays.asList(new PathEntryDTO(42L, 101L, "dir/hello.txt",
                                    "hello.txt", 11L, null, null, false, new Date(file1.lastModified())),
                                    new PathEntryDTO(42L, 101L, "dir/read.me", "read.me", 15L,
                                            null, null, false, new Date(file2.lastModified())),
                                    new PathEntryDTO(42L, 100L, "read.me", "read.me", 12L, null,
                                            null, false, new Date(file2.lastModified()))))));

                    one(dao).commit();
                }
            });

        IDataSetPathsInfoFeeder feeder =
                new DatabaseBasedDataSetPathsInfoFeeder(dao,
                        new DefaultFileBasedHierarchicalContentFactory(), false, null);
        feeder.addPaths("ds-1", "a/b/c/", workingDirectory);
        feeder.commit();
    }
}
