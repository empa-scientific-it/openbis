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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.property;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.property.PropertyAssignment")
public class PropertyAssignment implements Serializable, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PropertyAssignmentFetchOptions fetchOptions;

    @JsonProperty
    private String section;

    @JsonProperty
    private Integer ordinal;

    @JsonProperty
    private PropertyType propertyType;

    @JsonProperty
    private Boolean mandatory;

    @JsonProperty
    private Boolean showInEditView;

    @JsonProperty
    private Boolean showRawValueInForms;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date registrationDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PropertyAssignmentFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(PropertyAssignmentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getSection()
    {
        return section;
    }

    // Method automatically generated with DtoGenerator
    public void setSection(String section)
    {
        this.section = section;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Integer getOrdinal()
    {
        return ordinal;
    }

    // Method automatically generated with DtoGenerator
    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PropertyType getPropertyType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPropertyType())
        {
            return propertyType;
        }
        else
        {
            throw new NotFetchedException("Property type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isMandatory()
    {
        return mandatory;
    }

    // Method automatically generated with DtoGenerator
    public void setMandatory(Boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isShowInEditView()
    {
        return showInEditView;
    }

    // Method automatically generated with DtoGenerator
    public void setShowInEditView(Boolean showInEditView)
    {
        this.showInEditView = showInEditView;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isShowRawValueInForms()
    {
        return showRawValueInForms;
    }

    // Method automatically generated with DtoGenerator
    public void setShowRawValueInForms(Boolean showRawValueInForms)
    {
        this.showRawValueInForms = showRawValueInForms;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "PropertyAssignment property type: " + (propertyType != null ? propertyType.getCode() : null) + ", mandatory: " + mandatory;
    }

}
