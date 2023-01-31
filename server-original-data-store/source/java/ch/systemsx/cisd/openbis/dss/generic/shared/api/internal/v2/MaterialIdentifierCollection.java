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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * A collection of material identifiers.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialIdentifierCollection
{

    private List<String> materialIdentifiers = new ArrayList<String>();

    public void addIdentifier(String type, String code)
    {
        String identifier = new MaterialIdentifier(code, type).print();
        materialIdentifiers.add(identifier);
    }

    public List<String> getIdentifiers()
    {
        return Collections.unmodifiableList(materialIdentifiers);
    }
}
