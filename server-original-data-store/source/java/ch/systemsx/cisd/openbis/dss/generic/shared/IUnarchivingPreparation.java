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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Interface of classes which prepare a data set before it will be unarchived.
 * 
 * @author Franz-Josef Elmer
 */
public interface IUnarchivingPreparation
{
    /**
     * Prepares unarchiving of specified datasets. This method will be called for with all data sets in a batch as an argument before the unarchiving.
     * It can be used for operations like finding the right share according to the strategy which depends on all data sets or cleaning required amount
     * of space on target share. For regular archivers it will be called with a singleton list before each data set is unarchived.
     */
    public void prepareForUnarchiving(List<DatasetDescription> dataSets);

}
