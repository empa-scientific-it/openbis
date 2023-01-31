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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

/**
 * @author Jakub Straszewski
 */
public class ExternalDataManagementSystemImmutable implements
        IExternalDataManagementSystemImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem externalDMS;

    public ExternalDataManagementSystemImmutable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem externalDMS)
    {
        this.externalDMS = externalDMS;
    }

    @Override
    public String getLabel()
    {
        return externalDMS.getLabel();
    }

    @Override
    public String getUrlTemplate()
    {
        return externalDMS.getUrlTemplate();
    }

    @Override
    public boolean isOpenBIS()
    {
        return externalDMS.isOpenBIS();
    }

    @Override
    public String getCode()
    {
        return externalDMS.getCode();
    }

    @Override
    public String getAddress()
    {
        return externalDMS.getAddress();
    }

    @Override
    public ExternalDataManagementSystemType getAddressType()
    {
        return externalDMS.getAddressType();
    }

}
