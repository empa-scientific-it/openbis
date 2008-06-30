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

package ch.systemsx.cisd.datamover.console.server;

import javax.servlet.http.HttpSession;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ActionLog implements IActionLog
{

    public void logFailedLoginAttempt(String userCode)
    {
        System.out.println("authentication failed for user " + userCode);
    }

    public void logLogout(HttpSession httpSession)
    {
        System.out.println("log out user ");
        // TODO Auto-generated method stub

    }

    public void logSuccessfulLogin()
    {
        System.out.println("logged in");
    }

}
