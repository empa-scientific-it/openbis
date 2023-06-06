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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.dataset.DataSetType")
public class DataSetType implements Serializable, ICodeHolder, IDescriptionHolder, IEntityType, IModificationDateHolder, IPermIdHolder, IPropertyAssignmentsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetTypeFetchOptions fetchOptions;

    @JsonProperty
    private EntityTypePermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty
    private String mainDataSetPattern;

    @JsonProperty
    private String mainDataSetPath;

    @JsonProperty
    private Boolean disallowDeletion;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private List<PropertyAssignment> propertyAssignments;

    @JsonProperty
    private Plugin validationPlugin;

    @JsonProperty
    private Map<String, String> metaData;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DataSetTypeFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(DataSetTypeFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public EntityTypePermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(EntityTypePermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    // Method automatically generated with DtoGenerator
    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    // Method automatically generated with DtoGenerator
    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isDisallowDeletion()
    {
        return disallowDeletion;
    }

    // Method automatically generated with DtoGenerator
    public void setDisallowDeletion(Boolean disallowDeletion)
    {
        this.disallowDeletion = disallowDeletion;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<PropertyAssignment> getPropertyAssignments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPropertyAssignments())
        {
            return propertyAssignments;
        }
        else
        {
            throw new NotFetchedException("Property assigments have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPropertyAssignments(List<PropertyAssignment> propertyAssignments)
    {
        this.propertyAssignments = propertyAssignments;
    }

    @JsonIgnore
    public Plugin getValidationPlugin()
    {
        if (getFetchOptions() != null && getFetchOptions().hasValidationPlugin())
        {
            return validationPlugin;
        }
        else
        {
            throw new NotFetchedException("Validation plugin has not been fetched.");
        }
    }

    public void setValidationPlugin(Plugin validationPlugin)
    {
        this.validationPlugin = validationPlugin;
    }

    @JsonIgnore
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "DataSetType " + code;
    }

}
