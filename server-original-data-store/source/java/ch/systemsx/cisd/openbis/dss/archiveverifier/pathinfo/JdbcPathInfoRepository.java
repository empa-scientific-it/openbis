/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.pathinfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.IArchiveFileContent;
import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.IArchiveFileMetaDataRepository;

/**
 * Gets metadata of archive files from pathinfo db.
 * 
 * @author anttil
 */
public class JdbcPathInfoRepository implements IArchiveFileMetaDataRepository
{
    private static final String QUERY_PATHINFO =
            "SELECT dsf.relative_path, dsf.file_name, dsf.size_in_bytes, dsf.checksum_crc32, dsf.is_directory, dsf.last_modified " +
                    "FROM data_sets ds, data_set_files dsf " +
                    "WHERE dsf.dase_id = ds.id AND ds.code=?";

    private final Connection connection;

    public JdbcPathInfoRepository(Connection connection)
    {
        this.connection = connection;
    }

    @Override
    public IArchiveFileContent getMetaData(String dataSet)
    {
        try
        {
            PreparedStatement stmt = connection.prepareStatement(QUERY_PATHINFO);
            stmt.setString(1, dataSet);
            ResultSet result = stmt.executeQuery();

            final Map<String, PathInfoEntry> data = new HashMap<String, PathInfoEntry>();

            if (result.isBeforeFirst() == false)
            {
                return null;
            }

            while (result.next())
            {
                final String file = result.getString("relative_path");

                Integer crc = result.getInt("checksum_crc32");
                if (result.wasNull())
                {
                    crc = null;
                }
                Long size = result.getLong("size_in_bytes");
                if (result.wasNull())
                {
                    size = null;
                }

                data.put(file, new PathInfoEntry(getUnsignedLong(crc), size));
            }

            return new DataSetPathInfo(data);

        } catch (final SQLException ex)
        {
            return new IArchiveFileContent()
                {

                    @Override
                    public Long getFileCrc(String file)
                    {
                        throw new RuntimeException(ex);
                    }

                    @Override
                    public Long getFileSize(String file)
                    {
                        throw new RuntimeException(ex);
                    }

                };
        }
    }

    public Long getUnsignedLong(Integer x)
    {
        if (x == null)
        {
            return null;
        }
        return x & 0x00000000ffffffffL;
    }

    @Override
    public String getDescription()
    {
        return "pathinfo db";
    }
}
