/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.ANALYSIS_PROCEDURE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.EXPERIMENT;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.IMAGE_ANALYSIS_DATA_SET;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.IMAGE_DATA_SET;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.PLATE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.WELL;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds.WELL_IMAGES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumn;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NamedFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.WellSearchGridColumnIds;

/**
 * @author Franz-Josef Elmer
 */
public class WellContentProvider extends AbstractTableModelProvider<WellContent>
{
    static final String WELL_PROPERTY_ID_PREFIX = "WELL_PROPERTY-";

    static final String WELL_CONTENT_FEATURE_VECTOR_GROUP = "WELL_CONTENT_FEATURE_VECTOR-";

    private final IScreeningServer server;

    private final String sessionToken;

    private final WellSearchCriteria materialCriteria;

    public WellContentProvider(IScreeningServer server, String sessionToken,
            WellSearchCriteria materialCriteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.materialCriteria = materialCriteria;
    }

    @Override
    protected TypedTableModel<WellContent> createTableModel()
    {
        TypedTableModelBuilder<WellContent> builder = new TypedTableModelBuilder<WellContent>();

        List<WellContent> wells = server.listPlateWells(sessionToken, materialCriteria);
        List<IEntityProperty> materialPropertyOrder = extractOrderedMaterialProperties(wells);
        sortByMaterialCodes(wells, materialPropertyOrder);

        initColumnItems(builder, materialPropertyOrder);
        for (WellContent well : wells)
        {
            addRow(builder, well, materialPropertyOrder);
        }
        return builder.getModel();
    }

    private void initColumnItems(TypedTableModelBuilder<WellContent> builder,
            List<IEntityProperty> materialPropertyOrder)
    {
        builder.addColumn(WELL_IMAGES).withDefaultWidth(500);

        addMaterialColumns(builder, materialPropertyOrder);

        builder.columnGroup(WELL_PROPERTY_ID_PREFIX);
        builder.addColumn(EXPERIMENT);
        builder.addColumn(PLATE);
        builder.addColumn(WELL);
        builder.columnGroup(WELL_CONTENT_FEATURE_VECTOR_GROUP);
        builder.addColumn(FILE_FORMAT_TYPE);
        builder.addColumn(IMAGE_DATA_SET);
        builder.addColumn(IMAGE_ANALYSIS_DATA_SET);
        builder.addColumn(ANALYSIS_PROCEDURE);
    }

    private void addMaterialColumns(TypedTableModelBuilder<WellContent> builder,
            List<IEntityProperty> materialPropertyOrder)
    {
        for (IEntityProperty materialProperty : materialPropertyOrder)
        {
            List<PropertyType> materialPropertyType =
                    Collections.singletonList(materialProperty.getPropertyType());
            getMaterialColumnGroup(builder, materialProperty).addColumnsForPropertyTypes(
                    materialPropertyType);

            List<IEntityProperty> materialProperties =
                    materialProperty.getMaterial().getProperties();
            List<PropertyType> propertyTypes = extractPropertyTypes(materialProperties);
            getMaterialPropsColumnGroup(builder, materialProperty).addColumnsForPropertyTypes(
                    propertyTypes);
        }
    }

    /**
     * Return the id of a column group which will contain a single well property of type material. It has to be in a separate group to achieve a
     * specific sorting in UI, where a material precedes its own properties.
     */
    private IColumnGroup getMaterialColumnGroup(TypedTableModelBuilder<WellContent> builder,
            IEntityProperty materialProperty)
    {
        return builder.columnGroup(WellSearchGridColumnIds
                .getWellMaterialColumnGroupPrefix(materialProperty));
    }

    /**
     * Return the id of a column group which will contain all properties for a material.
     */
    private IColumnGroup getMaterialPropsColumnGroup(TypedTableModelBuilder<WellContent> builder,
            IEntityProperty materialProperty)
    {
        return builder.columnGroup(WellSearchGridColumnIds
                .getWellMaterialPropertyColumnGroupPrefix(materialProperty));
    }

    private void addRow(TypedTableModelBuilder<WellContent> builder, WellContent well,
            List<IEntityProperty> materialPropertyOrder)
    {
        builder.addRow(well);

        builder.column(WELL_IMAGES).addString(well.tryGetImageDataset() == null ? "" : "[images]");

        addMaterialProperties(builder, well, materialPropertyOrder);
        addNonMaterialProperties(builder, well);

        builder.column(EXPERIMENT).addString(well.getExperiment().toString());
        builder.column(PLATE).addString(well.getPlate().getCode());
        builder.column(WELL).addString(well.getWell().getCode());

        NamedFeatureVector featureVector = well.tryGetFeatureVectorValues();
        if (featureVector != null)
        {
            addFeatureColumns(builder, featureVector);
        }

        DatasetImagesReference imageDataset = well.tryGetImageDataset();
        builder.column(FILE_FORMAT_TYPE).addString(
                imageDataset == null ? null : imageDataset.getDatasetReference().getFileTypeCode());
        builder.column(IMAGE_DATA_SET).addString(
                imageDataset == null ? null : imageDataset.getDatasetCode());
        DatasetReference dataset = well.tryGetFeatureVectorDataset();
        builder.column(IMAGE_ANALYSIS_DATA_SET).addString(
                dataset == null ? null : dataset.getCode());
        builder.column(ANALYSIS_PROCEDURE).addString(
                dataset == null ? null : dataset.getAnalysisProcedure());

    }

    private void addNonMaterialProperties(TypedTableModelBuilder<WellContent> builder,
            WellContent well)
    {
        List<IEntityProperty> wellProperties = well.getWellProperties();
        List<IEntityProperty> nonMaterialProperties = new ArrayList<IEntityProperty>();
        for (IEntityProperty property : wellProperties)
        {
            DataTypeCode propertyDataTypeCode = property.getPropertyType().getDataType().getCode();
            if (propertyDataTypeCode == DataTypeCode.MATERIAL)
            {
                // skip
            } else
            {
                nonMaterialProperties.add(property);
            }

        }
        builder.columnGroup(WELL_PROPERTY_ID_PREFIX).addProperties(nonMaterialProperties);

    }

    private void addMaterialProperties(TypedTableModelBuilder<WellContent> builder,
            WellContent well, List<IEntityProperty> materialPropsOrder)
    {
        for (IEntityProperty materialTypeProperty : well.getMaterialTypeProperties())
        {
            Material materialOrNull = materialTypeProperty.getMaterial();
            if (materialOrNull != null)
            {
                // add material column
                getMaterialColumnGroup(builder, materialTypeProperty).addProperties(
                        Collections.singletonList(materialTypeProperty));

                // add material properties columns
                List<IEntityProperty> materialProperties = materialOrNull.getProperties();
                if (materialProperties != null)
                {
                    getMaterialPropsColumnGroup(builder, materialTypeProperty).addProperties(
                            materialOrNull.getProperties());
                }
            }
        }
    }

    private void addFeatureColumns(TypedTableModelBuilder<WellContent> builder,
            NamedFeatureVector featureVector)
    {
        FeatureValue[] values = featureVector.getValues();
        String[] labels = featureVector.getFeatureLabels();
        String[] codes = featureVector.getFeatureCodes();
        for (int i = 0; i < values.length; i++)
        {
            IColumn column =
                    builder.column(WELL_CONTENT_FEATURE_VECTOR_GROUP + codes[i]).withTitle(
                            labels[i]);
            FeatureValue featureValue = values[i];
            if (featureValue.isFloat())
            {
                column.addDouble(Double.valueOf(featureValue.asFloat()));
            } else
            {
                column.addString(featureValue.tryAsVocabularyTerm());
            }
        }
    }

    private List<PropertyType> extractPropertyTypes(List<IEntityProperty> materialProperties)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        if (materialProperties != null)
        {
            for (IEntityProperty prop : materialProperties)
            {
                propertyTypes.add(prop.getPropertyType());
            }
        }
        return propertyTypes;
    }

    private List<IEntityProperty> extractOrderedMaterialProperties(List<WellContent> wells)
    {
        TreeMap<String, IEntityProperty> orderedMaterialProps =
                new TreeMap<String, IEntityProperty>();
        for (WellContent well : wells)
        {
            for (IEntityProperty materialProperty : well.getMaterialTypeProperties())
            {
                String propCode = materialProperty.getPropertyType().getCode();
                orderedMaterialProps.put(propCode, materialProperty);
            }
        }

        return new ArrayList<IEntityProperty>(orderedMaterialProps.values());
    }

    private void sortByMaterialCodes(List<WellContent> wells,
            final List<IEntityProperty> materialPropsOrder)
    {
        Collections.sort(wells, new Comparator<WellContent>()
            {
                @Override
                public int compare(WellContent o1, WellContent o2)
                {
                    for (IEntityProperty materialProperty : materialPropsOrder)
                    {
                        String materialPropCode = materialProperty.getPropertyType().getCode();
                        boolean o1HasMaterial = hasMaterialTypeProperty(o1, materialPropCode);
                        boolean o2HasMaterial = hasMaterialTypeProperty(o2, materialPropCode);
                        if (o1HasMaterial != o2HasMaterial)
                        {
                            return (o1HasMaterial) ? -1 : 1;
                        }
                    }
                    return 0;
                }

            });
    }

    private boolean hasMaterialTypeProperty(WellContent well, String materialPropCode)
    {
        for (IEntityProperty materialProp : well.getMaterialTypeProperties())
        {
            if (materialProp.getPropertyType().getCode().equals(materialPropCode))
            {
                return true;
            }
        }
        return false;
    }
}
