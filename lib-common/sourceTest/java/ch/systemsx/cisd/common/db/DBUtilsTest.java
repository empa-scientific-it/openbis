/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.db;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Pawel Glyzewski
 */
public class DBUtilsTest
{
    @Test
    public void testTryToTranslateRegExpToLikeForm()
    {
        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("^test$"), "test");
        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("ala ma kota"), "%ala ma kota%");
        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("^ala/.*/kota$"), "ala/%/kota");
        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("^ala.ma\\.kota$"),
                "ala_ma.kota");
        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("^ala.+ma\\+kota$"),
                "ala_%ma+kota");

        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("^ala%ma_ko\\\\ta$"),
                "ala\\%ma\\_ko\\\\ta");
        Assert.assertEquals(DBUtils.tryToTranslateRegExpToLikePattern("^$"), "");

        Assert.assertNull(DBUtils.tryToTranslateRegExpToLikePattern(null));
        Assert.assertNull(DBUtils.tryToTranslateRegExpToLikePattern("ala(ma|kota)"));
        Assert.assertNull(DBUtils.tryToTranslateRegExpToLikePattern("a*la"));
    }
}
