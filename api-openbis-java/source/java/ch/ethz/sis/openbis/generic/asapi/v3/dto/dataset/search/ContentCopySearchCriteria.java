/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author anttil
 */
@JsonObject("as.dto.dataset.search.ContentCopySearchCriteria")
public class ContentCopySearchCriteria extends AbstractObjectSearchCriteria<IExternalDmsId>
{

    private static final long serialVersionUID = 1L;

    public ExternalDmsSearchCriteria withExternalDms()
    {
        return with(new ExternalDmsSearchCriteria());
    }

    public ExternalCodeSearchCriteria withExternalCode()
    {
        return with(new ExternalCodeSearchCriteria());
    }

    public PathSearchCriteria withPath()
    {
        return with(new PathSearchCriteria());
    }

    public GitCommitHashSearchCriteria withGitCommitHash()
    {
        return with(new GitCommitHashSearchCriteria());
    }

    public GitRepositoryIdSearchCriteria withGitRepositoryId()
    {
        return with(new GitRepositoryIdSearchCriteria());
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("CONTENT_COPY");
        return builder;
    }

}
