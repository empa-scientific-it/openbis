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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;

public class IncomingEntity<T> extends AbstractTimestampsAndUserHolder
{
    private final T entity;

    private final SyncEntityKind entityKind;

    private List<Connection> connections = new ArrayList<Connection>();

    private boolean hasAttachments;

    public List<Connection> getConnections()
    {
        return connections;
    }

    public SyncEntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setConnections(List<Connection> conns)
    {
        // TODO do this better
        this.connections = conns;
    }

    public boolean hasAttachments()
    {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments)
    {
        this.hasAttachments = hasAttachments;
    }

    public T getEntity()
    {
        return entity;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String getPermID()
    {
        return permId;
    }

    public FrozenFlags getFrozenFlags()
    {
        return frozenFlags;
    }

    public Date getLastModificationDate()
    {
        return lastModificationDate;
    }

    private final Date lastModificationDate;

    private FrozenFlags frozenFlags;

    private String identifier;

    private String permId;

    IncomingEntity(T entity, FrozenFlags frozenFlags, SyncEntityKind entityKind, Date lastModDate)
    {
        this(entity, getIdentifier(entity), getPermId(entity), frozenFlags, entityKind, lastModDate);
    }

    @SuppressWarnings("unused")
    private static String getIdentifier(Object entity)
    {
        if (entity instanceof Identifier)
        {

            return ((Identifier) entity).getIdentifier();
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static String getPermId(Object entity)
    {
        if (entity instanceof Identifier)
        {

            return ((Identifier) entity).getPermID();
        }
        return null;
    }

    IncomingEntity(T entity, String identifier, String permId, FrozenFlags frozenFlags, SyncEntityKind entityKind, Date lastModDate)
    {
        this.entity = entity;
        this.identifier = identifier;
        this.permId = permId;
        this.frozenFlags = frozenFlags;
        this.entityKind = entityKind;
        this.lastModificationDate = lastModDate;
    }
}