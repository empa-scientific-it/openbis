/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base;

import ch.systemsx.cisd.base.convert.NativeDataTests;
import ch.systemsx.cisd.base.convert.NativeTaggedArrayTests;
import ch.systemsx.cisd.base.exceptions.IOExceptionUncheckedTests;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFileTests;
import ch.systemsx.cisd.base.io.RandomAccessFileImplTests;
import ch.systemsx.cisd.base.mdarray.MDArrayTests;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutorTest;

/**
 * Run all unit tests.
 *
 * @author Bernd Rinn
 */
public class AllTests
{

    public static void main(String[] args) throws Throwable
    {
        NativeDataTests.main(args);
        System.out.println();
        NativeTaggedArrayTests.main(args);
        System.out.println();
        IOExceptionUncheckedTests.main(args);
        System.out.println();
        ByteBufferRandomAccessFileTests.main(args);
        System.out.println();
        RandomAccessFileImplTests.main(args);
        System.out.println();
        MDArrayTests.main(args);
        System.out.println();
        NamingThreadPoolExecutorTest.main(args);
        System.out.println();
    }

}
