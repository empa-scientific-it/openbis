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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Business object of a script.
 * 
 * @author Izabela Adamczyk
 */
public interface IScriptBO extends IEntityBusinessObject
{

    /**
     * Deletes and returns script of specified id.
     * 
     * @param scriptId script technical identifier
     * @throws UserFailureException if script with given technical identifier is not found.
     */
    public ScriptPE deleteByTechId(TechId scriptId);

    /**
     * Defines a new script.
     */
    public void define(Script script);

    /**
     * Updates the script.
     */
    public void update(IScriptUpdates updates);

    public void tryDefineOrUpdateIfPossible(Script script);

    public void tryDeleteOrInvalidatePredeployedPlugin(String name, ScriptType scriptType);

    public ScriptPE getScript();

}
