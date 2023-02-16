/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

public abstract class ArchiveFolders
{

    public abstract File getFolder(DatasetDescription dataSetDescription);

    public abstract File getFolder(long fileSize);

    public abstract Collection<File> getAllFolders();

    public static ArchiveFolders create(File[] folders, boolean createFolders, Long smallDataSetsSizeLimit)
    {
        String[] folderPaths = new String[folders.length];
        for (int i = 0; i < folders.length; i++)
        {
            File folder = folders[i];
            if (folder != null)
            {
                folderPaths[i] = folder.getPath();
            }
        }
        return create(folderPaths, createFolders, smallDataSetsSizeLimit);
    }

    public static ArchiveFolders create(String[] folderPaths, boolean createFolders, Long smallDataSetsSizeLimit)
    {
        List<String> notBlankPaths = new ArrayList<String>();

        for (String folderPath : folderPaths)
        {
            if (false == StringUtils.isBlank(folderPath))
            {
                notBlankPaths.add(folderPath.trim());
            }
        }

        if (notBlankPaths.isEmpty())
        {
            return null;
        } else if (notBlankPaths.size() == 1)
        {
            return new CommonArchiveFolder(notBlankPaths.get(0), createFolders);
        } else if (notBlankPaths.size() == 2)
        {
            return new SizeDependentArchiveFolders(notBlankPaths.get(0), notBlankPaths.get(1), createFolders, smallDataSetsSizeLimit);
        } else
        {
            throw new IllegalArgumentException(
                    "Found "
                            + notBlankPaths.size()
                            + " archive folders: "
                            + notBlankPaths
                            + ". Please specify only one folder for all sizes of data sets or two separate folders for big and small data sets respectively.");
        }
    }

    protected File prepareFolder(String folderPath, boolean createFolder)
    {
        File folder = new File(folderPath);

        if (createFolder)
        {
            if (folder.isFile())
            {
                throw new IllegalArgumentException("Archive folder '" + folder + "' is a file.");
            }
            if (folder.exists() == false)
            {
                boolean success = folder.mkdirs();
                if (success == false)
                {
                    throw new IllegalArgumentException("Couldn't create archive folder '" + folder + "'.");
                }
            }
        } else
        {
            if (folder.isDirectory() == false)
            {
                throw new IllegalArgumentException("Archive folder '" + folder + "' doesn't exists or is a file.");
            }
        }

        return folder;
    }

    static class CommonArchiveFolder extends ArchiveFolders
    {
        private File dataSetsFolder;

        private CommonArchiveFolder(String folderPath, boolean createFolder)
        {
            this.dataSetsFolder = prepareFolder(folderPath, createFolder);
        }

        @Override
        public File getFolder(DatasetDescription dataSetDescription)
        {
            return dataSetsFolder;
        }

        @Override
        public File getFolder(long fileSize)
        {
            return dataSetsFolder;
        }

        @Override
        public Collection<File> getAllFolders()
        {
            return Arrays.asList(dataSetsFolder);
        }

    }

    static class SizeDependentArchiveFolders extends ArchiveFolders
    {

        private File bigDataSetsFolder;

        private File smallDataSetsFolder;

        private Long smallDataSetsSizeLimit;

        private IEncapsulatedOpenBISService service;

        private IShareIdManager shareIdManager;

        private IConfigProvider configProvider;

        private SizeDependentArchiveFolders(String bigDataSetsFolderPath, String smallDataSetsFolderPath, boolean createFolders,
                Long smallDataSetsSizeLimit)
        {
            if (smallDataSetsSizeLimit == null)
            {
                throw new IllegalArgumentException("Small data set size limit cannot be null");
            }
            this.bigDataSetsFolder = prepareFolder(bigDataSetsFolderPath, createFolders);
            this.smallDataSetsFolder = prepareFolder(smallDataSetsFolderPath, createFolders);
            this.smallDataSetsSizeLimit = smallDataSetsSizeLimit;
        }

        @Override
        public File getFolder(DatasetDescription dataSetDescription)
        {
            if (dataSetDescription.getDataSetSize() == null)
            {
                String shareId = getShareIdManager().getShareId(dataSetDescription.getDataSetCode());
                File shareFolder = new File(getConfigProvider().getStoreRoot(), shareId);
                long size = FileUtils.sizeOfDirectory(new File(shareFolder, dataSetDescription.getDataSetLocation()));
                getService().updateShareIdAndSize(dataSetDescription.getDataSetCode(), shareId, size);
                dataSetDescription.setDataSetSize(size);
            }

            return getFolder(dataSetDescription.getDataSetSize());
        }

        @Override
        public File getFolder(long fileSize)
        {
            if (fileSize > smallDataSetsSizeLimit)
            {
                return bigDataSetsFolder;
            } else
            {
                return smallDataSetsFolder;
            }
        }

        @Override
        public Collection<File> getAllFolders()
        {
            return Arrays.asList(bigDataSetsFolder, smallDataSetsFolder);
        }

        private IConfigProvider getConfigProvider()
        {
            if (configProvider == null)
            {
                configProvider = ServiceProvider.getConfigProvider();
            }
            return configProvider;
        }

        private IShareIdManager getShareIdManager()
        {
            if (shareIdManager == null)
            {
                shareIdManager = ServiceProvider.getShareIdManager();
            }
            return shareIdManager;
        }

        private IEncapsulatedOpenBISService getService()
        {
            if (service == null)
            {
                service = ServiceProvider.getOpenBISService();
            }
            return service;
        }

    }

}