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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores the kind of database object that was modified and the kind of modification.
 * 
 * @author Tomasz Pylak
 */
public class DatabaseModificationKind implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final DatabaseModificationKind[] EMPTY_ARRAY = new DatabaseModificationKind[0];

    public enum ObjectKind implements IsSerializable
    {
        AUTHORIZATION_GROUP, SAMPLE, EXPERIMENT, MATERIAL, DATA_SET, SAMPLE_TYPE, EXPERIMENT_TYPE,
        MATERIAL_TYPE, DATASET_TYPE, FILE_FORMAT_TYPE, PROJECT, SPACE, PROPERTY_TYPE,
        PROPERTY_TYPE_ASSIGNMENT, VOCABULARY, VOCABULARY_TERM, ROLE_ASSIGNMENT, PERSON,
        GRID_CUSTOM_FILTER, GRID_CUSTOM_COLUMN, SCRIPT, DELETION, POSTREGISTRATION_QUEUE,
        // TODO 2010-02-19, PTR: no easy way to extend current modification notification solution in
        // modules
        QUERY, METAPROJECT, OPERATION_EXECUTION, SEMANTIC_ANNOTATION, PERSONAL_ACCESS_TOKEN
    }

    public enum OperationKind implements IsSerializable
    {
        CREATE_OR_DELETE, UPDATE
    }

    // ----------------

    private ObjectKind objectType;

    private OperationKind operationKind;

    // GWT only
    @SuppressWarnings("unused")
    private DatabaseModificationKind()
    {
    }

    public static final void addAny(Collection<DatabaseModificationKind> result,
            ObjectKind objectType)
    {
        result.add(createOrDelete(objectType));
        result.add(edit(objectType));
    }

    public static final DatabaseModificationKind[] any(ObjectKind objectType)
    {
        List<DatabaseModificationKind> result = new ArrayList<DatabaseModificationKind>();
        result.add(createOrDelete(objectType));
        result.add(edit(objectType));
        return result.toArray(EMPTY_ARRAY);
    }

    public static final DatabaseModificationKind createOrDelete(ObjectKind objectType)
    {
        return new DatabaseModificationKind(objectType, OperationKind.CREATE_OR_DELETE);
    }

    public static final DatabaseModificationKind edit(ObjectKind objectType)
    {
        return new DatabaseModificationKind(objectType, OperationKind.UPDATE);
    }

    public DatabaseModificationKind(ObjectKind objectType, OperationKind operationKind)
    {
        this.objectType = objectType;
        this.operationKind = operationKind;
    }

    public ObjectKind getObjectType()
    {
        return objectType;
    }

    public OperationKind getOperationKind()
    {
        return operationKind;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof DatabaseModificationKind == false)
        {
            return false;
        }
        DatabaseModificationKind that = (DatabaseModificationKind) obj;
        return objectType == that.objectType && operationKind == that.operationKind;
    }

    @Override
    public int hashCode()
    {
        return 17 * objectType.hashCode() + operationKind.hashCode();
    }

    @Override
    public String toString()
    {
        return "modification(object type: " + objectType + ", kind: " + operationKind + ")";
    }
}
