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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

/**
 * Contains a list of new materials and their type.
 * 
 * @author Pawel Glyzewski
 */

public class NewMaterialsWithTypes extends NewEntitiesWithTypes<MaterialType, NewMaterial>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public NewMaterialsWithTypes()
    {
        super();
    }

    public NewMaterialsWithTypes(MaterialType type, List<NewMaterial> newMaterials)
    {
        super(type, newMaterials);
    }
}