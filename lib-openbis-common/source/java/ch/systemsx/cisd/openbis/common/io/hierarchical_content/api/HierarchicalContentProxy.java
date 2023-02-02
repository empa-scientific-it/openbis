package ch.systemsx.cisd.openbis.common.io.hierarchical_content.api;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalContentProxy implements IHierarchicalContent
{

    private final IHierarchicalContent hierarchicalContent;

    private final List<IHierarchicalContentExecuteOnAccess> executeOnAccessList;

    private HierarchicalContentProxy(IHierarchicalContent hierarchicalContent, List<IHierarchicalContentExecuteOnAccess> executeOnAccessList)
    {
        super();
        if (hierarchicalContent == null)
        {
            throw new IllegalArgumentException("hierarchicalContent was null");
        } else
        {
            this.hierarchicalContent = hierarchicalContent;
        }

        if (executeOnAccessList == null)
        {
            this.executeOnAccessList = new ArrayList<IHierarchicalContentExecuteOnAccess>();
        } else
        {
            this.executeOnAccessList = executeOnAccessList;
        }
    }

    public static IHierarchicalContent getProxyFor(IHierarchicalContent hierarchicalContent,
            List<IHierarchicalContentExecuteOnAccess> executeOnAccessList)
    {
        if (hierarchicalContent != null)
        {
            HierarchicalContentProxy newIntance = new HierarchicalContentProxy(hierarchicalContent, executeOnAccessList);
            return newIntance;
        }
        return null;
    }

    public void addExecuteOnAccessMethod(IHierarchicalContentExecuteOnAccess executeOnAccessMethod)
    {
        this.executeOnAccessList.add(executeOnAccessMethod);
    }

    private void executeOnAccessMethods()
    {
        for (IHierarchicalContentExecuteOnAccess executeOnAccess : executeOnAccessList)
        {
            executeOnAccess.execute();
        }
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {
        executeOnAccessMethods();
        return hierarchicalContent.getRootNode();
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {
        executeOnAccessMethods();
        return hierarchicalContent.getNode(relativePath);
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        executeOnAccessMethods();
        return hierarchicalContent.tryGetNode(relativePath);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        executeOnAccessMethods();
        return hierarchicalContent.listMatchingNodes(relativePathPattern);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath, String fileNamePattern)
    {
        executeOnAccessMethods();
        return hierarchicalContent.listMatchingNodes(startingPath, fileNamePattern);
    }

    @Override
    public void close()
    {
        hierarchicalContent.close();
    }

    @Override
    public int hashCode()
    {
        return (hierarchicalContent != null) ? hierarchicalContent.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        return hierarchicalContent == obj || (hierarchicalContent != null && hierarchicalContent.equals(obj));
    }

    @Override
    public String toString()
    {
        return hierarchicalContent.toString();
    }

}