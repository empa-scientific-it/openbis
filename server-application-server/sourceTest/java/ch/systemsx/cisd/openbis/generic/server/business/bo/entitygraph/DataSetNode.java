/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph;

import java.util.ArrayList;
import java.util.List;

public final class DataSetNode extends EntityNode
{
    private boolean deletable = true;

    ExperimentNode experiment;

    SampleNode sample;

    private final List<DataSetNode> components = new ArrayList<DataSetNode>();

    private final List<DataSetNode> containers = new ArrayList<DataSetNode>();

    private final List<DataSetNode> children = new ArrayList<DataSetNode>();

    private final List<DataSetNode> parents = new ArrayList<DataSetNode>();

    DataSetNode(long id)
    {
        super("DS", id);
    }

    public boolean isDeletable()
    {
        return deletable;
    }

    public ExperimentNode getExperiment()
    {
        return experiment;
    }

    public SampleNode getSample()
    {
        return sample;
    }

    public List<DataSetNode> getChildren()
    {
        return children;
    }

    public List<DataSetNode> getParents()
    {
        return parents;
    }

    public List<DataSetNode> getComponents()
    {
        return components;
    }

    public List<DataSetNode> getContainers()
    {
        return containers;
    }

    public DataSetNode nonDeletable()
    {
        deletable = false;
        return this;
    }

    void hasComponents(DataSetNode... someComponentDataSets)
    {
        for (DataSetNode componentDataSet : someComponentDataSets)
        {
            components.add(componentDataSet);
            componentDataSet.containers.add(this);
        }
    }

    void hasChildren(DataSetNode... someChildDataSets)
    {
        for (DataSetNode childDataSet : someChildDataSets)
        {
            children.add(childDataSet);
            childDataSet.parents.add(this);
        }
    }

    void hasParents(DataSetNode... someParentDataSets)
    {
        for (DataSetNode parentDataSet : someParentDataSets)
        {
            parents.add(parentDataSet);
            parentDataSet.children.add(this);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(super.toString());
        if (deletable == false)
        {
            builder.append(" (non deletable)");
        }
        Utils.appendTo(builder, "experiment", experiment);
        Utils.appendTo(builder, "sample", sample);
        Utils.appendTo(builder, "children", children);
        Utils.appendTo(builder, "parents", parents);
        Utils.appendTo(builder, "components", components);
        Utils.appendTo(builder, "containers", containers);
        return builder.toString();
    }
}