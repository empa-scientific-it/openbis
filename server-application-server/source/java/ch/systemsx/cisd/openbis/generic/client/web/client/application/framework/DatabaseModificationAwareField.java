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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.widget.form.Field;

/**
 * A field which is aware of database modifications.
 * 
 * @author Tomasz Pylak
 */
public class DatabaseModificationAwareField<T> extends DatabaseModificationAwareObject<Field<T>>
{
    /**
     * Creates a mock with a dummy database modification observer. Use this method if your field does not need to be refreshed when the database
     * changes.
     */
    public static <T> DatabaseModificationAwareField<T> wrapUnaware(Field<T> field)
    {
        return new DatabaseModificationAwareField<T>(field, createDummyModificationObserver());
    }

    public DatabaseModificationAwareField(Field<T> holder,
            IDatabaseModificationObserver modificationObserver)
    {
        super(holder, modificationObserver);
    }

}
