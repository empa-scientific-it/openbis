/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.PlateMetadataGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.PlateMetadataGridColumnIDs.TYPE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Original data provider for plate content.
 * 
 * @author Izabela Adamczyk
 * @author Franz-Josef Elmer
 */
public class PlateMetadataProvider extends AbstractTableModelProvider<WellMetadata>
{
    static final String CONTENT_PROPERTY_PREFIX = "CONTENT_PROPERTY__";

    private final IScreeningServer server;

    private final String sessionToken;

    private final TechId plateId;

    public PlateMetadataProvider(IScreeningServer server, String sessionToken, TechId plateId)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.plateId = plateId;
    }

    @Override
    public TypedTableModel<WellMetadata> createTableModel()
    {
        TypedTableModelBuilder<WellMetadata> builder = new TypedTableModelBuilder<WellMetadata>();
        PlateContent plateContent = server.getPlateContent(sessionToken, plateId);
        List<WellMetadata> wells = plateContent.getPlateMetadata().getWells();
        builder.addColumn(CODE);
        builder.addColumn(TYPE);
        for (WellMetadata wellMetadata : wells)
        {
            builder.addRow(wellMetadata);
            Sample well = wellMetadata.getWellSample();
            builder.column(CODE).addString(well.getCode());
            builder.column(TYPE).addString(well.getSampleType().getCode());
            builder.columnGroup(CONTENT_PROPERTY_PREFIX).addProperties(well.getProperties());
        }
        return builder.getModel();
    }

}
