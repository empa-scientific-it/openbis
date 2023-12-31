/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * Use this interface to delegate any kind of action to a different part of code without adding an explicit dependency.
 * 
 * @author Tomasz Pylak
 */
public interface IDelegatedAction extends ScheduledCommand
{
    public static IDelegatedAction DO_NOTHING = new IDelegatedAction()
        {
            @Override
            public void execute()
            {
            }
        };

    @Override
    void execute();
}
