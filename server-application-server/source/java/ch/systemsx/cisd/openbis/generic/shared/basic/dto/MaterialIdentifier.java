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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;

/**
 * Identifier for searching material through <i>Web Service</i> lookup methods in the database.
 * 
 * @author Christian Ribeaud
 */
public final class MaterialIdentifier implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String TYPE_SEPARATOR_PREFIX = " (";

    public static final String TYPE_SEPARATOR_SUFFIX = ")";

    /**
     * The material code of this material: together with typeCode uniquely identifies the material.
     * <p>
     * Could not be <code>null</code>.
     * </p>
     */
    private String code;

    /**
     * The code of material type of this material: together with code uniquely identifies the material.
     * <p>
     * Could not be <code>null</code>.
     * </p>
     */
    private String typeCode;

    public MaterialIdentifier()
    {
    }

    public MaterialIdentifier(String code, String typeCode)
    {
        this.code = code;
        this.typeCode = typeCode;
    }

    public MaterialIdentifier(IEntityInformationHolderWithPermId material)
    {
        this(material.getCode(), material.getEntityType().getCode());
    }

    public final String getCode()
    {
        return code;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(final String typeCode)
    {
        this.typeCode = typeCode;
    }

    // -----------
    /**
     * Creates material identifier from specified identifier or code and material type code if no full identifier is specified.
     * 
     * @return <code>null</code> if no full identifier specified and material type is unknown.
     * @throw {@link IllegalArgumentException} if material type of full identifier doesn't match specified material type.
     */
    public static MaterialIdentifier tryCreate(String codeOrIdentifierOrNull,
            ICodeHolder materialTypeCodeHolderOrNull)
    {
        MaterialIdentifier materialIdentifier =
                MaterialIdentifier.tryParseIdentifier(codeOrIdentifierOrNull);
        if (materialIdentifier != null)
        {
            if (materialTypeCodeHolderOrNull != null
                    && materialIdentifier.getTypeCode().equals(
                            materialTypeCodeHolderOrNull.getCode()) == false)
            {
                throw new IllegalArgumentException("Material identified by '" + materialIdentifier
                        + "' has to be of type " + materialTypeCodeHolderOrNull.getCode() + ".");
            }
            return materialIdentifier;
        }
        // if the material type of the property is fixed, then we accept when only material
        // code is specified and its type is skipped (we know what the type should be)
        if (materialTypeCodeHolderOrNull != null
                && StringUtils.isBlank(codeOrIdentifierOrNull) == false)
        {
            return new MaterialIdentifier(codeOrIdentifierOrNull,
                    materialTypeCodeHolderOrNull.getCode());
        }
        // identifier is invalid or null
        return null;
    }

    /**
     * Parses the material code and type. Assumes the syntax: "code (type)". Returns the chosen material if parsing went ok, null otherwise.
     */
    public static MaterialIdentifier tryParseIdentifier(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        int typePrefix = value.indexOf(TYPE_SEPARATOR_PREFIX);
        if (typePrefix == -1)
        {
            return null;
        }
        String code = value.substring(0, typePrefix);
        String typeCode = value.substring(typePrefix + TYPE_SEPARATOR_PREFIX.length());
        // we allow to omit the closing brace
        if (typeCode.endsWith(TYPE_SEPARATOR_SUFFIX))
        {
            typeCode = typeCode.substring(0, typeCode.length() - TYPE_SEPARATOR_SUFFIX.length());
        }
        return new MaterialIdentifier(code, typeCode);
    }

    /** Prints the identifier in the canonical form */
    public String print()
    {
        return print(getCode(), getTypeCode());
    }

    /** Prints the identifier in the canonical form */
    public static String print(String code, String typeCode)
    {
        return toString(code, typeCode);
    }

    /** Prints the identifier in the canonical form */
    public static String toString(String code, String typeCode)
    {
        return code + TYPE_SEPARATOR_PREFIX + typeCode + TYPE_SEPARATOR_SUFFIX;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        assert code != null : "code not given";
        assert typeCode != null : "type not given";

        if (obj == null || obj instanceof MaterialIdentifier == false)
        {
            return false;
        }
        MaterialIdentifier that = (MaterialIdentifier) obj;
        return code.equals(that.code) && typeCode.equals(that.typeCode);
    }

    @Override
    public final int hashCode()
    {
        assert code != null : "code not given";
        assert typeCode != null : "type not given";
        return code.hashCode() ^ typeCode.hashCode();
    }

    @Override
    public String toString()
    {
        return print();
    }

}
