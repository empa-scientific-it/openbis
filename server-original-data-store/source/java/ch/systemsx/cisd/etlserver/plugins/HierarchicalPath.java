package ch.systemsx.cisd.etlserver.plugins;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;

public class HierarchicalPath
{
    private final String path;

    private final ContainerDataSet container;

    public HierarchicalPath(String path, ContainerDataSet container)
    {
        this.path = path;
        this.container = container;
    }

    public String getPath()
    {
        return path;
    }

    public ContainerDataSet getContainer()
    {
        return container;
    }
}
