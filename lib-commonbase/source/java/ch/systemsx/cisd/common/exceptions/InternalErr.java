/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.exceptions;

import ch.systemsx.cisd.common.exceptions.InternalErr;

/**
 * Exception representing internal error in the program caused by programmer error. It should never be caught. We throw this exception to say the
 * system (e.g. some data structure) is in the state, in which it never meant to be when a particular piece of code was written. This is something
 * different from the situation when a user has provided wrong input data. <br>
 * This class becomes handy when we do comparison of some value with the enumerator. The following 'enum-match' pattern should be applied: 1. all
 * values of the enumerator should be checked 2. if no matching of the value and enumerator is found, it is a programmer mistake and internal error
 * occurs. It means that when one adds a new enumerator value, one should check every comparison of other values and update them.
 * 
 * <pre>
 * Example:
 * enum TypeX { A, B, C }
 *  
 * void doSomething(TypeX value) {
 *   if (value.equals(TypeX.A) {
 *      // process A
 *   } else if (value.equals(TypeX.B) {
 *      // process C
 *   } else if (value.equals(TypeX.C) {
 *      // process C
 *   } else
 *     throw InternalErr.error(value);
 * }
 * </pre>
 * 
 * @author Tomasz Pylak
 */
public class InternalErr extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private InternalErr(String message)
    {
        super(message);
    }

    public static final RuntimeException error()
    {
        return new InternalErr("This should never happen");
    }

    public static final RuntimeException error(String errorMessage)
    {
        return new InternalErr(errorMessage);
    }

    public static RuntimeException error(Object enumValue)
    {
        return error("Internal error: action for enum " + enumValue + " is missing.");
    }
}
