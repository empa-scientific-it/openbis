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
package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Criteria for listing scripts.
 * 
 * @author Izabela Adamczyk
 */
public final class ListScriptsCriteria extends DefaultResultSetConfig<String, TableModelRowWithObject<Script>> implements
        IsSerializable
{
    // If "null", all scripts should be included
    private EntityKind entityKindOrNull;

    private ScriptType scriptTypeOrNull;

    public ListScriptsCriteria()
    {
    }

    public EntityKind tryGetEntityKind()
    {
        return entityKindOrNull;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKindOrNull = entityKind;
    }

    public ScriptType tryGetScriptType()
    {
        return scriptTypeOrNull;
    }

    public void setScriptType(ScriptType scriptType)
    {
        this.scriptTypeOrNull = scriptType;
    }

}
