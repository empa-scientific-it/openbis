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

package ch.systemsx.cisd.common.spring;

import java.util.Collections;
import java.util.List;

import org.springframework.aop.ThrowsAdvice;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;

/**
 * A <code>ThrowsAdvice</code> implementation which remove proprietary/external libraries specific exceptions from the one thrown on the server side.
 * <p>
 * This is used just before result returns to the client, on the service layer.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ServiceExceptionTranslator implements ThrowsAdvice
{
    private List<String> packages = Collections.emptyList();

    public final void setPackagesNotMasqueraded(List<String> packages)
    {
        this.packages = packages;
    }

    //
    // ThrowsAdvice
    //

    public final void afterThrowing(final Exception exception) throws Exception
    {
        throw ExceptionUtils.createMasqueradingExceptionIfNeeded(exception, packages);
    }
}
