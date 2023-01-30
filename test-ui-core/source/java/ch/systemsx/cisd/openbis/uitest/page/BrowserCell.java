/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page;

/**
 * @author anttil
 */
public class BrowserCell
{
    private final String text;

    private final String url;

    public BrowserCell(String text, String url)
    {
        this.text = text;
        this.url = url;
    }

    public String getText()
    {
        return text;
    }

    public String getUrl()
    {
        return url;
    }

    public boolean hasLink()
    {
        return this.url != null;
    }

    @Override
    public String toString()
    {
        return "Cell[text=" + text + ", url=" + url + "]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof BrowserCell))
        {
            return false;
        }

        BrowserCell cell = (BrowserCell) o;
        return toString().equals(cell.toString());
    }
}
