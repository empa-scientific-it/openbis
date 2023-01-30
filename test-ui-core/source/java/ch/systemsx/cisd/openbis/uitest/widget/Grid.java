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

package ch.systemsx.cisd.openbis.uitest.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.BrowserCell;
import ch.systemsx.cisd.openbis.uitest.page.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.webdriver.Contextual;

/**
 * @author anttil
 */
public class Grid implements Widget
{

    @Contextual
    private WebElement context;

    public BrowserRow select(String column, String value)
    {

        List<WebElement> columns = this.getColumns();

        int index = 0;
        boolean found = false;
        for (WebElement element : columns)
        {
            if (element.getText().equalsIgnoreCase(column))
            {
                found = true;
                break;
            }
            index++;
        }

        if (!found)
        {
            throw new IllegalArgumentException("Column " + column + " does not exist");
        }

        int numColumns = columns.size();

        List<WebElement> cells = this.getCells();
        found = false;
        for (; index < cells.size(); index += numColumns)
        {
            if (cells.get(index).getText().equalsIgnoreCase(value))
            {
                cells.get(index).findElement(By.xpath("./..")).click();
                found = true;
                break;
            }
        }

        if (!found)
        {
            return new BrowserRow();
        }

        index = index - (index % numColumns);

        Map<String, BrowserCell> m = new HashMap<String, BrowserCell>();
        for (int i = 0; i < numColumns; i++)
        {
            WebElement element = cells.get(i + index);
            m.put(columns.get(i).getText(),
                    new BrowserCell(element.getText(), element.getAttribute("href")));
        }
        return new BrowserRow(m);
    }

    public List<BrowserRow> getData()
    {
        List<String> columns = getColumnNames();

        List<BrowserRow> result = new ArrayList<BrowserRow>();
        Map<String, BrowserCell> map = new HashMap<String, BrowserCell>();
        int index = 0;
        for (WebElement element : getCells())
        {
            map.put(columns.get(index), new BrowserCell(element.getText(), element
                    .getAttribute("href")));
            index++;
            if (index % columns.size() == 0)
            {
                result.add(new BrowserRow(map));
                map = new HashMap<String, BrowserCell>();
                index = 0;
            }
        }

        return result;
    }

    public List<String> getColumnNames()
    {
        List<String> columns = new ArrayList<String>();
        for (WebElement column : getColumns())
        {
            columns.add(column.getText());
        }
        return columns;
    }

    private List<WebElement> getColumns()
    {
        return context
                .findElements(By
                        .xpath(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-header ')]//span[not(*)]"));
    }

    private List<WebElement> getCells()
    {
        SeleniumTest.setImplicitWait(500, TimeUnit.MILLISECONDS);
        try
        {
            return context
                    .findElements(By
                            .xpath(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-col ')]//*[not(*)]"));
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }
    }

    @Override
    public String toString()
    {
        List<WebElement> columns = getColumns();
        String s = "";
        for (WebElement column : columns)
        {
            s += column.getText() + "\t";
        }
        s += "\n";

        int counter = 0;
        for (WebElement cell : getCells())
        {
            s += cell.getText() + "\t";
            counter++;
            if (counter % columns.size() == 0)
            {
                s += "\n";
            }
        }
        return s;
    }
}
