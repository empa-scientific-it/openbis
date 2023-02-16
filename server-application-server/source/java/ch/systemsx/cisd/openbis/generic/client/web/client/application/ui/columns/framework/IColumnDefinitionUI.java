/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * Describes column's metadata and UI.
 * 
 * @author Tomasz Pylak
 */
public interface IColumnDefinitionUI<T> extends IColumnDefinition<T>
{
    /** width of the column */
    int getWidth();

    /** Returns <code>true</code> if this column should initially be hidden. */
    boolean isHidden();

    /**
     * Returns <code>true</code> if the values of the column are numerically.
     */
    boolean isNumeric();

    /**
     * Returns <code>true</code> if the column cell is editable.
     */
    boolean isEditable();

    /**
     * Returns <code>true</code> if the column cell is the controlled vocabulary
     */
    boolean isVocabulary();

    /**
     * Returns the vocabulary if the column cell is a controlled vocabulary
     */
    Vocabulary tryGetVocabulary();

    String tryGetLink(T entity);

    /**
     * Returns <code>true</code> if the column cell is a dynamic property
     */
    boolean isDynamicProperty();
}
