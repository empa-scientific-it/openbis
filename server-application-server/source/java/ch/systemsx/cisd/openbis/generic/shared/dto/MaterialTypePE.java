/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Persistence entity representing type of material.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.MATERIAL_TYPES_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN }) })
public final class MaterialTypePE extends EntityTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    private Set<MaterialTypePropertyTypePE> materialTypePropertyTypes =
            new HashSet<MaterialTypePropertyTypePE>();

    //
    // EntityTypePE
    //

    @Override
    @SequenceGenerator(name = SequenceNames.MATERIAL_TYPE_SEQUENCE, sequenceName = SequenceNames.MATERIAL_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.MATERIAL_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entityTypeInternal", orphanRemoval = true)
    private Set<MaterialTypePropertyTypePE> getMaterialTypePropertyTypesInternal()
    {
        return materialTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setMaterialTypePropertyTypesInternal(
            final Set<MaterialTypePropertyTypePE> materialTypePropertyTypes)
    {
        this.materialTypePropertyTypes = materialTypePropertyTypes;
    }

    @Transient
    public Set<MaterialTypePropertyTypePE> getMaterialTypePropertyTypes()
    {
        return getMaterialTypePropertyTypesInternal();
    }

    public final void setMaterialTypePropertyTypes(
            final Set<MaterialTypePropertyTypePE> materialTypePropertyTypes)
    {
        getMaterialTypePropertyTypesInternal().clear();
        for (final MaterialTypePropertyTypePE child : materialTypePropertyTypes)
        {
            addMaterialTypePropertyType(child);
        }
    }

    public void addMaterialTypePropertyType(final MaterialTypePropertyTypePE child)
    {
        final MaterialTypePE parent = (MaterialTypePE) child.getEntityType();
        if (parent != null)
        {
            parent.getMaterialTypePropertyTypesInternal().remove(child);
        }
        child.setEntityTypeInternal(this);
        getMaterialTypePropertyTypesInternal().add(child);
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    @Transient
    public Collection<? extends EntityTypePropertyTypePE> getEntityTypePropertyTypes()
    {
        return getMaterialTypePropertyTypes();
    }

}
