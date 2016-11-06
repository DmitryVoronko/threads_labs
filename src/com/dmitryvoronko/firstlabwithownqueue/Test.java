package com.dmitryvoronko.firstlabwithownqueue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Dmitry on 31/10/2016.
 */
public final class Test
{
    public static void main(final String[] args) throws IOException
    {
        final Random rnd = new Random(0);
        final byte[] testData = new byte[64 * 1024];
        rnd.nextBytes(testData);
        try (ByteArrayOutputStream dst = new ByteArrayOutputStream())
        {
            try (ByteArrayInputStream src = new ByteArrayInputStream(testData))
            {
                CopyUtil.copy(src, dst);
            }
            if (!Arrays.equals(testData, dst.toByteArray()))
            {
                throw new AssertionError("Lab decision wrong!");
            } else
            {
                System.out.println("OK!");
            }
        }

    }
}
