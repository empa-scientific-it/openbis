/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.project;

import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;

/**
 * @author pkupczyk
 */
public class TestProject implements IProject
{

    private Long id;

    private String permId;

    private String identifier;

    public TestProject(Long id, String permId, String identifier)
    {
        this.id = id;
        this.permId = permId;
        this.identifier = identifier;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public String getPermId()
    {
        return permId;
    }

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return "TestProject[id: " + id + ", permId: " + permId + ", identifier: " + identifier + "]";
    }

}
