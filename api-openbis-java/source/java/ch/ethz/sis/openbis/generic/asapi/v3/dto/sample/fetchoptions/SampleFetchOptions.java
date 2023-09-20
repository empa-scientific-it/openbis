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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.AbstractEntityFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.sample.fetchoptions.SampleFetchOptions")
public class SampleFetchOptions extends AbstractEntityFetchOptions<Sample> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SampleTypeFetchOptions type;

    @JsonProperty
    private ProjectFetchOptions project;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private MaterialFetchOptions materialProperties;

    @JsonProperty
    private SampleFetchOptions sampleProperties;

    @JsonProperty
    private SampleFetchOptions parents;

    @JsonProperty
    private SampleFetchOptions children;

    @JsonProperty
    private SampleFetchOptions container;

    @JsonProperty
    private SampleFetchOptions components;

    @JsonProperty
    private DataSetFetchOptions dataSets;

    @JsonProperty
    private HistoryEntryFetchOptions history;

    @JsonProperty
    private HistoryEntryFetchOptions propertiesHistory;

    @JsonProperty
    private HistoryEntryFetchOptions spaceHistory;

    @JsonProperty
    private HistoryEntryFetchOptions projectHistory;

    @JsonProperty
    private HistoryEntryFetchOptions experimentHistory;

    @JsonProperty
    private HistoryEntryFetchOptions parentsHistory;

    @JsonProperty
    private HistoryEntryFetchOptions childrenHistory;

    @JsonProperty
    private HistoryEntryFetchOptions containerHistory;

    @JsonProperty
    private HistoryEntryFetchOptions componentsHistory;

    @JsonProperty
    private HistoryEntryFetchOptions dataSetsHistory;

    @JsonProperty
    private HistoryEntryFetchOptions unknownHistory;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    @JsonProperty
    private SampleSortOptions sort;

    // Method automatically generated with DtoGenerator
    public SampleTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new SampleTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with DtoGenerator
    public SampleTypeFetchOptions withTypeUsing(SampleTypeFetchOptions fetchOptions)
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
    public SpaceFetchOptions withSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpaceUsing(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSpace()
    {
        return space != null;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperimentUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasExperiment()
    {
        return experiment != null;
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
    public SampleFetchOptions withParents()
    {
        if (parents == null)
        {
            parents = new SampleFetchOptions();
        }
        return parents;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withParentsUsing(SampleFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasParents()
    {
        return parents != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withChildren()
    {
        if (children == null)
        {
            children = new SampleFetchOptions();
        }
        return children;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withChildrenUsing(SampleFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasChildren()
    {
        return children != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withContainer()
    {
        if (container == null)
        {
            container = new SampleFetchOptions();
        }
        return container;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withContainerUsing(SampleFetchOptions fetchOptions)
    {
        return container = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContainer()
    {
        return container != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withComponents()
    {
        if (components == null)
        {
            components = new SampleFetchOptions();
        }
        return components;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withComponentsUsing(SampleFetchOptions fetchOptions)
    {
        return components = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasComponents()
    {
        return components != null;
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
    public HistoryEntryFetchOptions withSpaceHistory()
    {
        if (spaceHistory == null)
        {
            spaceHistory = new HistoryEntryFetchOptions();
        }
        return spaceHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withSpaceHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return spaceHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSpaceHistory()
    {
        return spaceHistory != null;
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
    public HistoryEntryFetchOptions withExperimentHistory()
    {
        if (experimentHistory == null)
        {
            experimentHistory = new HistoryEntryFetchOptions();
        }
        return experimentHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withExperimentHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return experimentHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasExperimentHistory()
    {
        return experimentHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withParentsHistory()
    {
        if (parentsHistory == null)
        {
            parentsHistory = new HistoryEntryFetchOptions();
        }
        return parentsHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withParentsHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return parentsHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasParentsHistory()
    {
        return parentsHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withChildrenHistory()
    {
        if (childrenHistory == null)
        {
            childrenHistory = new HistoryEntryFetchOptions();
        }
        return childrenHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withChildrenHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return childrenHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasChildrenHistory()
    {
        return childrenHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withContainerHistory()
    {
        if (containerHistory == null)
        {
            containerHistory = new HistoryEntryFetchOptions();
        }
        return containerHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withContainerHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return containerHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContainerHistory()
    {
        return containerHistory != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withComponentsHistory()
    {
        if (componentsHistory == null)
        {
            componentsHistory = new HistoryEntryFetchOptions();
        }
        return componentsHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withComponentsHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return componentsHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasComponentsHistory()
    {
        return componentsHistory != null;
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
    public SampleSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new SampleSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SampleSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Sample", this);
        f.addFetchOption("Type", type);
        f.addFetchOption("Project", project);
        f.addFetchOption("Space", space);
        f.addFetchOption("Experiment", experiment);
        f.addFetchOption("Properties", properties);
        f.addFetchOption("MaterialProperties", materialProperties);
        f.addFetchOption("SampleProperties", sampleProperties);
        f.addFetchOption("Parents", parents);
        f.addFetchOption("Children", children);
        f.addFetchOption("Container", container);
        f.addFetchOption("Components", components);
        f.addFetchOption("DataSets", dataSets);
        f.addFetchOption("History", history);
        f.addFetchOption("PropertiesHistory", propertiesHistory);
        f.addFetchOption("SpaceHistory", spaceHistory);
        f.addFetchOption("ProjectHistory", projectHistory);
        f.addFetchOption("ExperimentHistory", experimentHistory);
        f.addFetchOption("ParentsHistory", parentsHistory);
        f.addFetchOption("ChildrenHistory", childrenHistory);
        f.addFetchOption("ContainerHistory", containerHistory);
        f.addFetchOption("ComponentsHistory", componentsHistory);
        f.addFetchOption("DataSetsHistory", dataSetsHistory);
        f.addFetchOption("UnknownHistory", unknownHistory);
        f.addFetchOption("Tags", tags);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Modifier", modifier);
        f.addFetchOption("Attachments", attachments);
        return f;
    }

}
