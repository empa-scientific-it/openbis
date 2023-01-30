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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Criteria for listing <i>data sets</i> related to {@link MatchingEntity}s like samples and experiments.
 * 
 * @author Piotr Buczek
 */
public class RelatedDataSetCriteria<E extends IEntityInformationHolder> implements IsSerializable
{

    private TableExportCriteria<TableModelRowWithObject<E>> displayedEntitiesOrNull;

    private List<TableModelRowWithObject<E>> selectedEntitiesOrNull;

    public static <E extends IEntityInformationHolder> RelatedDataSetCriteria<E> createDisplayedEntities(
            TableExportCriteria<TableModelRowWithObject<E>> displayedEntities)
    {
        return new RelatedDataSetCriteria<E>(displayedEntities, null);
    }

    public static <E extends IEntityInformationHolder> RelatedDataSetCriteria<E> createSelectedEntities(
            List<TableModelRowWithObject<E>> selectedEntities)
    {
        return new RelatedDataSetCriteria<E>(null, selectedEntities);
    }

    private RelatedDataSetCriteria(
            TableExportCriteria<TableModelRowWithObject<E>> displayedEntitiesOrNull,
            List<TableModelRowWithObject<E>> selectedEntitiesOrNull)
    {
        assert (displayedEntitiesOrNull == null) != (selectedEntitiesOrNull == null) : "Exactly one arg must be null and one non-null";
        this.displayedEntitiesOrNull = displayedEntitiesOrNull;
        this.selectedEntitiesOrNull = selectedEntitiesOrNull;
    }

    public TableExportCriteria<TableModelRowWithObject<E>> tryGetDisplayedEntities()
    {
        return displayedEntitiesOrNull;
    }

    public List<TableModelRowWithObject<E>> tryGetSelectedEntities()
    {
        return selectedEntitiesOrNull;
    }

    // GWT only
    private RelatedDataSetCriteria()
    {
    }

    @SuppressWarnings("unused")
    private TableExportCriteria<TableModelRowWithObject<E>> getDisplayedEntitiesOrNull()
    {
        return displayedEntitiesOrNull;
    }

    @SuppressWarnings("unused")
    private void setDisplayedEntitiesOrNull(
            TableExportCriteria<TableModelRowWithObject<E>> displayedEntitiesOrNull)
    {
        this.displayedEntitiesOrNull = displayedEntitiesOrNull;
    }

    @SuppressWarnings("unused")
    private List<TableModelRowWithObject<E>> getSelectedEntitiesOrNull()
    {
        return selectedEntitiesOrNull;
    }

    @SuppressWarnings("unused")
    private void setSelectedEntitiesOrNull(
            List<TableModelRowWithObject<E>> selectedEntitiesOrNull)
    {
        this.selectedEntitiesOrNull = selectedEntitiesOrNull;
    }

}
