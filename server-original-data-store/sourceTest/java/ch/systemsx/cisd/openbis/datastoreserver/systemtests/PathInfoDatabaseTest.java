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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang3.time.StopWatch;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "slow")
public class PathInfoDatabaseTest extends SystemTestCase
{

    @BeforeTest
    private void initDatabase() throws Exception
    {
        DataSource dataSource = PathInfoDataSourceProvider.getDataSource();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        cleanUpDatabase(dataSource);

        System.out.println(stopWatch.getTime() + " msec for cleaning up old entries");

        stopWatch.reset();
        stopWatch.start();

        int n = feedDataBase(dataSource);

        System.out.println(stopWatch.getTime() + " msec for creating " + n + " entries");
    }

    @Test
    public void testPathInfoDatabase() throws Exception
    {
        DataSource dataSource = PathInfoDataSourceProvider.getDataSource();
        StopWatch stopWatch = new StopWatch();
        List<String> paths = new ArrayList<String>();

        Connection connection = null;
        String regex = ".*file-[2-3].*81.*(25|49).*36.*";
        try
        {
            connection = dataSource.getConnection();
            stopWatch.start();
            PreparedStatement s =
                    connection.prepareStatement("select * from data_set_files "
                            + "where relative_path ~ ?");
            System.out.println(stopWatch.getTime() + " msec for preparing SQL statement "
                    + stopWatch);
            s.setString(1, regex);
            ResultSet rs = s.executeQuery();
            while (rs.next())
            {
                paths.add(rs.getString("relative_path"));
            }
            System.out.println(stopWatch.getTime() + " msec for PostgreSQL regex search");
            Collections.sort(paths);
            assertEquals("[file-2-4/file-9-81/file-5-25/file-6-36-xyz.xml, "
                    + "file-2-4/file-9-81/file-7-49/file-6-36-xyz.xml, "
                    + "file-3-9/file-9-81/file-5-25/file-6-36-xyz.xml, "
                    + "file-3-9/file-9-81/file-7-49/file-6-36-xyz.xml]", paths.toString());
        } finally
        {
            close(connection);
        }

        stopWatch.reset();
        stopWatch.start();
        List<DataSetPathInfo> results =
                ServiceProvider.getDataSetPathInfoProvider().listPathInfosByRegularExpression(
                        "ds-1", regex);
        System.out.println(stopWatch.getTime() + " msec for reading db with regex");
        paths.clear();
        for (DataSetPathInfo info : results)
        {
            paths.add(info.getRelativePath());
        }
        Collections.sort(paths);
        assertEquals("[file-2-4/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-2-4/file-9-81/file-7-49/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-7-49/file-6-36-xyz.xml]", paths.toString());

        stopWatch.reset();
        stopWatch.start();
        final Pattern pattern = Pattern.compile(regex);
        DataSetPathInfo root =
                ServiceProvider.getDataSetPathInfoProvider().tryGetFullDataSetRootPathInfo("ds-1");
        System.out.println(stopWatch.getTime() + " msec for reading db");
        results.clear();
        search(results, root, pattern);
        System.out.println(stopWatch.getTime() + " msec for reading db and searching");
        paths.clear();
        for (DataSetPathInfo info : results)
        {
            paths.add(info.getRelativePath());
        }
        Collections.sort(paths);
        assertEquals("[file-2-4/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-2-4/file-9-81/file-7-49/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-5-25/file-6-36-xyz.xml, "
                + "file-3-9/file-9-81/file-7-49/file-6-36-xyz.xml]", paths.toString());
    }

    @Test
    public void testListMatchingPathInfosWithRelativePathPatternWithExactMatch()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider
                .listMatchingPathInfos("file-9-81/file-9-81/file-9-81/file-9-81-xyz.xml");

        assertRelativePaths(actualFiles, "file-9-81/file-9-81/file-9-81/file-9-81-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithRelativePathPatternWithoutAnyMatch()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider
                .listMatchingPathInfos("file-9-81/file-9-81/file-9-81/file-9-81-xyz.xml.notexisting");

        assertRelativePaths(actualFiles, new String[] {});
    }

    @Test
    public void testListMatchingPathInfosWithRelativePathPatternWithSpecialCharactersConvertableToLike()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-2");

        List<DataSetPathInfo> actualFiles = provider
                .listMatchingPathInfos("special_characters_%\\?\\.file-9-81/.*file.*1.*xml");

        assertRelativePaths(actualFiles, "special_characters_%?.file-9-81/special_characters_%?.file-1-1-xyz.xml",
                "special_characters_%?.file-9-81/special_characters_%?.file-4-16-xyz.xml",
                "special_characters_%?.file-9-81/special_characters_%?.file-9-81-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithRelativePathPatternWithSpecialCharactersNotConvertableToLike()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-2");

        List<DataSetPathInfo> actualFiles = provider
                .listMatchingPathInfos("special_characters_%\\?\\.file-9-81/.*(file-0-0|file-2-4).*");

        assertRelativePaths(actualFiles, "special_characters_%?.file-9-81/special_characters_%?.file-0-0-xyz.xml",
                "special_characters_%?.file-9-81/special_characters_%?.file-2-4-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathAndFileNamePatternWithExactMatch()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "file-9-81/file-9-81/file-9-81", "file-9-81-xyz.xml");

        assertRelativePaths(actualFiles, "file-9-81/file-9-81/file-9-81/file-9-81-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithEmptyStartingPath()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-2");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos("", "special_characters_%\\?\\.file-8-64");

        assertRelativePaths(actualFiles, "special_characters_%?.file-8-64");
    }

    @Test
    public void testListMatchingPathInfosWithNullStartingPath()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-2");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(null, "special_characters_%\\?\\.file-8-64");

        assertRelativePaths(actualFiles, "special_characters_%?.file-8-64");
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathWithoutSlash()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "file-9-81/file-9-81/file-9-81", "file-9-81-xyz.xml");

        assertRelativePaths(actualFiles, "file-9-81/file-9-81/file-9-81/file-9-81-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathWithSlash()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "file-9-81/file-9-81/file-9-81/", "file-9-81-xyz.xml");

        assertRelativePaths(actualFiles, "file-9-81/file-9-81/file-9-81/file-9-81-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathAndFileNamePatternWithoutAnyMatch()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "file-9-81/file-9-81/file-9-81", "file-9-81-xyz.xml.notexisting");

        assertRelativePaths(actualFiles, new String[] {});
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathThatMatchesRelativePathAndEmptyFileNamePattern()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-1");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "file-9-81/file-9-81/file-9-81-xyz.xml", "");

        assertRelativePaths(actualFiles, new String[] {});
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathWithSpecialCharactersAndFileNamePatternConvertableToLike()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-2");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "special_characters_%?.file-9-81", ".*file.*1.*xml");

        assertRelativePaths(actualFiles, "special_characters_%?.file-9-81/special_characters_%?.file-1-1-xyz.xml",
                "special_characters_%?.file-9-81/special_characters_%?.file-4-16-xyz.xml",
                "special_characters_%?.file-9-81/special_characters_%?.file-9-81-xyz.xml");
    }

    @Test
    public void testListMatchingPathInfosWithStartingPathWithSpecialCharactersAndFileNamePatternNotConvertableToLike()
    {
        ISingleDataSetPathInfoProvider provider = ServiceProvider
                .getDataSetPathInfoProvider()
                .tryGetSingleDataSetPathInfoProvider("ds-2");

        List<DataSetPathInfo> actualFiles = provider.listMatchingPathInfos(
                "special_characters_%?.file-9-81", ".*(file-0-0|file-2-4).*");

        assertRelativePaths(actualFiles, "special_characters_%?.file-9-81/special_characters_%?.file-0-0-xyz.xml",
                "special_characters_%?.file-9-81/special_characters_%?.file-2-4-xyz.xml");
    }

    private void assertRelativePaths(List<DataSetPathInfo> actualFiles, String... expectedRelativePaths)
    {
        Assert.assertEquals(actualFiles.size(), expectedRelativePaths.length);

        if (actualFiles.size() > 0)
        {
            String[] actualRelativePaths = new String[actualFiles.size()];

            for (int i = 0; i < actualFiles.size(); i++)
            {
                actualRelativePaths[i] = actualFiles.get(i).getRelativePath();
            }

            Arrays.sort(actualRelativePaths);
            Arrays.sort(expectedRelativePaths);
            Assert.assertEquals(actualRelativePaths, expectedRelativePaths);
        }
    }

    private int feedDataBase(DataSource dataSource)
    {
        int numberOfEntries = 0;
        IPathsInfoDAO dao = QueryTool.getQuery(dataSource, IPathsInfoDAO.class);
        try
        {
            long id;
            long parentId;

            id = dao.createDataSet("ds-1", "a/b/c/");
            parentId = dao.createDataSetFile(id, null, "", "ds-1", 0, true, null, null, new Date(4711));
            numberOfEntries += feedDataBase(dao, id, parentId, 3, "", "");

            id = dao.createDataSet("ds-2", "a2/b2/c2/");
            parentId = dao.createDataSetFile(id, null, "", "ds-2", 0, true, null, null, new Date(4722));
            numberOfEntries += feedDataBase(dao, id, parentId, 1, "", "special_characters_%?.");

            dao.commit();
        } catch (Exception ex)
        {
            dao.rollback();
            ex.printStackTrace();
        }
        dao.close();
        return numberOfEntries;
    }

    private int feedDataBase(IPathsInfoDAO dao, long dataSetId, Long parentId, int level,
            String prefix, String fileNamePrefix)
    {
        int numberOfEntries = 0;
        for (int i = 0; i < 10; i++)
        {
            boolean directory = level > 0;
            String fileName = fileNamePrefix + "file-" + i + "-" + (i * i) + (directory ? "" : "-xyz.xml");
            long id =
                    dao.createDataSetFile(dataSetId, parentId, prefix + fileName, fileName, level
                            * 100 + i, directory, null, null, new Date(4711));
            numberOfEntries++;
            if (directory)
            {
                numberOfEntries +=
                        feedDataBase(dao, dataSetId, id, level - 1, prefix + fileName + "/", fileNamePrefix);
            }
        }
        return numberOfEntries;
    }

    private void search(List<DataSetPathInfo> results, DataSetPathInfo info, Pattern pattern)
    {
        if (pattern.matcher(info.getRelativePath()).matches())
        {
            results.add(info);
        }
        @SuppressWarnings("deprecation")
        List<DataSetPathInfo> children = info.getChildren();
        for (DataSetPathInfo child : children)
        {
            search(results, child, pattern);
        }
    }

    private void cleanUpDatabase(DataSource dataSource) throws SQLException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            connection.createStatement().execute("delete from data_sets where code like 'ds-1' or code like 'ds-2'");
        } finally
        {
            close(connection);
        }
    }

    private void close(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

}
