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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * A command for deleting data sets, based on their location relative to the data store root.
 *
 * @author Franz-Josef Elmer
 */
class DeletionCommand implements IDataSetCommand
{
    private static final long serialVersionUID = 1L;
    
    private final List<DatasetDescription> dataSets;

    DeletionCommand(List<DatasetDescription> dataSets)
    {
        this.dataSets = dataSets;
    }

    public void execute(File store)
    {
        for (DatasetDescription dataSet : dataSets)
        {
            File share = new File(store, dataSet.getDataSetShareId());
            QueueingPathRemoverService.removeRecursively(new File(share, dataSet.getDataSetLocation()));
        }
    }

    public String getDescription()
    {
        final StringBuilder b = new StringBuilder();
        b.append("Delete data set paths: ");
        for (DatasetDescription dataset : dataSets)
        {
            b.append(dataset.getDataSetShareId()).append('/').append(dataset.getDataSetLocation());
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}
