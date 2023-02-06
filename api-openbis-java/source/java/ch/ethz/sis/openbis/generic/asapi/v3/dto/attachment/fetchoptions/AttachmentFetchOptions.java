/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.attachment.fetchoptions.AttachmentFetchOptions")
public class AttachmentFetchOptions extends FetchOptions<Attachment> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private AttachmentFetchOptions previousVersion;

    @JsonProperty
    private EmptyFetchOptions content;

    @JsonProperty
    private AttachmentSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    // Method automatically generated with DtoGenerator
    public AttachmentFetchOptions withPreviousVersion()
    {
        if (previousVersion == null)
        {
            previousVersion = new AttachmentFetchOptions();
        }
        return previousVersion;
    }

    // Method automatically generated with DtoGenerator
    public AttachmentFetchOptions withPreviousVersionUsing(AttachmentFetchOptions fetchOptions)
    {
        return previousVersion = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPreviousVersion()
    {
        return previousVersion != null;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withContent()
    {
        if (content == null)
        {
            content = new EmptyFetchOptions();
        }
        return content;
    }

    // Method automatically generated with DtoGenerator
    public EmptyFetchOptions withContentUsing(EmptyFetchOptions fetchOptions)
    {
        return content = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContent()
    {
        return content != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public AttachmentSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new AttachmentSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public AttachmentSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Attachment", this);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("PreviousVersion", previousVersion);
        f.addFetchOption("Content", content);
        return f;
    }

}
