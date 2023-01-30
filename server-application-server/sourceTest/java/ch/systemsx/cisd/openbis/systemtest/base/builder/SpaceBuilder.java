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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SpaceBuilder extends Builder<Space>
{
    private static int number;

    private String code;

    public SpaceBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
        this.code = "S" + number++;
    }

    public SpaceBuilder withCode(String spaceCode)
    {
        this.code = spaceCode;
        return this;
    }

    @Override
    public Space create()
    {
        commonServer.registerSpace(sessionToken, code, "description");
        return getSpace(code);
    }

    private Space getSpace(String spaceCode)
    {
        for (Space space : commonServer.listSpaces(sessionToken))
        {
            if (space.getCode().equalsIgnoreCase(spaceCode))
            {
                return space;
            }
        }
        throw new IllegalArgumentException("Space " + spaceCode + " does not exist");
    }
}
