/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISampleHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;

public class Node<T extends IRegistrationDateHolder & IPermIdHolder & ICodeHolder> implements INode
{
    private final List<EdgeNodePair> connections;

    private final List<Attachment> attachments;

    protected final T entity;

    public T getEntity()
    {
        return entity;
    }

    public Node(T entity)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException("Unspecified entity");
        }
        this.entity = entity;
        this.connections = new ArrayList<EdgeNodePair>();
        this.attachments = new ArrayList<Attachment>();
    }

    @Override
    public int hashCode()
    {
        return this.getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof Node == false)
        {
            return false;
        }
        return this.getIdentifier().equals(((Node<?>) obj).getIdentifier());
    }

    @Override
    public Map<String, Serializable> getPropertiesOrNull()
    {
        if (IPropertiesHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        return ((IPropertiesHolder) entity).getProperties();
    }

    public String getSpaceOrNull()
    {
        Space space = getSpace();
        if (space == null)
        {
            return null;
        }
        return space.getCode();
    }

    public Experiment getExperimentOrNull()
    {
        if (IExperimentHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        else
        {
            return ((IExperimentHolder) entity).getExperiment();
        }
    }

    public String getExperimentIdentifierOrNull()
    {
        Experiment exp = getExperimentOrNull();
        if (exp != null)
        {
            return exp.getIdentifier().toString();
        }
        return null;
    }

    public String getProjectOrNull()
    {
        if (IProjectHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        
        Project project = ((IProjectHolder) entity).getProject(); 
        return project != null ? project.getCode() : null;
    }

    public Sample getSampleOrNull()
    {
        if (ISampleHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        else 
        {
            return ((ISampleHolder) entity).getSample();
        }
    }

    public String getSampleIdentifierOrNull()
    {
        Sample sample = getSampleOrNull();
        if (sample != null)
        {
            return sample.getIdentifier().toString();
        }
        return null;
    }

    @Override
    public String getTypeCodeOrNull()
    {
        if (IPropertiesHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        if(entity instanceof Sample) {
            return ((Sample) entity).getType().getCode();
        }
        else if (entity instanceof Experiment)
        {
            return ((Experiment) entity).getType().getCode();
        }
        else if (entity instanceof DataSet)
        {
            return ((DataSet) entity).getType().getCode();
        }
        // TODO exception
        return null;
    }

    @Override
    public void addConnection(EdgeNodePair enPair)
    {
        connections.add(enPair);
    }

    public List<EdgeNodePair> getConnections()
    {
        return connections;
    }

    public void setAttachments(List<Attachment> attachmentList)
    {
        attachments.clear();
        attachments.addAll(attachmentList);
    }

    public List<Attachment> getAttachmentsOrNull()
    {
        if (entity instanceof IAttachmentsHolder)
        {
            IAttachmentsHolder holder = (IAttachmentsHolder) entity;
            return holder.getAttachments();
        }
        return null;
    }

    @Override
    public String getPermId()
    {
        return this.entity.getPermId().toString();
    }

    @Override
    public NodeIdentifier getIdentifier()
    {
        if (entity instanceof Project)
        {
            return new NodeIdentifier(SyncEntityKind.PROJECT, ((Project) entity).getIdentifier().getIdentifier());
        }
        if (entity instanceof Experiment)
        {
            return new NodeIdentifier(SyncEntityKind.EXPERIMENT, ((Experiment) entity).getIdentifier().getIdentifier());
        }
        if (entity instanceof Sample)
        {
            return new NodeIdentifier(SyncEntityKind.SAMPLE, ((Sample) entity).getIdentifier().getIdentifier());
        }
        else if (entity instanceof DataSet)
        {
            return new NodeIdentifier(SyncEntityKind.DATA_SET, ((DataSet) entity).getPermId().toString());
        }
        throw new IllegalStateException("Entity " + entity + " is of invalid kind");
    }

    @Override
    public String getCode()
    {
        return this.entity.getCode();
    }

    public Date getRegistrationDate()
    {
        return entity.getRegistrationDate();
    }

    @Override
    public SyncEntityKind getEntityKind()
    {
        if (entity instanceof Project)
        {
            return SyncEntityKind.PROJECT;
        }
        else if (entity instanceof Experiment)
        {
            return SyncEntityKind.EXPERIMENT;
        }
        else if (entity instanceof Sample)
        {
            return SyncEntityKind.SAMPLE;
        }
        else if (entity instanceof DataSet)
        {
            return SyncEntityKind.DATA_SET;
        }
        return null;
    }

    @Override
    public Space getSpace()
    {
        if (entity instanceof Experiment)
        {
            return ((Experiment) entity).getProject().getSpace();
        }
        if (ISpaceHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        return ((ISpaceHolder) entity).getSpace();
    }
}
