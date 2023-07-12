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
package ch.systemsx.cisd.openbis.generic.server.business.search.sort;

import java.io.Serializable;
import java.util.Map;

/**
 * This interface is used by SearchResultSorter as a layer of abstraction to avoid to have several implementations of the same algorithm for different
 * entities.
 * 
 * @author pkupczyk
 * @author juanf
 */
public interface IEntitySearchResult
{
    String getCode();

    String getTypeCode();

    Map<String, String> getProperties();
}
