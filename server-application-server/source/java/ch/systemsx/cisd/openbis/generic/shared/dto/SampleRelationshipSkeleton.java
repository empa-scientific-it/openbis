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

/**
 * Only primary and foreign keys of a sample relationship in the database.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleRelationshipSkeleton
{
    private long parentSampleID;

    private long childSampleID;

    private long relationshipTypeID;

    public final long getParentSampleID()
    {
        return parentSampleID;
    }

    public final void setParentSampleID(long parentSampleID)
    {
        this.parentSampleID = parentSampleID;
    }

    public final long getChildSampleID()
    {
        return childSampleID;
    }

    public final void setChildSampleID(long childSampleID)
    {
        this.childSampleID = childSampleID;
    }

    public final long getRelationshipTypeID()
    {
        return relationshipTypeID;
    }

    public final void setRelationshipTypeID(long relationShipTypeID)
    {
        this.relationshipTypeID = relationShipTypeID;
    }
}
