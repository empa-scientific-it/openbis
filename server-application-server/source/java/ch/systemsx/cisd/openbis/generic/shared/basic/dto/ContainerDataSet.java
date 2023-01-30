/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * A virtual data set "containing" other data sets. Container data sets have no physical representation. Their sole purpose is to provide a merged
 * view of their contents.
 * 
 * @author Kaloyan Enimanev
 */
public class ContainerDataSet extends AbstractExternalData
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<AbstractExternalData> containedDataSets = new ArrayList<AbstractExternalData>();

    public ContainerDataSet()
    {
        this(false);
    }

    public ContainerDataSet(boolean isStub)
    {
        super(isStub);
    }

    @Override
    public boolean isContainer()
    {
        return true;
    }

    @Override
    public ContainerDataSet tryGetAsContainerDataSet()
    {
        return this;
    }

    @Override
    public DataSetKind getDataSetKind()
    {
        return DataSetKind.CONTAINER;
    }

    public List<AbstractExternalData> getContainedDataSets()
    {
        return containedDataSets;
    }

    public void setContainedDataSets(List<AbstractExternalData> containedDataSets)
    {
        this.containedDataSets = containedDataSets;
    }

}
