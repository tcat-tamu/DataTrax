package edu.tamu.tcat.analytics.datatrax;

public class FactoryUnavailableException extends Exception
{

   public FactoryUnavailableException()
   {
   }

   public FactoryUnavailableException(String message)
   {
      super(message);
   }

   public FactoryUnavailableException(Throwable cause)
   {
      super(cause);
   }

   public FactoryUnavailableException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public FactoryUnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
