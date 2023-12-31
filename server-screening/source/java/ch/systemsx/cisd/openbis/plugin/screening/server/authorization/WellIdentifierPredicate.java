/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DelegatedPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePermIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;

/**
 * A predicate for {@link WellIdentifier}.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Piotr Buczek
 */
public class WellIdentifierPredicate extends DelegatedPredicate<PermId, WellIdentifier>
{

    public WellIdentifierPredicate()
    {
        super(new SamplePermIdPredicate(false, false));
    }

    @Override
    public final String getCandidateDescription()
    {
        return "well identifier";
    }

    @Override
    public PermId tryConvert(WellIdentifier value)
    {
        return new PermId(value.getPermId());
    }
}
