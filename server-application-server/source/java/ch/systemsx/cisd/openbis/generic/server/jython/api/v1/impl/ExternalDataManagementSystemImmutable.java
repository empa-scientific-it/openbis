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

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

/**
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystemImmutable implements
        IExternalDataManagementSystemImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem edms;

    ExternalDataManagementSystemImmutable(ExternalDataManagementSystem edms)
    {
        this.edms = edms;
    }

    ExternalDataManagementSystemImmutable(String code)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem());
        getExternalDataManagementSystem().setCode(code);
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem getExternalDataManagementSystem()
    {
        return edms;
    }

    @Override
    public String getCode()
    {
        return getExternalDataManagementSystem().getCode();
    }

    @Override
    public String getLabel()
    {
        return getExternalDataManagementSystem().getLabel();
    }

    @Override
    public String getUrlTemplate()
    {
        return getExternalDataManagementSystem().getUrlTemplate();
    }

    @Override
    public boolean isOpenBIS()
    {
        return getExternalDataManagementSystem().isOpenBIS();
    }

    @Override
    public String getAddress()
    {
        return getExternalDataManagementSystem().getAddress();
    }

    @Override
    public ExternalDataManagementSystemType getAddressType()
    {
        return getExternalDataManagementSystem().getAddressType();
    }
}
