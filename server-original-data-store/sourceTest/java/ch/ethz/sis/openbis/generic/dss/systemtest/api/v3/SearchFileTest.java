package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;

public class SearchFileTest extends AbstractFileTest
{

    @Override
    @BeforeClass
    protected void beforeClass() throws Exception
    {
        super.beforeClass();
        registerDataSet();
    }

    @Test
    public void allFilesOfGivenDatasetsAreReturned() throws Exception
    {
        DataSetFileSearchCriteria sc = new DataSetFileSearchCriteria();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);

        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);
        SearchResult<DataSetFile> searchResult = dss.searchFiles(sessionToken, sc, new DataSetFileFetchOptions());
        List<DataSetFile> searchFiles = searchResult.getObjects();

        assertThat(searchFiles, containsAll(filesAndDirectories));
        /* directory structure [dataset id]/original/[root folder] */
        assertThat(searchFiles.size(), is(filesAndDirectories.size() + 3));

        for (DataSetFile dataSetFile : searchFiles)
        {
            if (dataSetFile.isDirectory() == false)
            {
                String relativePath = dataSetFile.getPath().substring(("original/" + dataSetCode + "/").length());
                assertEquals(createRandomContent(relativePath).length(), dataSetFile.getFileLength());
                System.out.println(dataSetFile + ":" + dataSetFile.getChecksumCRC32());
            }
        }
    }

    @Test
    public void withAndOperator() throws Exception
    {
        // Given
        DataSetFileSearchCriteria sc = new DataSetFileSearchCriteria();
        sc.withAndOperator();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);
        sc.withDataSet().withPermId().thatEquals("blabla");
        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        // When
        List<DataSetFile> searchFiles = dss.searchFiles(sessionToken, sc, new DataSetFileFetchOptions()).getObjects();

        // Then
        assertEquals(0, searchFiles.size());
    }

    @Test
    public void withOrOperator() throws Exception
    {
        // Given
        DataSetFileSearchCriteria sc = new DataSetFileSearchCriteria();
        sc.withOrOperator();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);
        sc.withDataSet().withPermId().thatEquals("blabla");
        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        // When
        List<DataSetFile> searchFiles = dss.searchFiles(sessionToken, sc, new DataSetFileFetchOptions()).getObjects();

        // Then
        assertEquals(12, searchFiles.size());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = gis.tryToAuthenticateForAllServices(TEST_USER, PASSWORD);

        DataSetFileSearchCriteria c = new DataSetFileSearchCriteria();
        c.withDataSet().withPermId().thatEquals(dataSetCode);

        DataSetFileFetchOptions fo = new DataSetFileFetchOptions();

        dss.searchFiles(sessionToken, c, fo);

        assertAccessLog(
                "search-files  SEARCH_CRITERIA:\n'FILE\n    with dataset:\n        with attribute 'perm id' equal to '" + dataSetCode
                        + "'\n'\nFETCH_OPTIONS:\n'DataSetFile\n'");
    }

}