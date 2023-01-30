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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * @author Pawel Glyzewski
 */
public enum RelationType
{
    OWNER("Owner", "of"), OWNED("Owned", "by"), CHILD("Child", "of"), PARENT("Parent", "of"),
    CONTAINER("Container", "of"), COMPONENT("Component", "of"), CONTAINED("Component", "of");

    private String description;

    private String connectingWord;

    private RelationType(String description, String word)
    {
        this.description = description;
        this.connectingWord = word;
    }

    public String getDescription(String entityKind)
    {
        return StringUtils.isBlank(entityKind) ? description : description + " " + connectingWord + " "
                + entityKind;
    }
}
