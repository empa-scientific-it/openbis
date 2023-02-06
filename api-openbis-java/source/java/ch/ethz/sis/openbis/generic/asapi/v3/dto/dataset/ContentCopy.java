/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ContentCopyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.dataset.ContentCopy")
public class ContentCopy implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ContentCopyPermId id;

    @JsonProperty
    private ExternalDms externalDms;

    @JsonProperty
    private String externalCode;

    @JsonProperty
    private String path;

    @JsonProperty
    private String gitCommitHash;

    @JsonProperty
    private String gitRepositoryId;

    @JsonIgnore
    public ContentCopyPermId getId()
    {
        return id;
    }

    public void setId(ContentCopyPermId id)
    {
        this.id = id;
    }

    @JsonIgnore
    public ExternalDms getExternalDms()
    {
        return externalDms;
    }

    public void setExternalDms(ExternalDms externalDms)
    {
        this.externalDms = externalDms;
    }

    @JsonIgnore
    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    @JsonIgnore
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @JsonIgnore
    public String getGitCommitHash()
    {
        return gitCommitHash;
    }

    public void setGitCommitHash(String gitCommitHash)
    {
        this.gitCommitHash = gitCommitHash;
    }

    @JsonIgnore
    public String getGitRepositoryId()
    {
        return gitRepositoryId;
    }

    public void setGitRepositoryId(String gitRepositoryId)
    {
        this.gitRepositoryId = gitRepositoryId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("id", id).toString();
    }

}
