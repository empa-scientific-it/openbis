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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateMaterialRmi;
import ch.systemsx.cisd.openbis.uitest.type.Material;
import ch.systemsx.cisd.openbis.uitest.type.MaterialType;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
public class MaterialBuilder implements Builder<Material>
{

    private String code;

    private MaterialType materialType;

    @SuppressWarnings("unused")
    private Collection<MetaProject> metaProjects;

    private UidGenerator uid;

    public MaterialBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.code = uid.uid();
    }

    @Override
    public Material build(Application openbis, Ui ui)
    {
        if (materialType == null)
        {
            materialType = new MaterialTypeBuilder(uid).build(openbis, ui);
        }

        Material material = new MaterialDsl(code, materialType, new HashSet<MetaProject>());

        if (Ui.WEB.equals(ui))
        {
            throw new UnsupportedOperationException();
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            return openbis.execute(new CreateMaterialRmi(material));
        } else
        {
            return material;
        }
    }
}
