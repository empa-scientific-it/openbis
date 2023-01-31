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

package ch.systemsx.cisd.etlserver;

import java.util.List;

import ch.systemsx.cisd.etlserver.plugins.AutoArchiverTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * The interface that should be implemented by all {@link AutoArchiverTask} policies.
 * 
 * @author Piotr Buczek
 */
public interface IAutoArchiverPolicy
{

    /**
     * Returns given list of data sets filtered in a specific way.
     */
    public List<AbstractExternalData> filter(List<AbstractExternalData> dataSets);

}
