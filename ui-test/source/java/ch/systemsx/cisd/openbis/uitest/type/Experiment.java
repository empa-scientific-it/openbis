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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.infra.EntityType;

/**
 * @author anttil
 */
public class Experiment implements EntityType, Browsable
{
    private ExperimentType type;

    private final String code;

    private Project project;

    Experiment(ExperimentType type, String code, Project project)
    {
        this.type = type;
        this.code = code;
        this.project = project;
    }

    @Override
    public boolean isRepresentedBy(Map<String, String> row)
    {
        return code.equalsIgnoreCase(row.get("Code"));
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public ExperimentType getType()
    {
        return type;
    }

    public Project getProject()
    {
        return project;
    }

    void setType(ExperimentType type)
    {
        this.type = type;
    }

    void setProject(Project project)
    {
        this.project = project;
    }
}
