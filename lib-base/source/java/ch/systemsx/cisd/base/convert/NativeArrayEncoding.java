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

import ch.systemsx.cisd.base.convert.NativeData.ByteOrder;

/**
 * An enum for encoding array of numbers (integer or float) in native (host) format.
 * 
 * @author Bernd Rinn
 */
public enum NativeArrayEncoding
{
    INT8_NATIVE(false, (byte) 1, NativeData.ByteOrder.NATIVE),

    INT16_LITTLE_ENDIAN(false, (byte) 2, NativeData.ByteOrder.LITTLE_ENDIAN),

    INT32_LITTLE_ENDIAN(false, (byte) 4, NativeData.ByteOrder.LITTLE_ENDIAN),

    INT64_LITTLE_ENDIAN(false, (byte) 8, NativeData.ByteOrder.LITTLE_ENDIAN),

    INT16_BIG_ENDIAN(false, (byte) 2, NativeData.ByteOrder.BIG_ENDIAN),

    INT32_BIG_ENDIAN(false, (byte) 4, NativeData.ByteOrder.BIG_ENDIAN),

    INT64_BIG_ENDIAN(false, (byte) 8, NativeData.ByteOrder.BIG_ENDIAN),

    FLOAT32_LITTLE_ENDIAN(true, (byte) 4, NativeData.ByteOrder.LITTLE_ENDIAN),

    FLOAT64_LITTLE_ENDIAN(true, (byte) 8, NativeData.ByteOrder.LITTLE_ENDIAN),

    FLOAT32_BIG_ENDIAN(true, (byte) 4, NativeData.ByteOrder.BIG_ENDIAN),

    FLOAT64_BIG_ENDIAN(true, (byte) 8, NativeData.ByteOrder.BIG_ENDIAN),

    ;

    private static final int MIN_ENCODING_HEADER_SIZE = 8;

    private static final int CHAR_N = 78;

    private static final int CHAR_B = 66;

    private static final int CHAR_L = 76;

    private static final int CHAR_I = 73;

    private static final int CHAR_F = 70;

    private boolean floatingPoint;

    private ByteOrder byteOrder;

    private byte sizeInBytes;

    private byte[] magic;

    NativeArrayEncoding(boolean floatingPoint, byte sizeInBytes, NativeData.ByteOrder byteOrder)
    {
        this.floatingPoint = floatingPoint;
        this.byteOrder = byteOrder;
        this.sizeInBytes = sizeInBytes;
        this.magic =
                new byte[]
                    {
                            (byte) (floatingPoint ? CHAR_F : CHAR_I),
                            (byte) ((byteOrder == ByteOrder.LITTLE_ENDIAN) ? CHAR_L
                                    : (byteOrder == ByteOrder.BIG_ENDIAN) ? CHAR_B : CHAR_N),
                            sizeInBytes };
    }

    /**
     * Returns <code>true</code>, if the encoded array is a float array.
     */
    public boolean isFloatingPoint()
    {
        return floatingPoint;
    }

    /**
     * Returns <code>true</code>, if the encoded array is an int array.
     */
    public boolean isInteger()
    {
        return floatingPoint == false;
    }

    /**
     * Returns the byte order of the array.
     */
    public NativeData.ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    /**
     * Returns the size of one element in bytes.
     */
    public byte getSizeInBytes()
    {
        return sizeInBytes;
    }

    byte[] getMagic()
    {
        return magic;
    }

    static NativeArrayEncoding tryGetIntEncoding(ByteOrder byteOrder, byte sizeInBytes)
    {
        assert byteOrder != null;
        if (sizeInBytes == 1 && byteOrder == ByteOrder.NATIVE)
        {
            return INT8_NATIVE;
        } else if (sizeInBytes == 2)
        {
            return (byteOrder == ByteOrder.LITTLE_ENDIAN) ? INT16_LITTLE_ENDIAN : INT16_BIG_ENDIAN;
        } else if (sizeInBytes == 4)
        {
            return (byteOrder == ByteOrder.LITTLE_ENDIAN) ? INT32_LITTLE_ENDIAN : INT32_BIG_ENDIAN;

        } else if (sizeInBytes == 8)
        {
            return (byteOrder == ByteOrder.LITTLE_ENDIAN) ? INT64_LITTLE_ENDIAN : INT64_BIG_ENDIAN;
        }
        return null;
    }

    static NativeArrayEncoding tryGetFloatEncoding(ByteOrder byteOrder, byte sizeInBytes)
    {
        assert byteOrder != null;
        if (sizeInBytes == 4)
        {
            return (byteOrder == ByteOrder.LITTLE_ENDIAN) ? FLOAT32_LITTLE_ENDIAN
                    : FLOAT32_BIG_ENDIAN;
        } else if (sizeInBytes == 8)
        {
            return (byteOrder == ByteOrder.LITTLE_ENDIAN) ? FLOAT64_LITTLE_ENDIAN
                    : FLOAT64_BIG_ENDIAN;
        }
        return null;
    }

    /**
     * Returns the encoding for the given <var>byteArr</var>, or <code>null</code>, if
     * <var>byteArr</var> is not an encoded array.
     */
    public static NativeArrayEncoding tryGetEncoding(byte[] byteArr)
    {
        if (byteArr.length < MIN_ENCODING_HEADER_SIZE)
        {
            return null;
        }
        final ByteOrder byteOrder =
                (byteArr[1] == CHAR_L) ? ByteOrder.LITTLE_ENDIAN
                        : (byteArr[1] == CHAR_B) ? ByteOrder.BIG_ENDIAN : null;
        if (byteOrder == null)
        {
            return null;
        }
        if (byteArr[0] == CHAR_F)
        {
            return tryGetFloatEncoding(byteOrder, byteArr[2]);
        } else if (byteArr[0] == CHAR_I)
        {
            return tryGetIntEncoding(byteOrder, byteArr[2]);
        }
        return null;
    }
}