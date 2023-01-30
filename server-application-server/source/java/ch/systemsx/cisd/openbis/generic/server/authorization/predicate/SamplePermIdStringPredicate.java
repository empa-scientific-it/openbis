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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;

/**
 * @author pkupczyk
 */
public class SamplePermIdStringPredicate extends DelegatedPredicate<PermId, String>
{

    public SamplePermIdStringPredicate()
    {
        this(true);
    }

    public SamplePermIdStringPredicate(boolean isReadAccess)
    {
        super(new SamplePermIdPredicate(isReadAccess, false));
    }

    @Override
    public PermId tryConvert(String value)
    {
        return value != null ? new PermId(value) : null;
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample perm id";
    }

}
