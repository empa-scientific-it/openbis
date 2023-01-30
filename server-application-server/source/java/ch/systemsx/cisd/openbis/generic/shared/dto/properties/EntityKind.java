/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.properties;

import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedSamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Enumeration of entity kinds.
 * 
 * @author Franz-Josef Elmer
 */
public enum EntityKind
{
    MATERIAL(TableNames.MATERIALS_TABLE, null, "material", MaterialPE.class, null,
            MaterialTypePE.class, MaterialTypePropertyTypePE.class,
            MaterialPropertyHistoryPE.class, MaterialPropertyPE.class),

    EXPERIMENT(TableNames.EXPERIMENTS_VIEW, TableNames.EXPERIMENTS_ALL_TABLE, "experiment",
            ExperimentPE.class, DeletedExperimentPE.class, ExperimentTypePE.class,
            ExperimentTypePropertyTypePE.class, ExperimentHistoryPE.class,
            ExperimentPropertyPE.class),

    SAMPLE(TableNames.SAMPLES_VIEW, TableNames.SAMPLES_ALL_TABLE, "sample", SamplePE.class,
            DeletedSamplePE.class, SampleTypePE.class, SampleTypePropertyTypePE.class,
            SampleHistoryPE.class, SamplePropertyPE.class),

    DATA_SET(TableNames.DATA_VIEW, TableNames.DATA_ALL_TABLE, "dataSet", DataPE.class,
            DeletedDataPE.class, DataSetTypePE.class, DataSetTypePropertyTypePE.class,
            DataSetHistoryPE.class, DataSetPropertyPE.class);

    private final String entityTableName;

    private final String allEntitiesTableName;

    private final String entityLabel;

    private transient final Class<? extends IEntityInformationWithPropertiesHolder> entityClass;

    private transient final Class<? extends IDeletablePE> deletedEntityClass;

    private transient final Class<?> typeClass;

    private transient final Class<?> assignmentClass;

    private transient final Class<?> propertyClass;

    private transient final Class<? extends AbstractEntityPropertyHistoryPE> entityPropertyHistoryClass;

    private EntityKind(final String entityTableName, final String allEntitiesTableName,
            final String entityLabel,
            final Class<? extends IEntityInformationWithPropertiesHolder> entityClass,
            final Class<? extends IDeletablePE> deletedEntityClass, final Class<?> typeClass,
            final Class<?> assignmentClass,
            final Class<? extends AbstractEntityPropertyHistoryPE> entityPropertyHistoryClass,
            Class<?> propertyClass)
    {
        this.entityTableName = entityTableName;
        this.allEntitiesTableName = allEntitiesTableName;
        this.entityLabel = entityLabel;
        this.entityClass = entityClass;
        this.deletedEntityClass = deletedEntityClass;
        this.typeClass = typeClass;
        this.assignmentClass = assignmentClass;
        this.entityPropertyHistoryClass = entityPropertyHistoryClass;
        this.propertyClass = propertyClass;
    }

    @SuppressWarnings("unchecked")
    private final static <T> Class<T> cast(final Class<?> clazz)
    {
        return (Class<T>) clazz;
    }

    public final String getLabel()
    {
        return entityLabel;
    }

    public final String getTableName()
    {
        return entityTableName;
    }

    public final String getAllTableName()
    {
        return allEntitiesTableName;
    }

    public final <T extends EntityTypePE> Class<T> getTypeClass()
    {
        return cast(typeClass);
    }

    public Class<? extends AbstractEntityPropertyHistoryPE> getEntityPropertyHistoryClass()
    {
        return entityPropertyHistoryClass;
    }

    public final <T extends EntityTypePropertyTypePE> Class<T> getEntityTypePropertyTypeAssignmentClass()
    {
        return cast(assignmentClass);
    }

    public final <T extends EntityPropertyPE> Class<T> getEntityPropertyClass()
    {
        return cast(propertyClass);
    }

    public final <T extends IEntityInformationWithPropertiesHolder> Class<T> getEntityClass()
    {
        return cast(entityClass);
    }

    public final <T extends IDeletablePE> Class<T> getDeletedEntityClass()
    {
        if (deletedEntityClass == null)
        {
            throw new UnsupportedOperationException("No deleted entity class is specified for "
                    + name());
        }
        return cast(deletedEntityClass);
    }

    public final String getEntityTypeFieldName()
    {
        return entityLabel + "Type";
    }
}
