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

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreatePropertyTypeGui;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class PropertyTypeBuilder implements Builder<PropertyType>
{

    private String code;

    private String label;

    private String description;

    private PropertyTypeDataType dataType;

    private Vocabulary vocabulary;

    public PropertyTypeBuilder(UidGenerator uid, PropertyTypeDataType type)
    {
        this.code = uid.uid();
        this.label = "label of " + code;
        this.description = "description of " + code;
        this.dataType = type;
        this.vocabulary = null;
    }

    public PropertyTypeBuilder(UidGenerator uid, Vocabulary vocabulary)
    {
        this(uid, PropertyTypeDataType.CONTROLLED_VOCABULARY);
        this.vocabulary = vocabulary;
    }

    public PropertyTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public PropertyTypeBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public PropertyTypeBuilder withLabel(String label)
    {
        this.label = label;
        return this;
    }

    @Override
    public PropertyType build(Application openbis, Ui ui)
    {
        PropertyType type = new PropertyTypeDsl(code, label, description, dataType, vocabulary);

        if (Ui.WEB.equals(ui))
        {
            return openbis.execute(new CreatePropertyTypeGui(type));
        } else if (Ui.DUMMY.equals(ui))
        {
            return type;
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            throw new UnsupportedOperationException();
        } else
        {
            return type;
        }
    }

}
