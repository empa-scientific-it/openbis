/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.InternalImageTransformationInfo;

/**
 * @author pkupczyk
 */
public class InternalImageTransformationInfoTranslator
{

    public InternalImageTransformationInfo translate(
            ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo internalTransformation)
    {
        if (internalTransformation == null)
        {
            return null;
        }

        InternalImageTransformationInfo apiTransformation = new InternalImageTransformationInfo();
        apiTransformation.setCode(internalTransformation.getCode());
        apiTransformation.setLabel(internalTransformation.getLabel());
        apiTransformation.setDescription(internalTransformation.getDescription());
        apiTransformation.setTransformationSignature(internalTransformation.getTransformationSignature());
        apiTransformation.setDefault(internalTransformation.isDefault());

        return apiTransformation;
    }

}
