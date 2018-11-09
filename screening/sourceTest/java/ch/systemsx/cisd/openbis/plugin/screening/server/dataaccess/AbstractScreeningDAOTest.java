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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOWithoutContextTest;

/**
 * Abstract DAO test for screening.
 * 
 * @author Kaloyan Enimanev
 */
@ContextConfiguration(locations = "classpath:screening-applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback(value = false)
public class AbstractScreeningDAOTest extends AbstractDAOWithoutContextTest
{

}
