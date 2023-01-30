/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.List;

/**
 * Full specification of the field connected with an entity which can be used in detailed text queries.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class DetailedSearchField implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DetailedSearchFieldKind kind;

    private String codeOrNull; // attribute code / property code

    private List<String> allEntityPropertyCodesOrNull;

    public static DetailedSearchField createAnyField(List<String> allEntityPropertyCodes)
    {
        assert allEntityPropertyCodes != null : "property codes not set";
        return new DetailedSearchField(DetailedSearchFieldKind.ANY_FIELD, null,
                allEntityPropertyCodes);
    }

    public static DetailedSearchField createAnyPropertyField(List<String> allEntityPropertyCodes)
    {
        assert allEntityPropertyCodes != null : "property codes not set";
        return new DetailedSearchField(DetailedSearchFieldKind.ANY_PROPERTY, null,
                allEntityPropertyCodes);
    }

    public static DetailedSearchField createPropertyField(String propertyCode)
    {
        assert propertyCode != null : "property code not set";
        return new DetailedSearchField(DetailedSearchFieldKind.PROPERTY, propertyCode);
    }

    public static DetailedSearchField createAttributeField(
            IAttributeSearchFieldKind attributeFieldKind)
    {
        assert attributeFieldKind != null : "attribute not set";
        return createAttributeField(attributeFieldKind.getCode());
    }

    public static DetailedSearchField createRegistratorField()
    {
        return new DetailedSearchField(DetailedSearchFieldKind.REGISTRATOR, null);
    }

    private static DetailedSearchField createAttributeField(String code)
    {
        assert code != null : "code not set";
        return new DetailedSearchField(DetailedSearchFieldKind.ATTRIBUTE, code);
    }

    // GWT only
    private DetailedSearchField()
    {
        this(null, null);
    }

    private DetailedSearchField(DetailedSearchFieldKind kind, String codeOrNull)
    {
        this(kind, codeOrNull, null);
    }

    private DetailedSearchField(DetailedSearchFieldKind kind, String codeOrNull,
            List<String> allEntityPropertyCodesOrNull)
    {
        this.kind = kind;
        this.codeOrNull = codeOrNull;
        this.allEntityPropertyCodesOrNull = allEntityPropertyCodesOrNull;
    }

    public DetailedSearchFieldKind getKind()
    {
        return kind;
    }

    public String getAttributeCode()
    {
        assert kind == DetailedSearchFieldKind.ATTRIBUTE;
        return codeOrNull;
    }

    public String getPropertyCode()
    {
        assert kind == DetailedSearchFieldKind.PROPERTY;
        return codeOrNull;
    }

    public List<String> getAllEntityPropertyCodesOrNull()
    {
        assert kind == DetailedSearchFieldKind.ANY_PROPERTY
                || kind == DetailedSearchFieldKind.ANY_FIELD;
        return allEntityPropertyCodesOrNull;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getKind());
        if (codeOrNull != null)
        {
            sb.append(" " + codeOrNull);
        }
        return sb.toString();
    }
}
