/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * Handles the trash of entities.
 * 
 * @author Piotr Buczek
 */
public interface ITrashBO
{
    /** Puts back all objects moved to trash in deletion with specified id. */
    public void revertDeletion(TechId deletionId);

    public void createDeletion(String reason);

    public void trashSamples(List<TechId> sampleIds);

    public void trashExperiments(List<TechId> experimentIds);

    public void trashDataSets(List<DataPE> dataSets);

}