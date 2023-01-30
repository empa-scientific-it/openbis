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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * Translator for {@link ExternalDataManagementSystemPE} into {@link ExternalDataManagementSystem} and other way around.
 * 
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystemTranslator
{
    public static ExternalDataManagementSystem translate(ExternalDataManagementSystemPE edms)
    {
        if (edms == null)
        {
            return null;
        }

        ExternalDataManagementSystem result = new ExternalDataManagementSystem();

        result.setId(edms.getId());
        result.setCode(edms.getCode());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate());
        result.setLabel(edms.getLabel());
        result.setUrlTemplate(edms.getAddress());
        result.setAddress(edms.getAddress());

        if (edms.getAddressType() != null)
        {
            result.setAddressType(edms.getAddressType());
            result.setOpenBIS(ExternalDataManagementSystemType.OPENBIS.equals(edms.getAddressType()));
        } else
        {
            result.setOpenBIS(edms.isOpenBIS());
            if (edms.isOpenBIS())
            {
                result.setAddressType(ExternalDataManagementSystemType.OPENBIS);
            } else
            {
                result.setAddressType(ExternalDataManagementSystemType.URL);
            }
        }

        return result;
    }

    public static ExternalDataManagementSystemPE translate(ExternalDataManagementSystem edms,
            ExternalDataManagementSystemPE result)
    {
        result.setCode(edms.getCode());
        result.setLabel(edms.getLabel());
        if (edms.getAddress() != null)
        {
            result.setAddress(edms.getAddress());
        } else
        {
            result.setAddress(edms.getUrlTemplate());
        }
        if (edms.getAddressType() != null)
        {
            result.setAddressType(edms.getAddressType());
        } else
        {
            if (edms.isOpenBIS())
            {
                result.setAddressType(ExternalDataManagementSystemType.OPENBIS);
            } else
            {
                result.setAddressType(ExternalDataManagementSystemType.URL);
            }
        }

        return result;
    }
}
