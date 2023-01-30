/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * <i>Data Access Object</i> for {@link SpacePE}.
 * 
 * @author Christian Ribeaud
 */
public interface ISpaceDAO extends IGenericDAO<SpacePE>
{

    /**
     * Returns a list of {@link SpacePE}s (independent of {@link DatabaseInstancePE} each space belongs to).
     */
    public List<SpacePE> listSpaces() throws DataAccessException;

    /**
     * List spaces by ids.
     */
    public List<SpacePE> listByIDs(Collection<Long> ids);

    /** Creates a new space in the database. */
    public void createSpace(final SpacePE spaceDTO) throws DataAccessException;

    /**
     * Returns space identified by given <var>spaceCode</var> or <code>null</code> if such a space does not exist.
     */
    public SpacePE tryFindSpaceByCode(final String spaceCode) throws DataAccessException;

    /**
     * Returns spaces identified by given <var>spaceCodes</var> or an empty list if such spaces do not exist.
     */
    public List<SpacePE> tryFindSpaceByCodes(final List<String> spaceCodes) throws DataAccessException;

}
