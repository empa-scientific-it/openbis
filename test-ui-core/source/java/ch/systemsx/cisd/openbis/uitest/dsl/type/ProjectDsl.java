/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.dsl.type;

import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
class ProjectDsl extends Project
{
    private final String code;

    private Space space;

    private String description;

    ProjectDsl(String code, String description, Space space)
    {
        this.code = code;
        this.space = space;
        this.description = description;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public Space getSpace()
    {
        return space;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    void setSpace(Space space)
    {
        this.space = space;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return "Project " + code;
    }
}
