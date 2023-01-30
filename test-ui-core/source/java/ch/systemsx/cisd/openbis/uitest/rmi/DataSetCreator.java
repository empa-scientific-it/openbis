/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ch.systemsx.cisd.openbis.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;

/**
 * @author anttil
 */
public class DataSetCreator
{
    private File file;

    public DataSetCreator(String content)
    {
        File dataDir = new File("targets/selenium-datasets");
        dataDir.mkdir();

        try
        {
            file = createDummyFile(dataDir, UUID.randomUUID().toString() + ".txt", content);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

    }

    public NewDataSetDTO getMetadata(DataSet dataset)
    {

        String id;
        DataSetOwnerType ownerType;
        if (dataset.getSample() != null)
        {
            id = Identifiers.get(dataset.getSample()).toString();
            ownerType = DataSetOwnerType.SAMPLE;
        } else
        {
            id = Identifiers.get(dataset.getExperiment()).toString();
            ownerType = DataSetOwnerType.EXPERIMENT;
        }

        String typeCode = dataset.getType().getCode();

        List<String> parentCodes = new ArrayList<String>();
        for (DataSet ds : dataset.getParents())
        {
            parentCodes.add(ds.getCode());
        }

        return getNewDataSet(file, id, typeCode, ownerType, parentCodes);
    }

    public InputStream getData()
    {
        ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(file);

        return new ConcatenatedContentInputStream(true, getContentForFileInfos(
                file.getPath(), fileInfos));
    }

    private File createDummyFile(File dir, String name, String content) throws IOException
    {
        File dummyFile = new File(dir, name);
        dummyFile.createNewFile();
        PrintWriter out = new PrintWriter(dummyFile);
        for (int i = 0; i < content.length(); ++i)
        {
            out.append(content.substring(i, i + 1));
        }
        out.flush();
        out.close();

        return dummyFile;
    }

    @SuppressWarnings("hiding")
    private ArrayList<FileInfoDssDTO> getFileInfosForPath(File file)
    {
        ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
        if (false == file.exists())
        {
            return fileInfos;
        }

        try
        {
            String path = file.getCanonicalPath();
            if (false == file.isDirectory())
            {
                path = file.getParentFile().getCanonicalPath();
            }

            FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
            builder.appendFileInfosForFile(file, fileInfos, true);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return fileInfos;
    }

    @SuppressWarnings("hiding")
    private NewDataSetDTO getNewDataSet(File fileToUpload, String ownerId, String typeCode,
            DataSetOwnerType ownerType, List<String> parentCodes)
    {
        String ownerIdentifier = ownerId;
        DataSetOwner owner = new NewDataSetDTO.DataSetOwner(ownerType, ownerIdentifier);

        File file = fileToUpload;
        ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(file);

        // Get the parent
        String folderNameOrNull = null;
        if (file.isDirectory())
        {
            folderNameOrNull = file.getName();
        }

        NewDataSetDTO dataSet = new NewDataSetDTO(owner, folderNameOrNull, fileInfos);

        dataSet.setParentDataSetCodes(parentCodes);

        dataSet.setDataSetTypeOrNull(typeCode);
        return dataSet;
    }

    private List<IHierarchicalContentNode> getContentForFileInfos(String filePath,
            List<FileInfoDssDTO> fileInfos)
    {
        List<IHierarchicalContentNode> files = new ArrayList<IHierarchicalContentNode>();
        File parent = new File(filePath);
        if (false == parent.isDirectory())
        {
            return Collections.<IHierarchicalContentNode> singletonList(new FileBasedContentNode(
                    parent));
        }

        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            @SuppressWarnings("hiding")
            File file = new File(parent, fileInfo.getPathInDataSet());
            if (false == file.exists())
            {
                throw new IllegalArgumentException("File does not exist " + file);
            }
            // Skip directories
            if (false == file.isDirectory())
            {
                files.add(new FileBasedContentNode(file));
            }
        }

        return files;
    }
}
