/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

/**
 * Resolves paths of a form /DATA_SET_TYPE/DATA_SET_CODE/{files} <br>
 * / - list all data set types <br>
 * /UNKNOWN - list all data sets of type UNKNOWN <br>
 * /UNKNOWN/20193763213-123 - list in one directory all files belonging to the data set 20193763213-123 or any of it's parents <br>
 * /UNKNOWN/20193763213-123/test0123.png - download the content of a listed file
 * 
 * @author Jakub Straszewski
 */
public class DataSetTypeResolver implements IResolverPlugin
{
    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        if (subPath.length == 0)
        {
            return listDataSetTypes(context);
        }

        String dataSetType = subPath[0];
        if (subPath.length == 1)
        {
            return listDataSetsOfGivenType(dataSetType, context);
        }

        return resolveFileSearch(subPath, context);
    }

    private IFileSystemViewResponse resolveFileSearch(String[] subPath, IResolverContext context)
    {
        String dataSetCode = subPath[1];
        String requestedFileName = subPath.length == 2 ? null : subPath[2];
        if (subPath.length > 3)
        {
            throw new IllegalArgumentException("This resolver can't resolve path of that length");
        }

        List<DataSet> dataSetsToSearch = searchForDataSetAndParents(dataSetCode, context);

        if (dataSetsToSearch == null)
        {
            return context.createNonExistingFileResponse(null);
        }

        if (requestedFileName != null)
        {
            IFileSystemViewResponse result = findRequestedNode(dataSetsToSearch, requestedFileName, context);
            if (result != null)
            {
                return result;
            } else
            {
                return context.createNonExistingFileResponse("Unable to locate requested file");
            }
        }

        IDirectoryResponse response = context.createDirectoryResponse();
        for (IHierarchicalContentNode file : findAllNodes(dataSetsToSearch, context.getContentProvider()))
        {
            response.addFile(file.getName(), file);
        }
        return response;
    }

    private List<IHierarchicalContentNode> findAllNodes(List<DataSet> dataSetsToSearch, IHierarchicalContentProvider contentProvider)
    {
        List<IHierarchicalContentNode> result = new ArrayList<>();
        for (DataSet dataSet : dataSetsToSearch)
        {
            Stack<IHierarchicalContentNode> nodes = new Stack<>();
            nodes.push(contentProvider.asContent(dataSet.getCode()).getRootNode());

            while (false == nodes.isEmpty())
            {
                IHierarchicalContentNode node = nodes.pop();
                if (node.isDirectory())
                {
                    for (IHierarchicalContentNode child : node.getChildNodes())
                    {
                        nodes.push(child);
                    }
                } else
                {
                    result.add(node);
                }
            }
        }
        return result;
    }

    private IFileSystemViewResponse findRequestedNode(List<DataSet> dataSetsToSearch, String requestedFileName,
            IResolverContext context)
    {
        IHierarchicalContentProvider contentProvider = context.getContentProvider();
        for (DataSet dataSet : dataSetsToSearch)
        {
            IHierarchicalContent content = contentProvider.asContent(dataSet.getCode());
            IHierarchicalContentNode node = findRequestedNode(content.getRootNode(), requestedFileName);
            if (node != null)
            {
                return context.createFileResponse(node, content);
            }
        }
        return null;
    }

    private IHierarchicalContentNode findRequestedNode(IHierarchicalContentNode node, String requestedFileName)
    {
        if (node.isDirectory())
        {
            for (IHierarchicalContentNode subNode : node.getChildNodes())
            {
                IHierarchicalContentNode result = findRequestedNode(subNode, requestedFileName);
                if (result != null)
                {
                    return result;
                }
            }
        } else
        {
            if (node.getName().equals(requestedFileName))
            {
                return node;
            }
        }
        return null;
    }

    // returns a list of data sets starting with requested data set and continuedby it's parents
    private List<DataSet> searchForDataSetAndParents(String dataSetCode, IResolverContext context)
    {
        DataSetPermId dataId = new DataSetPermId(dataSetCode);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withParents();
        DataSet dataSet = context.getApi().getDataSets(context.getSessionToken(), Collections.singletonList(dataId), fetchOptions).get(dataId);

        if (dataSet == null)
        {
            return null;
        }

        List<DataSet> dataSetsToSearch = new ArrayList<>();
        dataSetsToSearch.add(dataSet);
        dataSetsToSearch.addAll(dataSet.getParents());
        return dataSetsToSearch;
    }

    private IFileSystemViewResponse listDataSetsOfGivenType(String dataSetType, IResolverContext context)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withParents();
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(dataSetType);
        List<DataSet> dataSets = context.getApi().searchDataSets(context.getSessionToken(), searchCriteria, fetchOptions).getObjects();

        IDirectoryResponse result = context.createDirectoryResponse();
        for (DataSet dataSet : dataSets)
        {
            result.addDirectory(dataSet.getCode(), dataSet.getModificationDate());
        }
        return result;
    }

    private IFileSystemViewResponse listDataSetTypes(IResolverContext context)
    {
        List<DataSetType> dataSetTypes =
                context.getApi().searchDataSetTypes(context.getSessionToken(), new DataSetTypeSearchCriteria(), new DataSetTypeFetchOptions())
                        .getObjects();

        IDirectoryResponse response = context.createDirectoryResponse();
        for (DataSetType type : dataSetTypes)
        {
            response.addDirectory(type.getCode());
        }
        return response;
    }

    @Override
    public void initialize(String name, String code)
    {
    }

}