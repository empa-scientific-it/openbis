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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.externaldms.update.ExternalDmsUpdate")
public class ExternalDmsUpdate implements IUpdate, IObjectUpdate<IExternalDmsId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IExternalDmsId externalDmsId;

    @JsonProperty
    private FieldUpdateValue<String> label = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> address = new FieldUpdateValue<String>();

    @Override
    @JsonIgnore
    public IExternalDmsId getObjectId()
    {
        return getExternalDmsId();
    }

    @JsonIgnore
    public IExternalDmsId getExternalDmsId()
    {
        return externalDmsId;
    }

    @JsonIgnore
    public void setExternalDmsId(IExternalDmsId externalDmsId)
    {
        this.externalDmsId = externalDmsId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getLabel()
    {
        return label;
    }

    @JsonIgnore
    public void setLabel(String label)
    {
        this.label.setValue(label);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getAddress()
    {
        return address;
    }

    @JsonIgnore
    public void setAddress(String address)
    {
        this.address.setValue(address);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("externalDmsId", externalDmsId).toString();
    }

}
