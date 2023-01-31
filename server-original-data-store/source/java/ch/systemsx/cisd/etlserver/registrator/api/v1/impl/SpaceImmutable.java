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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISpaceImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class SpaceImmutable implements ISpaceImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space space;

    private final boolean isExistingSpace;

    public SpaceImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space space)
    {
        this(space, true);
    }

    public SpaceImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space space,
            boolean isExistingSpace)
    {
        this.space = space;
        this.isExistingSpace = isExistingSpace;

    }

    @Override
    public String getSpaceCode()
    {
        return space.getCode();
    }

    @Override
    public String getDescription()
    {
        return space.getDescription();
    }

    @Override
    public boolean isExistingSpace()
    {
        return isExistingSpace;
    }

    /**
     * Throw an exception if the project does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingSpace())
        {
            throw new UserFailureException("Space does not exist.");
        }
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space getSpace()
    {
        return space;
    }

}
