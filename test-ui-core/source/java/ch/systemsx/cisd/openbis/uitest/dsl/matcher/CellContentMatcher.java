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

package ch.systemsx.cisd.openbis.uitest.dsl.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.uitest.page.BrowserCell;
import ch.systemsx.cisd.openbis.uitest.page.BrowserRow;

/**
 * @author anttil
 */
public class CellContentMatcher extends TypeSafeMatcher<BrowserRow>
{

    private String column;

    private String expected;

    private boolean link;

    public CellContentMatcher(String column, String value, boolean link)
    {
        this.column = column;
        this.expected = value;
        this.link = link;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(
                "A cell in column " + column + (link ? " linking to " : " displaying ") + expected);
    }

    @Override
    public boolean matchesSafely(BrowserRow row)
    {
        if (row.exists() == false)
        {
            return false;
        }

        BrowserCell actual = row.get(column);
        if (actual == null)
        {
            return false;
        }

        return expected.equalsIgnoreCase(link ? actual.getUrl() : actual.getText().trim());
    }
}
