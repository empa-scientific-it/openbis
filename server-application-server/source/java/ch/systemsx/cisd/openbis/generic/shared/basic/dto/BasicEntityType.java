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

import java.util.Date;

/**
 * An <i>abstract</i> entity type.
 * 
 * @author Christian Ribeaud
 */
public class BasicEntityType extends AbstractType
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final BasicEntityType UNSPECIFIED = null;

    private Date modificationDate;

    private Script validationScript;

    public BasicEntityType()
    {
    }

    public BasicEntityType(String code)
    {
        setCode(code);
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public Script getValidationScript()
    {
        return validationScript;
    }

    public void setValidationScript(Script validationScript)
    {
        this.validationScript = validationScript;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof BasicEntityType == false)
        {
            return false;
        }
        if (false == obj.getClass().equals(this.getClass()))
        {
            return false;
        }
        final BasicEntityType that = (BasicEntityType) obj;
        return getCode().equals(that.getCode());
    }

    @Override
    public final int hashCode()
    {
        return getCode().hashCode();
    }

}
