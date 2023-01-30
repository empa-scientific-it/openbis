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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ILinkGenerator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableGridColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * @author Franz-Josef Elmer
 */
public class TypedTableGridColumnDefinitionUI<T extends Serializable> extends
        TypedTableGridColumnDefinition<T> implements
        IColumnDefinitionUI<TableModelRowWithObject<T>>
{
    private transient final ILinkGenerator<T> linkGeneratorOrNull;

    private boolean hidden;

    public TypedTableGridColumnDefinitionUI(TableModelColumnHeader header, String title,
            String downloadURL, String sessionID, ILinkGenerator<T> linkGeneratorOrNull)
    {
        super(header, title, downloadURL, sessionID);
        this.linkGeneratorOrNull = linkGeneratorOrNull;
        if (header != null)
        {
            hidden = header.isHidden();
        }
    }

    // GWT only
    @SuppressWarnings("unused")
    private TypedTableGridColumnDefinitionUI()
    {
        this(null, null, null, null, null);
    }

    @Override
    public int getWidth()
    {
        return header.getDefaultColumnWidth();
    }

    public final void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    @Override
    public boolean isEditable()
    {
        return header.isEditable();
    }

    @Override
    public boolean isDynamicProperty()
    {
        return header.isDynamicProperty();
    }

    public boolean isLink()
    {
        return false;
    }

    @Override
    public String tryGetLink(TableModelRowWithObject<T> entity)
    {
        if (linkGeneratorOrNull == null)
        {
            return null;
        }
        final ISerializableComparable value = entity.getValues().get(header.getIndex());
        final T objectOrNull = entity.getObjectOrNull();
        return objectOrNull == null ? null : linkGeneratorOrNull.tryGetLink(objectOrNull, value);
    }

    @Override
    public boolean isNumeric()
    {
        DataTypeCode type = header.getDataType();
        return type == DataTypeCode.INTEGER || type == DataTypeCode.REAL;
    }

    @Override
    public boolean isVocabulary()
    {
        DataTypeCode type = header.getDataType();
        return type == DataTypeCode.CONTROLLEDVOCABULARY;
    }

    @Override
    public Vocabulary tryGetVocabulary()
    {
        return header.tryGetVocabulary();
    }

    @Override
    public boolean isCustom()
    {
        return false;
    }
}
