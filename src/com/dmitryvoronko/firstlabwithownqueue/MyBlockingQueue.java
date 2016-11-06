package com.dmitryvoronko.firstlabwithownqueue;

import java.util.concurrent.Semaphore;

/**
 * Created by Dmitry on 07/11/2016.
 */
public final class MyBlockingQueue<T>
{
    private T t;

    private static Semaphore semaphoreGet = new Semaphore(0);
    private static Semaphore semaphorePut = new Semaphore(1);

    public T get()
    {
        try
        {
            semaphoreGet.acquire();
        } catch (final InterruptedException e)
        {
            System.out.println("MyBlockingQueue.get - InterruptedException");
        }

        return t;
    }

    public void put(final T t)
    {
        try
        {
            semaphorePut.acquire();
        } catch (final InterruptedException e)
        {
            System.out.println("MyBlockingQueue.put - InterruptedException");
        }
        this.t = t;
        semaphoreGet.release();
    }

    public void release()
    {
        semaphorePut.release();
    }
}
