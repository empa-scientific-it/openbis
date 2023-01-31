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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.db.DBUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathInfoProvider implements IDataSetPathInfoProvider
{
    @Private
    public static class DataSetFileRecord
    {
        // Attribute names as defined in database schema
        public long id;

        public Long dase_id;

        public Long parent_id;

        public String relative_path;

        public String file_name;

        public long size_in_bytes;

        public Integer checksum_crc32;

        public String checksum;

        public boolean is_directory;

        public Date last_modified;
    }

    @Private
    public static class ExtendedDataSetFileRecord extends DataSetFileRecord
    {
        public String code;
    }

    @Private
    public static interface IPathInfoDAO extends BaseQuery
    {

        static String SELECT_DATA_SET_FILES =
                "SELECT id, parent_id, relative_path, file_name, size_in_bytes, checksum_crc32, checksum, "
                        + "is_directory, last_modified FROM data_set_files ";

        static String SELECT_DATA_SET_FILES_WITH_DATA_SET_INFO =
                "SELECT f.id, f.dase_id, f.parent_id, f.relative_path, f.file_name, f.size_in_bytes, "
                        + "f.checksum_crc32, f.checksum, f.is_directory, f.last_modified, s.code "
                        + " FROM data_set_files f LEFT JOIN data_sets s ON (s.id = f.dase_id) ";

        @Select("SELECT id FROM data_sets WHERE code = ?{1}")
        public Long tryToGetDataSetId(String dataSetCode);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1}")
        public List<DataSetFileRecord> listDataSetFiles(long dataSetId);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND parent_id is null")
        public DataSetFileRecord getDataSetRootFile(long dataSetId);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND relative_path = ?{2}")
        public DataSetFileRecord tryToGetRelativeDataSetFile(long dataSetId, String relativePath);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND parent_id = ?{2}")
        public List<DataSetFileRecord> listChildrenByParentId(long dataSetId, long parentId);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND relative_path ~ ?{2}")
        public List<DataSetFileRecord> listDataSetFilesByRelativePathRegex(long dataSetId,
                String relativePathRegex);

        @Select(SELECT_DATA_SET_FILES + "WHERE dase_id = ?{1} AND relative_path LIKE ?{2}")
        public List<DataSetFileRecord> listDataSetFilesByRelativePathLikeExpression(long dataSetId,
                String relativePathLikeExpression);

        @Select(SELECT_DATA_SET_FILES_WITH_DATA_SET_INFO + "WHERE LOWER(relative_path) LIKE LOWER(?{1})")
        public List<ExtendedDataSetFileRecord> listFilesByRelativePathLikeExpression(String relativePathLikeExpression);

        @Select(SELECT_DATA_SET_FILES
                + "WHERE dase_id = ?{1} AND relative_path = ?{2} || file_name AND file_name ~ ?{3}")
        public List<DataSetFileRecord> listDataSetFilesByFilenameRegex(
                long dataSetId, String startingPath, String filenameRegex);

        @Select(SELECT_DATA_SET_FILES
                + "WHERE dase_id = ?{1} AND relative_path = ?{2} || file_name AND file_name LIKE ?{3}")
        public List<DataSetFileRecord> listDataSetFilesByFilenameLikeExpression(
                long dataSetId, String startingPath,
                String filenameLikeExpression);

    }

    private static interface ILoader
    {
        List<DataSetFileRecord> listDataSetFiles(long dataSetId);
    }

    private IPathInfoDAO dao;

    public DatabaseBasedDataSetPathInfoProvider()
    {
    }

    @Private
    DatabaseBasedDataSetPathInfoProvider(IPathInfoDAO dao)
    {
        this.dao = dao;
    }

    @Override
    public Map<String, List<DataSetPathInfo>> listPathInfosBySearchString(String searchString)
    {
        List<ExtendedDataSetFileRecord> fileRecords = getDao().listFilesByRelativePathLikeExpression(
                searchString);

        Map<String, List<DataSetPathInfo>> allPathInfos = new HashMap<String, List<DataSetPathInfo>>();
        for (ExtendedDataSetFileRecord fileRecord : fileRecords)
        {
            // Build new info
            DataSetPathInfo dataSetPathInfo = new DataSetPathInfo();
            dataSetPathInfo.setChecksumCRC32(fileRecord.checksum_crc32);
            dataSetPathInfo.setChecksum(fileRecord.checksum);
            dataSetPathInfo.setDirectory(fileRecord.is_directory);
            dataSetPathInfo.setFileName(fileRecord.file_name);
            dataSetPathInfo.setId(fileRecord.id);
            dataSetPathInfo.setLastModified(fileRecord.last_modified);
            dataSetPathInfo.setRelativePath(fileRecord.relative_path);
            dataSetPathInfo.setSizeInBytes(fileRecord.size_in_bytes);
            if (fileRecord.parent_id != null)
            {
                DataSetPathInfo dataSetPathInfoParent = new DataSetPathInfo();
                dataSetPathInfoParent.setId(fileRecord.parent_id);
                dataSetPathInfo.setParent(dataSetPathInfoParent);
            }

            // Add to dataSetCode list
            List<DataSetPathInfo> dataSetPathInfos = allPathInfos.get(fileRecord.code);
            if (dataSetPathInfos == null)
            {
                dataSetPathInfos = new ArrayList<DataSetPathInfo>();
                allPathInfos.put(fileRecord.code, dataSetPathInfos);
            }
            dataSetPathInfos.add(dataSetPathInfo);
        }
        return allPathInfos;
    }

    @Override
    public List<DataSetPathInfo> listPathInfosByRegularExpression(String dataSetCode,
            final String regularExpression)
    {
        return new Loader(dataSetCode, new ILoader()
            {
                @Override
                public List<DataSetFileRecord> listDataSetFiles(long dataSetId)
                {
                    String likeExpressionOrNull =
                            DBUtils.tryToTranslateRegExpToLikePattern("^" + regularExpression + "$");

                    if (likeExpressionOrNull == null)
                    {
                        return getDao().listDataSetFilesByRelativePathRegex(dataSetId,
                                "^" + regularExpression + "$");
                    } else
                    {
                        return getDao().listDataSetFilesByRelativePathLikeExpression(dataSetId,
                                likeExpressionOrNull);
                    }
                }
            }).getInfos();
    }

    @Override
    public DataSetPathInfo tryGetFullDataSetRootPathInfo(String dataSetCode)
    {
        return new Loader(dataSetCode, new ILoader()
            {
                @Override
                public List<DataSetFileRecord> listDataSetFiles(long dataSetId)
                {
                    return getDao().listDataSetFiles(dataSetId);
                }
            }).getRoot();
    }

    @Override
    public Map<String, Integer> getDataSetChecksums(String dataSetCode)
    {
        Map<String, Integer> files = new HashMap<String, Integer>();
        IPathInfoDAO queries = getDao();
        Long id = queries.tryToGetDataSetId(dataSetCode);
        if (id == null)
        {
            return files;
        }
        List<DataSetFileRecord> records = getDao().listDataSetFiles(id);
        for (DataSetFileRecord record : records)
        {
            files.put(record.relative_path, record.checksum_crc32);
        }
        return files;
    }

    @Override
    public ISingleDataSetPathInfoProvider tryGetSingleDataSetPathInfoProvider(String dataSetCode)
    {
        final Long dataSetId = getDao().tryToGetDataSetId(dataSetCode);
        if (dataSetId != null)
        {
            return new SingleDataSetPathInfoProvider(dataSetId, getDao());
        }
        return null;
    }

    static class SingleDataSetPathInfoProvider implements ISingleDataSetPathInfoProvider
    {
        private final Long dataSetId;

        private final IPathInfoDAO dao;

        public SingleDataSetPathInfoProvider(Long dataSetId, IPathInfoDAO dao)
        {
            this.dataSetId = dataSetId;
            this.dao = dao;
        }

        @Override
        public DataSetPathInfo getRootPathInfo()
        {
            DataSetFileRecord record = dao.getDataSetRootFile(dataSetId);
            if (record != null)
            {
                return asPathInfo(record);
            } else
            {
                throw new IllegalStateException("root path wasn't found");
            }
        }

        @Override
        public DataSetPathInfo tryGetPathInfoByRelativePath(String relativePath)
        {
            final String normalizedRelativePath = relativePath.replaceAll("/+", "/");
            DataSetFileRecord record =
                    dao.tryToGetRelativeDataSetFile(dataSetId, normalizedRelativePath);
            if (record != null)
            {
                return asPathInfo(record);
            } else
            {
                return null;
            }
        }

        @Override
        public List<DataSetPathInfo> listChildrenPathInfos(DataSetPathInfo parent)
        {
            List<DataSetFileRecord> records = dao.listChildrenByParentId(dataSetId, parent.getId());
            return asPathInfos(records);
        }

        @Override
        public List<DataSetPathInfo> listMatchingPathInfos(String relativePathPattern)
        {
            String likeExpressionOrNull =
                    DBUtils.tryToTranslateRegExpToLikePattern(prepareDBStyleRegex(relativePathPattern));
            List<DataSetFileRecord> records;
            if (likeExpressionOrNull == null)
            {
                records =
                        dao.listDataSetFilesByRelativePathRegex(dataSetId,
                                prepareDBStyleRegex(relativePathPattern));
            } else
            {
                records =
                        dao.listDataSetFilesByRelativePathLikeExpression(dataSetId,
                                likeExpressionOrNull);
            }
            return asPathInfos(records);
        }

        @Override
        public List<DataSetPathInfo> listMatchingPathInfos(String startingPath,
                String fileNamePattern)
        {
            String aStartingPath = null;

            if (startingPath == null || startingPath.trim().length() == 0)
            {
                aStartingPath = "";
            } else if (startingPath.endsWith("/"))
            {
                aStartingPath = startingPath;
            } else
            {
                aStartingPath = startingPath + "/";
            }

            String likeExpressionOrNull =
                    DBUtils.tryToTranslateRegExpToLikePattern(prepareDBStyleRegex(fileNamePattern));
            List<DataSetFileRecord> records;
            if (likeExpressionOrNull == null)
            {
                records =
                        dao.listDataSetFilesByFilenameRegex(dataSetId, aStartingPath,
                                prepareDBStyleRegex(fileNamePattern));
            } else
            {
                records =
                        dao.listDataSetFilesByFilenameLikeExpression(dataSetId, aStartingPath,
                                likeExpressionOrNull);
            }
            return asPathInfos(records);
        }

        private DataSetPathInfo asPathInfo(DataSetFileRecord record)
        {
            DataSetPathInfo result = new DataSetPathInfo();
            result.setId(record.id);
            result.setFileName(record.file_name);
            result.setRelativePath(record.relative_path);
            result.setDirectory(record.is_directory);
            result.setSizeInBytes(record.size_in_bytes);
            result.setChecksumCRC32(record.checksum_crc32);
            result.setChecksum(record.checksum);
            result.setLastModified(record.last_modified);
            return result;
        }

        private List<DataSetPathInfo> asPathInfos(List<DataSetFileRecord> records)
        {
            List<DataSetPathInfo> results = new ArrayList<DataSetPathInfo>();
            for (DataSetFileRecord record : records)
            {
                results.add(asPathInfo(record));
            }
            return results;
        }

    }

    private final class Loader
    {
        private Map<Long, DataSetPathInfo> idToInfoMap = new HashMap<Long, DataSetPathInfo>();

        private DataSetPathInfo root;

        Loader(String dataSetCode, ILoader loader)
        {
            Long dataSetId = getDao().tryToGetDataSetId(dataSetCode);
            if (dataSetId != null)
            {
                List<DataSetFileRecord> dataSetFileRecords = loader.listDataSetFiles(dataSetId);
                Map<Long, List<DataSetPathInfo>> parentChildrenMap =
                        new HashMap<Long, List<DataSetPathInfo>>();
                for (DataSetFileRecord dataSetFileRecord : dataSetFileRecords)
                {
                    DataSetPathInfo dataSetPathInfo = new DataSetPathInfo();
                    dataSetPathInfo.setFileName(dataSetFileRecord.file_name);
                    dataSetPathInfo.setRelativePath(dataSetFileRecord.relative_path);
                    dataSetPathInfo.setDirectory(dataSetFileRecord.is_directory);
                    dataSetPathInfo.setSizeInBytes(dataSetFileRecord.size_in_bytes);
                    dataSetPathInfo.setChecksumCRC32(dataSetFileRecord.checksum_crc32);
                    dataSetPathInfo.setChecksum(dataSetFileRecord.checksum);
                    dataSetPathInfo.setLastModified(dataSetFileRecord.last_modified);
                    idToInfoMap.put(dataSetFileRecord.id, dataSetPathInfo);
                    Long parentId = dataSetFileRecord.parent_id;
                    if (parentId == null)
                    {
                        root = dataSetPathInfo;
                    } else
                    {
                        List<DataSetPathInfo> children = parentChildrenMap.get(parentId);
                        if (children == null)
                        {
                            children = new ArrayList<DataSetPathInfo>();
                            parentChildrenMap.put(parentId, children);
                        }
                        children.add(dataSetPathInfo);
                    }
                }
                linkParentsWithChildren(parentChildrenMap);
            }
        }

        @SuppressWarnings("deprecation")
        private void linkParentsWithChildren(Map<Long, List<DataSetPathInfo>> parentChildrenMap)
        {
            for (Entry<Long, List<DataSetPathInfo>> entry : parentChildrenMap.entrySet())
            {
                Long parentId = entry.getKey();
                DataSetPathInfo parent = idToInfoMap.get(parentId);
                if (parent != null)
                {
                    List<DataSetPathInfo> children = entry.getValue();
                    for (DataSetPathInfo child : children)
                    {
                        parent.addChild(child);
                        child.setParent(parent);
                    }
                }
            }
        }

        DataSetPathInfo getRoot()
        {
            return root;
        }

        List<DataSetPathInfo> getInfos()
        {
            return new ArrayList<DataSetPathInfo>(idToInfoMap.values());
        }
    }

    private IPathInfoDAO getDao()
    {
        if (dao == null)
        {
            dao =
                    QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(),
                            IPathInfoDAO.class);
        }
        return dao;
    }

    // java style patterns match the whole text, db style patterns match any fragment
    private static String prepareDBStyleRegex(String pattern)
    {
        return "^" + pattern + "$";
    }
}
