/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.testng.annotations.BeforeClass;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

public class AbstractFileTest extends SystemTestCase
{

    public static final String TEST_USER = "test";

    public static final String TEST_SPACE_USER = "test_space";

    public static final String ETL_SERVER_USER = "etlserver";

    public static final String PASSWORD = "password";

    protected IGeneralInformationService gis;

    protected IApplicationServerApi as;

    protected IDataStoreServerApi dss;

    protected String dataSetCode;

    protected Set<String> directories;

    protected Set<String> files;

    protected Set<String> filesAndDirectories;

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-simple-dataset-test");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 280;
    }

    @BeforeClass
    protected void beforeClass() throws Exception
    {
        gis = ServiceProvider.getGeneralInformationService();
        as = ServiceProvider.getV3ApplicationService();
        dss = (IDataStoreServerApi) ServiceProvider.getDssServiceV3().getService();
    }

    protected String registerDataSet() throws Exception
    {
        dataSetCode = UUID.randomUUID().toString().toUpperCase();

        File dataSetDir = new File(workingDirectory, dataSetCode);

        directories = new HashSet<>(Arrays.asList("subdir1", "subdir1/subdir2", "subdir3"));
        createDirectories(dataSetDir, directories);

        files =
                new HashSet<>(Arrays.asList("file1.txt", "file2.txt", "subdir1/file3.txt", "subdir1/file4.txt", "subdir1/subdir2/file5.txt",
                        "subdir3/file6.txt"));
        createFiles(dataSetDir, files);

        filesAndDirectories = new HashSet<>();
        filesAndDirectories.addAll(files);
        filesAndDirectories.addAll(directories);

        moveFileToIncoming(dataSetDir);
        waitUntilDataSetImported();
        waitUntilIndexUpdaterIsIdle();
        waitUntilDataSetPostRegistrationCompleted(dataSetCode);

        return dataSetCode;
    }

    protected void createDirectories(File dataSetDir, @SuppressWarnings("hiding") Set<String> directories)
    {
        for (String dir : directories)
        {
            new File(dataSetDir, dir).mkdirs();
        }
    }

    protected void createFiles(File dataSetDir, Collection<String> paths)
    {
        for (String path : paths)
        {
            File f = new File(dataSetDir, path);
            String content = createRandomContent(path);
            FileUtilities.writeToFile(f, content);
        }
    }

    protected String createRandomContent(String path)
    {
        StringBuilder builder = new StringBuilder().append("file content of ").append(path).append("\n");
        for (int i = 0; i < 30000; i++)
        {
            builder.append(UUID.nameUUIDFromBytes((path+ " "+i).getBytes()).toString()).append("\n");
        }
        String content = builder.toString();
        return content;
    }

    public static Matcher<Collection<DataSetFile>> containsAll(Collection<String> files)
    {
        return new CollectionContainsFilesMatcher(files);
    }

    public static class CollectionContainsFilesMatcher extends TypeSafeMatcher<Collection<DataSetFile>>
    {

        private final Collection<String> files;

        public CollectionContainsFilesMatcher(Collection<String> files)
        {
            this.files = files;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("A collection containing at least these elements:");
            for (String file : files)
            {
                description.appendText(file + " ");
            }
        }

        @Override
        protected boolean matchesSafely(Collection<DataSetFile> elements)
        {
            for (String file : files)
            {
                if (fileInElements(file, elements) == false)
                {
                    return false;
                }
            }
            return true;
        }

        private boolean fileInElements(String file, Collection<DataSetFile> elements)
        {
            for (DataSetFile dsf : elements)
            {
                if (dsf.getPath().endsWith(file))
                {
                    return true;
                }
            }
            return false;
        }
    }

    protected void assertUserFailureException(IDelegatedAction action, String expectedMessage)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            assertEquals(UserFailureException.class, e.getClass());
            AssertionUtil.assertContains(expectedMessage, e.getMessage());
        }
    }
}