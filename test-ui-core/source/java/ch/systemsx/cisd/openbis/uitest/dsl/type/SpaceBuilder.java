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

import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateSpaceGui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateSpaceRmi;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class SpaceBuilder implements Builder<Space>
{

    private String code;

    private String description;

    public SpaceBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
        this.description = "";
    }

    public SpaceBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public SpaceBuilder withCodePrefix(String prefix)
    {
        this.code = prefix + "_" + UUID.randomUUID().toString();
        return this;
    }

    @Override
    public Space build(Application openbis, Ui ui)
    {
        Space space = new SpaceDsl(code, description);

        if (Ui.WEB.equals(ui))
        {
            openbis.execute(new CreateSpaceGui(space));
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            openbis.execute(new CreateSpaceRmi(space));
        }

        return space;
    }
}
