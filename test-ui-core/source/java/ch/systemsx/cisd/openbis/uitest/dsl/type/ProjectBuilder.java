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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateProjectGui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateProjectRmi;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class ProjectBuilder implements Builder<Project>
{

    private String code;

    private String description;

    private Space space;

    private UidGenerator uid;

    public ProjectBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.code = uid.uid();
        this.description = "";
        this.space = null;
    }

    public ProjectBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public ProjectBuilder in(Space space)
    {
        this.space = space;
        return this;
    }

    @Override
    public Project build(Application openbis, Ui ui)
    {
        if (space == null)
        {
            space = new SpaceBuilder(uid).build(openbis, ui);
        }

        Project project = new ProjectDsl(code, description, space);
        if (Ui.WEB.equals(ui))
        {
            return openbis.execute(new CreateProjectGui(project));
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            return openbis.execute(new CreateProjectRmi(project));
        } else
        {
            return project;
        }
    }
}
