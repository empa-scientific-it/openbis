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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.objectkindmodification.ObjectKindModification")
public class ObjectKindModification implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ObjectKindModificationFetchOptions fetchOptions;

    @JsonProperty
    private ObjectKind objectKind;

    @JsonProperty
    private OperationKind operationKind;

    @JsonProperty
    private Date lastModificationTimeStamp;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ObjectKindModificationFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(ObjectKindModificationFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ObjectKind getObjectKind()
    {
        return objectKind;
    }

    // Method automatically generated with DtoGenerator
    public void setObjectKind(ObjectKind objectKind)
    {
        this.objectKind = objectKind;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public OperationKind getOperationKind()
    {
        return operationKind;
    }

    // Method automatically generated with DtoGenerator
    public void setOperationKind(OperationKind operationKind)
    {
        this.operationKind = operationKind;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getLastModificationTimeStamp()
    {
        return lastModificationTimeStamp;
    }

    // Method automatically generated with DtoGenerator
    public void setLastModificationTimeStamp(Date lastModificationTimeStamp)
    {
        this.lastModificationTimeStamp = lastModificationTimeStamp;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Last " + operationKind + " operation of an object of kind " + objectKind + " occured at " +  lastModificationTimeStamp;
    }

}
