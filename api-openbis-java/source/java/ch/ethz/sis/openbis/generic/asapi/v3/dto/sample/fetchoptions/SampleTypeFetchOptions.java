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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.sample.fetchoptions.SampleTypeFetchOptions")
public class SampleTypeFetchOptions extends FetchOptions<SampleType> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PropertyAssignmentFetchOptions propertyAssignments;

    @JsonProperty
    private SemanticAnnotationFetchOptions semanticAnnotations;

    @JsonProperty
    private PluginFetchOptions validationPlugin;

    @JsonProperty
    private SampleTypeSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PropertyAssignmentFetchOptions withPropertyAssignments()
    {
        if (propertyAssignments == null)
        {
            propertyAssignments = new PropertyAssignmentFetchOptions();
        }
        return propertyAssignments;
    }

    // Method automatically generated with DtoGenerator
    public PropertyAssignmentFetchOptions withPropertyAssignmentsUsing(PropertyAssignmentFetchOptions fetchOptions)
    {
        return propertyAssignments = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPropertyAssignments()
    {
        return propertyAssignments != null;
    }

    // Method automatically generated with DtoGenerator
    public SemanticAnnotationFetchOptions withSemanticAnnotations()
    {
        if (semanticAnnotations == null)
        {
            semanticAnnotations = new SemanticAnnotationFetchOptions();
        }
        return semanticAnnotations;
    }

    // Method automatically generated with DtoGenerator
    public SemanticAnnotationFetchOptions withSemanticAnnotationsUsing(SemanticAnnotationFetchOptions fetchOptions)
    {
        return semanticAnnotations = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSemanticAnnotations()
    {
        return semanticAnnotations != null;
    }

    public PluginFetchOptions withValidationPlugin()
    {
        if (validationPlugin == null)
        {
            validationPlugin = new PluginFetchOptions();
        }
        return validationPlugin;
    }

    public PluginFetchOptions withValidationPluginUsing(PluginFetchOptions fetchOptions)
    {
        return validationPlugin = fetchOptions;
    }

    public boolean hasValidationPlugin()
    {
        return validationPlugin != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SampleTypeSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new SampleTypeSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SampleTypeSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("SampleType", this);
        f.addFetchOption("PropertyAssignments", propertyAssignments);
        f.addFetchOption("SemanticAnnotations", semanticAnnotations);
        f.addFetchOption("ValidationPlugin", validationPlugin);
        return f;
    }

}
