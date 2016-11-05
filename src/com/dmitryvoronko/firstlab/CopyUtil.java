package com.dmitryvoronko.firstlab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Dmitry on 31/10/2016.
 */
final class CopyUtil
{
    static void copy(final InputStream src, final OutputStream dst) throws IOException
    {
        final BlockingQueue<byte[]> buffer = new ArrayBlockingQueue<>(64);

        final Thread reader = new Thread(() ->
        {
            final Runnable runnable = new Runnable()
            {
                @Override public synchronized void run()
                {
                    try
                    {
                        while (true)
                        {
                            final byte[] data = new byte[128];
                            final int count = src.read(data, 1, 127);
                            data[0] = (byte) count;
                            buffer.put(data);
                            if (count == -1)
                            {
                                break;
                            }
                        }
                    } catch (final InterruptedException | IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            runnable.run();
        });

        reader.start();

        final Runnable runnable = new Runnable()
        {
            @Override public synchronized void run()
            {
                try
                {
                    while (true)
                    {
                        final byte[] data = buffer.take();
                        if (data[0] == -1)
                        {
                            break;
                        }
                        dst.write(data, 1, data[0]);
                    }
                } catch (final InterruptedException | IOException e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnable.run();
    }
}