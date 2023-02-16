/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import java.io.IOException;
import java.util.Arrays;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.io.CollectionIO;

/**
 * Create a large file/folder structure that will take some time to copy.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TestBigStructureCreator
{
    private static final String START_DELETING_MSG = "Start deleting";

    private final File root;

    private final int[] numberOfFoldersPerLevel;

    private final int[] numberOfFilesPerFolder;

    /**
     * Create a larger structure that will take a few seconds to copy.
     */
    public static File createBigStructure(File directory, String name) throws IOException
    {
        final File root = new File(directory, name);
        TestBigStructureCreator creator = new TestBigStructureCreator(root);
        return creator.createBigStructure();
    }

    public TestBigStructureCreator(File root)
    {
        this(root, new int[]
        { 100, 10 }, new int[]
        { 1, 10, 10 });
    }

    public TestBigStructureCreator(File root, int[] numberOfFoldersPerLevel,
            int[] numberOfFilesPerFolder)
    {
        this.root = root;
        this.root.mkdir();
        assert numberOfFilesPerFolder.length == numberOfFoldersPerLevel.length + 1;
        this.numberOfFoldersPerLevel = numberOfFoldersPerLevel;
        this.numberOfFilesPerFolder = numberOfFilesPerFolder;
    }

    /**
     * Create a structure.
     */
    public File createBigStructure() throws IOException
    {
        return createStructure(root, 0);
    }

    /**
     * Delete the structure asynchronously.
     */
    public void deleteBigStructureAsync()
    {
        deleteBigStructureAsync(null);
    }

    /**
     * Delete the structure asynchronously.
     */
    public void deleteBigStructureAsync(MessageChannel channel)
    {
        Runnable deleter = new Runnable()
            {
                @Override
                public void run()
                {
                    if (channel == null)
                    {
                        try
                        {
                            Thread.sleep(100);
                        } catch (InterruptedException e)
                        {
                        }
                    } else
                    {
                        channel.send(START_DELETING_MSG);
                    }
                    FileUtilities.deleteRecursively(root);
                }
            };

        Thread deleterThread = new Thread(deleter);
        deleterThread.start();
    }

    public void waitUntilDeletionStarted(MessageChannel channel)
    {
        channel.assertNextMessage(START_DELETING_MSG);
    }

    /**
     * Verify that the structure is complete.
     */
    public boolean verifyStructure()
    {
        return verifyStructure(root, 0);
    }

    private File createStructure(File localRoot, int depth) throws IOException
    {
        final int maxDepth = numberOfFoldersPerLevel.length;
        for (int i = 0; i < numberOfFilesPerFolder[depth]; ++i)
        {
            File file = new File(localRoot, "File-" + i);
            file.createNewFile();
            CollectionIO.writeIterable(file,
                    Arrays.asList("test line 1", "test line 2", "test line 3"));
        }
        if (maxDepth == depth)
        {
            return localRoot;
        }

        for (int i = 0; i < numberOfFoldersPerLevel[depth]; ++i)
        {
            File folder = new File(localRoot, "Folder-" + i);
            folder.mkdir();
            createStructure(folder, depth + 1);
        }

        return localRoot;
    }

    private boolean verifyStructure(File localRoot, int depth)
    {
        final int maxDepth = numberOfFoldersPerLevel.length;
        for (int i = 0; i < numberOfFilesPerFolder[depth]; ++i)
        {
            File file = new File(localRoot, "File-" + i);
            if (false == file.exists())
            {
                return false;
            }
        }
        if (maxDepth == depth)
        {
            return true;
        }

        for (int i = 0; i < numberOfFoldersPerLevel[depth]; ++i)
        {
            File folder = new File(localRoot, "Folder-" + i);
            if (false == folder.exists())
            {
                return false;
            }
            if (false == verifyStructure(folder, depth + 1))
            {
                return false;
            }
        }

        return true;
    }
}
