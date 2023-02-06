/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IContentCopyId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.update.ContentCopyListUpdateValue")
public class ContentCopyListUpdateValue extends ListUpdateValue<ContentCopyCreation, IContentCopyId, ContentCopyCreation, Object>
{

    private static final long serialVersionUID = 1L;

}
