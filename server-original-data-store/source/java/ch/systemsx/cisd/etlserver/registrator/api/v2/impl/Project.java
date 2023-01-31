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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

/**
 * @author Kaloyan Enimanev
 */
class Project extends ProjectImmutable implements IProject
{
    private final List<NewAttachment> newAttachments;

    public Project(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project)
    {
        super(project);
        newAttachments = new ArrayList<NewAttachment>();
    }

    public Project(String projectIdentifier)
    {
        this(projectIdentifier, false);
    }

    public Project(String projectIdentifier, boolean isExistingProject)
    {
        super(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project(), isExistingProject);
        newAttachments = new ArrayList<NewAttachment>();
        getProject().setIdentifier(projectIdentifier);
    }

    @Override
    public void setDescription(String description)
    {
        getProject().setDescription(description);
    }

    @Override
    public void addAttachment(String filePath, String title, String description, byte[] content)
    {
        newAttachments.add(ConversionUtils.createAttachment(filePath, title, description, content));
    }

    /**
     * For conversion to updates DTO.
     */
    List<NewAttachment> getNewAttachments()
    {
        return newAttachments;
    }
}
