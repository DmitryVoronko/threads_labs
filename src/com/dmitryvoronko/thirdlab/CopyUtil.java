
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
        final int writersCount = dst.length;
        final ArrayList<BlockingQueue<byte[]>> buffers = new ArrayList<>(writersCount);
        for (final OutputStream ignored : dst)
        {
            buffers.add(new ArrayBlockingQueue<>(64));
        }

        final ArrayList<Thread> writers = new ArrayList<>(writersCount);

        final AtomicReference<Throwable> ex = new AtomicReference<>();
        final ThreadGroup group = new ThreadGroup("read-write")
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                ex.set(e);
            }
        };

        final Thread reader = new Thread(group, () ->
        {

            try
            {
                while (true)
                {
                    final byte[] data = new byte[128];
                    final int count = src.read(data, 1, 127);
                    data[0] = (byte) count;
                    for (final BlockingQueue<byte[]> buffer : buffers)
                    {
                        buffer.put(data);
                    }
                    if (count == -1)
                    {
                        break;
                    }

                }
            } catch (final Exception e)
            {
                group.interrupt();
            }
        });
        reader.start();

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
            } catch (final Exception e)
            {
                group.interrupt();
            }
        });
    }

}