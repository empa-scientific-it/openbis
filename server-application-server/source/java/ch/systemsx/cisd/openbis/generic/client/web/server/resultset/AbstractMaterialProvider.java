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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.MATERIAL_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.METAPROJECTS;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Abstract super class of {@link Material} providers.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractMaterialProvider extends AbstractTableModelProvider<Material>
{

    @Override
    protected TypedTableModel<Material> createTableModel()
    {
        List<Material> materials = getMaterials();
        TypedTableModelBuilder<Material> builder = new TypedTableModelBuilder<Material>();
        builder.addColumn(CODE);
        builder.addColumn(MATERIAL_TYPE).hideByDefault();
        builder.addColumn(REGISTRATOR);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(200);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(200).hideByDefault();
        builder.addColumn(METAPROJECTS);
        for (Material material : materials)
        {
            builder.addRow(material);
            builder.column(CODE).addEntityLink(material, material.getCode());
            MaterialType materialType = material.getMaterialType();
            builder.column(MATERIAL_TYPE).addString(materialType.getCode());
            builder.column(REGISTRATOR).addPerson(material.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(material.getRegistrationDate());
            builder.column(MODIFICATION_DATE).addDate(material.getModificationDate());
            builder.column(METAPROJECTS)
                    .addString(metaProjectsToString(material.getMetaprojects()));
            IColumnGroup columnGroup = builder.columnGroup(MaterialGridColumnIDs.PROPERTIES_GROUP);
            columnGroup.addColumnsForAssignedProperties(materialType);
            columnGroup.addProperties(material.getProperties());
        }

        return builder.getModel();
    }

    protected abstract List<Material> getMaterials();

}
