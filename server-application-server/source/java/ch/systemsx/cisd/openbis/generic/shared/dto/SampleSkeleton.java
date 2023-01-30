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
 * Only primary and foreign keys of a sample in the database.
 *
 * @author Franz-Josef Elmer
 */
public class SampleSkeleton
{
    private long id;

    private Long databaseInstanceID;

    private Long spaceID;

    private long typeID;

    private Long experimentID;

    public final long getId()
    {
        return id;
    }

    public final void setId(long id)
    {
        this.id = id;
    }

    public final Long getDatabaseInstanceID()
    {
        return databaseInstanceID;
    }

    public final void setDatabaseInstanceID(Long databaseInstanceID)
    {
        this.databaseInstanceID = databaseInstanceID;
    }

    public final Long getSpaceID()
    {
        return spaceID;
    }

    public final void setSpaceID(Long spaceID)
    {
        this.spaceID = spaceID;
    }

    public final long getTypeID()
    {
        return typeID;
    }

    public final void setTypeID(long typeID)
    {
        this.typeID = typeID;
    }

    public final Long getExperimentID()
    {
        return experimentID;
    }

    public final void setExperimentID(Long experimentID)
    {
        this.experimentID = experimentID;
    }

}
