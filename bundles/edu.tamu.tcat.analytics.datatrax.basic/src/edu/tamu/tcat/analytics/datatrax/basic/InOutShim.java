package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *  Represents the connection between two adjacent transformers in a workflow.
 *
 */
public class InOutShim<T> implements Supplier<T>, Consumer<T>
{
   private T value;
   
   public InOutShim()
   {
   }

   @Override
   public void accept(T t)
   {
      synchronized (this)
      {
         if (value != null)
            throw new IllegalStateException("A value has already been supplied. This Consumer does not accept multiple inputs.");
         
         value = t;
      }
   }
   
   @Override
   public T get()
   {
      synchronized (this)
      {
         Objects.requireNonNull(value, "The value is not ready.");
         return value;
      }
   }
}
