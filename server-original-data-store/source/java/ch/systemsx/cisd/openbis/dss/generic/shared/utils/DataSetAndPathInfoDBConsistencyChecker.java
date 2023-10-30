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
package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.Hdf5AwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.DssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IDssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBOnlyHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.PathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyChecker
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetAndPathInfoDBConsistencyChecker.class);

    private static final Comparator<Map.Entry<String, List<Difference>>> DATA_SET_COMPARATOR =
            new Comparator<Map.Entry<String, List<Difference>>>()
                {
                    @Override
                    public int compare(Entry<String, List<Difference>> e1,
                            Entry<String, List<Difference>> e2)
                    {
                        return e1.getKey().compareTo(e2.getKey());
                    }
                };

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContentProvider pathInfoProvider;

    private IDssServiceRpcGenericFactory serviceFactory;

    private Map<String, List<Difference>> differences = new HashMap<String, List<Difference>>();

    private ProcessingStatus status = new ProcessingStatus();

    private List<String> dataSets = new ArrayList<>();

    private boolean forceChecksumVerification;

    public DataSetAndPathInfoDBConsistencyChecker(
            IHierarchicalContentProvider fileProvider, IHierarchicalContentProvider pathInfoProvider)
    {
        this.fileProvider = fileProvider;
        this.pathInfoProvider = pathInfoProvider;
    }

    public void setForceChecksumVerification(final boolean forceChecksumVerification)
    {
        this.forceChecksumVerification = forceChecksumVerification;
    }

    public void check(List<? extends IDatasetLocation> datasets)
    {
        for (IDatasetLocation location : datasets)
        {
            String dataSetCode = location.getDataSetCode();
            if (location instanceof DatasetDescription)
            {
                DatasetDescription dataSet = (DatasetDescription) location;
                checkDataSet(dataSetCode, dataSet.isH5Folders(), dataSet.isH5arFolders());
            } else
            {
                checkDataSet(dataSetCode);
            }
        }
    }

    public void checkDataSet(String dataSetCode)
    {
    	checkDataSet(dataSetCode, false, true);
    }

    public void checkDataSet(String dataSetCode, boolean h5Folders, boolean h5arFolders)
    {
        dataSets.add(dataSetCode);
        IHierarchicalContent fileContent = null;
        IHierarchicalContent pathInfoContent = null;

        try
        {
            fileContent = tryGetContent(getFileProvider(h5Folders, h5arFolders), dataSetCode);
            pathInfoContent = tryGetContent(getPathInfoProvider(), dataSetCode);

            List<Difference> datasetDifferences = new ArrayList<Difference>();

            compare(fileContent, pathInfoContent, datasetDifferences);

            if (datasetDifferences.isEmpty() == false)
            {
                differences.put(dataSetCode, datasetDifferences);
            }
            status.addDatasetStatus(dataSetCode, Status.OK);

        } catch (Exception e)
        {
            operationLog.error(
                    "Couldn't check consistency of the file system and the path info database for a data set: "
                            + dataSetCode,
                    e);
            status.addDatasetStatus(
                    dataSetCode,
                    Status.createError("Couldn't check consistency of the file system and the path info database for a data set: "
                            + dataSetCode
                            + " because of the following exception: " + e.getMessage()));
        } finally
        {
            if (null != fileContent)
            {
                fileContent.close();
            }
            if (null != pathInfoContent)
            {
                pathInfoContent.close();
            }
        }
    }

    public ProcessingStatus getStatus()
    {
        if (status == null)
        {
            throw new IllegalStateException("Undefined status before check() has been executed.");
        }
        return status;
    }

    public boolean noErrorAndInconsistencyFound()
    {
        return status.getErrorStatuses().isEmpty() && differences.isEmpty();
    }

    public String createReport()
    {
        return status.getErrorStatuses().isEmpty() ? createNormalReport() : createErrorReport();
    }

    private String createNormalReport()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Data sets checked:\n\n");
        builder.append(CollectionUtils.abbreviate(dataSets, 1000));
        builder.append("\n\n");
        builder.append("Differences found:\n\n");

        if (differences.isEmpty())
        {
            builder.append("None");
        } else
        {
            List<Map.Entry<String, List<Difference>>> entries =
                    new ArrayList<Map.Entry<String, List<Difference>>>(
                            differences.entrySet());
            Collections.sort(entries, DATA_SET_COMPARATOR);
            for (Map.Entry<String, List<Difference>> differencesEntry : entries)
            {
                String dataSetCode = differencesEntry.getKey();
                List<Difference> datasetDifferences = differencesEntry.getValue();

                Collections.sort(datasetDifferences);

                builder.append("Data set " + dataSetCode + ":\n");
                for (Difference datasetDifference : datasetDifferences)
                {
                    builder.append("- " + datasetDifference.getDescription() + "\n");
                }
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    private String createErrorReport()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Error when checking datasets:\n\n");

        for (Status s : status.getErrorStatuses())
        {
            builder.append(s.toString());
            builder.append("\n");
        }

        return builder.toString();

    }

    private void compare(IHierarchicalContent fileContent, IHierarchicalContent pathInfoContent,
            List<Difference> diffs)
    {
        IHierarchicalContentNode fileRoot = tryGetRoot(fileContent);
        IHierarchicalContentNode pathInfoRoot = tryGetRoot(pathInfoContent);

        if (fileRoot != null && pathInfoRoot != null)
        {
            compare(fileRoot, pathInfoRoot, diffs);
        } else if (fileRoot == null ^ pathInfoRoot == null)
        {
            diffs.add(new RootExistenceDifference(fileRoot != null));
        } else
        {
            diffs.add(new RootExistence());
        }
    }

    @SuppressWarnings("null")
    private void compare(IHierarchicalContentNode fileNode, IHierarchicalContentNode pathInfoNode,
            List<Difference> diffs)
    {
        boolean fileNodeExists = fileNode != null && fileNode.exists();
        boolean pathInfoNodeExists = pathInfoNode != null && pathInfoNode.exists();

        // report a difference if one node is null and the other not
        if (fileNodeExists == false || pathInfoNodeExists == false)
        {
            if (fileNodeExists && pathInfoNodeExists == false)
            {
                diffs.add(new NodeExistenceDifference(fileNode.getRelativePath(), true));
            }
            if (pathInfoNodeExists && fileNodeExists == false)
            {
                diffs.add(new NodeExistenceDifference(pathInfoNode.getRelativePath(), false));
            }
            return;
        }

        // report a difference if node paths do not match
        if (fileNode.getRelativePath().equals(pathInfoNode.getRelativePath()) == false)
        {
            diffs.add(new NodeChildrenDifference(fileNode.getRelativePath(), true));
            diffs.add(new NodeChildrenDifference(pathInfoNode.getRelativePath(), false));
        }

        if (fileNode.isDirectory() && pathInfoNode.isDirectory())
        {
            compareDirectories(fileNode, pathInfoNode, diffs);

        } else if (fileNode.isDirectory() == false && pathInfoNode.isDirectory() == false)
        {
            compareFiles(fileNode, pathInfoNode, diffs);
        } else
        {
            // report a difference if one node is a directory and the other is a file
            diffs.add(new DirectoryDifference(fileNode.getRelativePath(), fileNode
                    .isDirectory()));
        }
    }

    private void compareDirectories(IHierarchicalContentNode fileNode, IHierarchicalContentNode pathInfoNode, List<Difference> diffs)
    {
        Children children = new Children(fileNode, pathInfoNode);

        // compare children nodes that exist both in the file system and the path info database
        for (String commonPath : children.getCommonPaths())
        {
            compare(children.getFileNode(commonPath), children.getPathInfoNode(commonPath),
                    diffs);
        }

        // report differences for nodes that exist only in one place
        for (IHierarchicalContentNode uncommonNode : children.getFileUncommonNodes())
        {
            diffs.add(new NodeChildrenDifference(uncommonNode.getRelativePath(), true));
        }
        for (IHierarchicalContentNode uncommonNode : children.getPathInfoUncommonNodes())
        {
            diffs.add(new NodeChildrenDifference(uncommonNode.getRelativePath(), false));
        }
    }

    private void compareFiles(IHierarchicalContentNode fileNode, IHierarchicalContentNode pathInfoNode, List<Difference> diffs)
    {
        // check file lengths
        if (fileNode.getFileLength() != pathInfoNode.getFileLength())
        {
            diffs.add(new SizeDifference(fileNode.getRelativePath(), fileNode
                    .getFileLength(), pathInfoNode.getFileLength()));
        }
        // check CRC32 checksums if stored in path Info db
        if (!pathInfoNode.isChecksumCRC32Precalculated() && forceChecksumVerification)
        {
            diffs.add(new CRC32ExistenceDifference(fileNode.getRelativePath()));
        }
        if (pathInfoNode.isChecksumCRC32Precalculated()
                && (fileNode.getChecksumCRC32() != pathInfoNode.getChecksumCRC32()))
        {
            diffs.add(new CRC32Difference(fileNode.getRelativePath(), fileNode
                    .getChecksumCRC32(), pathInfoNode.getChecksumCRC32()));
        }
        // check other checksum (if defined)
        if (pathInfoNode.getChecksum() != null)
        {
            String pathInfoChecksum = pathInfoNode.getChecksum();
            String checksum = fileNode.getChecksum();
            if (checksum == null)
            {
                String[] splittedPathInfoChecksum = pathInfoChecksum.split(":");
                if (splittedPathInfoChecksum.length > 1)
                {

                    PathInfo pathInfo = new PathInfo();
                    PathInfo.setChecksum(pathInfo, fileNode.getInputStream(), true, splittedPathInfoChecksum[0]);
                    checksum = pathInfo.getChecksum();
                }
            }
            if (checksum != null && pathInfoChecksum.equals(checksum) == false)
            {
                diffs.add(new ChecksumDifference(fileNode.getRelativePath(), checksum, pathInfoChecksum));
            }
        }
    }

    private abstract class Difference implements Comparable<Difference>
    {

        private String path;

        public Difference(String path)
        {
            this.path = path;
        }

        public String getPath()
        {
            return path;
        }

        public abstract String getDescription();

        @Override
        public int compareTo(Difference o)
        {
            return getPath().compareTo(o.getPath());
        }

    }

    private class DirectoryDifference extends Difference
    {

        private boolean dirInFS;

        public DirectoryDifference(String path, boolean dirInFS)
        {
            super(path);
            this.dirInFS = dirInFS;
        }

        @Override
        public String getDescription()
        {
            if (dirInFS)
            {
                return "'"
                        + getPath()
                        + "' is a directory in the file system but a file in the path info database";
            } else
            {
                return "'"
                        + getPath()
                        + "' is a directory in the path info database but a file in the file system";
            }
        }

    }

    private class RootExistence extends Difference
    {

        public RootExistence()
        {
            super(null);
        }

        @Override
        public String getDescription()
        {
            return "exists neither in the path info database nor in the file system";
        }

    }

    private class RootExistenceDifference extends Difference
    {

        private boolean existsInFS;

        public RootExistenceDifference(boolean existsInFS)
        {
            super(null);
            this.existsInFS = existsInFS;
        }

        @Override
        public String getDescription()
        {

            if (existsInFS)
            {
                return "exists in the file system but does not exist in the path info database";
            } else
            {
                return "exists in the path info database but does not exist in the file system";
            }
        }

    }

    private class NodeExistenceDifference extends Difference
    {

        private boolean existsInFS;

        public NodeExistenceDifference(String path, boolean existsInFS)
        {
            super(path);
            this.existsInFS = existsInFS;
        }

        @Override
        public String getDescription()
        {
            if (existsInFS)
            {
                return "'"
                        + getPath()
                        + "' exists on the file system but does not exist in the path info database";
            } else
            {
                return "'"
                        + getPath()
                        + "' exists in the path info database but does not exist on the file system";
            }
        }

    }

    private class NodeChildrenDifference extends Difference
    {

        private boolean existsInFS;

        public NodeChildrenDifference(String path, boolean existsInFS)
        {
            super(path);
            this.existsInFS = existsInFS;
        }

        @Override
        public String getDescription()
        {
            if (existsInFS)
            {
                return "'" + getPath()
                        + "' is on the file system but is not referenced in the path info database";
            } else
            {
                return "'"
                        + getPath()
                        + "' is referenced in the path info database but does not exist on the file system";
            }
        }

    }

    private class SizeDifference extends Difference
    {
        private long sizeInFS;

        private long sizeInDB;

        public SizeDifference(String path, long sizeInFS, long sizeInDB)
        {
            super(path);
            this.sizeInFS = sizeInFS;
            this.sizeInDB = sizeInDB;
        }

        @Override
        public String getDescription()
        {
            return "'" + getPath() + "' size in the file system = " + sizeInFS
                    + " bytes but in the path info database = " + sizeInDB + " bytes.";
        }
    }

    private class CRC32ExistenceDifference extends Difference
    {
        public CRC32ExistenceDifference(String path)
        {
            super(path);
        }

        @Override
        public String getDescription()
        {
            return "'" + getPath() + "' CRC32 checksum is missing in the path info database";
        }
    }

    private class CRC32Difference extends Difference
    {
        private int checksumInFS;

        private int checksumInDB;

        public CRC32Difference(String path, int checksumInFS, int checksumInDB)
        {
            super(path);
            this.checksumInFS = checksumInFS;
            this.checksumInDB = checksumInDB;
        }

        @Override
        public String getDescription()
        {
            return "'" + getPath() + "' CRC32 checksum in the file system = "
                    + IOUtilities.crc32ToString(checksumInFS) + " but in the path info database = "
                    + IOUtilities.crc32ToString(checksumInDB);
        }
    }

    private class ChecksumDifference extends Difference
    {
        private String checksumInFS;

        private String checksumInDB;

        public ChecksumDifference(String path, String checksumInFS, String checksumInDB)
        {
            super(path);
            this.checksumInFS = checksumInFS;
            this.checksumInDB = checksumInDB;
        }

        @Override
        public String getDescription()
        {
            return "'" + getPath() + "' checksum in the file system = " + checksumInFS
                    + " but in the path info database = " + checksumInDB;
        }
    }

    private class Children
    {

        private Map<String, IHierarchicalContentNode> fileChildrenMap;

        private Map<String, IHierarchicalContentNode> pathInfoChildrenMap;

        public Children(IHierarchicalContentNode fileNode, IHierarchicalContentNode pathInfoNode)
        {
            this.fileChildrenMap = getMap(fileNode);
            this.pathInfoChildrenMap = getMap(pathInfoNode);
        }

        public IHierarchicalContentNode getFileNode(String path)
        {
            return fileChildrenMap.get(path);
        }

        public IHierarchicalContentNode getPathInfoNode(String path)
        {
            return pathInfoChildrenMap.get(path);
        }

        public Set<IHierarchicalContentNode> getFileUncommonNodes()
        {
            return getUncommonNodes(fileChildrenMap);
        }

        public Set<IHierarchicalContentNode> getPathInfoUncommonNodes()
        {
            return getUncommonNodes(pathInfoChildrenMap);
        }

        public Set<String> getCommonPaths()
        {
            Set<String> commonPaths = new HashSet<String>();
            for (String path : fileChildrenMap.keySet())
            {
                if (pathInfoChildrenMap.containsKey(path))
                {
                    commonPaths.add(path);
                }
            }
            return commonPaths;
        }

        private Map<String, IHierarchicalContentNode> getMap(IHierarchicalContentNode node)
        {
            Map<String, IHierarchicalContentNode> map =
                    new TreeMap<String, IHierarchicalContentNode>();
            for (IHierarchicalContentNode child : node.getChildNodes())
            {
                map.put(child.getRelativePath(), child);
            }
            return map;
        }

        public Set<IHierarchicalContentNode> getUncommonNodes(
                Map<String, IHierarchicalContentNode> childrenMap)
        {
            Set<String> commonNames = getCommonPaths();
            Set<IHierarchicalContentNode> uncommonNodes = new HashSet<IHierarchicalContentNode>();

            for (Map.Entry<String, IHierarchicalContentNode> child : childrenMap.entrySet())
            {
                if (commonNames.contains(child.getValue().getRelativePath()) == false)
                {
                    uncommonNodes.add(child.getValue());
                }
            }
            return uncommonNodes;
        }

    }

    private IHierarchicalContent tryGetContent(IHierarchicalContentProvider contentProvider,
            String datasetCode)
    {
        try
        {
            return contentProvider.asContentWithoutModifyingAccessTimestamp(datasetCode);
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private IHierarchicalContentNode tryGetRoot(IHierarchicalContent content)
    {
        try
        {
            if (content == null)
            {
                return null;
            } else
            {
                return content.getRootNode();
            }
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private IHierarchicalContentProvider getFileProvider(boolean h5Folders, boolean h5arFolders)
    {
        if (fileProvider == null)
        {
            fileProvider =
                    new HierarchicalContentProvider(ServiceProvider.getOpenBISService(),
                            ServiceProvider.getShareIdManager(),
                            ServiceProvider.getConfigProvider(), ServiceProvider.getContentCache(),
                            new Hdf5AwareHierarchicalContentFactory(h5Folders, h5arFolders), getServiceFactory(),
                            null, null);
        }
        return fileProvider;
    }

    private IHierarchicalContentProvider getPathInfoProvider()
    {
        if (pathInfoProvider == null)
        {
            IHierarchicalContentFactory pathInfoDBFactory =
                    PathInfoDBOnlyHierarchicalContentFactory.create();

            if (pathInfoDBFactory == null)
            {
                throw new IllegalArgumentException("Path info database is not configured.");
            } else
            {
                pathInfoProvider =
                        new HierarchicalContentProvider(ServiceProvider.getOpenBISService(),
                                ServiceProvider.getShareIdManager(),
                                ServiceProvider.getConfigProvider(),
                                ServiceProvider.getContentCache(), pathInfoDBFactory,
                                getServiceFactory(), null, null);
            }
        }
        return pathInfoProvider;
    }

    private IDssServiceRpcGenericFactory getServiceFactory()
    {
        if (serviceFactory == null)
        {
            serviceFactory = new DssServiceRpcGenericFactory();
        }
        return serviceFactory;
    }
}
