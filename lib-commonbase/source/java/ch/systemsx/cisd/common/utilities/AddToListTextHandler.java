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

import java.util.List;

/**
 * A text handler which adds the text to be handled to a {@link List}.
 *
 * @author Franz-Josef Elmer
 */
public class AddToListTextHandler implements ITextHandler
{
    private List<String> texts;

    public AddToListTextHandler(List<String> textsOrNull)
    {
        this.texts = textsOrNull;
    }

    @Override
    public void handle(String text)
    {
        if (texts != null)
        {
            texts.add(text);
        }
    }

}
