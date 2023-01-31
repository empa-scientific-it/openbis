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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;

/**
 * @author Franz-Josef Elmer
 */
public class NodeIdentifier
{
    public static NodeIdentifier asNodeIdentifier(Project project)
    {
        return new NodeIdentifier(SyncEntityKind.PROJECT, project.getIdentifier().getIdentifier());
    }

    public static NodeIdentifier asNodeIdentifier(Experiment experiment)
    {
        return new NodeIdentifier(SyncEntityKind.EXPERIMENT, experiment.getIdentifier().getIdentifier());
    }

    public static NodeIdentifier asNodeIdentifier(Sample sample)
    {
        return new NodeIdentifier(SyncEntityKind.SAMPLE, sample.getIdentifier().getIdentifier());
    }

    public static NodeIdentifier asNodeIdentifier(DataSet dataSet)
    {
        return new NodeIdentifier(SyncEntityKind.DATA_SET, dataSet.getPermId().getPermId());
    }

    private SyncEntityKind entityKind;

    private String entityIdentifier;

    public NodeIdentifier(SyncEntityKind entityKind, String entityIdentifier)
    {
        if (entityKind == null)
        {
            throw new IllegalArgumentException("Unspecified entity kind.");
        }
        if (entityIdentifier == null)
        {
            throw new IllegalArgumentException("Unspecified entity identifier.");
        }
        this.entityKind = entityKind;
        this.entityIdentifier = entityIdentifier;
    }

    public SyncEntityKind getEntityKind()
    {
        return entityKind;
    }

    public String getEntityIdentifier()
    {
        return entityIdentifier;
    }

    @Override
    public int hashCode()
    {
        return 37 * entityKind.hashCode() + entityIdentifier.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof NodeIdentifier == false)
        {
            return false;
        }
        NodeIdentifier that = (NodeIdentifier) obj;
        return this.entityKind == that.entityKind && this.entityIdentifier.equals(that.entityIdentifier);
    }

    @Override
    public String toString()
    {
        return entityKind.getAbbreviation() + ":" + entityIdentifier;
    }

}
