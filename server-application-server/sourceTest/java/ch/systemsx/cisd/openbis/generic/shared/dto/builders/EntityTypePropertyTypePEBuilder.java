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

package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Builder for {@link EntityTypePropertyTypePE} instances.
 * 
 * @author felmer
 */
public class EntityTypePropertyTypePEBuilder
{
    private final EntityTypePropertyTypePE entityTypePropertyTypePE;

    EntityTypePropertyTypePEBuilder(EntityTypePropertyTypePE entityTypePropertyTypePE,
            PropertyTypePE propertyType)
    {
        this.entityTypePropertyTypePE = entityTypePropertyTypePE;
        entityTypePropertyTypePE.setPropertyType(propertyType);
    }

    public EntityTypePropertyTypePE getEntityTypePropertyType()
    {
        return entityTypePropertyTypePE;
    }

    public EntityTypePropertyTypePEBuilder ordinal(int ordinal)
    {
        entityTypePropertyTypePE.setOrdinal((long) ordinal);
        return this;
    }

    public EntityTypePropertyTypePEBuilder script(ScriptType scriptType, String script)
    {
        ScriptPE scriptPE = new ScriptPE();
        scriptPE.setScript(script);
        scriptPE.setScriptType(scriptType);
        scriptPE.setPluginType(PluginType.JYTHON);
        entityTypePropertyTypePE.setScript(scriptPE);
        return this;
    }
}
