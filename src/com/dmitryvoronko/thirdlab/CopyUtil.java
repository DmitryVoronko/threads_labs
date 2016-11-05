package com.dmitryvoronko.thirdlab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Dmitry on 31/10/2016.
 */
final class CopyUtil
{

    static void copy(final InputStream src, final OutputStream ... dst) throws IOException
    {
        // reader-to-writer byte[]-channel
        final int writersCount = dst.length;
        final ArrayList<BlockingQueue<byte[]>> buffers = new ArrayList<>(writersCount);
        for (int i = 0; i < writersCount; i++)
        {
            buffers.add(new ArrayBlockingQueue<>(64));
        }

        final ArrayList<Thread> writers = new ArrayList<>(writersCount);

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

            try
            {              // 'src0' for auto-closing
                while (true)
                {
                    System.out.println("Read");
                    final byte[] data = new byte[128];        // new data buffer
                    final int count = src.read(data, 1, 127); // read up to 127 bytes
                    data[0] = (byte) count;             // 0-byte is length-field
                    for (final BlockingQueue<byte[]> buffer : buffers)
                    {
                        buffer.put(data);
                    }                 // send to writer
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
        for (int i = 0; i < writersCount; i++)
        {
            final Thread writer = createWriterThread(group, dst[i], buffers.get(i));
            writer.start();
            writers.add(writer);
        }
        try
        {
            reader.join();
            for (final Thread writer : writers)
            {
                writer.join();
            }
        } catch (final InterruptedException e)
        {
            throw new IOException(e);
        }
        if (ex.get() != null)
        {
            throw new IOException(ex.get());
        }
    }

    private static Thread createWriterThread(final ThreadGroup group, final OutputStream dst,
                                             final BlockingQueue<byte[]> buffer)
    {
        return new Thread(group, () ->
            {
                try
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
    }

}