package net.jpountz.lz4;

/*
 * Copyright 2025 Jonas Konrad and the lz4-java contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.jpountz.lz4.LZ4Constants.MIN_MATCH;
import static org.junit.Assert.assertArrayEquals;

@RunWith(Parameterized.class)
public class OutOfBoundsTest {
    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{LZ4Factory.fastestInstance().fastDecompressor()},
                new Object[]{LZ4Factory.fastestJavaInstance().fastDecompressor()},
                new Object[]{LZ4Factory.nativeInstance().fastDecompressor()},
                new Object[]{LZ4Factory.safeInstance().fastDecompressor()},
                new Object[]{LZ4Factory.unsafeInstance().fastDecompressor()}
        );
    }

    @Parameterized.Parameter
    public LZ4FastDecompressor fastDecompressor;

    @Test
    public void test() {
        byte[] output = new byte[2055];
        for (int i = 0; i < 1000000; i++) {
            try {
                fastDecompressor.decompress(new byte[]{
                        (byte) 0xf0,
                        -1, -1, -1, -1, -1, -1, -1, -1, 0
                }, output);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
    }

    @Test
    public void testMatchLenOverflow() {
        byte[] input = new byte[]{
                (byte) 0xf0,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 0, 0
        };
        byte[] output = new byte[2055];
        try {
            fastDecompressor.decompress(input, output);
        } catch (LZ4Exception ignored) {
            // Expected
        }
    }

    @Test
    public void testCopyBeyondOutput() {
        // Test with different combinations of dec and len values
        for (int dec = 0; dec < 14; dec++) {
            for (int len = dec; len < 14; len++) {
                byte[] compressed = new byte[]{
                    // padding frame (14 bytes)
                    (byte) 0xe0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    // copy len bytes (+ 4 MIN_MATCH) from -dec
                    (byte) len, (byte) dec, 0,
                    // padding frame (12 bytes)
                    (byte) 0xc0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                };
                byte[] output = new byte[14 + MIN_MATCH + len + MIN_MATCH + 12];
                Arrays.fill(output, (byte) 0x77);

                try {
                    fastDecompressor.decompress(compressed, output);
                    // should be all zero
                    assertArrayEquals("Failed for dec=" + dec + ", len=" + len,
                                    new byte[output.length], output);
                } catch (LZ4Exception ignored) {
                    // Some decompressor implementations may throw instead, which is also acceptable
                }
            }
        }
    }
}
