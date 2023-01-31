/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

public class IncomingDataSet extends AbstractTimestampsAndUserHolder implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final NewExternalData dataSet;

    final Date lastModificationDate;

    private FullDataSetCreation fullDataSet;

    private FrozenFlags frozenFlags;

    public FrozenFlags getFrozenFlags()
    {
        return frozenFlags;
    }

    public Date getLastModificationDate()
    {
        return lastModificationDate;
    }

    public DataSetKind getKind()
    {
        return fullDataSet.getMetadataCreation().getDataSetKind();
    }

    public NewExternalData getDataSet()
    {
        return dataSet;
    }

    public FullDataSetCreation getFullDataSet()
    {
        return fullDataSet;
    }

    IncomingDataSet(NewExternalData dataSet, FrozenFlags frozenFlags, FullDataSetCreation fullDataSet, Date lastModDate)
    {
        super();
        this.dataSet = dataSet;
        this.frozenFlags = frozenFlags;
        this.fullDataSet = fullDataSet;
        this.lastModificationDate = lastModDate;
    }

    private List<Connection> connections = new ArrayList<Connection>();

    public List<Connection> getConnections()
    {
        return connections;
    }

    public void setConnections(List<Connection> conns)
    {
        // TODO do this better
        this.connections = conns;
    }
}