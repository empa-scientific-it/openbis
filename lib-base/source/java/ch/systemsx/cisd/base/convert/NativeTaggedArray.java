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

import static ch.systemsx.cisd.base.convert.NativeData.DOUBLE_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.FLOAT_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.INT_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.LONG_SIZE;
import static ch.systemsx.cisd.base.convert.NativeData.SHORT_SIZE;

import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;
import ch.systemsx.cisd.base.mdarray.MDAbstractArray;
import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.base.mdarray.MDLongArray;
import ch.systemsx.cisd.base.mdarray.MDShortArray;

/**
 * A utility class that supports encoding and decoding of arrays of primitive number types to byte
 * arrays such that the characteristics of the number type (float or integer, byte order, element
 * size) and the dimensions are known and can be checked for correctness when converted back to the
 * number type.
 * 
 * @author Bernd Rinn
 */
public class NativeTaggedArray
{

    private final static NativeData.ByteOrder NATIVE_BYTE_ORDER = NativeData.getNativeByteOrder();

    private static final int MAGIC_SIZE = 3;

    private static final int RANK_SIZE = 1;

    private static final int RANK_INDEX = 3;

    private static final int LENGTH_SIZE = 4;

    private static final int LENGTH_INDEX = 4;

    private static final int RANK_1 = 1;

    /**
     * A class to return the array encoding and dimensions of a native tagged array.
     */
    public static class NativeArrayTag
    {
        private final NativeArrayEncoding encoding;

        private final int[] dimensions;

        NativeArrayTag(NativeArrayEncoding encoding, int[] dimensions)
        {
            this.encoding = encoding;
            this.dimensions = dimensions;
        }

        /**
         * Returns the {@link NativeArrayEncoding} of the array.
         */
        public NativeArrayEncoding getEncoding()
        {
            return encoding;
        }

        /**
         * Resurns the dimensions of the array.
         */
        public int[] getDimensions()
        {
            return dimensions;
        }
    }

    /**
     * Returns the array tag of the native tagged array encoded in <var>data</var>, or
     * <code>null</code>, if <var>data</var> does not encode a native tagged array.
     */
    public static NativeArrayTag tryGetArrayTag(byte[] data)
    {
        final NativeArrayEncoding encodingOrNull = NativeArrayEncoding.tryGetEncoding(data);
        if (encodingOrNull == null)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        final int[] dimensions = new int[rank];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, rank,
                encodingOrNull.getByteOrder());
        return new NativeArrayTag(encodingOrNull, dimensions);
    }

    //
    // Float
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(float[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(float[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetFloatEncoding(byteOrder, (byte) FLOAT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + FLOAT_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyFloatToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(MDFloatArray data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(MDFloatArray data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetFloatEncoding(byteOrder, (byte) FLOAT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final byte rank = (byte) data.rank();
        final int headerSize = MAGIC_SIZE + RANK_SIZE + rank * LENGTH_SIZE;
        final float[] flatDataArray = data.getAsFlatArray();
        final byte[] byteArr = new byte[headerSize + FLOAT_SIZE * flatDataArray.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = rank;
        NativeData.copyIntToByte(data.dimensions(), 0, byteArr, LENGTH_INDEX, rank, byteOrder);
        NativeData.copyFloatToByte(flatDataArray, 0, byteArr, headerSize, flatDataArray.length,
                byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a float array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D float array.
     */
    public static float[] tryToFloatArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isInteger() || encoding.getSizeInBytes() != FLOAT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * FLOAT_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final float[] floatData = new float[dimensions[0]];
        NativeData.copyByteToFloat(data, LENGTH_INDEX + LENGTH_SIZE, floatData, 0,
                floatData.length, encoding.getByteOrder());
        return floatData;
    }

    /**
     * Returns the tagged array <var>data</var> as a {@link MDFloatArray} or <code>null</code>, if
     * <var>data</var> is not a tagged (multi-dimensional) float array.
     */
    public static MDFloatArray tryToFloatArray(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isInteger() || encoding.getSizeInBytes() != FLOAT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        final int[] dimensions = new int[rank];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, rank, encoding.getByteOrder());
        final int length = MDAbstractArray.getLength(dimensions);
        final int headerSize = LENGTH_INDEX + rank * LENGTH_SIZE;
        if (length * FLOAT_SIZE + headerSize != data.length)
        {
            return null;
        }
        final float[] intData = new float[length];
        NativeData.copyByteToFloat(data, headerSize, intData, 0, intData.length,
                encoding.getByteOrder());
        return new MDFloatArray(intData, dimensions);
    }

    //
    // Double
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(double[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(double[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetFloatEncoding(byteOrder, (byte) DOUBLE_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + DOUBLE_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyDoubleToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(MDDoubleArray data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(MDDoubleArray data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetFloatEncoding(byteOrder, (byte) DOUBLE_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final byte rank = (byte) data.rank();
        final int headerSize = MAGIC_SIZE + RANK_SIZE + rank * LENGTH_SIZE;
        final double[] flatDataArray = data.getAsFlatArray();
        final byte[] byteArr = new byte[headerSize + DOUBLE_SIZE * flatDataArray.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = rank;
        NativeData.copyIntToByte(data.dimensions(), 0, byteArr, LENGTH_INDEX, rank, byteOrder);
        NativeData.copyDoubleToByte(flatDataArray, 0, byteArr, headerSize, flatDataArray.length,
                byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a double array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D double array.
     */
    public static double[] tryToDoubleArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isInteger() || encoding.getSizeInBytes() != DOUBLE_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * DOUBLE_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final double[] doubleData = new double[dimensions[0]];
        NativeData.copyByteToDouble(data, LENGTH_INDEX + LENGTH_SIZE, doubleData, 0,
                doubleData.length, encoding.getByteOrder());
        return doubleData;
    }

    /**
     * Returns the tagged array <var>data</var> as a {@link MDDoubleArray} or <code>null</code>, if
     * <var>data</var> is not a tagged (multi-dimensional) double array.
     */
    public static MDDoubleArray tryToDoubleArray(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isInteger() || encoding.getSizeInBytes() != DOUBLE_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        final int[] dimensions = new int[rank];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, rank, encoding.getByteOrder());
        final int length = MDAbstractArray.getLength(dimensions);
        final int headerSize = LENGTH_INDEX + rank * LENGTH_SIZE;
        if (length * DOUBLE_SIZE + headerSize != data.length)
        {
            return null;
        }
        final double[] intData = new double[length];
        NativeData.copyByteToDouble(data, headerSize, intData, 0, intData.length,
                encoding.getByteOrder());
        return new MDDoubleArray(intData, dimensions);
    }

    //
    // Short
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(short[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(short[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) SHORT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + SHORT_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyShortToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(MDShortArray data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(MDShortArray data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) SHORT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final byte rank = (byte) data.rank();
        final int headerSize = MAGIC_SIZE + RANK_SIZE + rank * LENGTH_SIZE;
        final short[] flatDataArray = data.getAsFlatArray();
        final byte[] byteArr = new byte[headerSize + SHORT_SIZE * flatDataArray.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = rank;
        NativeData.copyIntToByte(data.dimensions(), 0, byteArr, LENGTH_INDEX, rank, byteOrder);
        NativeData.copyShortToByte(flatDataArray, 0, byteArr, headerSize, flatDataArray.length,
                byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a short array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D short array.
     */
    public static short[] tryToShortArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint()
                || encoding.getSizeInBytes() != SHORT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * SHORT_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final short[] shortData = new short[dimensions[0]];
        NativeData.copyByteToShort(data, LENGTH_INDEX + LENGTH_SIZE, shortData, 0,
                shortData.length, encoding.getByteOrder());
        return shortData;
    }

    /**
     * Returns the tagged array <var>data</var> as a {@link MDShortArray} or <code>null</code>, if
     * <var>data</var> is not a tagged (multi-dimensional) short array.
     */
    public static MDShortArray tryToShortArray(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint()
                || encoding.getSizeInBytes() != SHORT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        final int[] dimensions = new int[rank];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, rank, encoding.getByteOrder());
        final int length = MDAbstractArray.getLength(dimensions);
        final int headerSize = LENGTH_INDEX + rank * LENGTH_SIZE;
        if (length * SHORT_SIZE + headerSize != data.length)
        {
            return null;
        }
        final short[] intData = new short[length];
        NativeData.copyByteToShort(data, headerSize, intData, 0, intData.length,
                encoding.getByteOrder());
        return new MDShortArray(intData, dimensions);
    }

    //
    // Int
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(int[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(int[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) INT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + INT_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyIntToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(MDIntArray data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(MDIntArray data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) INT_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final byte rank = (byte) data.rank();
        final int headerSize = MAGIC_SIZE + RANK_SIZE + rank * LENGTH_SIZE;
        final int[] flatDataArray = data.getAsFlatArray();
        final byte[] byteArr = new byte[headerSize + INT_SIZE * flatDataArray.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = rank;
        NativeData.copyIntToByte(data.dimensions(), 0, byteArr, LENGTH_INDEX, rank, byteOrder);
        NativeData.copyIntToByte(flatDataArray, 0, byteArr, headerSize, flatDataArray.length,
                byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as an int array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D int array.
     */
    public static int[] tryToIntArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint() || encoding.getSizeInBytes() != INT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * INT_SIZE + LENGTH_INDEX + LENGTH_SIZE != data.length)
        {
            return null;
        }
        final int[] intData = new int[dimensions[0]];
        NativeData.copyByteToInt(data, LENGTH_INDEX + LENGTH_SIZE, intData, 0, intData.length,
                encoding.getByteOrder());
        return intData;
    }

    /**
     * Returns the tagged array <var>data</var> as a {@link MDIntArray} or <code>null</code>, if
     * <var>data</var> is not a tagged (multi-dimensional) int array.
     */
    public static MDIntArray tryToIntArray(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint() || encoding.getSizeInBytes() != INT_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        final int[] dimensions = new int[rank];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, rank, encoding.getByteOrder());
        final int length = MDAbstractArray.getLength(dimensions);
        final int headerSize = LENGTH_INDEX + rank * LENGTH_SIZE;
        if (length * INT_SIZE + headerSize != data.length)
        {
            return null;
        }
        final int[] intData = new int[length];
        NativeData.copyByteToInt(data, headerSize, intData, 0, intData.length,
                encoding.getByteOrder());
        return new MDIntArray(intData, dimensions);
    }

    //
    // Long
    //

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(long[] data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(long[] data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) LONG_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final int headerSize = MAGIC_SIZE + RANK_SIZE + 1 * LENGTH_SIZE;
        final byte[] byteArr = new byte[headerSize + LONG_SIZE * data.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = RANK_1;
        NativeData.copyIntToByte(new int[]
            { data.length }, 0, byteArr, LENGTH_INDEX, 1, byteOrder);
        NativeData.copyLongToByte(data, 0, byteArr, headerSize, data.length, byteOrder);
        return byteArr;
    }

    /**
     * Converts <var>data</var> into a tagged array in native byte order.
     */
    public static byte[] toByteArray(MDLongArray data)
    {
        return toByteArray(data, NATIVE_BYTE_ORDER);
    }

    /**
     * Converts <var>data</var> into a tagged array in given byte order.
     */
    public static byte[] toByteArray(MDLongArray data, ByteOrder byteOrder)
    {
        final byte[] magic =
                NativeArrayEncoding.tryGetIntEncoding(byteOrder, (byte) LONG_SIZE).getMagic();
        assert magic.length == MAGIC_SIZE;
        final byte rank = (byte) data.rank();
        final int headerSize = MAGIC_SIZE + RANK_SIZE + rank * LENGTH_SIZE;
        final long[] flatDataArray = data.getAsFlatArray();
        final byte[] byteArr = new byte[headerSize + LONG_SIZE * flatDataArray.length];
        System.arraycopy(magic, 0, byteArr, 0, MAGIC_SIZE);
        byteArr[RANK_INDEX] = rank;
        NativeData.copyIntToByte(data.dimensions(), 0, byteArr, LENGTH_INDEX, rank, byteOrder);
        NativeData.copyLongToByte(flatDataArray, 0, byteArr, headerSize, flatDataArray.length,
                byteOrder);
        return byteArr;
    }

    /**
     * Returns the tagged array <var>data</var> as a long array or <code>null</code>, if
     * <var>data</var> is not a tagged 1D long array.
     */
    public static long[] tryToLongArray1D(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint()
                || encoding.getSizeInBytes() != LONG_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        if (rank != 1)
        {
            return null;
        }
        final int[] dimensions = new int[1];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, 1, encoding.getByteOrder());
        if (dimensions[0] * LONG_SIZE + LENGTH_INDEX + 1 * LENGTH_SIZE != data.length)
        {
            return null;
        }
        final long[] longData = new long[dimensions[0]];
        NativeData.copyByteToLong(data, LENGTH_INDEX + LENGTH_SIZE, longData, 0, longData.length,
                encoding.getByteOrder());
        return longData;
    }

    /**
     * Returns the tagged array <var>data</var> as a {@link MDLongArray} or <code>null</code>, if
     * <var>data</var> is not a tagged (multi-dimensional) long array.
     */
    public static MDLongArray tryToLongArray(byte[] data)
    {
        final NativeArrayEncoding encoding = NativeArrayEncoding.tryGetEncoding(data);
        if (encoding == null || encoding.isFloatingPoint()
                || encoding.getSizeInBytes() != LONG_SIZE)
        {
            return null;
        }
        final int rank = data[RANK_INDEX];
        final int[] dimensions = new int[rank];
        NativeData.copyByteToInt(data, LENGTH_INDEX, dimensions, 0, rank, encoding.getByteOrder());
        final int length = MDAbstractArray.getLength(dimensions);
        final int headerSize = LENGTH_INDEX + rank * LENGTH_SIZE;
        if (length * LONG_SIZE + headerSize != data.length)
        {
            return null;
        }
        final long[] longData = new long[length];
        NativeData.copyByteToLong(data, headerSize, longData, 0, longData.length,
                encoding.getByteOrder());
        return new MDLongArray(longData, dimensions);
    }

}
