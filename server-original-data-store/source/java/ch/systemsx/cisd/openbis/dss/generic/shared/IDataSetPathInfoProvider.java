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
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * Provides informations about paths in data sets.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSetPathInfoProvider
{

    public List<DataSetPathInfo> listPathInfosByRegularExpression(String dataSetCode, String regularExpression);

    public Map<String, List<DataSetPathInfo>> listPathInfosBySearchString(String searchString);

    public DataSetPathInfo tryGetFullDataSetRootPathInfo(String dataSetCode);

    public ISingleDataSetPathInfoProvider tryGetSingleDataSetPathInfoProvider(String dataSetCode);

    public Map<String, Integer> getDataSetChecksums(String dataSetCode);
}
