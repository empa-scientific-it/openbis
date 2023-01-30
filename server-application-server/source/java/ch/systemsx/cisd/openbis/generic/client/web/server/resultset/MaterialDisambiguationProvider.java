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

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.MATERIAL_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.PROPERTIES_GROUP;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs.REGISTRATOR;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Table model provider of {@link Material} instances.
 * 
 * @author Tomasz Pylak
 */
public class MaterialDisambiguationProvider extends AbstractTableModelProvider<Material>
{
    private final List<Material> materials;

    public MaterialDisambiguationProvider(List<Material> materials)
    {
        this.materials = materials;
    }

    @Override
    protected TypedTableModel<Material> createTableModel()
    {

        TypedTableModelBuilder<Material> builder = new TypedTableModelBuilder<Material>();
        addStandardColumns(builder);

        for (Material material : materials)
        {
            addRow(builder, material);
        }
        return builder.getModel();
    }

    private void addStandardColumns(TypedTableModelBuilder<Material> builder)
    {
        builder.addColumn(CODE);
        builder.addColumn(MATERIAL_TYPE);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(REGISTRATOR).withDefaultWidth(200).hideByDefault();
    }

    private void addRow(TypedTableModelBuilder<Material> builder, Material material)
    {
        builder.addRow(material);
        builder.column(CODE).addString(material.getCode());
        builder.column(MATERIAL_TYPE).addString(material.getEntityType().getCode());
        builder.column(REGISTRATION_DATE).addDate(material.getRegistrationDate());
        builder.column(REGISTRATOR).addPerson(material.getRegistrator());

        builder.columnGroup(PROPERTIES_GROUP).addProperties(material.getProperties());
    }
}
