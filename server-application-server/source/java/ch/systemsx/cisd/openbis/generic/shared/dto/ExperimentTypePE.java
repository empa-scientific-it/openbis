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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;
import javax.persistence.Id;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.JsonMapUserType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * Persistence entity representing type of experiment.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.EXPERIMENT_TYPES_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN }) })
@TypeDefs({ @TypeDef(name = "JsonMap", typeClass = JsonMapUserType.class) })
public final class ExperimentTypePE extends EntityTypePE implements IEntityWithMetaData
{
    private static final long serialVersionUID = IServer.VERSION;

    private Set<ExperimentTypePropertyTypePE> exerimentTypePropertyTypes =
            new HashSet<ExperimentTypePropertyTypePE>();

    private Map<String, String> metaData;

    @Override
    @SequenceGenerator(name = SequenceNames.EXPERIMENT_TYPE_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entityTypeInternal", orphanRemoval = true)
    private Set<ExperimentTypePropertyTypePE> getExperimentTypePropertyTypesInternal()
    {
        return exerimentTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setExperimentTypePropertyTypesInternal(
            final Set<ExperimentTypePropertyTypePE> experimentTypePropertyTypes)
    {
        this.exerimentTypePropertyTypes = experimentTypePropertyTypes;
    }

    @Transient
    public Set<ExperimentTypePropertyTypePE> getExperimentTypePropertyTypes()
    {
        return getExperimentTypePropertyTypesInternal();
    }

    public final void setExperimentTypePropertyTypes(
            final Set<ExperimentTypePropertyTypePE> experimentTypePropertyTypes)
    {
        getExperimentTypePropertyTypesInternal().clear();
        for (final ExperimentTypePropertyTypePE child : experimentTypePropertyTypes)
        {
            addExperimentTypePropertyType(child);
        }
    }

    public void addExperimentTypePropertyType(final ExperimentTypePropertyTypePE child)
    {
        final ExperimentTypePE parent = (ExperimentTypePE) child.getEntityType();
        if (parent != null)
        {
            parent.getExperimentTypePropertyTypesInternal().remove(child);
        }
        child.setEntityTypeInternal(this);
        getExperimentTypePropertyTypesInternal().add(child);
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    @Transient
    public Collection<? extends EntityTypePropertyTypePE> getEntityTypePropertyTypes()
    {
        return getExperimentTypePropertyTypes();
    }

    @Override
    @Column(name = "meta_data")
    @Type(type = "JsonMap")
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    @Override
    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

}
