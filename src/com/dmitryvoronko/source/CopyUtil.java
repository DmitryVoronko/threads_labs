package com.dmitryvoronko.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Dmitry on 31/10/2016.
 */
public final class CopyUtil
{

    public static void copy(final InputStream src, final OutputStream dst) throws IOException
    {
        // reader-to-writer byte[]-channel
        final BlockingQueue<byte[]> buffer = new ArrayBlockingQueue<>(64);
        // exception-channel from reader/writer threads?
        final AtomicReference<Throwable> ex = new AtomicReference<>();
        final ThreadGroup group = new ThreadGroup("read-write")
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                ex.set(e);
            }
        };
        // reader from 'src'
        final Thread reader = new Thread(group, () ->
        {

            try (final InputStream src0 = src)
            {              // 'src0' for auto-closing
                while (true)
                {
                    System.out.println("Read");
                    final byte[] data = new byte[128];        // new data buffer
                    final int count = src.read(data, 1, 127); // read up to 127 bytes
                    data[0] = (byte) count;             // 0-byte is length-field
                    buffer.put(data);                   // send to writer
                    if (count == -1)
                    {
                        break;
                    }

                }
            } catch (final Exception e)
            {
                group.interrupt();
            }  // interrupt writer
        });
        reader.start();
        // writer to 'dst'
        final Thread writer = new Thread(group, () ->
        {
            try (final OutputStream dst0 = dst)
            {      // 'dst0' for auto-closing
                while (true)
                {
                    System.out.println("Write");
                    final byte[] data = buffer.take(); // get new data from reader
                    if (data[0] == -1)
                    {
                        break;
                    }  // its last data
                    dst.write(data, 1, data[0]); //

                }
            } catch (final Exception e)
            {
                group.interrupt();
            }  // interrupt writer
        });
        writer.start();
        // wait to complete read/write operations
        try
        {
            reader.join(); // wait for reader
            writer.join(); // wait for writer
        } catch (final InterruptedException e)
        {
            throw new IOException(e);
        }
        if (ex.get() != null)
        {
            throw new IOException(ex.get());
        }
    }
}