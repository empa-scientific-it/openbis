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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.EXPERIMENT_PERM_ID;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.MATERIAL_ID;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.MATERIAL_PROPS_GROUP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs;

/**
 * A provider for material feature vector summaries.
 * 
 * @author Kaloyan Enimanev
 */
public class FeatureVectorSummaryProvider extends
        AbstractTableModelProvider<MaterialFeatureVectorSummary>
{
    private static final String FEATURE_VALUE_PREFIX = "FEATURE_VALUE-";

    private final IScreeningServer server;

    private final String sessionToken;

    private final TechId experimentId;

    private final AnalysisProcedureCriteria analysisProcedureCriteria;

    private final ICommonServer commonServer;

    public FeatureVectorSummaryProvider(ICommonServer commonServer, IScreeningServer server, String sessionToken,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        this.commonServer = commonServer;
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentId = experimentId;
        this.analysisProcedureCriteria = analysisProcedureCriteria;
    }

    @Override
    public TypedTableModel<MaterialFeatureVectorSummary> createTableModel()
    {
        TypedTableModelBuilder<MaterialFeatureVectorSummary> builder =
                new TypedTableModelBuilder<MaterialFeatureVectorSummary>();
        ExperimentFeatureVectorSummary fvSummary =
                server.getExperimentFeatureVectorSummary(sessionToken, experimentId,
                        analysisProcedureCriteria);
        TableModel tableModel = fvSummary.getTableModelOrNull();
        if (tableModel == null)
        {
            return buildTableFromSummary(builder, fvSummary);
        }
        return buildTableFromTableModel(tableModel);
    }

    private TypedTableModel<MaterialFeatureVectorSummary> buildTableFromTableModel(
            TableModel tableModel)
    {
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        List<TableModelRow> rows = tableModel.getRows();
        Map<String, Material> materialsByIdentifer = new HashMap<String, Material>();
        int materialColumnIndex = findMaterialColumnIndex(tableModel);
        if (materialColumnIndex >= 0)
        {
            int indexOfMaterialId = getIndexOfHeaderById(headers, MATERIAL_ID);
            if (indexOfMaterialId != materialColumnIndex && indexOfMaterialId >= 0)
            {
                throw new IllegalArgumentException("There is already a column with id '"
                        + MATERIAL_ID + "'. Column index: " + indexOfMaterialId);
            }
            headers.get(materialColumnIndex).setId(MATERIAL_ID);
            List<Material> materials = loadMaterials(rows, materialColumnIndex);
            for (Material material : materials)
            {
                materialsByIdentifer.put(material.getIdentifier(), material);
            }
        }
        List<TableModelRowWithObject<MaterialFeatureVectorSummary>> list =
                new ArrayList<TableModelRowWithObject<MaterialFeatureVectorSummary>>();
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            Material material = null;
            if (materialColumnIndex >= 0)
            {
                String identifierOrNull = getMaterialIdentifier(values, materialColumnIndex);
                material = materialsByIdentifer.get(identifierOrNull);
            }
            MaterialFeatureVectorSummary summary =
                    new MaterialFeatureVectorSummary(material, null, null, null, 0);
            list.add(new TableModelRowWithObject<MaterialFeatureVectorSummary>(summary, values));
        }
        return new TypedTableModel<MaterialFeatureVectorSummary>(headers, list);
    }

    private int getIndexOfHeaderById(List<TableModelColumnHeader> headers, String id)
    {
        for (int i = 0; i < headers.size(); i++)
        {
            TableModelColumnHeader header = headers.get(i);
            if (id.equals(header.getId()))
            {
                return i;
            }
        }
        return -1;
    }

    private List<Material> loadMaterials(List<TableModelRow> rows, int materialColumnIndex)
    {
        List<MaterialIdentifier> materialIdentifiers = new ArrayList<MaterialIdentifier>();
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            String identifierOrNull = getMaterialIdentifier(values, materialColumnIndex);
            materialIdentifiers.add(MaterialIdentifier.tryParseIdentifier(identifierOrNull));
        }
        ListMaterialCriteria criteria =
                ListMaterialCriteria.createFromMaterialIdentifiers(materialIdentifiers);
        return commonServer.listMaterials(sessionToken, criteria, false);
    }

    private String getMaterialIdentifier(List<ISerializableComparable> values,
            int materialColumnIndex)
    {
        if (materialColumnIndex >= values.size())
        {
            throw new IllegalArgumentException("Material column index " + materialColumnIndex
                    + " is out of bounds for row: " + values);
        }
        ISerializableComparable cell = values.get(materialColumnIndex);
        if (cell instanceof EntityTableCell == false)
        {
            throw new IllegalArgumentException("Material column index " + materialColumnIndex
                    + " points to a cell which isn't a entity table cell: " + values);
        }
        String identifierOrNull = ((EntityTableCell) cell).getIdentifierOrNull();
        if (identifierOrNull == null)
        {
            throw new IllegalArgumentException("Material column index " + materialColumnIndex
                    + " points to an entity table cell which hasn't an identifier: " + values);
        }
        return identifierOrNull.toUpperCase();
    }

    private int findMaterialColumnIndex(TableModel tableModel)
    {
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        for (int i = 0; i < headers.size(); i++)
        {
            TableModelColumnHeader header = headers.get(i);
            if (EntityKind.MATERIAL.equals(header.tryGetEntityKind()))
            {
                return i;
            }
        }
        return -1;
    }

    private TypedTableModel<MaterialFeatureVectorSummary> buildTableFromSummary(
            TypedTableModelBuilder<MaterialFeatureVectorSummary> builder,
            ExperimentFeatureVectorSummary fvSummary)
    {
        builder.addColumn(MATERIAL_ID);
        builder.addColumn(EXPERIMENT_PERM_ID).hideByDefault();

        builder.columnGroup(MATERIAL_PROPS_GROUP);

        List<CodeAndLabel> featureDescriptions = fvSummary.getFeatureDescriptions();
        List<String> featureColumnIds = new ArrayList<String>();
        List<String> rankColumnIds = new ArrayList<String>();

        for (CodeAndLabel featureDescription : featureDescriptions)
        {
            String featureCode = featureDescription.getCode();
            String featureColumnId = getFeatureColumnId(featureCode);
            String featureLabel = featureDescription.getLabel();
            builder.addColumn(featureColumnId).withTitle(featureLabel)
                    .withDataType(DataTypeCode.REAL);
            featureColumnIds.add(featureColumnId);

            String rankColumnId = getRankColumnId(featureCode);
            String rankTitle = ScreeningProviderMessages.RANK_COLUMN_MSG;
            builder.addColumn(rankColumnId).withTitle(rankTitle).withDataType(DataTypeCode.INTEGER);
            rankColumnIds.add(rankColumnId);
        }

        for (MaterialFeatureVectorSummary summary : fvSummary.getMaterialsSummary())
        {
            addRow(builder, fvSummary.getExperiment(), summary, featureColumnIds, rankColumnIds);
        }
        return builder.getModel();
    }

    private void addRow(TypedTableModelBuilder<MaterialFeatureVectorSummary> builder,
            ExperimentReference experiment, MaterialFeatureVectorSummary summary,
            List<String> featureColumnIds, List<String> rankColumnIds)
    {
        builder.addRow(summary);

        Material material = summary.getMaterial();
        builder.column(MATERIAL_ID).addString(material.getCode());
        builder.column(EXPERIMENT_PERM_ID).addString(
                experiment != null ? experiment.getPermId() : null);

        if (material.getProperties() != null)
        {
            builder.columnGroup(MATERIAL_PROPS_GROUP).addProperties(material.getProperties());
        }

        float[] featureSummaries = summary.getFeatureVectorSummary();
        int[] ranksValues = summary.getFeatureVectorRanks();

        for (int pos = 0; pos < featureSummaries.length; pos++)
        {
            String featureColumnId = featureColumnIds.get(pos);
            builder.column(featureColumnId).addDouble((double) featureSummaries[pos]);

            String rankColumnId = rankColumnIds.get(pos);
            builder.column(rankColumnId).addInteger((long) ranksValues[pos]);
        }
    }

    private String getFeatureColumnId(String code)
    {
        return FEATURE_VALUE_PREFIX + code;
    }

    private String getRankColumnId(String code)
    {
        return FeatureVectorSummaryGridColumnIDs.RANK_PREFIX + code;
    }
}
