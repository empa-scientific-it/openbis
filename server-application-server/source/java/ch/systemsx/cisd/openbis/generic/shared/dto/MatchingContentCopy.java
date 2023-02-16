/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

public class MatchingContentCopy implements IRelatedEntity
{

    private String externalCode;

    private String path;

    @SuppressWarnings("unused")
    private String gitCommitHash;

    @SuppressWarnings("unused")
    private String gitRepositoryId;

    @SuppressWarnings("unused")
    private String externalDmsCode;

    @SuppressWarnings("unused")
    private String externalDmsLabel;

    @SuppressWarnings("unused")
    private String externalDmsAddress;

    @SuppressWarnings("unused")
    private ExternalDataManagementSystemPE externalDms;

    public MatchingContentCopy(String externalCode, String path, String gitCommitHash, String gitRepositoryId,
            String externalDmsCode, String externalDmsLabel, String externalDmsAddress, ExternalDataManagementSystemPE externalDms)
    {
        this.externalCode = externalCode;
        this.path = path;
        this.gitCommitHash = gitCommitHash;
        this.gitRepositoryId = gitRepositoryId;
        this.externalDmsCode = externalDmsCode;
        this.externalDmsLabel = externalDmsLabel;
        this.externalDmsAddress = externalDmsAddress;
        this.externalDms = externalDms;
    }

    @Override
    public String toString()
    {
        if (externalDms.getAddressType().equals(ExternalDataManagementSystemType.FILE_SYSTEM))
        {
            return externalDmsAddress + path;
        } else
        {
            return externalDmsAddress.replaceAll(Pattern.quote("${") + ".*" + Pattern.quote("}"), externalCode);
        }
    }

}
