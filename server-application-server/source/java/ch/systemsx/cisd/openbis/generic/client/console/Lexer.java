/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.console;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
class Lexer
{
    private static class ParsingContext
    {
        private final List<String> tokens = new ArrayList<String>();

        private final StringBuilder builder = new StringBuilder();

        public void addCharacter(char character)
        {
            builder.append(character);
        }

        public void finishToken()
        {
            tokens.add(builder.toString());
            builder.setLength(0);
        }

        public List<String> getTokens()
        {
            if (builder.length() > 0)
            {
                finishToken();
            }
            return tokens;
        }
    }

    private enum State
    {
        BETWEEN_TOKENS()
        {
            @Override
            public State next(char character, ParsingContext context)
            {
                if (Character.isWhitespace(character))
                {
                    return this;
                }
                if (character == '\"')
                {
                    return IN_QUOTED_TOKEN;
                }
                context.addCharacter(character);
                return IN_TOKEN;
            }
        },
        IN_TOKEN()
        {
            @Override
            public State next(char character, ParsingContext context)
            {
                if (Character.isWhitespace(character))
                {
                    context.finishToken();
                    return BETWEEN_TOKENS;
                }
                if (character == '\"')
                {
                    return IN_QUOTED_TOKEN;
                }
                context.addCharacter(character);
                return this;
            }
        },
        IN_QUOTED_TOKEN()
        {
            @Override
            public State next(char character, ParsingContext context)
            {
                if (character == '\"')
                {
                    return IN_TOKEN;
                }
                context.addCharacter(character);
                return this;
            }
        };

        abstract State next(char character, ParsingContext context);
    }

    static List<String> extractTokens(String string)
    {
        ParsingContext context = new ParsingContext();
        State state = State.BETWEEN_TOKENS;
        for (int i = 0, n = string.length(); i < n; i++)
        {
            state = state.next(string.charAt(i), context);
        }
        return context.getTokens();
    }
}
