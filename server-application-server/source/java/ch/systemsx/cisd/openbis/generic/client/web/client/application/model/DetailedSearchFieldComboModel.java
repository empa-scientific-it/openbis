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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISearchFieldKind;

/** Model of combo box used in detailed entity search. */
public class DetailedSearchFieldComboModel extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    private static final String KIND = "kind";

    public DetailedSearchFieldComboModel(String code, DetailedSearchField field,
            ISearchFieldKind kind)
    {
        set(ModelDataPropertyNames.CODE, code);
        set(ModelDataPropertyNames.OBJECT, field);
        set(KIND, kind);
    }

    public DetailedSearchField getField()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

    public ISearchFieldKind getKind()
    {
        return get(KIND);
    }

}
