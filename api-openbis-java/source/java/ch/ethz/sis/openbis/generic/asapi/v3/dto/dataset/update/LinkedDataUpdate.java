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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.update.LinkedDataUpdate")
public class LinkedDataUpdate implements IUpdate
{
    private static final long serialVersionUID = 1L;

    /**
     * @deprecated Use {@link ContentCopyListUpdateValue} instead.
     */
    @Deprecated
    @JsonProperty
    private FieldUpdateValue<String> externalCode = new FieldUpdateValue<String>();

    /**
     * @deprecated Use {@link ContentCopyListUpdateValue} instead.
     */
    @Deprecated
    @JsonProperty
    private FieldUpdateValue<IExternalDmsId> externalDmsId = new FieldUpdateValue<IExternalDmsId>();

    @JsonProperty
    private ContentCopyListUpdateValue contentCopies = new ContentCopyListUpdateValue();

    @JsonIgnore
    public FieldUpdateValue<String> getExternalCode()
    {
        return externalCode;
    }

    @JsonIgnore
    public void setExternalCode(String externalCode)
    {
        this.externalCode.setValue(externalCode);
    }

    @JsonIgnore
    public FieldUpdateValue<IExternalDmsId> getExternalDmsId()
    {
        return externalDmsId;
    }

    @JsonIgnore
    public void setExternalDmsId(IExternalDmsId externalDmsId)
    {
        this.externalDmsId.setValue(externalDmsId);
    }

    @JsonIgnore
    public ContentCopyListUpdateValue getContentCopies()
    {
        return contentCopies;
    }

    @JsonIgnore
    public void setContentCopyActions(List<ListUpdateAction<Object>> actions)
    {
        contentCopies.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("externalDmsId", externalDmsId).append("externalCode", externalCode).toString();
    }

}
