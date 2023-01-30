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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class MetaprojectNameTest
{

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNullName()
    {
        MetaprojectName.validate(null);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateEmptyName()
    {
        MetaprojectName.validate("");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNameWithSpace()
    {
        MetaprojectName.validate("TEST NAME");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNameWithTab()
    {
        MetaprojectName.validate("TEST\tNAME");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNameWithNewLine()
    {
        MetaprojectName.validate("TEST\nNAME");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNameWithComma()
    {
        MetaprojectName.validate("TEST,NAME");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNameWithSlash()
    {
        MetaprojectName.validate("TEST/NAME");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testValidateNameWithBackslash()
    {
        MetaprojectName.validate("TEST\\NAME");
    }

    @Test
    public void testValidateNameWithAllowedCharacters()
    {
        MetaprojectName.validate("TEST_NAME#with@allowed$characters?0123456789.[]-+*=");
    }

}
