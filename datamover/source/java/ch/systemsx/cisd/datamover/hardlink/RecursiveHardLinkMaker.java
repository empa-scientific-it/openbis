/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.hardlink;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.IPathImmutableCopier;
import ch.systemsx.cisd.datamover.helper.CmdLineHelper;

/**
 * Utility to create a hard link of a file or copy recursively a directories structure, creating a hard link for each
 * file inside. Note that presence of <code>ln</code> executable is required, which is not available under Windows.
 * 
 * @author Tomasz Pylak
 */
public class RecursiveHardLinkMaker implements IPathImmutableCopier
{
    private static final String HARD_LINK_EXEC = "ln";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RecursiveHardLinkMaker.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, RecursiveHardLinkMaker.class);

    private final String linkExecPath;

    private RecursiveHardLinkMaker(String linkExecPath)
    {
        this.linkExecPath = linkExecPath;
    }

    public static final IPathImmutableCopier create(String linkExecPath)
    {
        return new RecursiveHardLinkMaker(linkExecPath);
    }

    public static final IPathImmutableCopier tryCreate()
    {
        File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
        if (lnExec == null)
            return null;
        return new RecursiveHardLinkMaker(lnExec.getAbsolutePath());
    }

    /**
     * Copies resource (file or directory) to <code>destinationDirectory</code> by duplicating directory structure and
     * creating hard link for each file. Note that <code>resource</code> cannot be placed directly inside
     * <code>destinationDirectory</code>.
     * 
     * @return pointer to the copied resource or null if copy process failed
     */
    public File tryCopy(File resource, File destinationDirectory)
    {
        operationLog.info(String.format("Creating a hard link copy of '%s' in '%s'.", resource.getPath(),
                destinationDirectory.getPath()));
        String resourceParent = resource.getParentFile().getAbsolutePath();
        assert !resourceParent.equals(destinationDirectory.getAbsolutePath());
        return tryMakeCopy(resource, destinationDirectory);
    }

    private File tryMakeCopy(File resource, File destinationDirectory)
    {
        if (resource.isFile())
        {
            return tryCreateHardLinkIn(resource, destinationDirectory);
        } else
        {
            File dir = tryCreateDir(resource.getName(), destinationDirectory);
            if (dir == null)
                return null;
            for (File file : resource.listFiles())
            {
                if (tryMakeCopy(file, dir) == null)
                    return null;
            }
            return dir;
        }
    }

    private static File tryCreateDir(String name, File destDir)
    {
        File dir = new File(destDir, name);
        boolean ok = dir.mkdir();
        if (!ok)
        {
            if (dir.isDirectory())
            {
                machineLog.error(String.format("Directory %s already exists in %s", name, destDir.getAbsolutePath()));
                ok = true;
            } else
            {
                machineLog.error(String.format("Could not create directory %s inside %s.", name, destDir
                        .getAbsolutePath()));
                if (dir.isFile())
                {
                    machineLog.error("There is a file with a same name.");
                }
            }
        }
        return ok ? dir : null;
    }

    private File tryCreateHardLinkIn(File file, File destDir)
    {
        assert file.isFile();
        File destFile = new File(destDir, file.getName());
        List<String> cmd = createLnCmdLine(file, destFile);
        boolean ok = CmdLineHelper.run(cmd, operationLog, machineLog);
        return ok ? destFile : null;
    }

    private List<String> createLnCmdLine(File srcFile, File destFile)
    {
        List<String> tokens = new ArrayList<String>();
        tokens.add(linkExecPath);
        tokens.add(srcFile.getAbsolutePath());
        tokens.add(destFile.getAbsolutePath());
        return tokens;
    }
}
