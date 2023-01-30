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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

/**
 * A virtual data set storing the reference to the data set in external data management system. Link data sets have no physical representation in
 * local dss.
 * 
 * @author Pawel Glyzewski
 */
public class LinkDataSet extends AbstractExternalData
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ExternalDataManagementSystem externalDataManagementSystem;

    private String externalCode;

    private List<IContentCopy> copies;

    public LinkDataSet()
    {
        this(false);
    }

    public LinkDataSet(boolean isStub)
    {
        super(isStub);
    }

    public ExternalDataManagementSystem getExternalDataManagementSystem()
    {
        return externalDataManagementSystem;
    }

    public void setExternalDataManagementSystem(
            ExternalDataManagementSystem externalDataManagementSystem)
    {
        this.externalDataManagementSystem = externalDataManagementSystem;
    }

    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    public List<IContentCopy> getCopies()
    {
        return copies;
    }

    public void setCopies(List<IContentCopy> copies)
    {
        this.copies = copies;
    }

    @Override
    public boolean isLinkData()
    {
        return true; // overriden in subclasses
    }

    @Override
    public LinkDataSet tryGetAsLinkDataSet()
    {
        return this;
    }

    @Override
    public DataSetKind getDataSetKind()
    {
        return DataSetKind.LINK;
    }

    @Override
    public boolean isAvailable()
    {
        return false;
    }

}
