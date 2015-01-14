package edu.tamu.tcat.analytics.datatrax;

import java.util.UUID;


/**
 * WIP: Stub API for listening to various events within the lifecycle of a {@link WorkflowController}. 
 * This will be developed more fully as the initial version of the system is implemented, but 
 * currently serves as a placeholder to hang that functionality.
 * 
 * Called in response to various event within the execution of a {@link WorkflowController}.
 * Note that these events are 
 *
 */
public interface WorkflowObserver
{
   void handleEvent(ProcessingEvent evt);
   
   /**
    * WIP: Stub interface to be developed. 
    *
    */
   public interface ProcessingEvent
   {
      /**
       * @return The action or event that is being reported.
       */
      String getAction();
      
      /**
       * @return The id of the transformer associated with this event.
       */
      String getTransformerId();
      
      
      UUID getDataId();
      
      /**
       * @return The unique identifier associated with a the execution of a transformer within 
       *    the context of processing a single source data. 
       */
      UUID getTransformationTaskId();
   }
}