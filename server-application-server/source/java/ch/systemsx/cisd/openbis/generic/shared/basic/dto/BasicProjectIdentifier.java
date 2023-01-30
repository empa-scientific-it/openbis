/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.io.Serializable;

/**
 * The <i>GWT</i> counterpart to {@link ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier}.
 * 
 * @author Piotr Buczek
 */
public class BasicProjectIdentifier implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String instanceCode;

    private String spaceCode;

    private String projectCode;

    // for GWT serialization
    @SuppressWarnings("unused")
    private BasicProjectIdentifier()
    {
    }

    public BasicProjectIdentifier(final String instanceCode, final String spaceCode,
            final String projectCode)
    {
        this.instanceCode = instanceCode;
        this.spaceCode = spaceCode;
        this.projectCode = projectCode;
    }

    public BasicProjectIdentifier(final String spaceCode, final String projectCode)
    {
        this(null, spaceCode, projectCode);
    }

    public String getInstanceCode()
    {
        return instanceCode;
    }

    public void setInstanceCode(String instanceCode)
    {
        this.instanceCode = instanceCode;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof BasicProjectIdentifier == false)
        {
            return false;
        }
        final BasicProjectIdentifier that = (BasicProjectIdentifier) obj;
        return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

    @Override
    public String toString()
    {
        return (instanceCode != null ? instanceCode : "") + "/" + spaceCode + "/" + projectCode;
    }

}
