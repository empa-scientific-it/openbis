/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedSpaceException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class SpaceCodeHelper
{
    private SpaceCodeHelper()
    {
    }

    public static final String HOME_SPACE_CODE = null;

    public static boolean isHomeSpace(String spaceCodeOrNull)
    {
        return spaceCodeOrNull == HOME_SPACE_CODE;
    }

    /**
     * Tries to find out the space.
     * <p>
     * If not specified in given {@link SpaceIdentifier} the real space must be specified as home
     * space in given {@link PersonPE}.
     * </p>
     * 
     * @throws UndefinedSpaceException if no space could be found.
     */
    public final static String getSpaceCode(final PersonPE person,
            final SpaceIdentifier spaceIdentifier) throws UndefinedSpaceException
    {
        return getSpaceCode(person, spaceIdentifier.getSpaceCode());
    }

    /**
     * Tries to find out the space.
     * <p>
     * If not specified in given {@link GroupPE} the real space must be specified as home space in
     * given {@link PersonPE}.
     * </p>
     * 
     * @throws UndefinedSpaceException if no space could be found.
     */
    public final static String getSpaceCode(final PersonPE person, final GroupPE group)
            throws UndefinedSpaceException
    {
        return getSpaceCode(person, group.getCode());
    }

    /**
     * Tries to find out the space.
     * <p>
     * If given <var>spaceCode</var> is a home space, the real space must be specified as home space
     * in given {@link PersonPE}.
     * </p>
     * 
     * @throws UndefinedSpaceException if no space could be found.
     */
    private final static String getSpaceCode(final PersonPE person, final String spaceCode)
            throws UndefinedSpaceException
    {
        if (isHomeSpace(spaceCode))
        {
            final GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup == null)
            {
                throw new UndefinedSpaceException();
            }
            return homeGroup.getCode();
        } else
        {
            return spaceCode;
        }
    }

}
