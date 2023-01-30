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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.server.IBasicTableDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRenderer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TableForUpdateExporter
{

    @SuppressWarnings("unchecked")
    public static String getExportTableForUpdate(GridRowModels<?> rows, EntityKind entityKind,
            String lineSeparator, ICommonServer commonServer, String sessionToken)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return getExperimentTableForUpdate(
                        (GridRowModels<TableModelRowWithObject<Experiment>>) rows, lineSeparator,
                        commonServer, sessionToken);
            case SAMPLE:
                return getSampleTableForUpdate(
                        (GridRowModels<TableModelRowWithObject<Sample>>) rows, lineSeparator,
                        commonServer, sessionToken);
            case DATA_SET:
                return getDataSetTableForUpdate(
                        (GridRowModels<TableModelRowWithObject<AbstractExternalData>>) rows, lineSeparator,
                        commonServer, sessionToken);
            case MATERIAL:
                return "Export of materials for update is currently not supported.";
        }
        throw new IllegalArgumentException("Unspecified entity kind.");
    }

    private static <T extends Serializable> List<PropertyType> getAllPropertyTypes(
            GridRowModels<TableModelRowWithObject<T>> rows, List<? extends EntityType> types)
    {

        Set<PropertyType> propertyTypes = new HashSet<PropertyType>();

        for (GridRowModel<TableModelRowWithObject<T>> row : rows)
        {
            T t = row.getOriginalObject().getObjectOrNull();
            if (t == null)
            {
                continue;
            }

            for (EntityType type : types)
            {
                if (type.getCode().equals(getType(t).getCode()))
                {
                    for (EntityTypePropertyType<?> et : type.getAssignedPropertyTypes())
                    {
                        if (et.isDynamic() == false && et.isManaged() == false)
                        {
                            propertyTypes.add(et.getPropertyType());
                        }
                    }
                }
            }
        }

        return new ArrayList<PropertyType>(propertyTypes);
    }

    private static <T> EntityType getType(T t)
    {
        if (t instanceof Experiment)
        {
            return ((Experiment) t).getExperimentType();
        } else if (t instanceof AbstractExternalData)
        {
            return ((AbstractExternalData) t).getDataSetType();
        } else if (t instanceof Sample)
        {
            return ((Sample) t).getSampleType();
        }

        throw new IllegalArgumentException("Unknown data type " + t);
    }

    public static String getExperimentTableForUpdate(
            GridRowModels<TableModelRowWithObject<Experiment>> rows, String lineSeparator,
            ICommonServer commonServer, String sessionToken)
    {
        TypedTableModelBuilder<Experiment> builder = new TypedTableModelBuilder<Experiment>();
        builder.addColumn(NewExperiment.IDENTIFIER_COLUMN);
        builder.addColumn("project");

        builder.columnGroup("").addColumnsForPropertyTypesForUpdate(
                getAllPropertyTypes(rows, commonServer.listExperimentTypes(sessionToken)));

        for (GridRowModel<TableModelRowWithObject<Experiment>> row : rows)
        {
            Experiment experiment = row.getOriginalObject().getObjectOrNull();
            builder.addRow(experiment);
            builder.column(NewExperiment.IDENTIFIER_COLUMN).addString(experiment.getIdentifier());
            addProperties(builder, experiment.getProperties());
        }
        return TSVRenderer.createTable(new Adapter<Experiment>(builder), lineSeparator);
    }

    public static String getSampleTableForUpdate(
            GridRowModels<TableModelRowWithObject<Sample>> rows, String lineSeparator,
            ICommonServer commonServer, String sessionToken)
    {
        Map<String, List<Sample>> samples = new TreeMap<String, List<Sample>>();
        Map<String, GridRowModels<TableModelRowWithObject<Sample>>> rowMap =
                new TreeMap<String, GridRowModels<TableModelRowWithObject<Sample>>>();
        for (GridRowModel<TableModelRowWithObject<Sample>> row : rows)
        {
            Sample sample = row.getOriginalObject().getObjectOrNull();
            String sampleType = sample.getSampleType().getCode();
            List<Sample> sampleList = samples.get(sampleType);
            GridRowModels<TableModelRowWithObject<Sample>> rowList = rowMap.get(sampleType);
            if (sampleList == null)
            {
                sampleList = new ArrayList<Sample>();
                samples.put(sampleType, sampleList);

                rowList =
                        rows.cloneWithData(new ArrayList<GridRowModel<TableModelRowWithObject<Sample>>>());
                rowMap.put(sampleType, rowList);
            }
            sampleList.add(sample);
            rowList.add(row);
        }

        StringBuilder builder = new StringBuilder();
        Set<Entry<String, List<Sample>>> entrySet = samples.entrySet();
        for (Entry<String, List<Sample>> entry : entrySet)
        {
            if (samples.size() > 1)
            {
                builder.append("[").append(entry.getKey()).append("]").append(lineSeparator);
            }
            builder.append(getTableForSamples(
                    entry,
                    lineSeparator,
                    getAllPropertyTypes(rowMap.get(entry.getKey()),
                            commonServer.listSampleTypes(sessionToken))));
        }
        return builder.toString();
    }

    private static String getTableForSamples(Map.Entry<String, List<Sample>> entry,
            String lineSeparator, List<PropertyType> allPropertyTypes)
    {
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(NewSample.IDENTIFIER_COLUMN);
        builder.addColumn(NewSample.CONTAINER);
        builder.addColumn(NewSample.PARENTS);
        builder.addColumn(NewSample.EXPERIMENT);

        builder.columnGroup("").addColumnsForPropertyTypesForUpdate(allPropertyTypes);

        List<Sample> samples = entry.getValue();
        Collections.sort(samples);
        for (Sample sample : samples)
        {
            builder.addRow(sample);
            builder.column(NewSample.IDENTIFIER_COLUMN).addString(sample.getIdentifier());
            Sample container = sample.getContainer();
            if (container != null)
            {
                builder.column(NewSample.CONTAINER).addString(container.getIdentifier());
            }
            Set<Sample> parents = sample.getParents();
            if (parents != null && parents.isEmpty() == false)
            {
                StringBuilder sb = new StringBuilder();
                for (Sample parent : parents)
                {
                    if (sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(parent.getIdentifier());
                }
                builder.column(NewSample.PARENTS).addString(sb.toString());
            }
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                builder.column(NewSample.EXPERIMENT).addString(experiment.getIdentifier());
            }
            addProperties(builder, sample.getProperties());
        }
        return TSVRenderer.createTable(new Adapter<Sample>(builder), lineSeparator);
    }

    public static String getDataSetTableForUpdate(
            GridRowModels<TableModelRowWithObject<AbstractExternalData>> rows, String lineSeparator,
            ICommonServer commonServer, String sessionToken)
    {
        TypedTableModelBuilder<AbstractExternalData> builder = new TypedTableModelBuilder<AbstractExternalData>();
        builder.addColumn(NewDataSet.CODE);
        builder.addColumn(NewDataSet.CONTAINER);
        builder.addColumn(NewDataSet.PARENTS);
        builder.addColumn(NewDataSet.EXPERIMENT);
        builder.addColumn(NewDataSet.SAMPLE);

        builder.columnGroup("").addColumnsForPropertyTypesForUpdate(
                getAllPropertyTypes(rows, commonServer.listDataSetTypes(sessionToken)));

        for (GridRowModel<TableModelRowWithObject<AbstractExternalData>> row : rows)
        {
            AbstractExternalData dataSet = row.getOriginalObject().getObjectOrNull();
            builder.addRow(dataSet);
            builder.column(NewDataSet.CODE).addString(dataSet.getCode());
            List<ContainerDataSet> containerDataSets = dataSet.getContainerDataSets();
            if (containerDataSets.isEmpty() == false)
            {
                CommaSeparatedListBuilder listBuilder = new CommaSeparatedListBuilder();
                for (ContainerDataSet containerDataSet : containerDataSets)
                {
                    listBuilder.append(containerDataSet.getCode());
                }
                builder.column(NewDataSet.CONTAINER).addString(listBuilder.toString());
            }
            Collection<AbstractExternalData> parents = dataSet.getParents();
            if (parents != null && parents.isEmpty() == false)
            {
                StringBuilder sb = new StringBuilder();
                for (AbstractExternalData parent : parents)
                {
                    if (sb.length() > 0)
                    {
                        sb.append(",");
                    }
                    sb.append(parent.getCode());
                }
                builder.column(NewDataSet.PARENTS).addString(sb.toString());
            }
            Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                builder.column(NewDataSet.EXPERIMENT).addString(experiment.getIdentifier());
            }
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                builder.column(NewDataSet.SAMPLE).addString(sample.getIdentifier());
            }
            addProperties(builder, dataSet.getProperties());
        }
        return TSVRenderer.createTable(new Adapter<AbstractExternalData>(builder), lineSeparator);
    }

    private static void addProperties(TypedTableModelBuilder<?> builder,
            List<IEntityProperty> properties)
    {
        builder.columnGroup("").addPropertiesForUpdate(filterUpdatableProperties(properties));
    }

    private static List<IEntityProperty> filterUpdatableProperties(List<IEntityProperty> properties)
    {
        List<IEntityProperty> filteredProperties = new ArrayList<IEntityProperty>();
        for (IEntityProperty property : properties)
        {
            if (property.isDynamic() == false && property.isManaged() == false)
            {
                filteredProperties.add(property);
            }
        }
        return filteredProperties;
    }

    private static final class Adapter<T extends Serializable> implements IBasicTableDataProvider
    {
        private TypedTableModel<T> model;

        Adapter(TypedTableModelBuilder<T> builder)
        {
            model = builder.getModel();
        }

        @Override
        public List<String> getAllColumnTitles()
        {
            List<TableModelColumnHeader> headers = model.getHeader();
            List<String> titles = new ArrayList<String>();
            for (TableModelColumnHeader header : headers)
            {
                titles.add(header.getId());
            }
            return titles;
        }

        @Override
        public List<List<? extends Comparable<?>>> getRows()
        {
            List<TableModelRowWithObject<T>> rows = model.getRows();
            List<List<? extends Comparable<?>>> result =
                    new ArrayList<List<? extends Comparable<?>>>();
            for (TableModelRowWithObject<T> row : rows)
            {
                result.add(row.getValues());
            }
            return result;
        }
    }

}
