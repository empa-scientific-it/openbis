/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.MatchingEntitiesPanel;

/**
 * IDs of static columns of {@link MatchingEntitiesPanel}.
 *
 * @author Franz-Josef Elmer
 */
public class MatchingEntitiesPanelColumnIDs
{
    public static final String ENTITY_KIND = "ENTITY_KIND";

    public static final String ENTITY_TYPE = "ENTITY_TYPE";

    public static final String SEARCH_DOMAIN_TYPE = "SEARCH_DOMAIN_TYPE";
    
    public static final String IDENTIFIER = "IDENTIFIER";

    public static final String REGISTRATOR = "REGISTRATOR";

    public static final String MATCH = "MATCH";

    public static final String RANK = "RANK";

    public static final String START_HIGHLIGHT = "|||startHighlight|||";

    public static final String END_HIGHLIGHT = "|||endHighlight|||";

}
