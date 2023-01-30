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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Criteria for listing <i>materials</i> and displaying them in the grid.<br>
 * 
 * @author Piotr Buczek
 */
public class ListMaterialDisplayCriteria extends
        DefaultResultSetConfig<String, TableModelRowWithObject<Material>> implements IsSerializable
{
    public static ListMaterialDisplayCriteria createForMaterialType(final MaterialType materialType)
    {
        return new ListMaterialDisplayCriteria(materialType);
    }

    public static ListMaterialDisplayCriteria createForMetaproject(
            MetaprojectCriteria metaprojectCriteria)
    {
        return new ListMaterialDisplayCriteria(metaprojectCriteria);
    }

    private ListMaterialCriteria listCriteria;

    private MetaprojectCriteria metaprojectCriteria;

    private ListMaterialDisplayCriteria(final MaterialType materialType)
    {
        assert materialType != null : "material type not set";
        this.listCriteria = ListMaterialCriteria.createFromMaterialType(materialType);
    }

    private ListMaterialDisplayCriteria(final MetaprojectCriteria metaprojectCriteria)
    {
        this.metaprojectCriteria = metaprojectCriteria;
    }

    public ListMaterialCriteria getListCriteria()
    {
        return listCriteria;
    }

    public MetaprojectCriteria getMetaprojectCriteria()
    {
        return metaprojectCriteria;
    }

    //
    // GWT only
    //
    private ListMaterialDisplayCriteria()
    {
    }

}
