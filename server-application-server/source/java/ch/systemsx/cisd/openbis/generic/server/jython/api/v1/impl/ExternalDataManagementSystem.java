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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

/**
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystem extends ExternalDataManagementSystemImmutable implements
        IExternalDataManagementSystem
{
    ExternalDataManagementSystem(String code)
    {
        super(code);
    }

    @Override
    public void setLabel(String label)
    {
        getExternalDataManagementSystem().setLabel(label);
    }

    @Override
    public void setAddress(String address)
    {
        getExternalDataManagementSystem().setAddress(address);
        getExternalDataManagementSystem().setUrlTemplate(address);
    }

    @Override
    public void setAddressType(ExternalDataManagementSystemType addressType)
    {
        getExternalDataManagementSystem().setAddressType(addressType);
        getExternalDataManagementSystem().setOpenBIS(ExternalDataManagementSystemType.OPENBIS.equals(addressType));
    }

    @Override
    public void setUrlTemplate(String urlTemplate)
    {
        setAddress(urlTemplate);
    }

    @Override
    public void setOpenBIS(boolean openBIS)
    {
        if (openBIS)
        {
            setAddressType(ExternalDataManagementSystemType.OPENBIS);
        } else
        {
            setAddressType(ExternalDataManagementSystemType.URL);
        }
    }

}
