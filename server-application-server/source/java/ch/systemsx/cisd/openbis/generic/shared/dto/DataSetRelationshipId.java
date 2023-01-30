/*
 * Copyright 2012 ETH Zuerich, CISD
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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Pawel Glyzewski
 */
public class DataSetRelationshipId implements Serializable
{
    private static final long serialVersionUID = -3819640534421748352L;

    private DataPE parentDataSet;

    @NotNull(message = ValidationMessages.PARENT_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_PARENT_COLUMN)
    public DataPE getParentDataSet()
    {
        return parentDataSet;
    }

    public void setParentDataSet(DataPE parentDataSet)
    {
        this.parentDataSet = parentDataSet;
    }

    @NotNull(message = ValidationMessages.CHILD_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_CHILD_COLUMN)
    public DataPE getChildDataSet()
    {
        return childDataSet;
    }

    public void setChildDataSet(DataPE childDataSet)
    {
        this.childDataSet = childDataSet;
    }

    private DataPE childDataSet;

    @NotNull(message = ValidationMessages.RELATIONSHIP_NOT_NULL_MESSAGE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.RELATIONSHIP_COLUMN)
    public RelationshipTypePE getRelationshipType()
    {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipTypePE relationship)
    {
        this.relationshipType = relationship;
    }

    private RelationshipTypePE relationshipType;

    @Deprecated
    public DataSetRelationshipId()
    {
    }

    public DataSetRelationshipId(DataPE parentDataSet, DataPE childDataSet, RelationshipTypePE relationshipType)
    {
        this.parentDataSet = parentDataSet;
        this.childDataSet = childDataSet;
        this.relationshipType = relationshipType;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || !(other instanceof DataSetRelationshipId))
            return false;
        DataSetRelationshipId castOther = (DataSetRelationshipId) other;
        return new EqualsBuilder()
                .append(this.parentDataSet.getId(), castOther.parentDataSet.getId())
                .append(this.childDataSet.getId(), castOther.childDataSet.getId())
                .append(this.relationshipType.getId(), castOther.relationshipType.getId()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(parentDataSet.getId()).append(childDataSet.getId()).append(relationshipType.getId())
                .toHashCode();
    }
}
