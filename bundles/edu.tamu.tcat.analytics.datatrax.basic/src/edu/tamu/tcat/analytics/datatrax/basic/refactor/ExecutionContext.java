package edu.tamu.tcat.analytics.datatrax.basic.refactor;

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

   public void put(String transfomerId, String label, Object value)
   {
      
   }
   
   public void get()
   {
      
   }
   public static interface DataValueListener {
      
   }
}
