/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class MockMultiDataSetArchiverDBTransaction
        implements IMultiDataSetArchiverDBTransaction, IMultiDataSetArchiverReadonlyQueryDAO
{
    private int id;

    private List<MultiDataSetArchiverContainerDTO> containers = new ArrayList<>();

    private List<MultiDataSetArchiverDataSetDTO> dataSets = new ArrayList<>();

    private List<MultiDataSetArchiverContainerDTO> uncommittedContainers = new ArrayList<>();

    private List<MultiDataSetArchiverContainerDTO> uncommittedContainersDeletion = new ArrayList<>();

    private List<MultiDataSetArchiverDataSetDTO> uncommittedDataSets = new ArrayList<>();

    private boolean committed;

    private boolean rolledBack;

    boolean throwAnExceptionWhenUserTryToDeleteContainer;

    @Override
    public List<MultiDataSetArchiverDataSetDTO> getDataSetsForContainer(MultiDataSetArchiverContainerDTO container)
    {
        List<MultiDataSetArchiverDataSetDTO> result = new ArrayList<>();
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            if (dataSet.getContainerId() == container.getId())
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    @Override
    public MultiDataSetArchiverContainerDTO createContainer(String path)
    {
        MultiDataSetArchiverContainerDTO container = new MultiDataSetArchiverContainerDTO(id++, path);
        uncommittedContainers.add(container);
        return container;
    }

    @Override
    public void deleteContainer(String container)
    {
        if (throwAnExceptionWhenUserTryToDeleteContainer)
        {
            throw new RuntimeException("Can't delete the container because something bad happened!");
        }
        for (Iterator<MultiDataSetArchiverContainerDTO> iterator = containers.iterator(); iterator.hasNext(); )
        {
            MultiDataSetArchiverContainerDTO containerDTO = iterator.next();
            if (containerDTO.getPath().equals(container))
            {
                iterator.remove();
            }
        }
    }

    @Override
    public MultiDataSetArchiverDataSetDTO insertDataset(DatasetDescription dataSet, MultiDataSetArchiverContainerDTO container)
    {
        String dataSetCode = dataSet.getDataSetCode();
        Long dataSetSize = dataSet.getDataSetSize();
        MultiDataSetArchiverDataSetDTO dataSetDTO = new MultiDataSetArchiverDataSetDTO(id++, dataSetCode, container.getId(), dataSetSize);
        uncommittedDataSets.add(dataSetDTO);
        return dataSetDTO;
    }

    @Override
    public MultiDataSetArchiverDataSetDTO getDataSetForCode(String code)
    {
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            if (dataSet.getCode().equals(code))
            {
                return dataSet;
            }
        }
        return null;
    }

    @Override
    public MultiDataSetArchiverContainerDTO getContainerForId(long containerId)
    {
        for (MultiDataSetArchiverContainerDTO container : containers)
        {
            if (container.getId() == containerId)
            {
                return container;
            }
        }
        return null;
    }

    @Override
    public MultiDataSetArchiverDataSetDTO getDataSetForId(long dataSetId)
    {
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            if (dataSet.getId() == dataSetId)
            {
                return dataSet;
            }
        }
        return null;
    }

    @Override
    public List<MultiDataSetArchiverDataSetDTO> listDataSetsForContainerId(long containerId)
    {
        List<MultiDataSetArchiverDataSetDTO> result = new ArrayList<MultiDataSetArchiverDataSetDTO>();
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            if (dataSet.getContainerId() == containerId)
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    @Override
    public void requestUnarchiving(List<String> dataSetCodes)
    {
        for (String dataSetCode : dataSetCodes)
        {
            MultiDataSetArchiverDataSetDTO dataSet = getDataSetForCode(dataSetCode);
            if (dataSet != null)
            {
                MultiDataSetArchiverContainerDTO container = getContainerForId(dataSet.getContainerId());
                if (container != null)
                {
                    container.setUnarchivingRequested(true);
                }
            }
        }
    }

    @Override
    public List<MultiDataSetArchiverContainerDTO> listContainers()
    {
        return containers;
    }

    @Override public List<MultiDataSetArchiverContainerDTO> listContainersInChronologicalOrder()
    {
        return containers;
    }

    @Override public List<MultiDataSetArchiverContainerDTO> listContainersInRandomOrder()
    {
        return containers;
    }

    @Override
    public List<MultiDataSetArchiverContainerDTO> listContainersForUnarchiving()
    {
        List<MultiDataSetArchiverContainerDTO> result = new ArrayList<MultiDataSetArchiverContainerDTO>();
        for (MultiDataSetArchiverContainerDTO container : containers)
        {
            if (container.isUnarchivingRequested())
            {
                result.add(container);
            }
        }
        return result;
    }

    @Override
    public void resetRequestUnarchiving(long containerId)
    {
        getContainerForId(containerId).setUnarchivingRequested(false);
    }

    @Override
    public void deleteContainer(long containerId)
    {
        if (throwAnExceptionWhenUserTryToDeleteContainer)
        {
            throw new RuntimeException("Can't delete the container because something bad happened!");
        }
        for (MultiDataSetArchiverContainerDTO container : containers)
        {
            if (container.getId() == containerId)
            {
                uncommittedContainersDeletion.add(container);
            }
        }
    }

    @Override
    public void commit()
    {
        containers.addAll(uncommittedContainers);
        containers.removeAll(uncommittedContainersDeletion);
        dataSets.addAll(uncommittedDataSets);
        committed = true;
        clearUncommitted();
    }

    @Override
    public void rollback()
    {
        rolledBack = true;
        clearUncommitted();
    }

    private void clearUncommitted()
    {
        uncommittedContainers.clear();
        uncommittedContainersDeletion.clear();
        uncommittedDataSets.clear();
    }

    @Override
    public void close()
    {
    }

    @Override
    public boolean isClosed()
    {
        return false;
    }

    @Override
    public List<MultiDataSetArchiverContainerDTO> listContainersWithDataSets(String[] dataSetCodes)
    {
        Set<MultiDataSetArchiverContainerDTO> containers = new HashSet<>(); // only unique container
        for (String dataSetCode : dataSetCodes)
        {
            MultiDataSetArchiverDataSetDTO dataSet = getDataSetForCode(dataSetCode);
            if (dataSet != null)
            {
                MultiDataSetArchiverContainerDTO container = getContainerForId(dataSet.getContainerId());
                if (container != null)
                {
                    containers.add(container);
                }
            }
        }
        return new ArrayList<>(containers);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Containers:");
        for (MultiDataSetArchiverContainerDTO container : containers)
        {
            builder.append('\n').append(container);
        }
        builder.append("\nData sets:");
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            builder.append('\n').append(dataSet);
        }
        if (uncommittedContainers.isEmpty() == false)
        {
            builder.append("\nUncommitted containers:");
            for (MultiDataSetArchiverContainerDTO container : uncommittedContainers)
            {
                builder.append('\n').append(container);
            }
        }
        if (uncommittedDataSets.isEmpty() == false)
        {
            builder.append("\nUncommitted data sets:");
            for (MultiDataSetArchiverDataSetDTO dataSet : uncommittedDataSets)
            {
                builder.append('\n').append(dataSet);
            }
        }
        builder.append("\ncommitted: ").append(committed);
        builder.append(", rolledBack: ").append(rolledBack);
        return builder.toString();
    }

    @Override
    public long getTotalNoOfBytesInContainersWithUnarchivingRequested()
    {
        return 2 * FileUtils.ONE_MB;
    }
}