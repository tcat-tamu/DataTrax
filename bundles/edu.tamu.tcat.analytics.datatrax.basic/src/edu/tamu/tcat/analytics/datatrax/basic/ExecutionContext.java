package edu.tamu.tcat.analytics.datatrax.basic;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;

public class ExecutionContext
{

   public ExecutionContext()
   {
      // TODO Auto-generated constructor stub
   }
   
   /**
    * 
    * @param ears
    * @return
    */
   public AutoCloseable registerListener(DataValueListener ears)
   {
      throw new UnsupportedOperationException();
   }

   public void put(DataValueKey key, Object value)
   {
      
   }
   
   public Object get(DataValueKey key)
   {
      return null;
   }
   
   public static interface DataValueListener {
      
      /**
       * Called when data is made available to the {@link ExecutionContext}.
       *  
       * @param key The {@link DataValueKey} that defines  
       * @param value
       */
      void dataAvailable(DataValueKey key, Object value);

   }
}
