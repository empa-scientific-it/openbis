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

package ch.systemsx.cisd.common.security;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generator for secure passwords. The generator can create either completely random (stronger) or human pronounceable passwords (better memorable).
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
public class PasswordGenerator
{
    private final static char[] ALLOWED_CHARACTERS =
            new char[]
            { '!', '#', '$', '%', '&', '(', ')', '*', '+', '-', '/', '0', '1', '2', '3', '4',
                    '5', '6', '7', '8', '9', ':', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D',
                    'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
                    'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', ']', '^', 'a', 'b', 'c', 'd', 'e',
                    'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                    'u', 'v', 'w', 'x', 'y', 'z', '{', '}', '~' };

    private enum State
    {
        DIGIT()
        {
            @Override
            State nextState(Random random)
            {
                return random.nextBoolean() ? CONSONANT : VOWEL;
            }

            @Override
            String getCharacters()
            {
                return "0123456789";
            }
        },

        CONSONANT()
        {
            @Override
            State nextState(Random random)
            {
                return random.nextBoolean() ? DIGIT : VOWEL;
            }

            @Override
            String getCharacters()
            {
                return "bdcfghjklmnpqrstvwxzBCDFGHJKLMNPQRSTVWXZ";
            }

        },

        VOWEL()
        {
            @Override
            State nextState(Random random)
            {
                return random.nextBoolean() ? CONSONANT : DIGIT;
            }

            @Override
            String getCharacters()
            {
                return "aeiouyAEIOUY";
            }
        };

        State nextState(Random random)
        {
            return this;
        }

        void appendCharacterTo(StringBuilder builder, Random random)
        {
            String characters = getCharacters();
            builder.append(characters.charAt(random.nextInt(characters.length())));
        }

        abstract String getCharacters();
    }

    public final static int DEFAULT_LENGTH = 10;

    private final Random random;

    private final boolean generatePronouncablePasswordsDefault;

    private final int lengthDefault;

    /**
     * Creates an instance based on the default constructor of {@link SecureRandom}.
     */
    public PasswordGenerator()
    {
        this(DEFAULT_LENGTH, false);
    }

    /**
     * Creates an instance based on the default constructor of {@link Random}.
     */
    public PasswordGenerator(boolean generatePronouncablePasswords)
    {
        this(new SecureRandom(), DEFAULT_LENGTH, generatePronouncablePasswords);
    }

    /**
     * Creates an instance based on the default constructor of {@link Random}.
     */
    public PasswordGenerator(int length, boolean generatePronouncablePasswords)
    {
        this(new SecureRandom(), length, generatePronouncablePasswords);
    }

    /**
     * Creates an instance which uses the specified random number generator.
     */
    public PasswordGenerator(Random random, int length, boolean generatePronouncablePasswords)
    {
        assert random != null : "Unspecified random number generator.";
        this.random = random;
        this.lengthDefault = length;
        this.generatePronouncablePasswordsDefault = generatePronouncablePasswords;
    }

    private char nextChar()
    {
        return ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)];
    }

    /**
     * Creates a password.
     */
    public String generatePassword()
    {
        return generatePassword(lengthDefault, generatePronouncablePasswordsDefault);
    }

    /**
     * Creates a password with the specified strength.
     */
    public String generatePassword(boolean generatePronouncablePasswords)
    {
        return generatePassword(lengthDefault, generatePronouncablePasswordsDefault);
    }

    /**
     * Creates a password with the specified length and strength.
     */
    public String generatePassword(int length)
    {
        return generatePassword(length, generatePronouncablePasswordsDefault);
    }

    /**
     * Creates a password with the specified length and strength.
     */
    public String generatePassword(int length, boolean generatePronouncablePasswords)
    {
        final StringBuilder builder = new StringBuilder();
        if (generatePronouncablePasswords)
        {
            State state = State.DIGIT;
            for (int i = 0; i < length; i++)
            {
                state = state.nextState(random);
                state.appendCharacterTo(builder, random);
            }
            return builder.toString();
        } else
        {
            for (int i = 0; i < length; i++)
            {
                builder.append(nextChar());
            }
        }
        return builder.toString();
    }

}
