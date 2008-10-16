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

package ch.systemsx.cisd.common.array;

/**
 * 
 *
 * @author Bernd Rinn
 */
public class Test
{

    public static void main(String[] args)
    {
        MDIntArray a = new MDIntArray(new int[] { 1, 2, 3 }, new int[] { 3 });
        System.out.println(a.get(2));
    }

}
