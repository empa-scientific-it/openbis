/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.create.LinkedDataCreation")
public class LinkedDataCreation implements ICreation
{
    private static final long serialVersionUID = 1L;

    /**
     * @deprecated Use {@link ContentCopyCreation} instead.
     */
    @Deprecated
    @JsonProperty
    private String externalCode;

    /**
     * @deprecated Use {@link ContentCopyCreation} instead.
     */
    @Deprecated
    @JsonProperty
    private IExternalDmsId externalDmsId;

    private List<ContentCopyCreation> contentCopies;

    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    public IExternalDmsId getExternalDmsId()
    {
        return externalDmsId;
    }

    public void setExternalDmsId(IExternalDmsId externalDmsId)
    {
        this.externalDmsId = externalDmsId;
    }

    public List<ContentCopyCreation> getContentCopies()
    {
        return contentCopies;
    }

    public void setContentCopies(List<ContentCopyCreation> contentCopies)
    {
        this.contentCopies = contentCopies;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("externalDmsId", externalDmsId).append("externalCode", externalCode).toString();
    }

}
