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
import javax.persistence.Column;
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

import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Persistence Entity representing data set type.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.DATA_SET_TYPES_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { ColumnNames.CODE_COLUMN }) })
public class DataSetTypePE extends EntityTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    private Set<DataSetTypePropertyTypePE> dataSetTypePropertyTypes =
            new HashSet<DataSetTypePropertyTypePE>();

    private String mainDataSetPath;

    private String mainDataSetPattern;

    private boolean deletionDisallow;

    @Override
    @SequenceGenerator(name = SequenceNames.DATA_SET_TYPE_SEQUENCE, sequenceName = SequenceNames.DATA_SET_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_SET_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public void addDataSetTypePropertyType(final DataSetTypePropertyTypePE child)
    {
        final DataSetTypePE parent = (DataSetTypePE) child.getEntityType();
        if (parent != null)
        {
            parent.getDataSetTypePropertyTypesInternal().remove(child);
        }
        child.setEntityTypeInternal(this);
        getDataSetTypePropertyTypesInternal().add(child);
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entityTypeInternal", orphanRemoval = true)
    private Set<DataSetTypePropertyTypePE> getDataSetTypePropertyTypesInternal()
    {
        return dataSetTypePropertyTypes;
    }

    // Required by Hibernate.
    @SuppressWarnings("unused")
    private void setDataSetTypePropertyTypesInternal(
            final Set<DataSetTypePropertyTypePE> dataSetTypePropertyTypes)
    {
        this.dataSetTypePropertyTypes = dataSetTypePropertyTypes;
    }

    @Transient
    public Set<DataSetTypePropertyTypePE> getDataSetTypePropertyTypes()
    {
        return getDataSetTypePropertyTypesInternal();
    }

    public final void setDataSetTypePropertyTypes(
            final Set<DataSetTypePropertyTypePE> dataSetTypePropertyTypes)
    {
        getDataSetTypePropertyTypesInternal().clear();
        for (final DataSetTypePropertyTypePE child : dataSetTypePropertyTypes)
        {
            addDataSetTypePropertyType(child);
        }
    }

    @Column(name = ColumnNames.MAIN_DS_PATH)
    @Length(min = 1, max = GenericConstants.MAIN_DS_PATH_LENGTH, message = ValidationMessages.MAIN_DS_PATH_LENGTH_MESSAGE)
    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }

    @Column(name = ColumnNames.MAIN_DS_PATTERN)
    @Length(min = 1, max = GenericConstants.MAIN_DS_PATTERN_LENGTH, message = ValidationMessages.MAIN_DS_PATTERN_LENGTH_MESSAGE)
    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }

    @Override
    @Transient
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    @Transient
    public Collection<? extends EntityTypePropertyTypePE> getEntityTypePropertyTypes()
    {
        return getDataSetTypePropertyTypes();
    }

    /**
     * Returns <code>true</code> if data sets of this type require special user rights and an additional confirmation to be deleted.
     */
    @Column(name = ColumnNames.DELETION_DISALLOW)
    public boolean isDeletionDisallow()
    {
        return deletionDisallow;
    }

    /**
     * Set to <code>true</code> if data sets of this type require special user rights and an additional confirmation to be deleted.
     */
    public void setDeletionDisallow(boolean deletionDisallow)
    {
        this.deletionDisallow = deletionDisallow;
    }

}
