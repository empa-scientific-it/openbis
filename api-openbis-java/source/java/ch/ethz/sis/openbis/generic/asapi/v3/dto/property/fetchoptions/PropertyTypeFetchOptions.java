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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.property.fetchoptions.PropertyTypeFetchOptions")
public class PropertyTypeFetchOptions extends FetchOptions<PropertyType> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private VocabularyFetchOptions vocabulary;

    @JsonProperty
    private MaterialTypeFetchOptions materialType;

    @JsonProperty
    private SampleTypeFetchOptions sampleType;

    @JsonProperty
    private SemanticAnnotationFetchOptions semanticAnnotations;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PropertyTypeSortOptions sort;

    // Method automatically generated with DtoGenerator
    public VocabularyFetchOptions withVocabulary()
    {
        if (vocabulary == null)
        {
            vocabulary = new VocabularyFetchOptions();
        }
        return vocabulary;
    }

    // Method automatically generated with DtoGenerator
    public VocabularyFetchOptions withVocabularyUsing(VocabularyFetchOptions fetchOptions)
    {
        return vocabulary = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasVocabulary()
    {
        return vocabulary != null;
    }

    // Method automatically generated with DtoGenerator
    public MaterialTypeFetchOptions withMaterialType()
    {
        if (materialType == null)
        {
            materialType = new MaterialTypeFetchOptions();
        }
        return materialType;
    }

    // Method automatically generated with DtoGenerator
    public MaterialTypeFetchOptions withMaterialTypeUsing(MaterialTypeFetchOptions fetchOptions)
    {
        return materialType = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasMaterialType()
    {
        return materialType != null;
    }

    public SampleTypeFetchOptions withSampleType()
    {
        if (sampleType == null)
        {
            sampleType = new SampleTypeFetchOptions();
        }
        return sampleType;
    }

    public SampleTypeFetchOptions withSampleTypeUsing(SampleTypeFetchOptions fetchOptions)
    {
        return sampleType = fetchOptions;
    }

    public boolean hasSampleType()
    {
        return sampleType != null;
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
    @Override
    public PropertyTypeSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PropertyTypeSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public PropertyTypeSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("PropertyType", this);
        f.addFetchOption("Vocabulary", vocabulary);
        f.addFetchOption("MaterialType", materialType);
        f.addFetchOption("SampleType", sampleType);
        f.addFetchOption("SemanticAnnotations", semanticAnnotations);
        f.addFetchOption("Registrator", registrator);
        return f;
    }

}
