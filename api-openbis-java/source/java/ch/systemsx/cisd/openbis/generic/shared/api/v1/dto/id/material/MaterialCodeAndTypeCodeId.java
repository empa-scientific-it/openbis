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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Identifies a material by code and material type code.
 * 
 * @author pkupczyk
 */
@JsonObject("MaterialCodeAndTypeCodeId")
public class MaterialCodeAndTypeCodeId implements IMaterialId
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String typeCode;

    /**
     * @param code Material code
     * @param typeCode Material type code
     */
    public MaterialCodeAndTypeCodeId(String code, String typeCode)
    {
        setCode(code);
        setTypeCode(typeCode);
    }

    public String getCode()
    {
        return code;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private MaterialCodeAndTypeCodeId()
    {
    }

    private void setCode(String code)
    {
        if (code == null)
        {
            throw new IllegalArgumentException("Code cannot be null");
        }
        this.code = code;
    }

    private void setTypeCode(String typeCode)
    {
        if (typeCode == null)
        {
            throw new IllegalArgumentException("Type code cannot be null");
        }
        this.typeCode = typeCode;
    }

    @Override
    public String toString()
    {
        return getCode() + " (" + getTypeCode() + ")";
    }

}
