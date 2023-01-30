/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Span;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SearchableEntityTranslatorTest
{

    @Test
    public void testCreateSpanList()
    {
        check("PLA", "abc/Plate.png", "[4:7]");
        check("*pla?e*01*", "abc/(PLATE)01.png", "[5:8], [9:10], [11:13]");
        check("(PLATE)*?*[^abc]?{_.:}", "abc/(PLATE)123[^abc]x{_.:}.png", "[4:11], [14:20], [21:26]");
        check("(PLATE)*?*[^abc]?{_.:}", "abc/(PLATE)123[^abc]x{_d:}.png", "");
        check(")(](](]|[&)[&&)---:::^^^}}${{*.png", "abc/)(](](]|[&)[&&)---:::^^^}}${{-11234.png", "[4:33], [39:43]");
    }

    private void check(String searchString, String filePath, String expectedSpans)
    {
        List<Span> spanList = SearchableEntityTranslator.createSpanList(searchString, filePath);
        assertEquals(render(spanList), expectedSpans);
    }
    
    private String render(List<Span> spans)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (Span span : spans)
        {
            builder.append(render(span));
        }
        return builder.toString();
    }

    private String render(Span span)
    {
        return "[" + span.getStart() + ":" + span.getEnd() + "]";
    }
}
