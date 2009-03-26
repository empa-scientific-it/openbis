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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores the kind of database object that was modified and the kind of modification.
 * 
 * @author Tomasz Pylak
 */
public class DatabaseModificationKind implements IsSerializable
{
    public enum ObjectKind implements IsSerializable
    {
        SAMPLE, EXPERIMENT, MATERIAL, SAMPLE_TYPE, EXPERIMENT_TYPE, MATERIAL_TYPE, PROJECT, GROUP,
        DATA_SET, PROPERTY_TYPE, PROPERTY_TYPE_ASSIGNMENT, VOCABULARY, VOCABULARY_TERM, ROLE,
        PERSON
    }

    public enum OperationKind implements IsSerializable
    {
        CREATE_OR_DELETE, UPDATE
    }

    // ----------------

    private ObjectKind objectType;

    private OperationKind operationKind;

    // GWT only
    private DatabaseModificationKind()
    {
    }

    public static final DatabaseModificationKind createNew(ObjectKind objectType)
    {
        return new DatabaseModificationKind(objectType, OperationKind.CREATE_OR_DELETE);
    }

    public static final DatabaseModificationKind createUpdate(ObjectKind objectType)
    {
        return new DatabaseModificationKind(objectType, OperationKind.UPDATE);
    }

    private DatabaseModificationKind(ObjectKind objectType, OperationKind operationKind)
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
    public String toString()
    {
        return "modification(object type: " + objectType + ", kind: " + operationKind + ")";
    }
}
