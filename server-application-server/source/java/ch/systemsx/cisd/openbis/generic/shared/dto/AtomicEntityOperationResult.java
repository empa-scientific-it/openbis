/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.Serializable;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final long spacesCreatedCount;

    private final long projectsCreatedCount;

    private final long projectsUpdatedCount;

    private final long materialsCreatedCount;

    private final long materialsUpdatedCount;

    private final long experimentsCreatedCount;

    private final long experimentsUpdatedCount;

    private final long samplesCreatedCount;

    private final long samplesUpdatedCount;

    private final long dataSetsCreatedCount;

    private final long dataSetsUpdatedCount;

    private final long metaprojectsCreatedCount;

    private final long metaprojectsUpdatedCount;

    private final long vocabulariesUpdatedCount;

    private final long spaceRolesAssignedCount;

    private final long spaceRolesRevokedCount;

    public AtomicEntityOperationResult()
    {
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public AtomicEntityOperationResult(long spacesCreated, long projectsCreated,
            long projectsUpdated, long materialsCreated, long materialsUpdated,
            long experimentsCreated, long experimentsUpdated, long samplesCreated,
            long samplesUpdated, long dataSetsCreated, long dataSetsUpdated,
            long metaprojectsCreatedCount, long metaprojectsUpdatedCount,
            long vocabulariesUpdatedCount, long spaceRolesAssignedCount, long spaceRolesRevokedCount)
    {
        this.spacesCreatedCount = spacesCreated;
        this.projectsCreatedCount = projectsCreated;
        this.projectsUpdatedCount = projectsUpdated;
        this.materialsCreatedCount = materialsCreated;
        this.materialsUpdatedCount = materialsUpdated;
        this.experimentsCreatedCount = experimentsCreated;
        this.experimentsUpdatedCount = experimentsUpdated;
        this.samplesCreatedCount = samplesCreated;
        this.samplesUpdatedCount = samplesUpdated;
        this.dataSetsCreatedCount = dataSetsCreated;
        this.dataSetsUpdatedCount = dataSetsUpdated;
        this.metaprojectsCreatedCount = metaprojectsCreatedCount;
        this.metaprojectsUpdatedCount = metaprojectsUpdatedCount;
        this.vocabulariesUpdatedCount = vocabulariesUpdatedCount;
        this.spaceRolesAssignedCount = spaceRolesAssignedCount;
        this.spaceRolesRevokedCount = spaceRolesRevokedCount;
    }

    public long getExperimentsUpdatedCount()
    {
        return experimentsUpdatedCount;
    }

    public long getExperimentsCreatedCount()
    {
        return experimentsCreatedCount;
    }

    public long getSamplesUpdatedCount()
    {
        return samplesUpdatedCount;
    }

    public long getSamplesCreatedCount()
    {
        return samplesCreatedCount;
    }

    public long getDataSetsCreatedCount()
    {
        return dataSetsCreatedCount;
    }

    public long getDataSetsUpdatedCount()
    {
        return dataSetsUpdatedCount;
    }

    public long getSpacesCreatedCount()
    {
        return spacesCreatedCount;
    }

    public long getProjectsCreatedCount()
    {
        return projectsCreatedCount;
    }

    public long getProjectsUpdatedCount()
    {
        return projectsUpdatedCount;
    }

    public long getMaterialsCreatedCount()
    {
        return materialsCreatedCount;
    }

    public long getMaterialsUpdatedCount()
    {
        return materialsUpdatedCount;
    }

    public long getMetaprojectsCreatedCount()
    {
        return metaprojectsCreatedCount;
    }

    public long getMetaprojectsUpdatedCount()
    {
        return metaprojectsUpdatedCount;
    }

    public long getVocabulariesUpdatedCount()
    {
        return vocabulariesUpdatedCount;
    }

    private long getAllOperationsCount()
    {
        return spacesCreatedCount + projectsCreatedCount + projectsUpdatedCount + materialsCreatedCount + materialsUpdatedCount
                + experimentsCreatedCount + experimentsUpdatedCount + samplesCreatedCount + samplesUpdatedCount + dataSetsCreatedCount
                + dataSetsUpdatedCount + metaprojectsCreatedCount + metaprojectsUpdatedCount + vocabulariesUpdatedCount;
    }

    public long getSpaceRolesAssignedCount()
    {
        return spaceRolesAssignedCount;
    }

    public long getSpaceRolesRevokedCount()
    {
        return spaceRolesRevokedCount;
    }

    @Override
    public String toString()
    {
        if (getAllOperationsCount() == 0)
        {
            return "No entities have been created or updated";
        }

        StringBuilder message = new StringBuilder();

        message.append("Total of " + getAllOperationsCount() + " entities have been created or updated.\n");

        updateMessage(message, spacesCreatedCount, "Spaces created");
        updateMessage(message, projectsCreatedCount, "Projects created");
        updateMessage(message, projectsUpdatedCount, "Projects updated");
        updateMessage(message, materialsCreatedCount, "Materials created");
        updateMessage(message, materialsUpdatedCount, "Materials updated");
        updateMessage(message, experimentsCreatedCount, "Experiments created");
        updateMessage(message, experimentsUpdatedCount, "Experiments updated");
        updateMessage(message, samplesCreatedCount, "Samples created");
        updateMessage(message, samplesUpdatedCount, "Samples updated");
        updateMessage(message, dataSetsCreatedCount, "Data sets created");
        updateMessage(message, dataSetsUpdatedCount, "Data sets updated");
        updateMessage(message, metaprojectsCreatedCount, "Metaprojects created");
        updateMessage(message, metaprojectsUpdatedCount, "Metaprojects updated");
        updateMessage(message, vocabulariesUpdatedCount, "Vocabularies updated");
        updateMessage(message, spaceRolesAssignedCount, "Space roles assigned");
        updateMessage(message, spaceRolesRevokedCount, "Space roles revoked");

        return message.toString();

    }

    private void updateMessage(StringBuilder builder, long count, String msg)
    {
        if (count > 0)
        {
            builder.append(msg + ": " + count + "\n");
        }
    }

}
