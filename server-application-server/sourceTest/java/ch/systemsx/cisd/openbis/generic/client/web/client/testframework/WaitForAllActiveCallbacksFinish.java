/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

/**
 * {@link AbstractDefaultTestCommand} extension that does nothing. It should be used if we want to make sure that all callbacks will finish without
 * failure.
 * 
 * @author Piotr Buczek
 */
public class WaitForAllActiveCallbacksFinish extends AbstractDefaultTestCommand
{
    public WaitForAllActiveCallbacksFinish()
    {
        super();
    }

    @Override
    public void execute()
    {
    }

}
