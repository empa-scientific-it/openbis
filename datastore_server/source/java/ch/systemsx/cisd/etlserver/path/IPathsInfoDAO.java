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

import java.util.Collection;
import java.util.Date;
import java.util.List;


import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

/**
 * Data Access Object for feeding and updating pathinfo database.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPathsInfoDAO extends TransactionQuery
{
    @Select("select id from data_sets where code = ?{1}")
    public Long tryGetDataSetId(String code);

    @Select("insert into data_sets (code, location) values (?{1}, ?{2}) returning id")
    public long createDataSet(String code, String location);

    @Select("insert into data_set_files (dase_id, parent_id, relative_path, file_name, "
            + "size_in_bytes, is_directory, last_modified) values (?{1}, ?{2}, ?{3}, ?{4}, ?{5}, ?{6}, ?{7}) returning id")
    public long createDataSetFile(long dataSetId, Long parentId, String relativePath,
            String fileName, long sizeInBytes, boolean directory, Date lastModifiedDate);

    @Update(sql = "insert into data_set_files (dase_id, parent_id, relative_path, file_name, "
            + "size_in_bytes, checksum_crc32, is_directory, last_modified) values "
            + "(?{1.dataSetId}, ?{1.parentId}, ?{1.relativePath}, ?{1.fileName}, ?{1.sizeInBytes}, "
            + "?{1.checksumCRC32}, ?{1.directory}, ?{1.lastModifiedDate})", batchUpdate = true)
    public void createDataSetFiles(Collection<PathEntryDTO> filePaths);
    
    @Select("select f.id, d.code as data_set_code, location, relative_path " +
    		"from data_set_files as f join data_sets as d on f.dase_id = d.id " +
    		"where checksum_crc32 is null and is_directory = 'F' order by data_set_code")
    public List<PathEntryDTO> listDataSetFilesWithUnkownChecksum();
    
    @Update("update data_set_files set checksum_crc32 = ?{2} where id = ?{1}")
    public void updateChecksum(long id, long checksum);
    
}
