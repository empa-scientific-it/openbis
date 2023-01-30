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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Console;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.Material;

/**
 * @author anttil
 */
public class CreateMaterialRmi implements Command<Material>
{
    @Inject
    private String session;

    @Inject
    private IGenericServer genericServer;

    @Inject
    private ICommonServer commonServer;
    
    @Inject
    private Console console;

    private Material material;

    public CreateMaterialRmi(Material material)
    {
        this.material = material;
    }

    @Override
    public Material execute()
    {
        List<NewMaterialsWithTypes> newMaterials = new ArrayList<NewMaterialsWithTypes>();

        NewMaterialsWithTypes newMaterial = new NewMaterialsWithTypes();
        newMaterial.setAllowUpdateIfExist(false);

        MaterialType type = new MaterialType();
        type.setCode(material.getType().getCode());

        newMaterial.setEntityType(type);

        NewMaterial data = new NewMaterial();
        data.setCode(material.getCode());
        data.setProperties(new IEntityProperty[0]);
        List<NewMaterial> list = new ArrayList<NewMaterial>();
        list.add(data);
        newMaterial.setNewEntities(list);

        newMaterials.add(newMaterial);

        console.startBuffering();
        genericServer.registerMaterials(session, newMaterials);
        MaterialIdentifier identifier = new MaterialIdentifier(material.getCode(), type.getCode());
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material materialInfo 
                = commonServer.getMaterialInfo(session, identifier);

        return material;
    }
}
