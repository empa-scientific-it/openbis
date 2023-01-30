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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateDataSetTypeRmi;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class DataSetTypeBuilder implements Builder<DataSetType>
{

    private String code;

    private String description;

    private Collection<PropertyTypeAssignment> propertyTypeAssignments;

    public DataSetTypeBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
        this.description = "";
        this.propertyTypeAssignments = new HashSet<PropertyTypeAssignment>();
    }

    public DataSetTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public DataSetType build(Application openbis, Ui ui)
    {
        return openbis.execute(new CreateDataSetTypeRmi(new DataSetTypeDsl(code, description,
                propertyTypeAssignments)));
    }
}
