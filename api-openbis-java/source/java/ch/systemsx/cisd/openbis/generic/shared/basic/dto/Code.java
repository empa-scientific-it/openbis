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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;

/**
 * A code.
 * 
 * @author Christian Ribeaud
 */
public class Code<T extends Code<T>> implements Serializable, ICodeHolder, Comparable<T>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String CODE = "code";

    public final static Comparator<ICodeHolder> CODE_PROVIDER_COMPARATOR =
            new CodeProviderComparator();

    private String code;

    @BeanProperty(label = CODE)
    public final void setCode(final String code)
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
    public final int compareTo(final T o)
    {
        return CODE_PROVIDER_COMPARATOR.compare(this, o);
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Code<?> == false)
        {
            return false;
        }
        final Code<?> that = (Code<?>) obj;
        return getCode().equals(that.getCode());
    }

    @Override
    public int hashCode()
    {
        return getCode().hashCode();
    }

    @Override
    public String toString()
    {
        return getCode();
    }

    //
    // Helper classes
    //

    public final static List<String> extractCodes(Collection<? extends ICodeHolder> codeProviders)
    {
        List<String> codes = new ArrayList<String>();
        if (codeProviders != null)
        {
            for (ICodeHolder codeProvider : codeProviders)
            {
                codes.add(codeProvider.getCode());
            }
        }
        return codes;
    }

    public final static List<String> extractCodes(ICodeHolder[] codeProviders)
    {
        List<String> codes = new ArrayList<String>();
        if (codeProviders != null)
        {
            for (ICodeHolder codeProvider : codeProviders)
            {
                codes.add(codeProvider.getCode());
            }
        }
        return codes;
    }

    public final static String[] extractCodesToArray(Collection<? extends ICodeHolder> codeProviders)
    {
        int size = (codeProviders == null) ? 0 : codeProviders.size();
        return extractCodes(codeProviders).toArray(new String[size]);
    }

    private final static class CodeProviderComparator implements Comparator<ICodeHolder>,
            Serializable
    {
        private static final long serialVersionUID = 1L;

        //
        // Comparable
        //

        @Override
        public int compare(final ICodeHolder o1, final ICodeHolder o2)
        {
            assert o1 != null : "Unspecified code provider.";
            assert o2 != null : "Unspecified code provider.";
            final String thisCode = o1.getCode();
            final String thatCode = o2.getCode();
            if (thisCode == null)
            {
                return thatCode == null ? 0 : -1;
            }
            if (thatCode == null)
            {
                return 1;
            }
            return thisCode.compareTo(thatCode);
        }
    }
}
