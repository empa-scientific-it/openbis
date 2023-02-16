/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.session.ISessionInformationTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@Component
public class GetSessionInformationExecutor implements IGetSessionInformationExecutor
{

    @Autowired
    private ISessionInformationAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISessionInformationTranslator translator;

    @Override
    public SessionInformation getInformation(IOperationContext context)
    {
        authorizationExecutor.canGet(context);

        Session session = null;

        try
        {
            session = context.getSession();
        } catch (Exception ex)
        {
            // Ignore, if session is no longer available and error is thrown
        }

        if (session != null)
        {
            SessionInformationFetchOptions fetchOptions = new SessionInformationFetchOptions();
            fetchOptions.withPerson();
            fetchOptions.withCreatorPerson();

            return translator.translate(new TranslationContext(session), session, fetchOptions);
        }

        return null;
    }

}
