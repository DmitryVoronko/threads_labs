package com.dmitryvoronko.secondlab;

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
    public static void main(String[] args) throws IOException
    {
        Random rnd = new Random(0);
        byte[] testData = new byte[64 * 1024];
//        * 1024
        rnd.nextBytes(testData);
        ByteArrayOutputStream dst = new ByteArrayOutputStream();
        CopyUtil.copy(new ByteArrayInputStream(testData), dst);
        if (!Arrays.equals(testData, dst.toByteArray()))
        {
            throw new AssertionError("Lab decision wrong!");
        } else
        {
            System.out.println("OK!");
        }
    }
}
