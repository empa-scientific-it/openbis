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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.ObjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Identifies a metaproject by identifier.
 * 
 * @author pkupczyk
 */
@JsonObject("MetaprojectIdentifierId")
public class MetaprojectIdentifierId extends ObjectIdentifierId implements IMetaprojectId
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * @param identifier Metaproject identifier, e.g. "/MY_USER/MY_METAPROJECT".
     */
    public MetaprojectIdentifierId(String identifier)
    {
        super(identifier);
    }

    /**
     * @param identifier Metaproject identifier
     */
    public MetaprojectIdentifierId(MetaprojectIdentifier identifier)
    {
        super(identifier.format());
    }

    /**
     * @param ownerId Metaproject owner
     * @param name Metaproject name
     */
    public MetaprojectIdentifierId(String ownerId, String name)
    {
        this(new MetaprojectIdentifier(ownerId, name));
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private MetaprojectIdentifierId()
    {
        super();
    }

}
