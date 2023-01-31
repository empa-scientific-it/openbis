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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.etlserver.registrator.api.impl.MkdirsCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.MoveFileCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.NewFileCommand;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FileCommandsTest extends AbstractTestWithRollbackStack
{
    private File srcFile;

    private File dstDir;

    private MkdirsCommand mkdirsCmd;

    private File dstFile;

    private MoveFileCommand mvOldFile;

    private File newFile;

    private NewFileCommand newFileCmd;

    private File newNewFile;

    private MoveFileCommand mvNewFile;

    private BufferedAppender logAppender;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        srcFile = new File(workingDirectory, "read.me");
        fillContentsOfSource();

        dstDir = new File(workingDirectory, "bar-dir/");
        mkdirsCmd = new MkdirsCommand(dstDir.getAbsolutePath());

        dstFile = new File(dstDir, srcFile.getName());
        mvOldFile =
                new MoveFileCommand(workingDirectory.getAbsolutePath(), srcFile.getName(),
                        dstDir.getAbsolutePath(), srcFile.getName());

        newFile = new File(dstDir, "new-file.txt");
        newFileCmd = new NewFileCommand(newFile.getAbsolutePath());

        newNewFile = new File(dstDir, "new-new-file.txt");
        mvNewFile =
                new MoveFileCommand(dstDir.getAbsolutePath(), newFile.getName(),
                        dstDir.getAbsolutePath(), newNewFile.getName());

        logAppender = LogRecordingUtils.createRecorder();
    }

    @Test
    public void testFileCommands()
    {
        rollbackStack.pushAndExecuteCommand(mkdirsCmd);
        rollbackStack.pushAndExecuteCommand(mvOldFile);
        rollbackStack.pushAndExecuteCommand(newFileCmd);
        fillContentsOfFile(newFile, "This is a new file.");
        rollbackStack.pushAndExecuteCommand(mvNewFile);

        checkContentsOfFile(dstFile);
        checkContentsOfFile(newNewFile, "This is a new file.");
        assertFalse("The new file should have been moved", newFile.exists());
    }

    @Test
    public void testRollbackFileCommands()
    {
        rollbackStack.pushAndExecuteCommand(mkdirsCmd);
        rollbackStack.pushAndExecuteCommand(mvOldFile);
        rollbackStack.pushAndExecuteCommand(newFileCmd);
        fillContentsOfFile(newFile, "This is a new file.");
        rollbackStack.pushAndExecuteCommand(mvNewFile);

        checkContentsOfFile(dstFile);
        checkContentsOfFile(newNewFile, "This is a new file.");
        assertFalse("The new file should have been moved", newFile.exists());

        rollbackStack.rollbackAll();

        assertTrue("The file that we created and moved have been removed",
                false == newNewFile.exists());
        assertTrue("The file that we created should have been removed", false == newFile.exists());
        assertTrue("The file should have been deleted", false == dstFile.exists());
        assertTrue("The directory should have been deleted", false == dstDir.exists());
        checkContentsOfFile(srcFile);

        assertTrue(logAppender.getLogContent().length() > 0);
    }

    @Test
    public void testDoubleRollbackFileCommands()
    {
        rollbackStack.pushAndExecuteCommand(mkdirsCmd);
        rollbackStack.pushAndExecuteCommand(mvOldFile);
        rollbackStack.pushAndExecuteCommand(newFileCmd);
        fillContentsOfFile(newFile, "This is a new file.");
        rollbackStack.pushAndExecuteCommand(mvNewFile);

        checkContentsOfFile(dstFile);
        checkContentsOfFile(newNewFile, "This is a new file.");
        assertFalse("The new file should have been moved", newFile.exists());

        rollbackStack.rollbackAll();

        mvNewFile.rollback();
        newFileCmd.rollback();
        mvOldFile.rollback();
        mkdirsCmd.rollback();

        assertTrue("The file that we created and moved have been removed",
                false == newNewFile.exists());
        assertTrue("The file that we created should have been removed", false == newFile.exists());
        assertTrue("The file should have been deleted", false == dstFile.exists());
        assertTrue("The directory should have been deleted", false == dstDir.exists());
        checkContentsOfFile(srcFile);
    }

    private void checkContentsOfFile(File dst)
    {
        checkContentsOfFile(dst, "hello world");
    }

    private void checkContentsOfFile(File dst, String contents)
    {
        assertTrue("The file should exist", dst.exists());
        assertEquals(contents + "\n", FileUtilities.loadToString(dst));
    }

    private void fillContentsOfSource()
    {
        fillContentsOfFile(srcFile, "hello world");
    }

    private void fillContentsOfFile(File aFile, String contents)
    {
        FileUtilities.writeToFile(aFile, contents);
    }

}
