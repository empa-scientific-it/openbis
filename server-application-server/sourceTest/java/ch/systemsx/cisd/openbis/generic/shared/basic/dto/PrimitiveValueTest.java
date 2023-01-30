package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;

/**
 * @author Piotr Buczek
 */
public class PrimitiveValueTest extends AssertJUnit
{
    private enum ExpectedResult
    {
        LOWER, EQUAL, HIGHER;
    }

    @DataProvider(name = "values")
    protected Object[][] getValues()
    {
        return new Object[][]
        {
                /* NULL == NULL == "" */
                { PrimitiveValue.NULL, PrimitiveValue.NULL, ExpectedResult.EQUAL },
                { PrimitiveValue.NULL, new PrimitiveValue(""), ExpectedResult.EQUAL },
                /* NULL < anything else */
                { PrimitiveValue.NULL, new PrimitiveValue("text"), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue("5"), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue(-10L), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue(0L), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue(10L), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue(-10.0), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue(0.0), ExpectedResult.LOWER },
                { PrimitiveValue.NULL, new PrimitiveValue(10.0), ExpectedResult.LOWER },
                /* String < Number */
                { new PrimitiveValue("text"), new PrimitiveValue(-10L), ExpectedResult.LOWER },
                { new PrimitiveValue("text"), new PrimitiveValue(0L), ExpectedResult.LOWER },
                { new PrimitiveValue("text"), new PrimitiveValue(10L), ExpectedResult.LOWER },
                { new PrimitiveValue("text"), new PrimitiveValue(-10.0), ExpectedResult.LOWER },
                { new PrimitiveValue("text"), new PrimitiveValue(0.0), ExpectedResult.LOWER },
                { new PrimitiveValue("text"), new PrimitiveValue(10.0), ExpectedResult.LOWER },
                /* String number < Number */
                { new PrimitiveValue("5"), new PrimitiveValue(5L), ExpectedResult.LOWER },
                { new PrimitiveValue("5"), new PrimitiveValue(-10L), ExpectedResult.LOWER },
                { new PrimitiveValue("5"), new PrimitiveValue(0L), ExpectedResult.LOWER },
                { new PrimitiveValue("5"), new PrimitiveValue(10L), ExpectedResult.LOWER },
                { new PrimitiveValue("5"), new PrimitiveValue(-10.0), ExpectedResult.LOWER },
                { new PrimitiveValue("5"), new PrimitiveValue(0.0), ExpectedResult.LOWER },
                { new PrimitiveValue("5"), new PrimitiveValue(10.0), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(5.0), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(-10L), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(0L), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(10L), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(-10.0), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(0.0), ExpectedResult.LOWER },
                { new PrimitiveValue("5.0"), new PrimitiveValue(10.0), ExpectedResult.LOWER },
                /* String vs String */
                { new PrimitiveValue("aba"), new PrimitiveValue("aba"), ExpectedResult.EQUAL },
                { new PrimitiveValue("aba"), new PrimitiveValue("abc"), ExpectedResult.LOWER },
                { new PrimitiveValue("abc"), new PrimitiveValue("abca"), ExpectedResult.LOWER },
                { new PrimitiveValue("aba"), new PrimitiveValue("abca"), ExpectedResult.LOWER },
                /* Long vs Long */
                { new PrimitiveValue(10L), new PrimitiveValue(10L), ExpectedResult.EQUAL },
                { new PrimitiveValue(-10L), new PrimitiveValue(0L), ExpectedResult.LOWER },
                { new PrimitiveValue(-10L), new PrimitiveValue(10L), ExpectedResult.LOWER },
                { new PrimitiveValue(5L), new PrimitiveValue(10L), ExpectedResult.LOWER },
                /* Double vs Double */
                { new PrimitiveValue(9.9), new PrimitiveValue(9.9), ExpectedResult.EQUAL },
                { new PrimitiveValue(9.9), new PrimitiveValue(10.0), ExpectedResult.LOWER },
                /* Long vs Double */
                { new PrimitiveValue(0L), new PrimitiveValue(0.0), ExpectedResult.EQUAL },
                { new PrimitiveValue(10L), new PrimitiveValue(10.0), ExpectedResult.EQUAL },
                { new PrimitiveValue(-10L), new PrimitiveValue(-5.0), ExpectedResult.LOWER },
                { new PrimitiveValue(9.9), new PrimitiveValue(10L), ExpectedResult.LOWER },

        };
    }

    @Test(dataProvider = "values")
    public void testComparison(PrimitiveValue v1, PrimitiveValue v2, ExpectedResult expectedResult)
    {
        switch (expectedResult)
        {
            case EQUAL:
                assertTrue(v1.compareTo(v2) == 0);
                assertTrue(v2.compareTo(v1) == 0);
                break;
            case LOWER:
                assertTrue(v1.compareTo(v2) < 0);
                assertTrue(v2.compareTo(v1) > 0);
                break;
            case HIGHER:
                assertTrue(v1.compareTo(v2) > 0);
                assertTrue(v2.compareTo(v1) < 0);
                break;
        }
    }
}
