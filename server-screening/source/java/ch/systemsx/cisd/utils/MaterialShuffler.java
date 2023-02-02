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

package ch.systemsx.cisd.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseEngine;

/**
 * Exchanges the ids of entity (material,sample,experiment,material) properties of type material with other material ids. The material type of old and
 * new property remains the same.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialShuffler extends JdbcDaoSupport
{

    private final static RowMapper<Integer> ID_MAPPER =
            new RowMapper<Integer>()
                {
                    @Override
                    public final Integer mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        return rs.getInt("id");
                    }
                };

    static private class EntityIdMaterialId
    {
        final private Integer entity;

        final private Integer material;

        private Integer newMaterial;

        public EntityIdMaterialId(Integer entity, Integer material)
        {
            this.entity = entity;
            this.material = material;
        }

        public Integer getEntity()
        {
            return entity;
        }

        public Integer getMaterial()
        {
            return material;
        }

        public void setNewMaterial(Integer newMaterial)
        {
            this.newMaterial = newMaterial;
        }

        public Integer getNewMaterial()
        {
            return newMaterial;
        }

    }

    private final static RowMapper<EntityIdMaterialId> ENTITY_ID_MATERIAL_ID_MAPPER =
            new RowMapper<EntityIdMaterialId>()
                {
                    @Override
                    public final EntityIdMaterialId mapRow(final ResultSet rs, final int rowNum)
                            throws SQLException
                    {
                        Integer entity = rs.getInt("entity_id");
                        Integer material = rs.getInt("material_id");
                        return new EntityIdMaterialId(entity, material);
                    }
                };

    private static final Random GENERATOR = new Random();

    private static final RowMapper<String> CODE_MAPPER =
            new RowMapper<String>()
                {

                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException
                    {
                        return rs.getString("code");
                    }
                };

    public MaterialShuffler(DatabaseConfigurationContext dbConfigurationContext)
    {
        setDataSource(dbConfigurationContext.getDataSource());
    }

    /**
     * Expected parameter: database kind.
     */
    public static void main(String[] args) throws SQLException
    {
        if (args.length != 1)
        {
            System.err.println("Specify database kind (eg. productive or demo)");
            System.exit(1);
        }
        final DatabaseConfigurationContext dbContext = new DatabaseConfigurationContext();
        dbContext.setDatabaseEngineCode(DatabaseEngine.POSTGRESQL.getCode());
        dbContext.setBasicDatabaseName("openbis");
        dbContext.setDatabaseKind(args[0]);
        new MaterialShuffler(dbContext).shuffle();
    }

    /**
     * Performs the shuffling.
     */
    @Transactional
    public void shuffle()
    {
        try
        {
            System.out.println("SHUFFLE MATERIALS");
            List<String> entities = Arrays.asList("sample", "experiment", "data_set", "material");
            List<String> material_types = listMaterialTypes();
            for (String material_type : material_types)
            {
                List<Integer> materials = listMaterials(material_type);
                for (String entity : entities)
                {
                    List<EntityIdMaterialId> properties = listProperties(material_type, entity);
                    for (EntityIdMaterialId pair : properties)
                    {
                        selectNewMaterial(pair, materials);
                    }
                    System.out.print(String.format("Shuffling %s - %s (%s) ...",
                            material_type, entity, properties.size()));
                    updateMaterials(entity, properties);
                    System.out.println("DONE.");
                }
            }
            System.out.println("DONE.");
        } catch (Exception ex)
        {
            System.err.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    /**
     * Performs the db update of entity properties.
     */
    private void updateMaterials(String entity, final List<EntityIdMaterialId> properties)
    {
        if (properties.size() == 0)
        {
            return;
        }
        String sql =
                String.format("update %s_properties set mate_prop_id = ? where id = ? ", entity);
        getJdbcTemplate().batchUpdate(sql,
                new BatchPreparedStatementSetter()
                    {

                        @Override
                        public int getBatchSize()
                        {
                            return 1000;
                        }

                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException
                        {
                            EntityIdMaterialId entityIdMaterialId = properties.get(i);
                            ps.setInt(1, entityIdMaterialId.getNewMaterial());
                            ps.setInt(2, entityIdMaterialId.getEntity());
                        }
                    });
    }

    /**
     * Selects new material property for given entity.
     */
    private void selectNewMaterial(EntityIdMaterialId pair, List<Integer> materials)
    {
        Integer material = pair.getMaterial();
        if (material == null)
        {
            return;
        }
        Integer newMaterial;
        do
        {
            newMaterial = materials.get(GENERATOR.nextInt(materials.size()));
        } while (newMaterial == material);
        pair.setNewMaterial(newMaterial);
    }

    /**
     * Creates a list of pairs (entity property id - material id) for given entity.
     */
    private List<EntityIdMaterialId> listProperties(String material_type, String entity)
    {
        String propertySql =
                String
                        .format(
                                "select p.id as entity_id, p.mate_prop_id as material_id from %s_properties as p "
                                        + " where p.mate_prop_id is not null and p.mate_prop_id in "
                                        + " (select distinct m.id from materials m, material_types t where m.maty_id=t.id and t.code = ?);",
                                entity);
        return getJdbcTemplate().query(propertySql, ENTITY_ID_MATERIAL_ID_MAPPER,
                material_type);
    }

    /**
     * Creates a list of available materials of given type.
     */
    private List<Integer> listMaterials(String material_type)
    {
        String sql =
                "select distinct m.id from materials m, material_types t where m.maty_id=t.id and t.code = ?;";
        return getJdbcTemplate().query(sql, ID_MAPPER, material_type);
    }

    /**
     * Creates a list of codes of available material types.
     */
    private List<String> listMaterialTypes()
    {
        String sql = "select distinct code from material_types;";
        return getJdbcTemplate().query(sql, CODE_MAPPER);
    }
}
