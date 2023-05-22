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

package ch.systemsx.cisd.base.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for {@link NativeTaggedArray}.
 * 
 * @author Bernd Rinn
 */
public class NativeTaggedArrayTests
{

    @Test
    public static void testFloat1DArrayNativeByteOrder()
    {
        final float[] floatArr = new float[]
            { 1.1f, -3.2f, 1001.5f };
        final byte[] taggedArr = NativeTaggedArray.toByteArray(floatArr);
        assertEquals(3 * 4 + 4 + 4, taggedArr.length);
        final float[] convertedFloatArr = NativeTaggedArray.tryToFloatArray1D(taggedArr);
        final NativeTaggedArray.NativeArrayTag tag = NativeTaggedArray.tryGetArrayTag(taggedArr);
        final NativeArrayEncoding encoding = tag.getEncoding();
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertEquals(1, tag.getDimensions().length);
        assertEquals(3, tag.getDimensions()[0]);
        assertTrue(Arrays.equals(floatArr, convertedFloatArr));
    }

    @Test
    public static void testFloat1DArrayNonNativeByteOrder()
    {
        final float[] floatArr = new float[]
            { 1.1f, -3.2f, 1001.5f };
        final ByteOrder nonNativeByteOrder =
                (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(floatArr, nonNativeByteOrder);
        assertEquals(3 * 4 + 4 + 4, taggedArr.length);
        final float[] convertedFloatArr = NativeTaggedArray.tryToFloatArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(Arrays.equals(floatArr, convertedFloatArr));
    }

    @Test
    public static void testFloat2DArrayNativeByteOrder()
    {
        final MDFloatArray floatArr = new MDFloatArray(new float[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final byte[] taggedArr = NativeTaggedArray.toByteArray(floatArr);
        assertEquals(4 * 4 + 2 * 4 + 4, taggedArr.length);
        final MDFloatArray convertedFloatArr = NativeTaggedArray.tryToFloatArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(floatArr.equals(convertedFloatArr));
    }

    @Test
    public static void testFloat2DArrayNonNativeByteOrder()
    {
        final MDFloatArray floatArr = new MDFloatArray(new float[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final ByteOrder nonNativeByteOrder =
            (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(floatArr, nonNativeByteOrder);
        assertEquals(4 * 4 + 2 * 4 + 4, taggedArr.length);
        final MDFloatArray convertedFloatArr = NativeTaggedArray.tryToFloatArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(floatArr.equals(convertedFloatArr));
    }

    @Test
    public static void testDouble1DArrayNativeByteOrder()
    {
        final double[] doubleArr = new double[]
            { 1.1, -3.2, 1001.5 };
        final byte[] taggedArr = NativeTaggedArray.toByteArray(doubleArr);
        assertEquals(3 * 8 + 4 + 4, taggedArr.length);
        final double[] convertedDoubleArr = NativeTaggedArray.tryToDoubleArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(Arrays.equals(doubleArr, convertedDoubleArr));
    }

    @Test
    public static void testDouble1DArrayNonNativeByteOrder()
    {
        final double[] doubleArr = new double[]
            { 1.1, -3.2, 1001.5 };
        final ByteOrder nonNativeByteOrder =
                (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(doubleArr, nonNativeByteOrder);
        assertEquals(3 * 8 + 4 + 4, taggedArr.length);
        final double[] convertedDoubleArr = NativeTaggedArray.tryToDoubleArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(Arrays.equals(doubleArr, convertedDoubleArr));
    }

    @Test
    public static void testDouble2DArrayNativeByteOrder()
    {
        final MDDoubleArray doubleArr = new MDDoubleArray(new double[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final byte[] taggedArr = NativeTaggedArray.toByteArray(doubleArr);
        assertEquals(4 * 8 + 2 * 4 + 4, taggedArr.length);
        final MDDoubleArray convertedDoubleArr = NativeTaggedArray.tryToDoubleArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(doubleArr.equals(convertedDoubleArr));
    }

    @Test
    public static void testDouble2DArrayNonNativeByteOrder()
    {
        final MDDoubleArray doubleArr = new MDDoubleArray(new double[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final ByteOrder nonNativeByteOrder =
            (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(doubleArr, nonNativeByteOrder);
        assertEquals(4 * 8 + 2 * 4 + 4, taggedArr.length);
        final MDDoubleArray convertedDoubleArr = NativeTaggedArray.tryToDoubleArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertTrue(encoding.isFloatingPoint());
        assertFalse(encoding.isInteger());
        assertTrue(doubleArr.equals(convertedDoubleArr));
    }

    @Test
    public static void testShort1DArrayNativeByteOrder()
    {
        final short[] shortArr = new short[]
            { 1, -3, 1001 };
        final byte[] taggedArr = NativeTaggedArray.toByteArray(shortArr);
        assertEquals(3 * 2 + 4 + 4, taggedArr.length);
        final short[] convertedShortArr = NativeTaggedArray.tryToShortArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(2, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(Arrays.equals(shortArr, convertedShortArr));
    }

    @Test
    public static void testShort1DArrayNonNativeByteOrder()
    {
        final short[] shortArr = new short[]
            { 1, -3, 1001 };
        final ByteOrder nonNativeByteOrder =
                (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(shortArr, nonNativeByteOrder);
        assertEquals(3 * 2 + 4 + 4, taggedArr.length);
        final short[] convertedShortArr = NativeTaggedArray.tryToShortArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(2, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(Arrays.equals(shortArr, convertedShortArr));
    }

    @Test
    public static void testShort2DArrayNativeByteOrder()
    {
        final MDShortArray shortArr = new MDShortArray(new short[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final byte[] taggedArr = NativeTaggedArray.toByteArray(shortArr);
        assertEquals(4 * 2 + 2 * 4 + 4, taggedArr.length);
        final MDShortArray convertedShortArr = NativeTaggedArray.tryToShortArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(2, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(shortArr.equals(convertedShortArr));
    }

    @Test
    public static void testShort2DArrayNonNativeByteOrder()
    {
        final MDShortArray shortArr = new MDShortArray(new short[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final ByteOrder nonNativeByteOrder =
            (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(shortArr, nonNativeByteOrder);
        assertEquals(4 * 2 + 2 * 4 + 4, taggedArr.length);
        final MDShortArray convertedShortArr = NativeTaggedArray.tryToShortArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(2, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(shortArr.equals(convertedShortArr));
    }

    @Test
    public static void testInt1DArrayNativeByteOrder()
    {
        final int[] intArr = new int[]
            { 1, -3, 1001 };
        final byte[] taggedArr = NativeTaggedArray.toByteArray(intArr);
        assertEquals(3 * 4 + 4 + 4, taggedArr.length);
        final int[] convertedIntArr = NativeTaggedArray.tryToIntArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(Arrays.equals(intArr, convertedIntArr));
    }

    @Test
    public static void testInt1DArrayNonNativeByteOrder()
    {
        final int[] intArr = new int[]
            { 1, -3, 1001 };
        final ByteOrder nonNativeByteOrder =
                (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(intArr, nonNativeByteOrder);
        assertEquals(3 * 4 + 4 + 4, taggedArr.length);
        final int[] convertedIntArr = NativeTaggedArray.tryToIntArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(Arrays.equals(intArr, convertedIntArr));
    }

    @Test
    public static void testInt2DArrayNativeByteOrder()
    {
        final MDIntArray intArr = new MDIntArray(new int[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final byte[] taggedArr = NativeTaggedArray.toByteArray(intArr);
        assertEquals(4 * 4 + 2 * 4 + 4, taggedArr.length);
        final MDIntArray convertedIntArr = NativeTaggedArray.tryToIntArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(intArr.equals(convertedIntArr));
    }

    @Test
    public static void testInt2DArrayNonNativeByteOrder()
    {
        final MDIntArray intArr = new MDIntArray(new int[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final ByteOrder nonNativeByteOrder =
            (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(intArr, nonNativeByteOrder);
        assertEquals(4 * 4 + 2 * 4 + 4, taggedArr.length);
        final MDIntArray convertedIntArr = NativeTaggedArray.tryToIntArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(4, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(intArr.equals(convertedIntArr));
    }

    @Test
    public static void testLong1DArrayNativeByteOrder()
    {
        final long[] longArr = new long[]
            { 1, -3, 1001 };
        final byte[] taggedArr = NativeTaggedArray.toByteArray(longArr);
        assertEquals(3 * 8 + 4 + 4, taggedArr.length);
        final long[] convertedLongArr = NativeTaggedArray.tryToLongArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(Arrays.equals(longArr, convertedLongArr));
    }

    @Test
    public static void testLong1DArrayNonNativeByteOrder()
    {
        final long[] longArr = new long[]
            { 1, -3, 1001 };
        final ByteOrder nonNativeByteOrder =
                (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(longArr, nonNativeByteOrder);
        assertEquals(3 * 8 + 4 + 4, taggedArr.length);
        final long[] convertedLongArr = NativeTaggedArray.tryToLongArray1D(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(Arrays.equals(longArr, convertedLongArr));
    }

    @Test
    public static void testLong2DArrayNativeByteOrder()
    {
        final MDLongArray longArr = new MDLongArray(new long[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final byte[] taggedArr = NativeTaggedArray.toByteArray(longArr);
        assertEquals(4 * 8 + 2 * 4 + 4, taggedArr.length);
        final MDLongArray convertedLongArr = NativeTaggedArray.tryToLongArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(NativeData.getNativeByteOrder(), encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(longArr.equals(convertedLongArr));
    }

    @Test
    public static void testLong2DArrayNonNativeByteOrder()
    {
        final MDLongArray longArr = new MDLongArray(new long[]
            { 1, 2, 3, 4 }, new int[] { 2, 2 });
        final ByteOrder nonNativeByteOrder =
            (NativeData.getNativeByteOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN;
        final byte[] taggedArr = NativeTaggedArray.toByteArray(longArr, nonNativeByteOrder);
        assertEquals(4 * 8 + 2 * 4 + 4, taggedArr.length);
        final MDLongArray convertedLongArr = NativeTaggedArray.tryToLongArray(taggedArr);
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(taggedArr);
        assertNotNull(encoding);
        assertEquals(nonNativeByteOrder, encoding.getByteOrder());
        assertEquals(8, encoding.getSizeInBytes());
        assertFalse(encoding.isFloatingPoint());
        assertTrue(encoding.isInteger());
        assertTrue(longArr.equals(convertedLongArr));
    }

    private void afterClass()
    {
    }

    private void setUp()
    {
    }

    public static void main(String[] args) throws Throwable
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
        System.out.println("Test class: " + NativeTaggedArrayTests.class.getSimpleName());
        System.out.println();
        NativeData.ensureNativeLibIsLoaded();
        final NativeTaggedArrayTests test = new NativeTaggedArrayTests();
        try
        {
            for (Method m : NativeTaggedArrayTests.class.getMethods())
            {
                final Test testAnnotation = m.getAnnotation(Test.class);
                if (testAnnotation == null || m.getParameterTypes().length > 0)
                {
                    continue;
                }
                System.out.println("Running " + m.getName());
                test.setUp();
                try
                {
                    m.invoke(test);
                } catch (InvocationTargetException wrapperThrowable)
                {
                    final Throwable th = wrapperThrowable.getCause();
                    boolean exceptionFound = false;
                    for (Class<?> expectedExClazz : testAnnotation.expectedExceptions())
                    {
                        if (expectedExClazz == th.getClass())
                        {
                            exceptionFound = true;
                            break;
                        }
                    }
                    if (exceptionFound == false)
                    {
                        throw th;
                    }
                }
            }
            System.out.println("Tests OK!");
        } finally
        {
            test.afterClass();
        }
    }

}
