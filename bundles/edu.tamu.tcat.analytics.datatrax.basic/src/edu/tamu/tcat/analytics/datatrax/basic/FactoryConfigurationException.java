package edu.tamu.tcat.analytics.datatrax.basic;

public class FactoryConfigurationException extends Exception
{

   public FactoryConfigurationException()
   {
   }

   public FactoryConfigurationException(String message)
   {
      super(message);
   }

   public FactoryConfigurationException(Throwable cause)
   {
      super(cause);
   }

   public FactoryConfigurationException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public FactoryConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
