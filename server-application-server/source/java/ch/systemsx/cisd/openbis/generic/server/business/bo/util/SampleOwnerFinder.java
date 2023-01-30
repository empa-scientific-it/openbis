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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedSpaceException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Finds a project or a space instance for a given owner identifier.
 * 
 * @author Tomasz Pylak
 */
public class SampleOwnerFinder
{
    private final IAuthorizationDAOFactory daoFactory;

    private final PersonPE personPE;

    public SampleOwnerFinder(final IAuthorizationDAOFactory daoFactory, final PersonPE personPE)
    {
        assert daoFactory != null : "Unspecified DAOFactory";
        assert personPE != null : "Unspecified person";
        this.daoFactory = daoFactory;
        this.personPE = personPE;
    }

    public SampleOwner figureSampleOwner(final SampleOwnerIdentifier owner)
    {
        final SampleOwner ownerId = tryFigureSampleOwner(owner);
        if (ownerId == null)
        {
            throw UserFailureException.fromTemplate("Incorrect space or database name in '%s'",
                    owner);
        }
        return ownerId;
    }

    // determines the owner of the sample if it belongs to the home group or home database
    public SampleOwner tryFigureSampleOwner(final SampleOwnerIdentifier owner)
    {
        if (owner.isDatabaseInstanceLevel())
        {
            return tryFigureSampleDatabaseOwner();
        } else if (owner.isSpaceLevel())
        {
            return tryFigureSampleGroupOwner(owner);
        } else if (owner.isProjectLevel())
        {
            return tryFigureSampleProjectOwner(owner);
        } else
            throw InternalErr.error();
    }
    
    private SampleOwner tryFigureSampleProjectOwner(SampleOwnerIdentifier owner)
    {
        ProjectPE project = SpaceIdentifierHelper.tryGetProject(owner.getProjectLevel(), personPE, daoFactory);
        return project == null ? null : SampleOwner.createProject(project);
    }

    private SampleOwner tryFigureSampleGroupOwner(final SampleOwnerIdentifier owner)
    {
        if (owner.isInsideHomeSpace())
        {
            return createHomeGroupOwner(owner);
        } else
        {
            final SpaceIdentifier spaceIdentifier = owner.getSpaceLevel();
            return tryFindAbsoluteGroupOwner(spaceIdentifier);
        }
    }

    private SampleOwner tryFigureSampleDatabaseOwner()
    {
        return SampleOwner.createDatabaseInstance();
    }

    private SampleOwner tryFindAbsoluteGroupOwner(final SpaceIdentifier spaceIdentifier)
    {
        final SpacePE group =
                SpaceIdentifierHelper.tryGetSpace(spaceIdentifier, personPE, daoFactory);
        if (group == null)
        {
            return null;
        }
        return SampleOwner.createSpace(group);
    }

    private SampleOwner createHomeGroupOwner(final SampleOwnerIdentifier identifier)
    {
        final SpacePE homeGroup = personPE.getHomeSpace();
        if (homeGroup == null)
        {
            throw new UndefinedSpaceException();
        }
        return SampleOwner.createSpace(homeGroup);
    }
}
