/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.AbstractEntityFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.PhysicalDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.dataset.fetchoptions.DataSetFetchOptions")
public class DataSetFetchOptions extends AbstractEntityFetchOptions<DataSet> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetTypeFetchOptions type;

    @JsonProperty
    private DataStoreFetchOptions dataStore;

    @JsonProperty
    private PhysicalDataFetchOptions physicalData;

    @JsonProperty
    private LinkedDataFetchOptions linkedData;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private SampleFetchOptions sample;

    @JsonProperty
    private MaterialFetchOptions materialProperties;

    @JsonProperty
    private SampleFetchOptions sampleProperties;

    @JsonProperty
    private DataSetFetchOptions parents;

    @JsonProperty
    private DataSetFetchOptions children;

    @JsonProperty
    private DataSetFetchOptions containers;

    @JsonProperty
    private DataSetFetchOptions components;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private HistoryEntryFetchOptions history;

    @JsonProperty
    private HistoryEntryFetchOptions propertiesHistory;

    @JsonProperty
    private HistoryEntryFetchOptions experimentHistory;

    @JsonProperty
    private HistoryEntryFetchOptions sampleHistory;

    @JsonProperty
    private HistoryEntryFetchOptions parentsHistory;

    @JsonProperty
    private HistoryEntryFetchOptions childrenHistory;

    @JsonProperty
    private HistoryEntryFetchOptions containersHistory;

    @JsonProperty
    private HistoryEntryFetchOptions componentsHistory;

    @JsonProperty
    private HistoryEntryFetchOptions contentCopiesHistory;

    @JsonProperty
    private HistoryEntryFetchOptions unknownHistory;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private DataSetSortOptions sort;

    // Method automatically generated with DtoGenerator
    public DataSetTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new DataSetTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with DtoGenerator
    public DataSetTypeFetchOptions withTypeUsing(DataSetTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with DtoGenerator
    public DataStoreFetchOptions withDataStore()
    {
        if (dataStore == null)
        {
            dataStore = new DataStoreFetchOptions();
        }
        return dataStore;
    }

    // Method automatically generated with DtoGenerator
    public DataStoreFetchOptions withDataStoreUsing(DataStoreFetchOptions fetchOptions)
    {
        return dataStore = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDataStore()
    {
        return dataStore != null;
    }

    // Method automatically generated with DtoGenerator
    public PhysicalDataFetchOptions withPhysicalData()
    {
        if (physicalData == null)
        {
            physicalData = new PhysicalDataFetchOptions();
        }
        return physicalData;
    }

    // Method automatically generated with DtoGenerator
    public PhysicalDataFetchOptions withPhysicalDataUsing(PhysicalDataFetchOptions fetchOptions)
    {
        return physicalData = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPhysicalData()
    {
        return physicalData != null;
    }

    // Method automatically generated with DtoGenerator
    public LinkedDataFetchOptions withLinkedData()
    {
        if (linkedData == null)
        {
            linkedData = new LinkedDataFetchOptions();
        }
        return linkedData;
    }

    // Method automatically generated with DtoGenerator
    public LinkedDataFetchOptions withLinkedDataUsing(LinkedDataFetchOptions fetchOptions)
    {
        return linkedData = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasLinkedData()
    {
        return linkedData != null;
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
    public SampleFetchOptions withSample()
    {
        if (sample == null)
        {
            sample = new SampleFetchOptions();
        }
        return sample;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSampleUsing(SampleFetchOptions fetchOptions)
    {
        return sample = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSample()
    {
        return sample != null;
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
    public DataSetFetchOptions withParents()
    {
        if (parents == null)
        {
            parents = new DataSetFetchOptions();
        }
        return parents;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withParentsUsing(DataSetFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasParents()
    {
        return parents != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withChildren()
    {
        if (children == null)
        {
            children = new DataSetFetchOptions();
        }
        return children;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withChildrenUsing(DataSetFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasChildren()
    {
        return children != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withContainers()
    {
        if (containers == null)
        {
            containers = new DataSetFetchOptions();
        }
        return containers;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withContainersUsing(DataSetFetchOptions fetchOptions)
    {
        return containers = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContainers()
    {
        return containers != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withComponents()
    {
        if (components == null)
        {
            components = new DataSetFetchOptions();
        }
        return components;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withComponentsUsing(DataSetFetchOptions fetchOptions)
    {
        return components = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasComponents()
    {
        return components != null;
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
    public HistoryEntryFetchOptions withSampleHistory()
    {
        if (sampleHistory == null)
        {
            sampleHistory = new HistoryEntryFetchOptions();
        }
        return sampleHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withSampleHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return sampleHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSampleHistory()
    {
        return sampleHistory != null;
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
    public HistoryEntryFetchOptions withContainersHistory()
    {
        if (containersHistory == null)
        {
            containersHistory = new HistoryEntryFetchOptions();
        }
        return containersHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withContainersHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return containersHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContainersHistory()
    {
        return containersHistory != null;
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
    public HistoryEntryFetchOptions withContentCopiesHistory()
    {
        if (contentCopiesHistory == null)
        {
            contentCopiesHistory = new HistoryEntryFetchOptions();
        }
        return contentCopiesHistory;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withContentCopiesHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return contentCopiesHistory = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContentCopiesHistory()
    {
        return contentCopiesHistory != null;
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
    public DataSetSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new DataSetSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public DataSetSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("DataSet", this);
        f.addFetchOption("Type", type);
        f.addFetchOption("DataStore", dataStore);
        f.addFetchOption("PhysicalData", physicalData);
        f.addFetchOption("LinkedData", linkedData);
        f.addFetchOption("Experiment", experiment);
        f.addFetchOption("Sample", sample);
        f.addFetchOption("Properties", properties);
        f.addFetchOption("MaterialProperties", materialProperties);
        f.addFetchOption("SampleProperties", sampleProperties);
        f.addFetchOption("Parents", parents);
        f.addFetchOption("Children", children);
        f.addFetchOption("Containers", containers);
        f.addFetchOption("Components", components);
        f.addFetchOption("Tags", tags);
        f.addFetchOption("History", history);
        f.addFetchOption("PropertiesHistory", propertiesHistory);
        f.addFetchOption("ExperimentHistory", experimentHistory);
        f.addFetchOption("SampleHistory", sampleHistory);
        f.addFetchOption("ParentsHistory", parentsHistory);
        f.addFetchOption("ChildrenHistory", childrenHistory);
        f.addFetchOption("ContainersHistory", containersHistory);
        f.addFetchOption("ComponentsHistory", componentsHistory);
        f.addFetchOption("ContentCopiesHistory", contentCopiesHistory);
        f.addFetchOption("UnknownHistory", unknownHistory);
        f.addFetchOption("Modifier", modifier);
        f.addFetchOption("Registrator", registrator);
        return f;
    }

}
