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

package ch.systemsx.cisd.openbis.uitest.rmi;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
public class CreateSpaceRmi implements Command<Space>
{

    private final Space space;

    @Inject
    private ICommonServer commonServer;

    @Inject
    private String session;

    public CreateSpaceRmi(Space space)
    {
        this.space = space;
    }

    @Override
    public Space execute()
    {
        commonServer.registerSpace(session, space.getCode(), space.getDescription());
        return space;
    }
}
