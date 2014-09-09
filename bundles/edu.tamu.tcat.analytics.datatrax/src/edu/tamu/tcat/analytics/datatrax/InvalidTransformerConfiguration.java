package edu.tamu.tcat.analytics.datatrax;

public class InvalidTransformerConfiguration extends Exception
{

   public InvalidTransformerConfiguration()
   {
   }

   public InvalidTransformerConfiguration(String message)
   {
      super(message);
   }

   public InvalidTransformerConfiguration(Throwable cause)
   {
      super(cause);
   }

   public InvalidTransformerConfiguration(String message, Throwable cause)
   {
      super(message, cause);
   }

   public InvalidTransformerConfiguration(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
