/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.get.GetPersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.get.GetPersonsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetPersonsOperationExecutor
        extends GetObjectsOperationExecutor<IPersonId, Person, PersonFetchOptions>
        implements IGetPersonsOperationExecutor
{
    @Autowired
    private IMapPersonIdByIdExecutor mapExecutor;
    
    @Autowired
    private IPersonTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IPersonId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Person, PersonFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IPersonId, Person> getOperationResult(Map<IPersonId, Person> objectMap)
    {
        return new GetPersonsOperationResult(objectMap);
    }

    @Override
    protected Class<? extends GetObjectsOperation<IPersonId, PersonFetchOptions>> getOperationClass()
    {
        return GetPersonsOperation.class;
    }

}
