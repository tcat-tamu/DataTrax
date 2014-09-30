package edu.tamu.tcat.analytics.datatrax;

public class WorkflowConfigurationException extends Exception
{

   public WorkflowConfigurationException()
   {
   }

   public WorkflowConfigurationException(String message)
   {
      super(message);
   }

   public WorkflowConfigurationException(Throwable cause)
   {
      super(cause);
   }

   public WorkflowConfigurationException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public WorkflowConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
   {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
