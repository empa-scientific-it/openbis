/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.UnmodifiableListDecorator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistent entity representing script definition.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.SCRIPTS_TABLE)
public class ScriptPE extends HibernateAbstractRegistrationHolder implements IIdentityHolder,
        Comparable<ScriptPE>, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    protected Long id;

    private ScriptType scriptType;

    private PluginType pluginType;

    private String name;

    private String description;

    private String script;

    private EntityKind entityKind;

    private boolean available;

    //
    // assignments using the script - readonly
    //

    private List<SampleTypePropertyTypePE> sampleAssignments =
            new ArrayList<SampleTypePropertyTypePE>();

    private List<ExperimentTypePropertyTypePE> experimentAssignments =
            new ArrayList<ExperimentTypePropertyTypePE>();

    private List<MaterialTypePropertyTypePE> materialAssignments =
            new ArrayList<MaterialTypePropertyTypePE>();

    private List<DataSetTypePropertyTypePE> dataSetAssignments =
            new ArrayList<DataSetTypePropertyTypePE>();

    private Date modificationDate;

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @SequenceGenerator(name = SequenceNames.SCRIPT_SEQUENCE, sequenceName = SequenceNames.SCRIPT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SCRIPT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Override
    @Transient
    public String getPermId()
    {
        return getName();
    }

    @Override
    @Transient
    public String getIdentifier()
    {
        return getName();
    }

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.NAME_LENGTH_MESSAGE)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Version
    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @Column(name = ColumnNames.SCRIPT_COLUMN)
    @Length(min = 1, message = ValidationMessages.EXPRESSION_LENGTH_MESSAGE)
    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ScriptPE == false)
        {
            return false;
        }
        final ScriptPE that = (ScriptPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), that.getName());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getName();
    }

    @Override
    public int compareTo(ScriptPE that)
    {
        final String thatName = that.getName();
        final String thisName = getName();
        if (thisName == null)
        {
            return thatName == null ? 0 : -1;
        }
        if (thatName == null)
        {
            return 1;
        }
        return thisName.compareTo(thatName);
    }

    //
    // assignments using the script - readonly
    //

    @Transient
    /** all dynamic property assignments using the script */
    public List<EntityTypePropertyTypePE> getPropertyAssignments()
    {
        List<EntityTypePropertyTypePE> assignments = new ArrayList<EntityTypePropertyTypePE>();
        assignments.addAll(getDataSetAssignments());
        assignments.addAll(getExperimentAssignments());
        assignments.addAll(getMaterialAssignments());
        assignments.addAll(getSampleAssignments());
        return new UnmodifiableListDecorator<EntityTypePropertyTypePE>(assignments);
    }

    @Private
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    // exposed for tests
    public List<SampleTypePropertyTypePE> getSampleAssignments()
    {
        return sampleAssignments;
    }

    @SuppressWarnings("unused")
    private void setSampleAssignments(List<SampleTypePropertyTypePE> sampleAssignments)
    {
        this.sampleAssignments = sampleAssignments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<ExperimentTypePropertyTypePE> getExperimentAssignments()
    {
        return experimentAssignments;
    }

    @SuppressWarnings("unused")
    private void setExperimentAssignments(List<ExperimentTypePropertyTypePE> experimentAssignments)
    {
        this.experimentAssignments = experimentAssignments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<MaterialTypePropertyTypePE> getMaterialAssignments()
    {
        return materialAssignments;
    }

    @SuppressWarnings("unused")
    private void setMaterialAssignments(List<MaterialTypePropertyTypePE> materialAssignments)
    {
        this.materialAssignments = materialAssignments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "script")
    private List<DataSetTypePropertyTypePE> getDataSetAssignments()
    {
        return dataSetAssignments;
    }

    @SuppressWarnings("unused")
    private void setDataSetAssignments(List<DataSetTypePropertyTypePE> dataSetAssignments)
    {
        this.dataSetAssignments = dataSetAssignments;
    }

    @Column(name = ColumnNames.ENTITY_KIND)
    @Enumerated(EnumType.STRING)
    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    @NotNull(message = ValidationMessages.SCRIPT_TYPE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.SCRIPT_TYPE)
    @Enumerated(EnumType.STRING)
    public ScriptType getScriptType()
    {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType)
    {
        this.scriptType = scriptType;
    }

    @NotNull(message = ValidationMessages.PLUGIN_TYPE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.PLUGIN_TYPE)
    @Enumerated(EnumType.STRING)
    public PluginType getPluginType()
    {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType)
    {
        this.pluginType = pluginType;
    }

    @NotNull(message = ValidationMessages.IS_AVAILABLE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.IS_AVAILABLE)
    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(boolean available)
    {
        this.available = available;
    }

    @Transient
    public boolean isDynamic()
    {
        return scriptType == ScriptType.DYNAMIC_PROPERTY;
    }

    @Transient
    public boolean isManaged()
    {
        return scriptType == ScriptType.MANAGED_PROPERTY;
    }

}
