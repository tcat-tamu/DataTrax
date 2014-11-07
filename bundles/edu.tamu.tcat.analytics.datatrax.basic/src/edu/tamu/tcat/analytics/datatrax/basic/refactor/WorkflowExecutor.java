package edu.tamu.tcat.analytics.datatrax.basic.refactor;

import java.io.UncheckedIOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.Transformer;

/**
 * Responsible for processing a single data instance through the workflow that has been 
 * instantiated by the {@link WorkflowController} and for exporting the final data results 
 * defined in the {@code WorkflowConfiguration}. Upon instantiation, the WorkflowExecutor will create a single ExecutionContext that will be used to store the in-process results of different data transformations, along with a TransformerExecutionController corresponding to each Transformer declared in the WorkflowConfiguration. The data transformation process will be initiated by setting the initial source data instance into the ExecutionContext, thereby causing the TransformerExecutionControllers that rely only on this source data instance to activate.
The WorkflowExecutor is supplied with a Java ExecutorService by the WorkflowController to be used when executing TransformerTasks.

 */
public class WorkflowExecutor
{

   public WorkflowExecutor()
   {
      // TODO Auto-generated constructor stub
   }

   public 
   public static class WorkflowDefinition
   {
      public Set<Transformer> getTransformers()
      {
         throw new UnsupportedOperationException();
      }
      
      public Set<DataValueKey> getDeclaredOuputs() 
      {
         throw new UnsupportedOperationException();
      }
      
      public TaskExecutionService getExecutionService()
      {
         throw new UnsupportedOperationException();
      }
   }
}
