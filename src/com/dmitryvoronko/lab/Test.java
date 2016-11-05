package com.dmitryvoronko.lab;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Dmitry on 31/10/2016.
 */
public class Test
{
    public static void main(final String[] args) throws IOException
    {
        final Random rnd = new Random(0);
        final byte[] testData = new byte[64 * 1024];
        rnd.nextBytes(testData);
        final ByteArrayOutputStream dst = new ByteArrayOutputStream();
        CopyUtil.copy(new ByteArrayInputStream(testData), dst);
        if (!Arrays.equals(testData, dst.toByteArray()))
        {
            throw new AssertionError("Lab decision wrong!");
        } else
        {
            System.out.println("OK!");
//            displayData(testData);
//            final byte[] resultData = dst.toByteArray();
//            displayData(resultData);

        }

    }

    private static void displayData(byte[] resultData)
    {
        System.out.println("length = " + resultData.length);
        for (int i = 0; i < resultData.length; i++)
        {
            System.out.print("data = " + resultData[i]);
        }
        System.out.println("");
    }
}
