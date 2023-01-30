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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Helper class for handling {@link IModification}s.
 * 
 * @author Franz-Josef Elmer
 */
public class ModificationsData<T extends Serializable>
{
    /**
     * Call back interface for applying modifications.
     * 
     * @author Franz-Josef Elmer
     */
    public static interface IModificationsHandler<T extends Serializable>
    {
        public void applyModifications(Map<BaseEntityModel<TableModelRowWithObject<T>>, List<IModification>> modificationsByModel);
    }

    private final Map<BaseEntityModel<TableModelRowWithObject<T>>, List<IModification>> modificationsByModel =
            new LinkedHashMap<BaseEntityModel<TableModelRowWithObject<T>>, List<IModification>>();

    private final Map<BaseEntityModel<TableModelRowWithObject<T>>, String> failedModifications =
            new HashMap<BaseEntityModel<TableModelRowWithObject<T>>, String>();

    private int finishedModifications;

    private boolean saving;

    public void clearData()
    {
        saving = false;
        finishedModifications = 0;
        failedModifications.clear();
        modificationsByModel.clear();
    }

    public boolean isSaving()
    {
        return saving;
    }

    public void handleModifications(IModificationsHandler<T> handler)
    {
        saving = true;
        finishedModifications = 0;
        handler.applyModifications(modificationsByModel);
    }

    public void addModification(BaseEntityModel<TableModelRowWithObject<T>> model, String columnID,
            String newValueOrNull)
    {
        List<IModification> modificationsForModel = modificationsByModel.get(model);
        if (modificationsForModel == null)
        {
            modificationsForModel = new ArrayList<IModification>();
            modificationsByModel.put(model, modificationsForModel);
        }
        modificationsForModel.add(new Modification(columnID, newValueOrNull));

    }

    public void handleResponseAfterModificationHasBeenApplied(
            BaseEntityModel<TableModelRowWithObject<T>> model, String errorMessageOrNull)
    {
        finishedModifications++;
        if (errorMessageOrNull != null)
        {
            failedModifications.put(model, errorMessageOrNull);
        }
    }

    public boolean isApplyModificationsComplete()
    {
        return finishedModifications == modificationsByModel.size();
    }

    public boolean hasFailedModifications()
    {
        return failedModifications.isEmpty() == false;
    }

    public String createFailureTitle()
    {
        return (failedModifications.size() == modificationsByModel.size()) ? "Operation failed"
                : "Operation partly failed";
    }

    public String createFailedModificationsReport()
    {
        assert failedModifications.size() > 0;
        StringBuilder result = new StringBuilder();
        result.append("Modifications of " + failedModifications.size() + " entities failed:");
        for (String error : failedModifications.values())
        {
            result.append("<br/>- " + error);
        }
        return result.toString();
    }

}
