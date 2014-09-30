package edu.tamu.tcat.analytics.datatrax;

/**
 * Indicates that a {@link TransformerFactory} has not been properly configured. 
 */
public class FactoryConfigurationException extends RuntimeException
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
