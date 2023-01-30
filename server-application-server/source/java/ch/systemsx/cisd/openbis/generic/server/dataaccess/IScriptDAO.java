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

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * <i>Data Access Object</i> for {@link ScriptPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IScriptDAO extends IGenericDAO<ScriptPE>
{

    /**
     * Creates or updates the specified script.
     */
    void createOrUpdate(ScriptPE script);

    /**
     * Returns script with given name or null if no such script exists.
     */
    ScriptPE tryFindByName(String scriptName);

    /**
     * Returns entities.
     */
    public List<ScriptPE> listEntities(ScriptType scriptTypeOrNull, EntityKind entityKindOrNull)
            throws DataAccessException;

}
