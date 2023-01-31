/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;

class ConnectionsBuilder
{
    private enum ConnectionType
    {
        CONNECTION, CHILD, COMPONENT
    }

    private static final class Connection
    {
        private String permId;

        private ConnectionsBuilder.ConnectionType type;

        Connection(String permId, ConnectionsBuilder.ConnectionType type)
        {
            this.permId = permId;
            this.type = type;
        }
    }

    private List<ConnectionsBuilder.Connection> connections = new ArrayList<>();

    void addChildren(List<? extends IPermIdHolder> entities)
    {
        addConnections(entities, ConnectionType.CHILD);
    }

    void addComponents(List<? extends IPermIdHolder> entities)
    {
        addConnections(entities, ConnectionType.COMPONENT);
    }
    
    void addConnections(List<? extends IPermIdHolder> entities)
    {
        addConnections(entities, ConnectionType.CONNECTION);
    }
    
    void writeTo(XMLStreamWriter writer) throws XMLStreamException
    {
        if (connections.isEmpty())
        {
            return;
        }
        writer.writeStartElement("x:connections");
        for (Connection connection : connections)
        {
            writer.writeStartElement("x:connection");
            writer.writeAttribute("to", connection.permId);
            writer.writeAttribute("type", StringUtils.capitalize(connection.type.toString().toLowerCase()));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
    
    private void addConnections(List<? extends IPermIdHolder> entities, ConnectionsBuilder.ConnectionType child)
    {
        for (IPermIdHolder entity : entities)
        {
            connections.add(new Connection(entity.getPermId().toString(), child));
        }
    }
}