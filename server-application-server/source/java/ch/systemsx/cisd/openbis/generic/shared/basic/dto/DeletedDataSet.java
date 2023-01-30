/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;

/**
 * Describes a deleted data set.
 * 
 * @author Izabela Adamczyk
 */
public class DeletedDataSet implements Serializable, ICodeHolder
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final long eventId;

    private final String identifier;

    private DeletedDataSetLocation locationObjectOrNull; // the location where the data set existed
                                                         // before deletion

    public DeletedDataSet(long eventId, String identifier)
    {
        this.eventId = eventId;
        this.identifier = identifier;
    }

    public long getEventId()
    {
        return eventId;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public String getCode()
    {
        return getIdentifier();
    }

    public String getDatastoreCodeOrNull()
    {
        return locationObjectOrNull != null ? locationObjectOrNull.getDatastoreCode() : null;
    }

    public String getShareIdOrNull()
    {
        return locationObjectOrNull != null ? locationObjectOrNull.getShareId() : null;
    }

    public String getLocationOrNull()
    {
        return locationObjectOrNull != null ? locationObjectOrNull.getLocation() : null;
    }

    public DeletedDataSetLocation getLocationObjectOrNull()
    {
        return locationObjectOrNull;
    }

    public void setLocationObjectOrNull(DeletedDataSetLocation locationOrNull)
    {
        this.locationObjectOrNull = locationOrNull;
    }

    @Override
    public String toString()
    {
        return "DeletedDataSet [identifier=" + identifier + "]";
    }

}
