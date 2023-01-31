/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.MetaDataBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Creates the hierarchical structure of data sets registered in openBIS for given data store.
 * 
 * @author Izabela Adamczyk
 * @author Kaloyan Enimanev
 */
public class HierarchicalStorageUpdater implements IDataStoreLockingMaintenanceTask
{
    public static final String STOREROOT_DIR_LINK_PATH_KEY = "storeroot-dir-link-path";

    public static final String HIERARCHY_ROOT_DIR_KEY = "hierarchy-root-dir";

    public static final String HIERARCHY_LINK_NAMING_STRATEGY = "link-naming-strategy";

    public static final String LINK_SOURCE_SUBFOLDER = "link-source-subpath";

    public static final String LINK_FROM_FIRST_CHILD = "link-from-first-child";

    /**
     * Property indicating if directory with link and meta-data file or only links should be created.
     */
    public static final String WITH_META_DATA = "with-meta-data";

    /**
     * Name of the link to create in a directory in links-only is set to false
     */
    public static final String LINK_DIRECTORY = "data";

    private static final String META_DATA_TSV_FILE = "meta-data.tsv";

    private static final String MODIFICATION_TIMESTAMP_FILE = "modification_timestamp";

    private static final String REBUILDING_HIERARCHICAL_STORAGE = "Rebuilding hierarchical storage";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HierarchicalStorageUpdater.class);

    private static class LinkSourceDescriptor
    {
        private final String subFolder;

        private final boolean linkFromFirstChild;

        public LinkSourceDescriptor(String subFolder, boolean linkFromFirstChild)
        {
            this.subFolder = subFolder;
            this.linkFromFirstChild = linkFromFirstChild;
        }

        public String getSubFolder()
        {
            return subFolder;
        }

        public boolean isLinkFromFirstChild()
        {
            return linkFromFirstChild;
        }
    }

    private IEncapsulatedOpenBISService openBISService;

    private IHierarchicalStorageLinkNamingStrategy linkNamingStrategy;

    private File storeRoot;

    private File hierarchyRoot;

    private boolean withMetaData;

    private Map<String /* data set type */, LinkSourceDescriptor> linkSourceDescriptors;

    @Override
    public void setUp(String pluginName, Properties pluginProperties)
    {
        LogInitializer.init();
        String storeRootFileName = pluginProperties.getProperty(STOREROOT_DIR_LINK_PATH_KEY);
        if (storeRootFileName == null)
        {
            storeRootFileName = PropertyUtils.getMandatoryProperty(pluginProperties, DssPropertyParametersUtil.STOREROOT_DIR_KEY);
        }
        String hierarchyRootFileName =
                PropertyUtils.getMandatoryProperty(pluginProperties, HIERARCHY_ROOT_DIR_KEY);

        openBISService = ServiceProvider.getOpenBISService();
        linkNamingStrategy = createLinkNamingStrategy(pluginProperties);
        storeRoot = new File(storeRootFileName);
        hierarchyRoot = new File(hierarchyRootFileName);
        linkSourceDescriptors = initializeLinkSourceDescriptors(pluginProperties);
        withMetaData = PropertyUtils.getBoolean(pluginProperties, WITH_META_DATA, false);

        operationLog.info("Plugin initialized with: store root = " + storeRootFileName
                + ", hierarchy root = " + hierarchyRootFileName);
    }

    @Override
    public void execute()
    {
        rebuildHierarchy();
    }

    /**
     * @see IDataStoreLockingMaintenanceTask#requiresDataStoreLock()
     */
    @Override
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    private IHierarchicalStorageLinkNamingStrategy createLinkNamingStrategy(Properties properties)
    {
        String linkNamingStrategyClassName =
                PropertyUtils.getProperty(properties, HIERARCHY_LINK_NAMING_STRATEGY,
                        TemplateBasedLinkNamingStrategy.class.getName());
        ExtendedProperties strategyProperties =
                ExtendedProperties
                        .getSubset(properties, HIERARCHY_LINK_NAMING_STRATEGY + ".", true);
        try
        {
            return ClassUtils.create(IHierarchicalStorageLinkNamingStrategy.class,
                    Class.forName(linkNamingStrategyClassName), strategyProperties);
        } catch (ClassNotFoundException ex)
        {
            throw ConfigurationFailureException.fromTemplate("Wrong '%s' property: %s",
                    HIERARCHY_LINK_NAMING_STRATEGY, ex.getMessage());
        }
    }

    private Map<String, LinkSourceDescriptor> initializeLinkSourceDescriptors(
            Properties pluginProperties)
    {
        HashMap<String, LinkSourceDescriptor> result = new HashMap<String, LinkSourceDescriptor>();

        ExtendedProperties subFolderProps =
                ExtendedProperties.getSubset(pluginProperties, LINK_SOURCE_SUBFOLDER + ".", true);
        ExtendedProperties linkFromFirstChildProps =
                ExtendedProperties.getSubset(pluginProperties, LINK_FROM_FIRST_CHILD + ".", true);

        HashSet<Object> dataSetTypes = new HashSet<Object>();
        dataSetTypes.addAll(subFolderProps.keySet());
        dataSetTypes.addAll(linkFromFirstChildProps.keySet());
        for (Object o : dataSetTypes)
        {
            String dataSetType = (String) o;
            String subFolder = subFolderProps.getProperty(dataSetType);
            boolean linkFromFirstChild =
                    Boolean.parseBoolean(linkFromFirstChildProps.getProperty(dataSetType));
            LinkSourceDescriptor descriptor =
                    new LinkSourceDescriptor(subFolder, linkFromFirstChild);
            result.put(dataSetType, descriptor);
        }

        return result;
    }

    private Set<String> startingWith(String prefix, Set<String> set)
    {
        Set<String> result = new HashSet<>();
        for (String s : set)
        {
            if (s.startsWith(prefix))
            {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Refreshes the hierarchy of the data inside hierarchical storage accordingly to the database content.
     */
    private void rebuildHierarchy()
    {
        operationLog.info(REBUILDING_HIERARCHICAL_STORAGE);
        List<DataSetInformation> newLinkMappings = collectDataSet();

        // Includes files and all symbolic links. Does no include directories.
        Set<String> existingPaths = linkNamingStrategy.extractPaths(hierarchyRoot);

        for (DataSetInformation dataSetInformation : newLinkMappings)
        {
            String targetPath = dataSetInformation.targetFile.getAbsolutePath();

            Set<String> existing = startingWith(targetPath, existingPaths);

            if (existing.size() > 0)
            {
                existingPaths.removeAll(existing);
                if (dataSetInformation.containerDto != null)
                {
                    File[] containerMetadata = dataSetInformation.targetFile.getParentFile().listFiles(new FileFilter()
                        {
                            @Override
                            public boolean accept(File file)
                            {
                                return file.isFile() && (FileUtilities.isSymbolicLink(file) == false);
                            }
                        });
                    for (File cm : containerMetadata)
                    {
                        existingPaths.remove(cm.getAbsolutePath());
                    }
                }
                handleExistingEntry(dataSetInformation);
            } else
            {
                handleNonExistingEntry(dataSetInformation);
            }

        }

        // by this time - only paths which should be deleted are left in the existingPaths
        deleteObsoleteLinks(existingPaths);
        operationLog.info(REBUILDING_HIERARCHICAL_STORAGE + " finished");
    }

    private void handleNonExistingEntry(DataSetInformation info)
    {
        if (withMetaData)
        {
            createDataSetFolder(info);
        } else
        {
            createLink(info.targetFile, info.linkSource);
        }
    }

    /**
     * Handle a case when the data set directory already exists in the hierarchical store. In this case we should check if the metadata file is up to
     * date and recreate it if necessary
     */
    private void handleExistingEntry(DataSetInformation info)
    {
        String errorMsgLinksOnlyModeChanged = "The state of hierarchical store is corrupted or property '" + WITH_META_DATA
                + "' or '" + TemplateBasedLinkNamingStrategy.COMPONENT_LINKS_TEMPLATE_PROP_NAME
                + "' has been modified after hierarchical store has been built. In this case the hierarchical store directory "
                + "should be deleted manually. It will be recreated after DSS start up.";
        if (withMetaData)
        {
            if (FileUtilities.isSymbolicLink(info.targetFile))
            {
                throw new IllegalStateException(errorMsgLinksOnlyModeChanged);
            }
            if (info.targetFile.isDirectory())
            {
                Date storedModificationDate = getModificationDateFromFile(info);
                if (storedModificationDate == null || storedModificationDate.before(info.dto.getModificationDate()))
                {
                    createDataSetFolder(info);
                }
            } else
            {
                throw new IllegalStateException(errorMsgLinksOnlyModeChanged);
            }
        } else
        {
            if (FileUtilities.isSymbolicLink(info.targetFile))
            {
                // nothing to do as the link is already in place
                return;
            } else
            {
                throw new IllegalStateException(errorMsgLinksOnlyModeChanged);
            }
        }

    }

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private void createDataSetFolder(DataSetInformation info)
    {
        createLink(new File(info.targetFile, LINK_DIRECTORY), info.linkSource);
        createModificationDateFile(info);
        createMetaDataFile(info);
    }

    private void createMetaDataFile(DataSetInformation info)
    {
        File file = new File(info.targetFile, META_DATA_TSV_FILE);
        String content = MetaDataBuilder.createMetaData(info.dto);
        FileUtilities.writeToFile(file, content);

        if (info.containerDto != null)
        {
            file = new File(info.targetFile + "/..", META_DATA_TSV_FILE);
            if (file.exists() == false)
            {
                content = MetaDataBuilder.createMetaData(info.containerDto);
                FileUtilities.writeToFile(file, content);
            }
        }
    }

    private void createModificationDateFile(DataSetInformation info)
    {
        File file = new File(info.targetFile, MODIFICATION_TIMESTAMP_FILE);
        FileUtilities.writeToFile(file, dateFormat.format(info.dto.getModificationDate()));
        if (info.containerDto != null)
        {
            file = new File(info.targetFile + "/..", MODIFICATION_TIMESTAMP_FILE);
            FileUtilities.writeToFile(file, dateFormat.format(info.containerDto.getModificationDate()));
        }
    }

    private Date getModificationDateFromFile(DataSetInformation info)
    {
        File file = new File(info.targetFile, MODIFICATION_TIMESTAMP_FILE);
        if (file.exists() == false)
            return null;
        String content = FileUtilities.loadToString(file);
        try
        {
            return dateFormat.parse(content);
        } catch (ParseException pe)
        {
            operationLog.error("Modificaction date of dataset stored in Hierarchical store in file " + file.getAbsolutePath() + " is corrupted");
            return null;
        }
    }

    private class DataSetInformation
    {
        /**
         * The DTO object that should be linked
         */
        AbstractExternalData dto;

        /**
         * DTO of container. Might be null.
         */
        ContainerDataSet containerDto;

        /**
         * Path where the dataset metadata and link to store should be placed
         */
        File targetFile;

        /**
         * The location in dss store that should be linked
         */
        File linkSource;
    }

    private HashMap<String, AbstractExternalData> getAbstractExternalDataByCode(Collection<SimpleDataSetInformationDTO> dataSets)
    {
        List<String> codes = new ArrayList<>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            codes.add(dataSet.getDataSetCode());
        }
        List<AbstractExternalData> listDataSetsByCode = openBISService.listDataSetsByCode(codes);
        HashMap<String, AbstractExternalData> dataSetsByCode = new HashMap<>();
        for (AbstractExternalData abstractExternalData : listDataSetsByCode)
        {
            dataSetsByCode.put(abstractExternalData.getCode(), abstractExternalData);
        }
        return dataSetsByCode;
    }

    /**
     * Extracts a {@link Map}: (target,source) from a collection of data sets
     */
    private List<DataSetInformation> collectDataSet()
    {
        Collection<SimpleDataSetInformationDTO> dataSets = openBISService.listPhysicalDataSets();

        HashMap<String, AbstractExternalData> dataSetsByCode = getAbstractExternalDataByCode(dataSets);
        ArrayList<DataSetInformation> result = new ArrayList<DataSetInformation>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            AbstractExternalData abstractData = dataSetsByCode.get(dataSet.getDataSetCode());
            Set<HierarchicalPath> paths = linkNamingStrategy.createHierarchicalPaths(abstractData);

            for (HierarchicalPath hierarchicalPath : paths)
            {

                File targetFile =
                        new File(hierarchyRoot, hierarchicalPath.getPath());
                File share = new File(storeRoot, dataSet.getDataSetShareId());
                File dataSetLocationRoot = new File(share, dataSet.getDataSetLocation());
                File linkSource = determineLinkSource(dataSetLocationRoot, dataSet.getDataSetType());

                DataSetInformation info = new DataSetInformation();
                info.dto = abstractData;
                info.linkSource = linkSource;
                info.targetFile = targetFile;
                info.containerDto = hierarchicalPath.getContainer();

                if (linkSource == null)
                {

                    String logMessage =
                            String.format("Can not determine the link source file for data set '%s', "
                                    + "dataSetType='%s'. Link creation will be skipped.",
                                    dataSetLocationRoot, dataSet.getDataSetType());
                    operationLog.warn(logMessage);
                }
                // we still want a directory with metadata if metadata option is specified
                // if only links should be created we don't want to create broken links
                if (withMetaData || linkSource != null)
                {
                    result.add(info);
                }
            }
        }
        return result;
    }

    private File determineLinkSource(File dataSetLocationRoot, String dataSetType)
    {
        LinkSourceDescriptor linkSourceDescriptor = getLinkSourceDescriptor(dataSetType);
        File source = dataSetLocationRoot;

        if (linkSourceDescriptor != null)
        {
            String subPath = linkSourceDescriptor.getSubFolder();
            if (StringUtils.isBlank(subPath) == false)
            {
                source = new File(source.getAbsolutePath() + File.separator + subPath);
                if (source.exists() == false)
                {
                    String logMessage =
                            String.format("Invalid '%s' configuration for "
                                    + "data set '%s'. Subfolder '%s' does not exist",
                                    LINK_SOURCE_SUBFOLDER, dataSetLocationRoot, source);
                    operationLog.warn(logMessage);
                    return null;
                }
            }
            if (linkSourceDescriptor.isLinkFromFirstChild() && source.isDirectory())
            {
                File[] rootChildren = source.listFiles();
                if (rootChildren == null || rootChildren.length == 0)
                {
                    String logMessage =
                            String.format("Invalid '%s' configuration for "
                                    + "data set '%s'. Subfolder '%s' has no children",
                                    LINK_FROM_FIRST_CHILD, dataSetLocationRoot, source);
                    operationLog.warn(logMessage);
                    return null;
                }
                source = rootChildren[0];
            }
        }

        return source;
    }

    private LinkSourceDescriptor getLinkSourceDescriptor(String dataSetType)
    {
        return linkSourceDescriptors.get(dataSetType);
    }

    /**
     * Recursively removes from the file system files with paths defined in <code>toBeDeleted</code> {@link Set}.
     */
    private void deleteObsoleteLinks(Set<String> toBeDeleted)
    {
        for (String pathToDelete : toBeDeleted)
        {
            File toDelete = new File(pathToDelete);
            File parent = toDelete.getParentFile();
            deleteWithSymbolicLinks(toDelete);
            while (parent != null && false == isSameFile(parent, hierarchyRoot))
            {
                if (parent.list().length == 0)
                {
                    toDelete = parent;
                    parent = toDelete.getParentFile();
                    delete(toDelete);
                } else
                {
                    break;
                }
            }
        }
    }

    private boolean isSameFile(File file1, File file2)
    {
        try
        {
            return file1.getCanonicalPath().equals(file2.getCanonicalPath());
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
        }
    }

    private void deleteWithSymbolicLinks(File toDelete)
    {
        if (FileUtilities.isSymbolicLink(toDelete) == false && toDelete.isDirectory())
        {
            for (File file : toDelete.listFiles())
            {
                // all these files should be symbolic links to a dataset directory.
                // We cannot delete recursively here, it would remove the original files.
                boolean ok = delete(file);
                if (ok == false)
                {
                    operationLog.error("Cannot delete the file: " + file.getPath());
                }
            }
        }

        delete(toDelete);
    }

    private boolean delete(File file)
    {
        if (canBeDeleted(file))
        {
            operationLog.info("Deleting " + file.getAbsolutePath());
            return file.delete();
        } else
        {
            operationLog.error(file.getPath() + " is not a symbolic link and will not be deleted.");
            return false;
        }

    }

    private boolean canBeDeleted(File file)
    {
        if (isUnderHierarchyRoot(file))
        {
            // we try to be safe and delete only links and files that we know we created
            return FileUtilities.isSymbolicLink(file) ||
                    file.isDirectory() ||
                    file.getName().equals(MODIFICATION_TIMESTAMP_FILE) ||
                    file.getName().equals(META_DATA_TSV_FILE);
        } else
        {
            operationLog.warn("Aborting an attempt to delete content outside of hierarchy root : "
                    + file.getAbsolutePath() + ". Please analyze, this is a programming error.");
            return false;
        }
    }

    /**
     * we cannot use cannonical paths here, because they resolve symbolic links.
     */
    private boolean isUnderHierarchyRoot(File file)
    {
        return file.getAbsolutePath().startsWith(hierarchyRoot.getAbsolutePath());
    }

    /**
     * Creates the soft links defined files. If the link already exists it is being deleted and recreated. If the source file is null or doesn't exist
     * then a link is not created.
     */
    private void createLink(File targetFile, File sourceFile)
    {
        if (targetFile.exists())
        {
            targetFile.delete();
        }
        targetFile.getParentFile().mkdirs();
        if (sourceFile != null && sourceFile.exists())
        {
            SoftLinkMaker.createSymbolicLink(sourceFile.getAbsoluteFile(), targetFile.getAbsoluteFile());
        }
    }

}
