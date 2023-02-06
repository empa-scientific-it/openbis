/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICreationIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author anttil
 */
@JsonObject("as.dto.externaldms.create.ExternalDmsCreation")
public class ExternalDmsCreation implements ICreation, IObjectCreation, ICreationIdHolder
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String address;

    private ExternalDmsAddressType addressType;

    private CreationId creationId;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public ExternalDmsAddressType getAddressType()
    {
        return addressType;
    }

    public void setAddressType(ExternalDmsAddressType addressType)
    {
        this.addressType = addressType;
    }

    @Override
    public CreationId getCreationId()
    {
        return creationId;
    }

    public void setCreationId(CreationId creationId)
    {
        this.creationId = creationId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("code", code).toString();
    }

}
