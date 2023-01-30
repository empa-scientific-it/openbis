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

package ch.systemsx.cisd.openbis.generic.server.util;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

/**
 * Some useful identifier methods on the <i>server</i> side.
 * 
 * @author Franz-Josef Elmer
 */
public final class SpaceIdentifierHelper
{

    private SpaceIdentifierHelper()
    {
        // Can not be instantiated.
    }

    /** finds a space in the database for the given identifier */
    public static final SpacePE tryGetSpace(final SpaceIdentifier spaceIdentifier,
            final PersonPE person, final IAuthorizationDAOFactory daoFactory)
    {
        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceIdentifier);
        final ISpaceDAO groupDAO = daoFactory.getSpaceDAO();
        return groupDAO.tryFindSpaceByCode(spaceCode);
    }
    
    public static ProjectPE tryGetProject(ProjectIdentifier projectIdentifier,
            final PersonPE person, final IAuthorizationDAOFactory daoFactory)
    {
        String spaceCode = SpaceCodeHelper.getSpaceCode(person, projectIdentifier);
        IProjectDAO projectDAO = daoFactory.getProjectDAO();
        return projectDAO.tryFindProject(spaceCode, projectIdentifier.getProjectCode());
    }
}
