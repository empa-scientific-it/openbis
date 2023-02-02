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

package ch.systemsx.cisd.common.string;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.Template.IToken;

/**
 * Test cases for the {@link Template}.
 * 
 * @author Franz-Josef Elmer
 */
public class TemplateTest
{
    @Test
    public void testEmptyTemplate()
    {
        assertEquals("", new Template("").createText());
    }

    @Test
    public void testWithoutPlaceholders()
    {
        assertEquals("hello", new Template("hello").createText());
    }

    @Test
    public void testWithOnePlaceholder()
    {
        Template template = new Template("hello ${name}!");
        template.bind("name", "world");
        assertEquals(0, template.tryGetIndex("name"));
        assertEquals("hello world!", template.createText());
    }

    @Test
    public void testWithOnePlaceholderManipulateTokens()
    {
        Template template = new Template("hello ${name}!");
        assertEquals(0, template.tryGetIndex("name"));
        for (IToken token : template.getTokens())
        {
            if (token.isVariable())
            {
                token.setSubString(0, 0, "'", "'");
            } else
            {
                if (StringUtils.endsWith(token.tryGetValue(), " "))
                {
                    token.setValue(token.tryGetValue().substring(0, token.tryGetValue().length() - 1));
                }
            }
        }
        template.bind("name", "world");
        assertEquals("hello'world'!", template.createText());
    }

    @Test
    public void testWithTwoPlaceholdersReplaceBrackets()
    {
        Template template = new Template("hello (${name}) (${name2}) {${name3}}");
        assertEquals(0, template.tryGetIndex("name"));
        assertEquals(1, template.tryGetIndex("name2"));
        final List<IToken> replaced1 = template.replaceBrackets("(", ")", "[", "]");
        assertEquals(2, replaced1.size());
        assertEquals("name", replaced1.get(0).tryGetName());
        assertEquals("name2", replaced1.get(1).tryGetName());
        final List<IToken> replaced2 = template.replaceBrackets("{", "}", "", "");
        assertEquals(1, replaced2.size());
        assertEquals("name3", replaced2.get(0).tryGetName());
        template.bind("name", "world");
        template.bind("name2", "universe");
        template.bind("name3", "???");
        assertEquals("hello [world] [universe] ???", template.createText());
    }

    @Test
    public void testWithPlaceholderMetadata()
    {
        Template template =
                new Template("hello ${name::some metadata} - welcome to ${CISD:shortcut}!");
        assertEquals("some metadata", template.tryGetMetadata("name"));
        assertNull(template.tryGetMetadata("CISD"));
        assertNull(template.tryGetMetadata("CISD:shortcut"));
        template.bind("name", "world");
        template.bind("CISD:shortcut", "Center for Sciences and Databases");
        assertEquals("hello world - welcome to Center for Sciences and Databases!",
                template.createText());
    }

    @Test
    public void testWithTwiceTheSamePlaceholder()
    {
        Template template = new Template("hello ${name}${name}");
        template.bind("name", "world");
        assertEquals(1, template.getPlaceholderNames().size());
        assertEquals(0, template.tryGetIndex("name"));
        assertEquals("hello worldworld", template.createText());
    }

    @Test
    public void testWithTwoPlaceholders()
    {
        Template template = new Template("hello ${name}, do you know ${name2}?");
        template.bind("name", "world");
        template.bind("name2", "Albert Einstein");
        Set<String> placeholderNames = template.getPlaceholderNames();
        assertEquals(true, placeholderNames.contains("name"));
        assertEquals(true, placeholderNames.contains("name2"));
        assertEquals(2, placeholderNames.size());
        assertEquals(0, template.tryGetIndex("name"));
        assertEquals(1, template.tryGetIndex("name2"));
        assertEquals("hello world, do you know Albert Einstein?", template.createText());
    }

    @Test
    public void testWithEscaping()
    {
        Template template = new Template("hello $${name}. I have 25$.");
        assertEquals("hello ${name}. I have 25$.", template.createText());
    }

    @Test
    public void testNamelessPlaceholderInTemplate()
    {
        try
        {
            new Template("hello ${}");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Nameless placeholder ${} found.", e.getMessage());
        }
    }

    @Test
    public void testUnfinishedPlaceholderInTemplate()
    {
        try
        {
            new Template("hello ${name");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Incomplete placeholder detected at the end.", e.getMessage());
        }
        try
        {
            new Template("hello ${");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Incomplete placeholder detected at the end.", e.getMessage());
        }
        try
        {
            new Template("hello $");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Incomplete placeholder detected at the end.", e.getMessage());
        }
    }

    @Test
    public void testBindUnknownPlaceholder()
    {
        Template template = new Template("hello ${name}!");
        try
        {
            template.bind("blabla", "blub");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Unknown variable 'blabla'.", e.getMessage());
        }
    }

    @Test
    public void testAttemptToBind()
    {
        Template template = new Template("${greeting} ${name}!");
        assertEquals(true, template.attemptToBind("greeting", "Hi"));
        assertEquals(false, template.attemptToBind("blabla", "blub"));
        assertEquals("Hi ${name}!", template.createText(false));
    }

    @Test
    public void testIncompleteBinding()
    {
        Template template = new Template("${greeting} ${name}!");
        try
        {
            template.createText(true);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("The following variables are not bound: greeting name ", e.getMessage());
        }

        template.bind("greeting", "hello");
        assertEquals("hello ${name}!", template.createText(false));
        try
        {
            template.createText(true);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("The following variables are not bound: name ", e.getMessage());
        }
    }

    @Test
    public void testCreateFreshCopy()
    {
        Template template = new Template("hello ${name}.${name}!");
        Template template1 = template.createFreshCopy();
        try
        {
            template1.createText();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e)
        {
            assertEquals("The following variables are not bound: name ", e.getMessage());
        }
        template1.bind("name", "world");
        assertEquals("hello world.world!", template1.createText());
        assertEquals("hello ${name}.${name}!", template.createText(false));

        Template template2 = template.createFreshCopy();
        template2.bind("name", "universe");
        assertEquals("hello universe.universe!", template2.createText());
        assertEquals("hello world.world!", template1.createText());
        assertEquals("hello ${name}.${name}!", template.createText(false));
    }
}
