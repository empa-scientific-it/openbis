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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;

/**
 * Predicate based on {@link ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample}. This predicate authorizes for read-only access, i.e. it will
 * allow access to shared samples for all users.
 * 
 * @author Bernd Rinn
 */
public class SamplePredicate extends DelegatedPredicate<List<Sample>, Sample>
{
    public SamplePredicate()
    {
        super(new SampleListPredicate());
    }

    @Override
    public List<Sample> tryConvert(Sample value)
    {
        return Collections.singletonList(value);
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample";
    }

}
