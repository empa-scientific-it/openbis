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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.project;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IIdentifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.project.Project")
public class Project implements Serializable, IAttachmentsHolder, ICodeHolder, IDescriptionHolder, IExperimentsHolder, IIdentifierHolder, IModificationDateHolder, IModifierHolder, IPermIdHolder, IRegistrationDateHolder, IRegistratorHolder, ISpaceHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ProjectFetchOptions fetchOptions;

    @JsonProperty
    private ProjectPermId permId;

    @JsonProperty
    private ProjectIdentifier identifier;

    @JsonProperty
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty
    private boolean frozen;

    @JsonProperty
    private boolean frozenForExperiments;

    @JsonProperty
    private boolean frozenForSamples;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private List<Experiment> experiments;

    @JsonProperty
    private List<Sample> samples;

    @JsonProperty
    private List<HistoryEntry> history;

    @JsonProperty
    private List<HistoryEntry> spaceHistory;

    @JsonProperty
    private List<HistoryEntry> experimentsHistory;

    @JsonProperty
    private List<HistoryEntry> samplesHistory;

    @JsonProperty
    private Space space;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private Person leader;

    @JsonProperty
    private List<Attachment> attachments;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ProjectFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(ProjectFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public ProjectPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(ProjectPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public ProjectIdentifier getIdentifier()
    {
        return identifier;
    }

    // Method automatically generated with DtoGenerator
    public void setIdentifier(ProjectIdentifier identifier)
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
    public boolean isFrozenForExperiments()
    {
        return frozenForExperiments;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForExperiments(boolean frozenForExperiments)
    {
        this.frozenForExperiments = frozenForExperiments;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isFrozenForSamples()
    {
        return frozenForSamples;
    }

    // Method automatically generated with DtoGenerator
    public void setFrozenForSamples(boolean frozenForSamples)
    {
        this.frozenForSamples = frozenForSamples;
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
    public List<Experiment> getExperiments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperiments())
        {
            return experiments;
        }
        else
        {
            throw new NotFetchedException("Experiments have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperiments(List<Experiment> experiments)
    {
        this.experiments = experiments;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<Sample> getSamples()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSamples())
        {
            return samples;
        }
        else
        {
            throw new NotFetchedException("Samples have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSamples(List<Sample> samples)
    {
        this.samples = samples;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasHistory())
        {
            return history;
        }
        else
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
    public List<HistoryEntry> getSpaceHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSpaceHistory())
        {
            return spaceHistory;
        }
        else
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
    public List<HistoryEntry> getExperimentsHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasExperimentsHistory())
        {
            return experimentsHistory;
        }
        else
        {
            throw new NotFetchedException("Experiments history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperimentsHistory(List<HistoryEntry> experimentsHistory)
    {
        this.experimentsHistory = experimentsHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getSamplesHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSamplesHistory())
        {
            return samplesHistory;
        }
        else
        {
            throw new NotFetchedException("Samples history have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSamplesHistory(List<HistoryEntry> samplesHistory)
    {
        this.samplesHistory = samplesHistory;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Space getSpace()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSpace())
        {
            return space;
        }
        else
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
    public Person getModifier()
    {
        if (getFetchOptions() != null && getFetchOptions().hasModifier())
        {
            return modifier;
        }
        else
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
    public Person getLeader()
    {
        if (getFetchOptions() != null && getFetchOptions().hasLeader())
        {
            return leader;
        }
        else
        {
            throw new NotFetchedException("Leader has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setLeader(Person leader)
    {
        this.leader = leader;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public List<Attachment> getAttachments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasAttachments())
        {
            return attachments;
        }
        else
        {
            throw new NotFetchedException("Attachments have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Project " + permId;
    }

}
