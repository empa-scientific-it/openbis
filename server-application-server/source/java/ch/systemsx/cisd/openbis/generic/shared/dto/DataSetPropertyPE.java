/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Persistence entity representing data set property.
 * 
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.DATA_SET_PROPERTIES_TABLE)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DataSetPropertyPE extends EntityPropertyWithSampleDataTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    public final static DataSetPropertyPE[] EMPTY_ARRAY = new DataSetPropertyPE[0];

    //
    // EntityPropertyPE
    //

    @Override
    @NotNull(message = ValidationMessages.DATA_SET_TYPE_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = DataSetTypePropertyTypePE.class)
    @JoinColumn(name = ColumnNames.DATA_SET_TYPE_PROPERTY_TYPE_COLUMN)
    public EntityTypePropertyTypePE getEntityTypePropertyType()
    {
        return entityTypePropertyType;
    }

    @Override
    @SequenceGenerator(name = SequenceNames.DATA_SET_PROPERTY_SEQUENCE, sequenceName = SequenceNames.DATA_SET_PROPERTY_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_SET_PROPERTY_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    @Override
    @NotNull(message = ValidationMessages.DATA_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.DATA_SET_COLUMN)
    public DataPE getEntity()
    {
        return (DataPE) entity;
    }

    @Override
    @NotNull
    @Column(name = ColumnNames.DATA_SET_FROZEN_COLUMN, nullable = false)
    public boolean isEntityFrozen()
    {
        return entityFrozen;
    }

}
