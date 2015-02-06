package edu.tamu.tcat.analytics.datatrax;

import java.util.function.Supplier;

import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;

/**
 * Responsible for managing the flow of data through a network of {@link Transformer}s 
 * and supplying the declared outputs of the workflow to a {@link ResultsCollector}. The 
 * {@code WorkflowController} is the primary unit for DataTrax clients to interact with in 
 * order to process a data set.
 * 
 * <p>
 * Client should load a {@link WorkflowConfiguration} from an appropriate source and obtain
 * a {@code WorkflowController} (see below for more details on how to achieve this)
 * 
 * 
 *  FIXME too much implementation detail for an API doc. Need to scope this information 
 *       appropriately.
 * <p>
 * 
 * The WorkflowController is responsible for reading a WorkflowConfiguration, instantiating 
 * and configuring Transformer instances and for creating and maintaining the 
 * ExecutorService instances to be used to execute both overall workflow executions and 
 * individual TransformerTasks. The WorkflowController is the main point of entry for 
 * applications to supply data objects to be transformed. For each supplied data-object, the
 * WorkflowController will instantiate a WorkflowExecutor to manage the process of 
 * moving the source data through the transformation workflow defined by the 
 * WorkflowConfiguration.
 * 
 *  <p>
 *  Note that a workflow will instantiate and configure a single Transformer instance 
 *  for each declared transformer. The WorkflowExecutor will then create 
 *  TransformerExecutionController instances that are used to control these transformers 
 *  as the data sources they require becomes available.
 *  
 *  <p>
 *  The WorkflowController will provide hooks for clients to receive notifications about 
 *  the execution of individual Transformers in order to support auditing, performance 
 *  monitoring and other features that need to receive detailed notification of the 
 *  in-progress operation of the data flow.
 */
public interface WorkflowController extends AutoCloseable
{
   /**
    * Executes this workflow for a given input object and supply all results to the
    * provided {@link ResultsCollector}. This method executes asynchronously. Typically it 
    * will return immediately, but it may block if there are no available resources to accept
    * the supplied data. 
    * 
    * @param sourceData The input data instance to be processed by this workflow. Must be thread 
    *       safe.
    * @param collector The {@link ResultsCollector} to be used to accumlulate the outputs
    *       of this workflow.
    */
   <X> void process(Supplier<X> sourceData, ResultsCollector<X> collector);
   
   /**
    * Registers an observer to be notified of various events during the workflow lifecycle.
    * 
    * @param ears The observer to be notified of events.
    * @return An event registration object to be used to remove the attached observer. Note that,
    *       if the returned registration is not closed, the observer will be automatically 
    *       removed when the {@link WorkflowController} is closed.
    */
   AutoCloseable addListener(WorkflowObserver ears);
   // TODO consider adding more specific methods

   /**
    * Blocks current thread until execution of this workflow has completed.
    */
   void join();
}
