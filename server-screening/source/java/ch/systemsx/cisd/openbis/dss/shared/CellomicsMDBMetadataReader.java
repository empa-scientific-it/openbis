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

package ch.systemsx.cisd.openbis.dss.shared;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

/**
 * @author Jakub Straszewski
 */
public class CellomicsMDBMetadataReader
{
    private final Database database;

    private HashMap<String, FeatureDefinition> featureTypesDescription;

    public CellomicsMDBMetadataReader(String mdbPath) throws IOException
    {
        database = Database.open(new File(mdbPath));
        initializeFeatureDescriptions();
    }

    public void writeAllMetadata(String dir) throws IOException
    {
        writeAllMetadata(new File(dir));
    }

    public void writeAllMetadata(File dir) throws IOException
    {
        writeEntities("asnPlate", new File(dir, "Plate.tsv"));
        writeFeatures("asnPlateFeature", "PlateID", new File(dir, "PlateFeature.tsv"));
        writeEntities("asnWell", new File(dir, "Well.tsv"));
        writeFeatures("asnWellFeature", "WellID", new File(dir, "WellFeature.tsv"));
        writeEntities("WField", new File(dir, "Field.tsv"));
        writeFeatures("WFieldFeature", "WFieldID", new File(dir, "FieldFeature.tsv"));
        writeEntities("Cell", new File(dir, "Cell.tsv"));
        writeFeatures("CellFeature", "CellID", new File(dir, "CellFeature.tsv"));
        writeFeatureDefinitions(new File(dir, "FeatureDefinitions.tsv"));
    }

    private class CellomicsWriter
    {
        int indexesCount = 0;

        final HashMap<String, Integer> indexes = new HashMap<String, Integer>();

        final CsvWriter csvWriter;

        final Writer outputStream;

        public CellomicsWriter(File f) throws IOException
        {
            this.outputStream = new FileWriter(f);
            csvWriter = new CsvWriter(outputStream, '\t');
        }

        void addIndexedField(String fieldName)
        {
            if (!indexes.containsKey(fieldName))
            {
                indexes.put(fieldName, indexesCount++);
            }
        }

        void writeHeaders() throws IOException
        {
            int n = indexes.size();
            String[] headers = new String[n];
            for (Map.Entry<String, Integer> entry : indexes.entrySet())
            {
                headers[entry.getValue()] = entry.getKey();
            }
            csvWriter.writeRecord(headers);
        }

        void writeEntity(Entity entity) throws IOException
        {
            int n = indexes.size();
            String[] row = new String[n];
            for (Map.Entry<String, String> entry : entity.fields)
            {
                int index = indexes.get(entry.getKey());
                row[index] = entry.getValue();
            }
            csvWriter.writeRecord(row);
        }

        void writeFeature(Feature feature) throws IOException
        {
            String[] row = new String[]
            { feature.entityId, feature.getFeatureDescription(), feature.value.toString() };
            csvWriter.writeRecord(row);
        }

        public void writeFeatureDefinition(FeatureDefinition fd) throws IOException
        {
            String[] row = new String[]
            { fd.description, fd.notes };
            csvWriter.writeRecord(row);
        }

        void close() throws IOException
        {
            outputStream.close();
        }

    }

    private void initializeFeatureDescriptions() throws IOException
    {
        featureTypesDescription = new HashMap<String, FeatureDefinition>();

        Table featureTypes = database.getTable("asnFeatureType");
        for (Map<String, Object> row : featureTypes)
        {
            String id = getIDValue(row, "ID");
            featureTypesDescription.put(id, new FeatureDefinition(row));
        }
    }

    void writeEntities(String entityType, File f) throws IOException
    {
        CellomicsWriter cw = null;

        try
        {
            cw = new CellomicsWriter(f);

            Table entityTable = database.getTable(entityType);

            boolean firstRow = true;

            for (Map<String, Object> row : entityTable)
            {
                if (firstRow)
                {
                    for (String key : row.keySet())
                    {
                        cw.addIndexedField(key);
                    }
                    cw.writeHeaders();
                    firstRow = false;
                }
                Entity entity = new Entity(row);
                cw.writeEntity(entity);
            }
        } finally
        {
            try
            {
                if (cw != null)
                {
                    cw.close();
                }
            } catch (IOException ioe)
            {
                // ignore
            }
        }
    }

    void writeFeatures(String featureType, String entityForeignKey, File file) throws IOException
    {
        CellomicsWriter cw = null;
        try
        {
            cw = new CellomicsWriter(file);

            Table features = database.getTable(featureType);

            cw.addIndexedField(entityForeignKey);
            cw.addIndexedField("Feature");
            cw.addIndexedField("Value");

            cw.writeHeaders();

            for (Map<String, Object> row : features)
            {
                Feature f = new Feature(row, entityForeignKey);
                cw.writeFeature(f);
            }
        } finally
        {
            try
            {
                if (cw != null)
                {
                    cw.close();
                }
            } catch (IOException ioe)
            {
                // ignore
            }
        }
    }

    void writeFeatureDefinitions(File file) throws IOException
    {
        CellomicsWriter cw = null;
        try
        {
            cw = new CellomicsWriter(file);

            cw.addIndexedField("Definition");
            cw.addIndexedField("Note");

            cw.writeHeaders();

            for (FeatureDefinition fd : featureTypesDescription.values())
            {
                cw.writeFeatureDefinition(fd);
            }
        } finally
        {
            try
            {
                if (cw != null)
                {
                    cw.close();
                }
            } catch (IOException ioe)
            {
                // ignore
            }
        }
    }

    private class FeatureDefinition
    {
        private String description;

        private String notes;

        public FeatureDefinition(Map<String, Object> row)
        {
            this.description = row.get("Description").toString();
            this.notes = row.get("Notes").toString();
        }

    }

    private class Feature
    {
        private final Double value;

        private final String entityId;

        private final String featureTypeId;

        public Feature(Map<String, Object> row, String entityForeignKey)
        {
            featureTypeId = getIDValue(row, "TypeID");
            entityId = getIDValue(row, entityForeignKey);
            value = getFeatureValue(row);
        }

        String getFeatureDescription()
        {
            return featureTypesDescription.get(featureTypeId).description;
        }
    }

    private static class Entity
    {
        private final String id;

        private final List<Map.Entry<String, String>> fields =
                new LinkedList<Map.Entry<String, String>>();

        Entity(Map<String, Object> row)
        {
            for (Map.Entry<String, Object> o : row.entrySet())
            {
                put(o.getKey(), o.getValue() == null ? "" : o.getValue().toString());
            }
            id = getIDValue(row, "ID");
        }

        /**
         * Add feature with the given key and value
         */
        void put(String key, String value)
        {
            fields.add(new AbstractMap.SimpleEntry<String, String>(key, value));
        }

    }

    private static String getIDValue(Map<String, Object> row, String idField)
    {
        return row.get(idField).toString();
    }

    private static Double getFeatureValue(Map<String, Object> feature)
    {
        return (Double) feature.get("valdbl");
    }
}
