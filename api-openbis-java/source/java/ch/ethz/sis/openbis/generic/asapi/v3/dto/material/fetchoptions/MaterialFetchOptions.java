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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.AbstractEntityFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.material.fetchoptions.MaterialFetchOptions")
public class MaterialFetchOptions extends AbstractEntityFetchOptions<Material> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private MaterialTypeFetchOptions type;

    @JsonProperty
    private HistoryEntryFetchOptions history;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private MaterialFetchOptions materialProperties;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private MaterialSortOptions sort;

    // Method automatically generated with DtoGenerator
    public MaterialTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new MaterialTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with DtoGenerator
    public MaterialTypeFetchOptions withTypeUsing(MaterialTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withHistory()
    {
        if (history == null)
        {
            history = new HistoryEntryFetchOptions();
        }
        return history;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return history = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasHistory()
    {
        return history != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
    }


    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterialProperties()
    {
        if (materialProperties == null)
        {
            materialProperties = new MaterialFetchOptions();
        }
        return materialProperties;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterialPropertiesUsing(MaterialFetchOptions fetchOptions)
    {
        return materialProperties = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasMaterialProperties()
    {
        return materialProperties != null;
    }

    // Method automatically generated with DtoGenerator
    public TagFetchOptions withTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    // Method automatically generated with DtoGenerator
    public TagFetchOptions withTagsUsing(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasTags()
    {
        return tags != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public MaterialSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new MaterialSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public MaterialSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Material", this);
        f.addFetchOption("Type", type);
        f.addFetchOption("History", history);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Properties", properties);
        f.addFetchOption("MaterialProperties", materialProperties);
        f.addFetchOption("Tags", tags);
        return f;
    }

}
