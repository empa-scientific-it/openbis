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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public class DeleteDataSetEventBuilder
{

    private EventPE event;

    public DeleteDataSetEventBuilder(DataPE dataSet, PersonPE registrator)
    {
        this(Collections.singletonList(dataSet), registrator);
    }

    public DeleteDataSetEventBuilder(List<DataPE> dataSets, PersonPE registrator)
    {
        event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.DATASET);

        setDataSets(dataSets);
        setRegistrator(registrator);
    }

    private void setDataSets(List<DataPE> dataSets)
    {
        if (dataSets == null || dataSets.isEmpty())
        {
            throw new IllegalArgumentException("Data sets cannot be null or empty");
        }

        List<String> identifiers = new ArrayList<String>();
        List<DeletedDataSetLocation> locations = new ArrayList<DeletedDataSetLocation>();

        for (DataPE dataSet : dataSets)
        {
            identifiers.add(dataSet.getCode());

            DeletedDataSetLocation location = new DeletedDataSetLocation();
            location.setDatastoreCode(dataSet.getDataStore().getCode());

            if (dataSet.isExternalData())
            {
                location.setShareId(dataSet.tryAsExternalData().getShareId());
                location.setLocation(dataSet.tryAsExternalData().getLocation());
            }

            locations.add(location);
        }

        event.setIdentifiers(identifiers);
        event.setDescription(DeletedDataSetLocation.format(locations));
    }

    private void setRegistrator(PersonPE registrator)
    {
        if (registrator == null)
        {
            throw new IllegalArgumentException("Registrator cannot be null");
        }
        event.setRegistrator(registrator);
    }

    public void setReason(String reason)
    {
        event.setReason(reason);
    }

    public EventPE getEvent()
    {
        return event;
    }

}
