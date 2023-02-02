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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.base.io.IInputStream;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.concurrent.RecordingActivityObserverSensor;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.test.RetryTen;
import ch.systemsx.cisd.common.test.TestReportCleaner;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * Test cases for {@link FileOperations}.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = FileOperations.class)
@Listeners(TestReportCleaner.class)
public class FileOperationsTest extends AbstractFileSystemTestCase
{

    static class HangingIInputStream implements IInputStream
    {
        private final long hangMillis;

        HangingIInputStream(long hangMillis)
        {
            this.hangMillis = hangMillis;
        }

        private void hang()
        {
            ConcurrencyUtilities.sleep(hangMillis);
        }

        @Override
        public int available() throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        @Override
        public void close() throws IOExceptionUnchecked
        {
            hang();
        }

        @Override
        public void mark(int readlimit)
        {
            hang();
        }

        @Override
        public boolean markSupported()
        {
            hang();
            return false;
        }

        @Override
        public int read() throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        @Override
        public int read(byte[] b) throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

        @Override
        public void reset() throws IOExceptionUnchecked
        {
            hang();
        }

        @Override
        public long skip(long n) throws IOExceptionUnchecked
        {
            hang();
            return 0;
        }

    }

    static class HangingFileOperations extends FileOperations
    {

        private static final long serialVersionUID = 1L;

        private final long hangMillis;

        HangingFileOperations(TimingParameters parameters, IActivityObserver observer,
                long hangMillis)
        {
            super(parameters, observer);
            this.hangMillis = hangMillis;
        }

        @Override
        IInputStream internalGetIInputStream(File file) throws FileNotFoundException
        {
            return new HangingIInputStream(hangMillis);
        }

    }

    private static IFileOperations create(TimingParameters parameters, long hangMillis)
    {
        final RecordingActivityObserverSensor observerSensor =
                new RecordingActivityObserverSensor();
        return MonitoringProxy.create(IFileOperations.class,
                new HangingFileOperations(parameters, observerSensor, hangMillis)).timing(
                parameters).sensor(observerSensor).errorLog(
                new Log4jSimpleLogger(FileOperations.operationLog)).name("remote file operations")
                .get();
    }

    @Test(groups = "slow", expectedExceptions = TimeoutExceptionUnchecked.class)
    public void testNoTimeoutOnInputStream() throws IOException
    {
        final IFileOperations ops = create(TimingParameters.createNoRetries(50L), 100L);
        final InputStream is = ops.getInputStream(new File("bla.txt"));
        is.read(); // times out
    }

    @Test(groups = "slow", retryAnalyzer = RetryTen.class)
    public void testTimeoutOnInputStream() throws IOException
    {
        final IFileOperations ops = create(TimingParameters.createNoRetries(50L), 20L);
        final InputStream is = ops.getInputStream(new File("bla.txt"));
        is.read(); // doesn't time out
    }

    @Test
    // copies a source directory to a destination directory with a new name
    public final void testCopyToDirectoryAsDir() throws IOException
    {
        File srcDir = new File(workingDirectory, "srcDir");
        File destDir = new File(workingDirectory, "destDir");
        // create src dir substructure
        File srcSubDir = new File(srcDir, "a/b");
        srcSubDir.mkdirs();
        new File(srcSubDir, "data.txt").createNewFile();

        String newName = "srcDirNew";
        FileOperations.getInstance().copyToDirectoryAs(srcDir, destDir, newName);
        File newDestFile = new File(destDir, newName);
        AssertJUnit.assertTrue(newDestFile.isDirectory());
        AssertJUnit.assertTrue(new File(newDestFile, "a/b/data.txt").isFile());

        // clean up
        FileUtils.deleteDirectory(srcDir);
        FileUtils.deleteDirectory(destDir);
    }

    @Test
    // copies a source file to a destination directory with a new name
    public final void testCopyToDirectoryAsFile() throws IOException
    {
        File srcFile = new File(workingDirectory, "data.txt");
        srcFile.createNewFile();

        String newName = "newFileName";
        FileOperations.getInstance().copyToDirectoryAs(srcFile, workingDirectory, newName);
        File newDestFile = new File(workingDirectory, newName);
        AssertJUnit.assertTrue(newDestFile.isFile());

        // clean up
        srcFile.delete();
        newDestFile.delete();
    }

    @Test
    public final void testCopyToNonexistingParentFolder() throws IOException
    {
        File srcDir = new File(workingDirectory, "srcDir");
        srcDir.mkdirs();
        AssertJUnit.assertTrue(srcDir.isDirectory());

        File destDir = new File(new File(workingDirectory, "non-existing"), srcDir.getName());

        FileOperations.getInstance().move(srcDir, destDir);
        AssertJUnit.assertFalse(srcDir.isDirectory());
        AssertJUnit.assertTrue(destDir.isDirectory());

        // clean up
        destDir.delete();
    }
}
