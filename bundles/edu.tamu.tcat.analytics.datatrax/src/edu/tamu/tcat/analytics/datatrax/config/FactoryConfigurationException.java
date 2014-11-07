package edu.tamu.tcat.analytics.datatrax.config;

import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;

/**
 * Indicates that a {@link TransformerRegistration} has not been properly configured. 
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
