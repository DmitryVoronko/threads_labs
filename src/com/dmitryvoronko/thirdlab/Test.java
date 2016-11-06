package com.dmitryvoronko.thirdlab;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Dmitry on 31/10/2016.
 */
public final class Test
{
    private static final int writersCount = 4;

    public static void main(final String[] args) throws IOException
    {
        final Random rnd = new Random(0);
        byte[] testData = new byte[64 * 1024];
        rnd.nextBytes(testData);

        final ByteArrayOutputStream[] byteArrayOutputStreams = new ByteArrayOutputStream[writersCount];

        for (int i = 0; i < writersCount; i++)
        {
            final ByteArrayOutputStream dst = new ByteArrayOutputStream();
            byteArrayOutputStreams[i] = dst;
        }


        CopyUtil.copy(new ByteArrayInputStream(testData), (OutputStream[]) byteArrayOutputStreams);


        for (int i = 0; i < writersCount; i++)
        {
            final byte[] result = byteArrayOutputStreams[i].toByteArray();
            if (!Arrays.equals(testData, result))
            {
                System.out.println(Arrays.toString(result));
                throw new AssertionError("Lab decision wrong!");
            } else
            {
                System.out.println("OK!");
            }
        }
    }
}
