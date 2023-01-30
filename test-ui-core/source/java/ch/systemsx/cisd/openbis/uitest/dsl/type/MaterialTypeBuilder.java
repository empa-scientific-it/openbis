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

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateMaterialTypeRmi;
import ch.systemsx.cisd.openbis.uitest.type.MaterialType;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
public class MaterialTypeBuilder implements Builder<MaterialType>
{
    private String code;

    public MaterialTypeBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
    }

    @SuppressWarnings("hiding")
    public MaterialTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public MaterialType build(Application openbis, Ui ui)
    {
        MaterialType type = new MaterialTypeDsl(code);
        if (Ui.WEB.equals(ui))
        {
            throw new UnsupportedOperationException();
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            return openbis.execute(new CreateMaterialTypeRmi(type));
        } else
        {
            return type;
        }
    }
}
