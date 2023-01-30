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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

/**
 * @author Pawel Glyzewski
 */
public interface IExternalDataManagementSystem extends IExternalDataManagementSystemImmutable
{
    public void setLabel(String label);

    public void setUrlTemplate(String urlTemplate);

    public void setOpenBIS(boolean openBIS);

    public void setAddress(String address);

    public void setAddressType(ExternalDataManagementSystemType addressType);
}
