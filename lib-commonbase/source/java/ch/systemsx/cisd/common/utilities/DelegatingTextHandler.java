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

package ch.systemsx.cisd.common.utilities;

/**
 * A {@link ITextHandler} implementation which delegates {@link #handle(String)} to many wrapped {@link ITextHandler} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class DelegatingTextHandler implements ITextHandler
{
    private ITextHandler[] textHandlers;

    public DelegatingTextHandler(ITextHandler... textHandlers)
    {
        this.textHandlers = textHandlers;
    }

    @Override
    public void handle(String text)
    {
        if (textHandlers != null)
        {
            for (ITextHandler textHandler : textHandlers)
            {
                if (textHandler != null)
                {
                    textHandler.handle(text);
                }
            }
        }
    }
}