/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;

/**
 * @author pkupczyk
 */
public class DeleteDataSetEventParser
{

    private EventPE event;

    public DeleteDataSetEventParser(EventPE event)
    {
        if (event == null)
        {
            throw new IllegalArgumentException("Event cannot be null");
        }
        this.event = event;
    }

    public List<DeletedDataSet> getDeletedDatasets()
    {
        List<DeletedDataSet> deletedDatasets = new ArrayList<DeletedDataSet>();
        Iterator<DeletedDataSetLocation> locationIterator = getLocations().iterator();

        for (String identifier : event.getIdentifiers())
        {
            DeletedDataSet deletedDataSet = new DeletedDataSet(event.getId(), identifier);
            if (locationIterator.hasNext())
            {
                deletedDataSet.setLocationObjectOrNull(locationIterator.next());
            }
            deletedDatasets.add(deletedDataSet);
        }

        return deletedDatasets;
    }

    private List<DeletedDataSetLocation> getLocations()
    {
        return DeletedDataSetLocation.parse(event.getDescription());
    }

}
