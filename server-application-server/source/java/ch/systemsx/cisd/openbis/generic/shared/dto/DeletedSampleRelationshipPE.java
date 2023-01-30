/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author Kaloyan Enimanev
 */
@Entity
@Table(name = TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE)
public class DeletedSampleRelationshipPE implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private Long parentId;

    /**
     * Deletion information.
     * <p>
     * If not <code>null</code>, then this data set is considered <i>deleted</i> (moved to trash).
     * </p>
     */
    private DeletionPE deletion;

    @Id
    @SequenceGenerator(name = SequenceNames.SAMPLE_SEQUENCE, sequenceName = SequenceNames.SAMPLE_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.SAMPLE_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Column(name = ColumnNames.PARENT_SAMPLE_COLUMN, nullable = false, insertable = false, updatable = false)
    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(final Long parentId)
    {
        this.parentId = parentId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DELETION_COLUMN)
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    public void setDeletion(final DeletionPE deletion)
    {
        this.deletion = deletion;
    }
}
