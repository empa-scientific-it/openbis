/*
 * Copyright 2015 ETH Zuerich, SIS
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

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Predicate for arguments of type {@link SamplePE} which can be <code>null</code>.
 *
 * @author Franz-Josef Elmer
 */
public class SamplePEOrNullPredicate extends SamplePEPredicate
{
    public SamplePEOrNullPredicate()
    {
        super(true);
    }
}
