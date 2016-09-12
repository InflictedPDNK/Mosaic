package org.pdnk.canvaprocessor.Common;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Extended version of Runnable extending for generic parameter
 * @param <T> incoming parameter type
 */
public interface ParametricRunnable<T>
{
    void run(T param);
}