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
package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;

/**
 * Stores entries to pathinfo db based on given DataSetFileCreation instances.
 * 
 * @author anttil
 */
public class PathInfoFeeder
{

    private DirectoryNode root = new DirectoryNode(null, null);

    private final long dataSetId;

    private String dataSetCode;

    public PathInfoFeeder(long dataSetId, String dataSetCode, Collection<DataSetFileCreation> files)
    {
        this.dataSetCode = dataSetCode;
        checkInput(files);
        for (DataSetFileCreation file : files)
        {
            if (file.isDirectory())
            {
                root = root.addDirectory(Paths.get(file.getPath()));
            } else
            {
                String checksum = file.getChecksum();
                if (checksum != null)
                {
                    String checksumType = file.getChecksumType(); // checksumType != null already checked
                    checksum = checksumType + ":" + checksum;
                }
                root = root.addFile(Paths.get(file.getPath()), file.getFileLength(), file.getChecksumCRC32(), checksum);
            }
        }
        this.dataSetId = dataSetId;
    }

    public void storeFilesWith(IPathsInfoDAO dao)
    {
        storeFilesWith(dao, root, null, dataSetCode, new Date());
    }

    private void storeFilesWith(IPathsInfoDAO dao, DataSetContentNode node, Long parentId, String name, Date timestamp)
    {
        long id;
        String fullPath = node.getFullPath() == null ? "" : node.getFullPath();
        id = dao.createDataSetFile(dataSetId, parentId, fullPath, name, node.getLength(),
                node.isDirectory(), node.getChecksumCRC32(), node.getChecksum(), timestamp);

        if (node.isDirectory())
        {
            for (Entry<String, DataSetContentNode> entry : ((DirectoryNode) node).getChildren().entrySet())
            {
                storeFilesWith(dao, entry.getValue(), id, entry.getKey(), timestamp);
            }
        }
    }

    private static void checkInput(Collection<DataSetFileCreation> files)
    {
        Set<String> paths = new HashSet<>();
        for (DataSetFileCreation file : files)
        {
            checkInput(file);
            check(paths.contains(file.getPath()), "Path '" + file.getPath() + "' appears twice");
            paths.add(file.getPath());
        }
    }

    @SuppressWarnings("null")
    private static void checkInput(DataSetFileCreation file)
    {
        String path = file.getPath();
        check(path == null || path.length() == 0, "Path of " + file + " was null");
        check(path.startsWith("/"), "Path '" + path + "' is absolute");
        check(file.getChecksum() != null && file.getChecksumType() == null, "Checksum with out type specified: " + file.getChecksum());
        check(file.isDirectory() == false && file.getFileLength() == null, "Size of '" + path + "' is null");
        check(file.isDirectory() && file.getFileLength() != null, "Directory '" + path + "' has size "
                + file.getFileLength());
        check(file.isDirectory() && file.getChecksumCRC32() != null, "Directory '" + path + "' has CRC32 checksum "
                + file.getChecksumCRC32());
        check(file.isDirectory() && file.getChecksum() != null && file.getChecksumType() != null, "Directory '" + path + "' has checksum of type "
                + file.getChecksumType() + ": " + file.getChecksum());
    }

    private static void check(boolean condition, String message)
    {
        if (condition)
        {
            throw new IllegalArgumentException(message);
        }
    }
}
