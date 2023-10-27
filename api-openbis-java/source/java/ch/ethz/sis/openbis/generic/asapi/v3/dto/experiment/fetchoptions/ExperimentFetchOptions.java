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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.AbstractEntityFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.experiment.fetchoptions.ExperimentFetchOptions")
public class ExperimentFetchOptions extends AbstractEntityFetchOptions<Experiment> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentTypeFetchOptions type;

    @JsonProperty
    private ProjectFetchOptions project;

    @JsonProperty
    private DataSetFetchOptions dataSets;

    @JsonProperty
    private SampleFetchOptions samples;

    @JsonProperty
    private HistoryEntryFetchOptions history;

    @JsonProperty
    private HistoryEntryFetchOptions propertiesHistory;

    @JsonProperty
    private HistoryEntryFetchOptions projectHistory;

    @JsonProperty
    private HistoryEntryFetchOptions samplesHistory;

    @JsonProperty
    private HistoryEntryFetchOptions dataSetsHistory;

    @JsonProperty
    private HistoryEntryFetchOptions unknownHistory;

    @JsonProperty
    private MaterialFetchOptions materialProperties;

    @JsonProperty
    private SampleFetchOptions sampleProperties;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    @JsonProperty
    private ExperimentSortOptions sort;

    // Method automatically generated with DtoGenerator
    public ExperimentTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new ExperimentTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentTypeFetchOptions withTypeUsing(ExperimentTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with DtoGenerator
    public ProjectFetchOptions withProject()
    {
        if (project == null)
        {
            project = new ProjectFetchOptions();
        }
        return project;
    }

    // Method automatically generated with DtoGenerator
    public ProjectFetchOptions withProjectUsing(ProjectFetchOptions fetchOptions)
    {
        return project = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasProject()
    {
        return project != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSets()
    {
        if (dataSets == null)
        {
            dataSets = new DataSetFetchOptions();
        }
        return dataSets;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSetsUsing(DataSetFetchOptions fetchOptions)
    {
        return dataSets = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDataSets()
    {
        return dataSets != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamples()
    {
        if (samples == null)
        {
            samples = new SampleFetchOptions();
        }
        return samples;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamplesUsing(SampleFetchOptions fetchOptions)
    {
        return samples = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSamples()
    {
        return samples != null;
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
    public HistoryEntryFetchOptions withPropertiesHistory()
    {
        if (propertiesHistory == null)
        {
            propertiesHistory = new HistoryEntryFetchOptions();
        }
        return propertiesHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withPropertiesHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return propertiesHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPropertiesHistory()
    {
        return propertiesHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withProjectHistory()
    {
        if (projectHistory == null)
        {
            projectHistory = new HistoryEntryFetchOptions();
        }
        return projectHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withProjectHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return projectHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasProjectHistory()
    {
        return projectHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withSamplesHistory()
    {
        if (samplesHistory == null)
        {
            samplesHistory = new HistoryEntryFetchOptions();
        }
        return samplesHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withSamplesHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return samplesHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSamplesHistory()
    {
        return samplesHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withDataSetsHistory()
    {
        if (dataSetsHistory == null)
        {
            dataSetsHistory = new HistoryEntryFetchOptions();
        }
        return dataSetsHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withDataSetsHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return dataSetsHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDataSetsHistory()
    {
        return dataSetsHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withUnknownHistory()
    {
        if (unknownHistory == null)
        {
            unknownHistory = new HistoryEntryFetchOptions();
        }
        return unknownHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withUnknownHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return unknownHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasUnknownHistory()
    {
        return unknownHistory != null;
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
    public SampleFetchOptions withSampleProperties()
    {
        if (sampleProperties == null)
        {
            sampleProperties = new SampleFetchOptions();
        }
        return sampleProperties;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamplePropertiesUsing(SampleFetchOptions fetchOptions)
    {
        return sampleProperties = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSampleProperties()
    {
        return sampleProperties != null;
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
    public PersonFetchOptions withModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withModifierUsing(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasModifier()
    {
        return modifier != null;
    }

    // Method automatically generated with DtoGenerator
    public AttachmentFetchOptions withAttachments()
    {
        if (attachments == null)
        {
            attachments = new AttachmentFetchOptions();
        }
        return attachments;
    }

    // Method automatically generated with DtoGenerator
    public AttachmentFetchOptions withAttachmentsUsing(AttachmentFetchOptions fetchOptions)
    {
        return attachments = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasAttachments()
    {
        return attachments != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public ExperimentSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new ExperimentSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public ExperimentSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Experiment", this);
        f.addFetchOption("Type", type);
        f.addFetchOption("Project", project);
        f.addFetchOption("DataSets", dataSets);
        f.addFetchOption("Samples", samples);
        f.addFetchOption("History", history);
        f.addFetchOption("PropertiesHistory", propertiesHistory);
        f.addFetchOption("ProjectHistory", projectHistory);
        f.addFetchOption("SamplesHistory", samplesHistory);
        f.addFetchOption("DataSetsHistory", dataSetsHistory);
        f.addFetchOption("UnknownHistory", unknownHistory);
        f.addFetchOption("Properties", properties);
        f.addFetchOption("MaterialProperties", materialProperties);
        f.addFetchOption("SampleProperties", sampleProperties);
        f.addFetchOption("Tags", tags);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Modifier", modifier);
        f.addFetchOption("Attachments", attachments);
        return f;
    }

}
