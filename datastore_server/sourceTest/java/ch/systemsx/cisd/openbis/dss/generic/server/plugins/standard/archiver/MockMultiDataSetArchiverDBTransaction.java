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

    private boolean readOnlyThrowAnException; // used to simulate an exception.

    private boolean throwAnException; // used to simulate an exception.

    @Override
    public List<MultiDataSetArchiverDataSetDTO> getDataSetsForContainer(MultiDataSetArchiverContainerDTO container)
    {
        if (throwAnException) {
            throw new RuntimeException("Can't get the data sets for the container because something bad happened!");
        }
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
        if (throwAnException) {
            throw new RuntimeException("Can't create a container because something bad happened!");
        }
        MultiDataSetArchiverContainerDTO container = new MultiDataSetArchiverContainerDTO(id++, path);
        uncommittedContainers.add(container);
        return container;
    }

    @Override
    public void deleteContainer(String container)
    {
        if (throwAnException) {
            throw new RuntimeException("Can't delete the container because something bad happened!");
        }
        for (Iterator<MultiDataSetArchiverContainerDTO> iterator = containers.iterator(); iterator.hasNext();)
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
        if (throwAnException) {
            throw new RuntimeException("Can't insert dataset because something bad happened!");
        }
        String dataSetCode = dataSet.getDataSetCode();
        Long dataSetSize = dataSet.getDataSetSize();
        MultiDataSetArchiverDataSetDTO dataSetDTO = new MultiDataSetArchiverDataSetDTO(id++, dataSetCode, container.getId(), dataSetSize);
        uncommittedDataSets.add(dataSetDTO);
        return dataSetDTO;
    }

    @Override
    public MultiDataSetArchiverDataSetDTO getDataSetForCode(String code)
    {
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't get dataSet because something bad happened!");
        }
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
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't get container because something bad happened!");
        }
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
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't get dataSet because something bad happened!");
        }
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
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't get dataSets for container because something bad happened!");
        }
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
        if (throwAnException) {
            throw new RuntimeException("Can't request unarchiving because something bad happened!");
        }
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
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't return list of containers because something bad happened!");
        }
        return containers;
    }

    @Override
    public List<MultiDataSetArchiverContainerDTO> listContainersForUnarchiving()
    {
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't return list of containers because something bad happened!");
        }
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
        if (throwAnException) {
            throw new RuntimeException("Can't reset request unarchiving because something bad happened!");
        }
        getContainerForId(containerId).setUnarchivingRequested(false);
    }

    @Override
    public void deleteContainer(long containerId)
    {
        if (throwAnException) {
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

    public void setThrowAnException(boolean throwAnException) {
        this.throwAnException = throwAnException;
    }

    public void setReadOnlyThrowAnException(boolean throwAnException) {
        this.readOnlyThrowAnException = throwAnException;
    }

    @Override
    public List<MultiDataSetArchiverContainerDTO> listContainersWithDataSets(String[] dataSetCodes)
    {
        if (readOnlyThrowAnException) {
            throw new RuntimeException("Can't return list of containers because something bad happened!");
        }
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