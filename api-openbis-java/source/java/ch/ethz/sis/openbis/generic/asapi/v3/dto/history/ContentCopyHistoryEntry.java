/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.history.ContentCopyHistoryEntry")
public class ContentCopyHistoryEntry extends HistoryEntry
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String externalCode;

    @JsonProperty
    private String path;

    @JsonProperty
    private String gitCommitHash;

    @JsonProperty
    private String gitRepositoryId;

    @JsonProperty
    private Long externalDmsId;

    @JsonProperty
    private String externalDmsCode;

    @JsonProperty
    private String externalDmsLabel;

    @JsonProperty
    private String externalDmsAddress;


    @JsonIgnore
    public String getExternalCode()
    {
        return externalCode;
    }

    @JsonIgnore
    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    @JsonIgnore
    public String getPath()
    {
        return path;
    }

    @JsonIgnore
    public void setPath(String path)
    {
        this.path = path;
    }

    @JsonIgnore
    public String getGitCommitHash()
    {
        return gitCommitHash;
    }

    @JsonIgnore
    public void setGitCommitHash(String gitCommitHash)
    {
        this.gitCommitHash = gitCommitHash;
    }

    @JsonIgnore
    public String getGitRepositoryId()
    {
        return gitRepositoryId;
    }

    @JsonIgnore
    public void setGitRepositoryId(String gitRepositoryId)
    {
        this.gitRepositoryId = gitRepositoryId;
    }

    @JsonIgnore
    public Long getExternalDmsId()
    {
        return externalDmsId;
    }

    @JsonIgnore
    public void setExternalDmsId(Long externalDmsId)
    {
        this.externalDmsId = externalDmsId;
    }

    @JsonIgnore
    public String getExternalDmsCode()
    {
        return externalDmsCode;
    }

    @JsonIgnore
    public void setExternalDmsCode(String externalDmsCode)
    {
        this.externalDmsCode = externalDmsCode;
    }

    @JsonIgnore
    public String getExternalDmsLabel()
    {
        return externalDmsLabel;
    }

    @JsonIgnore
    public void setExternalDmsLabel(String externalDmsLabel)
    {
        this.externalDmsLabel = externalDmsLabel;
    }

    @JsonIgnore
    public String getExternalDmsAddress()
    {
        return externalDmsAddress;
    }

    @JsonIgnore
    public void setExternalDmsAddress(String externalDmsAddress)
    {
        this.externalDmsAddress = externalDmsAddress;
    }

    @JsonIgnore
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

}
