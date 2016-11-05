package com.dmitryvoronko.lab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

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
                    try (final InputStream src0 = src)
                    {              // 'src0' for auto-closing
                        while (true)
                        {


                            final byte[] data = new byte[128];        // new data buffer
                            final int count = src0.read(data, 1, 127); // read up to 127 bytes
                            data[0] = (byte) count;             // 0-byte is length-field
                            buffer.put(data);
                            if (count == -1)
                            {
                                break;
                            }
                        }
                    } catch (final Exception ignored)
                    {
                    }
                }
            };
            runnable.run();
        });

        reader.start();
        reader.setPriority(1);


        final Runnable runnable = new Runnable()
        {
            @Override public synchronized void run()
            {
                try (final OutputStream dst0 = dst)
                {      // 'dst0' for auto-closing
                    while (true)
                    {
                        final byte[] data = buffer.take(); // get new data from reader
                        if (data[0] == -1)
                        {
                            break;
                        }  // its last data
                        dst0.write(data, 1, data[0]);
                    }
                } catch (final Exception ignored)
                {
                }  // interrupt writer
            }
        };

        runnable.run();
    }
}