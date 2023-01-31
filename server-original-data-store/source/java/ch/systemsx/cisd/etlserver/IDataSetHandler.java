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

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Interface implemented by classes which can handle an incoming data set file or directory.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSetHandler
{
    /**
     * Properties key prefix to find the {@link IDataSetHandler} implementation which takes the properties and the primary (original) IDataSetHandler
     * implementation as a parameter.
     */
    public static final String DATASET_HANDLER_KEY = "dataset-handler";

    /**
     * Handles specified data set and returns informations (like data set code) of all actually registered data sets.
     */
    public List<DataSetInformation> handleDataSet(final File dataSet);

}