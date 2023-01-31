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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Piotr Buczek
 */
public class FolderFileSizesReportGeneratorTest extends AssertJUnit
{
    private static String PATH_1 = "a/b/c/path1";

    private static String PATH_2 = "a/b/c/path2";

    private static String PATH_3 = "b/c/d/path3";

    private static String PATH_4 = "b/c/d/path4";

    private static Long SIZE_1 = 10L;

    private static Long SIZE_2 = 20L;

    private static Long SIZE_3 = 30L;

    private static Long SIZE_4 = 40L;

    Map<String, Long> storeFileSizesByPaths;

    Map<String, Long> destinationFileSizesByPaths;

    private String createReport()
    {
        return LocalDataSetFileOperationsExcecutor.FolderFileSizesReportGenerator
                .findInconsistencies(storeFileSizesByPaths, destinationFileSizesByPaths);
    }

    private void addToStore(String path, Long size)
    {
        storeFileSizesByPaths.put(path, size);
    }

    private void addToDestination(String path, Long size)
    {
        destinationFileSizesByPaths.put(path, size);
    }

    private static void assertSuccessful(String report)
    {
        assertEquals("", report);
    }

    private static String missingInDestinationMsg(String path)
    {
        return "'" + path + "' - exists in store but is missing in destination";
    }

    private static String missingInStoreMsg(String path)
    {
        return "'" + path + "' - exists in destination but is missing in store";
    }

    private static String differentSizesMsg(String path, Long sizeInStore, Long sizeInDestination)
    {
        return "'" + path + "' - different file sizes; store: " + sizeInStore + ", destination: "
                + sizeInDestination;
    }

    @BeforeMethod
    public void beforeMethod() throws IOException
    {
        storeFileSizesByPaths = new TreeMap<String, Long>();
        destinationFileSizesByPaths = new TreeMap<String, Long>();
    }

    @Test
    public void testSuccessfulWithEmptyFolders()
    {
        String report = createReport();
        assertSuccessful(report);
    }

    @Test
    public void testSuccessfulWithSingleFile()
    {
        addToStore(PATH_1, SIZE_1);
        addToDestination(PATH_1, SIZE_1);

        String report = createReport();
        assertSuccessful(report);
    }

    @Test
    public void testSuccessfulWithManyFiles()
    {
        addToStore(PATH_1, SIZE_1);
        addToStore(PATH_2, SIZE_2);
        addToStore(PATH_3, SIZE_3);
        addToDestination(PATH_1, SIZE_1);
        addToDestination(PATH_2, SIZE_2);
        addToDestination(PATH_3, SIZE_3);

        String report = createReport();
        assertSuccessful(report);
    }

    @Test
    public void testMissingFileInDestination()
    {
        addToStore(PATH_1, SIZE_1);

        String report = createReport();
        String[] inconsistencies = report.split("\n");
        assertEquals(1, inconsistencies.length);
        assertEquals(missingInDestinationMsg(PATH_1), inconsistencies[0]);
    }

    @Test
    public void testMissingFileInStore()
    {
        addToDestination(PATH_1, SIZE_1);

        String report = createReport();
        String[] inconsistencies = report.split("\n");
        assertEquals(1, inconsistencies.length);
        assertEquals(missingInStoreMsg(PATH_1), inconsistencies[0]);
    }

    @Test
    public void testMissingFiles()
    {
        addToStore(PATH_2, SIZE_2);
        addToStore(PATH_1, SIZE_1);
        addToDestination(PATH_4, SIZE_4);
        addToDestination(PATH_3, SIZE_3);

        String report = createReport();
        String[] inconsistencies = report.split("\n");
        assertEquals(4, inconsistencies.length);
        assertEquals(missingInDestinationMsg(PATH_1), inconsistencies[0]);
        assertEquals(missingInDestinationMsg(PATH_2), inconsistencies[1]);
        assertEquals(missingInStoreMsg(PATH_3), inconsistencies[2]);
        assertEquals(missingInStoreMsg(PATH_4), inconsistencies[3]);
    }

    @Test
    public void testDifferentFileSizes()
    {
        addToStore(PATH_1, SIZE_1);
        addToStore(PATH_2, SIZE_2);
        addToDestination(PATH_1, SIZE_1 + 1);
        addToDestination(PATH_2, SIZE_2 - 1);

        String report = createReport();
        String[] inconsistencies = report.split("\n");
        assertEquals(2, inconsistencies.length);
        assertEquals(differentSizesMsg(PATH_1, SIZE_1, SIZE_1 + 1), inconsistencies[0]);
        assertEquals(differentSizesMsg(PATH_2, SIZE_2, SIZE_2 - 1), inconsistencies[1]);
    }

    @Test
    public void testComplexCase()
    {
        // path 1 missing in destination
        addToStore(PATH_1, SIZE_1);
        // path 2 missing with different sizes
        addToStore(PATH_2, SIZE_2);
        addToDestination(PATH_2, SIZE_2 + 1);
        // path 3 missing in store
        addToDestination(PATH_3, SIZE_2);
        // path 4 is ok
        addToStore(PATH_4, SIZE_4);
        addToDestination(PATH_4, SIZE_4);

        String report = createReport();
        String[] inconsistencies = report.split("\n");
        assertEquals(3, inconsistencies.length);
        assertEquals(missingInDestinationMsg(PATH_1), inconsistencies[0]);
        assertEquals(differentSizesMsg(PATH_2, SIZE_2, SIZE_2 + 1), inconsistencies[1]);
        assertEquals(missingInStoreMsg(PATH_3), inconsistencies[2]);
    }
}
