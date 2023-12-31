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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * A {@link ModelData} implementation for {@link SearchableEntity}.
 * 
 * @author Christian Ribeaud
 */
public final class SearchableEntityModel extends SimplifiedBaseModelData
{
    public final static SearchableEntityModel NULL_SEARCHABLE_ENTITY_MODEL =
            createNullSearchableEntityModel();

    private static final long serialVersionUID = 1L;

    private SearchableEntityModel()
    {
    }

    private final static SearchableEntityModel createNullSearchableEntityModel()
    {
        final SearchableEntityModel model = new SearchableEntityModel();
        model.set(ModelDataPropertyNames.DESCRIPTION, "All");
        model.set(ModelDataPropertyNames.OBJECT, null);
        return model;
    }

    public SearchableEntityModel(final SearchableEntity searchableEntity)
    {
        assert searchableEntity != null : "Unspecified searchable entity.";
        set(ModelDataPropertyNames.DESCRIPTION, searchableEntity.getDescription());
        set(ModelDataPropertyNames.OBJECT, searchableEntity);
    }
    
    public SearchableEntity getSearchableEntity()
    {
        return (SearchableEntity) get(ModelDataPropertyNames.OBJECT);
    }

    public final static List<SearchableEntityModel> convert(
            final List<SearchableEntity> searchableEntities)
    {
        final List<SearchableEntityModel> result = new ArrayList<SearchableEntityModel>();
        for (final SearchableEntity searchableEntity : searchableEntities)
        {
            result.add(new SearchableEntityModel(searchableEntity));
        }
        return result;
    }

}
