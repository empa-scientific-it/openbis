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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;

/**
 * An {@link AbstractRegistrationHolder} extension which holds a code.
 * 
 * @author Christian Ribeaud
 */
public class CodeWithRegistration<T extends CodeWithRegistration<T>> extends
        AbstractRegistrationHolder implements ICodeHolder, Comparable<T>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    public void setCode(final String code)
    {
        this.code = code;
    }

    //
    // ICodeProvider
    //

    @Override
    public final String getCode()
    {
        return code;
    }

    //
    // Comparable
    //

    @Override
    public int compareTo(final T o)
    {
        return Code.CODE_PROVIDER_COMPARATOR.compare(this, o);
    }
}
