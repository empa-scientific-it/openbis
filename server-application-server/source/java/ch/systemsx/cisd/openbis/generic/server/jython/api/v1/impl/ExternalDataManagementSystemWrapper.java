/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
 * Wrapper of {@link ExternalDataManagementSystemImmutable} as {@link IExternalDataManagementSystem} where setters do nothing.
 * 
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystemWrapper extends ExternalDataManagementSystemImmutable
        implements IExternalDataManagementSystem
{
    ExternalDataManagementSystemWrapper(ExternalDataManagementSystemImmutable edms)
    {
        super(edms.getExternalDataManagementSystem());
    }

    @Override
    public void setLabel(String label)
    {
    }

    @Override
    public void setUrlTemplate(String urlTemplate)
    {
    }

    @Override
    public void setOpenBIS(boolean openBIS)
    {
    }

    @Override
    public void setAddress(String address)
    {
    }

    @Override
    public void setAddressType(ExternalDataManagementSystemType addressType)
    {
    }
}
