/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Franz-Josef Elmer
 */
public class Utils
{

    public static DataSetFile createDataSetFile(String code, IHierarchicalContentNode node, DataStore dataStore)
    {
        DataSetFile file = new DataSetFile();
        DataSetPermId permId = new DataSetPermId(code);
        file.setPermId(new DataSetFilePermId(permId, node.getRelativePath()));
        file.setPath(node.getRelativePath());
        file.setDataSetPermId(permId);
        file.setDataStore(dataStore);
        file.setDirectory(node.isDirectory());
        if (node.isDirectory() == false)
        {
            file.setFileLength(node.getFileLength());
            if (node.isChecksumCRC32Precalculated())
            {
                file.setChecksumCRC32(node.getChecksumCRC32());
            }
            setChecksumOf(file, node.getChecksum());
        }
        return file;
    }

    private static void setChecksumOf(DataSetFile file, String checksum)
    {
        if (checksum == null)
        {
            return;
        }
        String[] splitted = checksum.split(":", 2);
        if (splitted.length < 2 || splitted[0].length() == 0 || splitted[1].length() == 0)
        {
            return;
        }
        file.setChecksumType(splitted[0]);
        file.setChecksum(splitted[1]);
    }

}
