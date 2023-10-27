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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity.AbstractEntity;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDataSetsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.sample.Sample")
public class Sample extends AbstractEntity<Sample>
        implements Serializable, IAttachmentsHolder, ICodeHolder, IDataSetsHolder,
        IEntityTypeHolder, IExperimentHolder, IIdentifierHolder, IMaterialPropertiesHolder,
        IModificationDateHolder, IModifierHolder, IParentChildrenHolder<Sample>, IPermIdHolder,
        IProjectHolder, IPropertiesHolder, IRegistrationDateHolder, IRegistratorHolder,
        ISpaceHolder, ITagsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SamplePermId permId;

    @JsonProperty
    private SampleIdentifier identifier;

    @JsonProperty
    private String code;

    @JsonProperty
    private boolean frozen;

    @JsonProperty
    private boolean frozenForComponents;

    @JsonProperty
    private boolean frozenForChildren;

    @JsonProperty
    private boolean frozenForParents;

    @JsonProperty
    private boolean frozenForDataSets;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private SampleType type;

    @JsonProperty
    private Project project;

    @JsonProperty
    private Space space;

    @JsonProperty
    private Experiment experiment;

    @JsonProperty
    private Map<String, Material> materialProperties;

    @JsonProperty
    private Map<String, Sample[]> sampleProperties;

    @JsonProperty
    private List<Sample> parents;

    @JsonProperty
    private Map<SamplePermId, Relationship> parentsRelationships;

    @JsonProperty
    private List<Sample> children;

    @JsonProperty
    private Map<SamplePermId, Relationship> childrenRelationships;

    @JsonProperty
    private Sample container;

    @JsonProperty
    private List<Sample> components;

    @JsonProperty
    private List<DataSet> dataSets;

    @JsonProperty
    private List<HistoryEntry> history;

    @JsonProperty
    private List<HistoryEntry> propertiesHistory;

    @JsonProperty
    private List<HistoryEntry> spaceHistory;

    @JsonProperty
    private List<HistoryEntry> projectHistory;

    @JsonProperty
    private List<HistoryEntry> experimentHistory;

    @JsonProperty
    private List<HistoryEntry> parentsHistory;

    @JsonProperty
    private List<HistoryEntry> childrenHistory;

    @JsonProperty
    private List<HistoryEntry> containerHistory;

    @JsonProperty
    private List<HistoryEntry> componentsHistory;

    @JsonProperty
    private List<HistoryEntry> dataSetsHistory;

    @JsonProperty
    private List<HistoryEntry> unknownHistory;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private List<Attachment> attachments;

    @JsonProperty
    private Map<String, String> metaData;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public SampleFetchOptions getFetchOptions()
    {
        return (SampleFetchOptions) super.getFetchOptions();
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(SampleFetchOptions fetchOptions)
    {
        super.setFetchOptions(fetchOptions);
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public SamplePermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(SamplePermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public SampleIdentifier getIdentifier()
    {
        return identifier;
    }

    // Method automatically generated with DtoGenerator
    public void setIdentifier(SampleIdentifier identifier)
    {
        this.identifier = identifier;
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
    public boolean isFrozen()
    {
        return frozen;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForComponents()
    {
        return frozenForComponents;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForComponents(boolean frozenForComponents)
    {
        this.frozenForComponents = frozenForComponents;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForChildren()
    {
        return frozenForChildren;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForChildren(boolean frozenForChildren)
    {
        this.frozenForChildren = frozenForChildren;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForParents()
    {
        return frozenForParents;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForParents(boolean frozenForParents)
    {
        this.frozenForParents = frozenForParents;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForDataSets()
    {
        return frozenForDataSets;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForDataSets(boolean frozenForDataSets)
    {
        this.frozenForDataSets = frozenForDataSets;
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
    public SampleType getType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasType())
        {
            return type;
        } else
        {
            throw new NotFetchedException("Sample type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setType(SampleType type)
    {
        this.type = type;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Project getProject()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProject())
        {
            return project;
        } else
        {
            throw new NotFetchedException("Project has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProject(Project project)
    {
        this.project = project;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Space getSpace()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSpace())
        {
            return space;
        } else
        {
            throw new NotFetchedException("Space has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSpace(Space space)
    {
        this.space = space;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Experiment getExperiment()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperiment())
        {
            return experiment;
        } else
        {
            throw new NotFetchedException("Experiment has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Map<String, Material> getMaterialProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasMaterialProperties())
        {
            return materialProperties;
        } else
        {
            throw new NotFetchedException("Material Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    @Override
    public void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Map<String, Sample[]> getSampleProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSampleProperties())
        {
            return sampleProperties;
        } else
        {
            throw new NotFetchedException("Sample Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSampleProperties(Map<String, Sample[]> sampleProperties)
    {
        this.sampleProperties = sampleProperties;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Sample> getParents()
    {
        if (getFetchOptions() != null && getFetchOptions().hasParents())
        {
            return parents;
        } else
        {
            throw new NotFetchedException("Parents have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setParents(List<Sample> parents)
    {
        this.parents = parents;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Map<SamplePermId, Relationship> getParentsRelationships()
    {
        if (getFetchOptions() != null && getFetchOptions().hasParents())
        {
            return parentsRelationships;
        } else
        {
            throw new NotFetchedException("Parents have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setParentsRelationships(Map<SamplePermId, Relationship> parentsRelationships)
    {
        this.parentsRelationships = parentsRelationships;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Sample> getChildren()
    {
        if (getFetchOptions() != null && getFetchOptions().hasChildren())
        {
            return children;
        } else
        {
            throw new NotFetchedException("Children have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setChildren(List<Sample> children)
    {
        this.children = children;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Map<SamplePermId, Relationship> getChildrenRelationships()
    {
        if (getFetchOptions() != null && getFetchOptions().hasChildren())
        {
            return childrenRelationships;
        } else
        {
            throw new NotFetchedException("Children have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setChildrenRelationships(Map<SamplePermId, Relationship> childrenRelationships)
    {
        this.childrenRelationships = childrenRelationships;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Sample getContainer()
    {
        if (getFetchOptions() != null && getFetchOptions().hasContainer())
        {
            return container;
        } else
        {
            throw new NotFetchedException("Container sample has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setContainer(Sample container)
    {
        this.container = container;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<Sample> getComponents()
    {
        if (getFetchOptions() != null && getFetchOptions().hasComponents())
        {
            return components;
        } else
        {
            throw new NotFetchedException("Component samples have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setComponents(List<Sample> components)
    {
        this.components = components;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<DataSet> getDataSets()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataSets())
        {
            return dataSets;
        } else
        {
            throw new NotFetchedException("Data sets have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSets(List<DataSet> dataSets)
    {
        this.dataSets = dataSets;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasHistory())
        {
            return history;
        } else
        {
            throw new NotFetchedException("History have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setHistory(List<HistoryEntry> history)
    {
        this.history = history;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getPropertiesHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPropertiesHistory())
        {
            return propertiesHistory;
        } else
        {
            throw new NotFetchedException("Properties history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPropertiesHistory(List<HistoryEntry> propertiesHistory)
    {
        this.propertiesHistory = propertiesHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getSpaceHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSpaceHistory())
        {
            return spaceHistory;
        } else
        {
            throw new NotFetchedException("Space history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSpaceHistory(List<HistoryEntry> spaceHistory)
    {
        this.spaceHistory = spaceHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getProjectHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProjectHistory())
        {
            return projectHistory;
        } else
        {
            throw new NotFetchedException("Project history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProjectHistory(List<HistoryEntry> projectHistory)
    {
        this.projectHistory = projectHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getExperimentHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperimentHistory())
        {
            return experimentHistory;
        } else
        {
            throw new NotFetchedException("Experiment history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperimentHistory(List<HistoryEntry> experimentHistory)
    {
        this.experimentHistory = experimentHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getParentsHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasParentsHistory())
        {
            return parentsHistory;
        } else
        {
            throw new NotFetchedException("Parents history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setParentsHistory(List<HistoryEntry> parentsHistory)
    {
        this.parentsHistory = parentsHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getChildrenHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasChildrenHistory())
        {
            return childrenHistory;
        } else
        {
            throw new NotFetchedException("Children history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setChildrenHistory(List<HistoryEntry> childrenHistory)
    {
        this.childrenHistory = childrenHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getContainerHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasContainerHistory())
        {
            return containerHistory;
        } else
        {
            throw new NotFetchedException("Container history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setContainerHistory(List<HistoryEntry> containerHistory)
    {
        this.containerHistory = containerHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getComponentsHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasComponentsHistory())
        {
            return componentsHistory;
        } else
        {
            throw new NotFetchedException("Components history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setComponentsHistory(List<HistoryEntry> componentsHistory)
    {
        this.componentsHistory = componentsHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getDataSetsHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasDataSetsHistory())
        {
            return dataSetsHistory;
        } else
        {
            throw new NotFetchedException("Data sets history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSetsHistory(List<HistoryEntry> dataSetsHistory)
    {
        this.dataSetsHistory = dataSetsHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getUnknownHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasUnknownHistory())
        {
            return unknownHistory;
        } else
        {
            throw new NotFetchedException("Unknown history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setUnknownHistory(List<HistoryEntry> unknownHistory)
    {
        this.unknownHistory = unknownHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Set<Tag> getTags()
    {
        if (getFetchOptions() != null && getFetchOptions().hasTags())
        {
            return tags;
        } else
        {
            throw new NotFetchedException("Tags have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
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
    public Person getModifier()
    {
        if (getFetchOptions() != null && getFetchOptions().hasModifier())
        {
            return modifier;
        } else
        {
            throw new NotFetchedException("Modifier has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Attachment> getAttachments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasAttachments())
        {
            return attachments;
        } else
        {
            throw new NotFetchedException("Attachments have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    @Override
    public Material getMaterialProperty(String propertyName)
    {
        return getMaterialProperties() != null ? getMaterialProperties().get(propertyName) : null;
    }

    @Override
    public void setMaterialProperty(String propertyName, Material propertyValue)
    {
        if (materialProperties == null)
        {
            materialProperties = new HashMap<String, Material>();
        }
        materialProperties.put(propertyName, propertyValue);
    }

    @JsonIgnore
    public Relationship getParentRelationship(ISampleId parentId)
    {
        Map<SamplePermId, Relationship> relationships = getParentsRelationships();
        return relationships == null ? new Relationship() : relationships.get(parentId);
    }

    @JsonIgnore
    public Relationship getChildRelationship(ISampleId childId)
    {
        Map<SamplePermId, Relationship> relationships = getChildrenRelationships();
        return relationships == null ? new Relationship() : relationships.get(childId);
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
        return "Sample " + permId;
    }

}
