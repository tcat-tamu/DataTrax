package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector.TranformationResult;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector.TransformationError;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.WorkflowController;
import edu.tamu.tcat.analytics.datatrax.WorkflowObserver;
import edu.tamu.tcat.analytics.datatrax.basic.ExecutionContext.DataAvailableEvent;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;

/**
 * Responsible for managing the flow of data through a network of {@link Transformer}s and 
 * supplying the declared outputs of the workflow to a {@link ResultsCollector}. The 
 * {@link WorkflowController} is the  primary unit for DataTrax clients to interact with in 
 * order to process a data set.
 * 
 * The Client should load a {@link WorkflowConfiguration} from an appropriate source and obtain 
 * a {@code WorkflowController} (see below for more details on how to achieve this)
 * 
 * The {@code WorkflowController} is responsible for reading a {@code WorkflowConfiguration}, 
 * instantiating and configuring {@code Transformer} instances and for creating and maintaining 
 * the {@link ExecutorService} instances to be used to execute both overall workflow executions 
 * and individual TransformerTaskss. The {@link WorkflowController} is the main point of entry 
 * for applications to supply data objects to be transformed. For each supplied data-object, 
 * the {@link WorkflowController} will instantiate a {@link WorkflowExecutor} to manage the 
 * process of moving the source data through the transformation workflow defined by the 
 * {@link WorkflowConfiguration}.
 * 
 *  Note that a workflow will instantiate and configure a single {@link Transformer} instance 
 *  for each declared transformer. The {@link WorkflowExecutor} will then create 
 *  TransformerExecutionController instances that are used to control these 
 *  transformers as the data sources they require becomes available.
 *  
 *  The {@link WorkflowController} will provide hooks for clients to receive notifications 
 *  about the execution of individual Transformers in order to support auditing, performance 
 *  monitoring and other features that need to receive detailed notification of the 
 *  in-progress operation of the data flow.
 */
public class WorkflowControllerImpl implements WorkflowController
{
   // TODO ensure that this implementation satisifies the above description
   private final static Logger logger = Logger.getLogger(WorkflowControllerImpl.class.getName());

   
   // executor for tasks submitted by individual workflows
   private ExecutorService taskExector;
   
   // executor for running the workflow over a single input data
   private ExecutorService workflowExectorService;
   
   private final DataValueKey inputKey;
   private final WorkflowConfiguration config;
   private final Set<ConfiguredTransformer> transformers;

   private WorkflowControllerImpl(WorkflowConfiguration config, Set<ConfiguredTransformer> transformers)
   {
      this.config = config;
      this.transformers = transformers;
      this.inputKey = config.getInputKey();
      
      taskExector = Executors.newCachedThreadPool();
      workflowExectorService = Executors.newCachedThreadPool();
   }
   
   public static WorkflowControllerImpl create(WorkflowConfiguration config) throws TransformerConfigurationException
   {
      // TODO probably need to tune these to prevent poor thread usage. Need to investigate work-stealing 
      // TODO need to supply better exception
      Set<ConfiguredTransformer> transformers = new HashSet<>();
      Collection<TransformerConfiguration> tConfigs = config.getTransformers();
      for (TransformerConfiguration cfg : tConfigs)
      {
         TransformerRegistration registration = cfg.getRegistration();
         Transformer transformer = registration.instantiate(cfg);
         
         transformers.add(new ConfiguredTransformer(cfg, transformer));
      }
      
      return new WorkflowControllerImpl(config, transformers);
   }
   
   private void execute(Runnable task)
   {
      taskExector.submit(task);
   }

   @Override
   public void close() throws Exception
   {
      // TODO Auto-generated method stub
      taskExector.awaitTermination(10, TimeUnit.SECONDS);
      workflowExectorService.awaitTermination(10, TimeUnit.SECONDS);
      
      throw new UnsupportedOperationException();
      
   }

   private void handleError(ResultsCollector collector, final Exception ex)
   {
      if (collector == null)
         return;
      
      TransformationError err = new TransformationError() {

         @Override
         public Exception getException()
         {
            return ex;
         }
      };
      
      try 
      {
         collector.handleError(err);
      } 
      catch (Exception e)
      {
         e.addSuppressed(ex);
         logger.log(Level.WARNING, "Error notifying result collector of workflow execution error.", e);
      }
   }
   
   @Override
   public <X> void process(X sourceData, ResultsCollector collector)
   {
      Objects.requireNonNull(sourceData, "Null source data input");
      
      workflowExectorService.submit(new Runnable()
      {
         @Override
         public void run()
         {
            // FIXME check to ensure this hasn't been closed
            try 
            {
               WorkflowExecutor workflow = createExecutor(); 
               workflow.process(sourceData, collector);
            }
            catch (Exception ex)
            {
               logger.log(Level.SEVERE, "Failed to execute workflow.", ex);
               handleError(collector, ex);
            }
         }
      });
   }

   @Override
   public AutoCloseable addListener(WorkflowObserver ears)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   private WorkflowExecutor createExecutor() {
      ExecutionContext context = new ExecutionContext();
      
      Set<TransformerController> controllers = new HashSet<>();
      transformers.forEach((cfgTransformer) -> 
            controllers.add(buildTransformerController(context, cfgTransformer)));
      
      return new WorkflowExecutor(context, inputKey, controllers);
   }

   private TransformerController buildTransformerController(ExecutionContext context, ConfiguredTransformer cfgTransformer)
   {
      TransformerConfiguration cfg = cfgTransformer.cfg;
      Transformer transformer = cfgTransformer.transformer;
      
      TransformerController controller = new TransformerController(transformer, cfg, this::execute, context);
      
      AutoCloseable registration = context.registerListener(controller.getKeys(), controller::dataAvailable);
      controller.setListenerRegistration(registration);
      
      return controller;
   }

   public static class ConfiguredTransformer
   {
      public final TransformerConfiguration cfg;
      public final Transformer transformer;
      
      public ConfiguredTransformer(TransformerConfiguration cfg, Transformer transformer)
      {
         this.cfg = cfg;
         this.transformer = transformer;
      }
   }


   /**
    * Responsible for processing a single data instance through the workflow that has been 
    * instantiated by the {@link WorkflowControllerImpl} and for exporting the final data results 
    * defined in the {@code WorkflowConfiguration}. Upon instantiation, the WorkflowExecutor will create a single ExecutionContext that will be used to store the in-process results of different data transformations, along with a TransformerExecutionController corresponding to each Transformer declared in the WorkflowConfiguration. The data transformation process will be initiated by setting the initial source data instance into the ExecutionContext, thereby causing the TransformerExecutionControllers that rely only on this source data instance to activate.
   The WorkflowExecutor is supplied with a Java ExecutorService by the WorkflowController to be used when executing TransformerTasks.

    */
   private class WorkflowExecutor
   {
      private final UUID id;
      private final DataValueKey inputKey;
      private final ExecutionContext context;
      private final Collection<TransformerController> controllers;
      
      private Object inputData;
      private ResultsCollector collector;
      private CountDownLatch outputDataLatch;

      private WorkflowExecutor(ExecutionContext context, DataValueKey inputKey, Set<TransformerController> controllers)
      {
         this.id = UUID.randomUUID();
         this.context = context;
         this.inputKey = inputKey;
         this.controllers = controllers;     // unneeded
      }
      
      void process(Object data, ResultsCollector collector)
      {
         Objects.requireNonNull(collector, "Input data must not be null.");
         Objects.requireNonNull(collector, "No results collector supplied.");
         
         this.inputData = data;
         this.collector = collector;
         
         // register handlers for data to export
         Set<DataValueKey> outputs = config.getDeclaredOutputs();
         outputDataLatch = new CountDownLatch(outputs.size());
         context.registerListener(outputs, this::onDataAvailable);
         
         // stitch together error handling and execution completion
         
         context.put(inputKey, data);
         
         awaitCompletion();
      }

      private void awaitCompletion()
      {
         try 
         {
            outputDataLatch.await();       // TODO provide timeout, support cancellation
            collector.finished();
         }
         catch (Exception ex)
         {
            // handle exceptions from the supplied collector.
            logger.log(Level.WARNING, "Notification of results collector of workflow completion failed. ", ex);
         }
      }
      
      private void onDataAvailable(DataAvailableEvent evt)
      {
         DataValueKey key = evt.getKey();
         TranformationResult result = new TransResultImpl(key, evt.getValue(), inputData);
         try
         {
            collector.handleResult(result);
         }
         catch (Exception ex)
         {
            // handle exceptions from the supplied collector.
            logger.log(Level.WARNING, "Notification of results collector of data available failed [" + key + "]. ", ex);
         }
         
         outputDataLatch.countDown();
      }
   }
   
   private final class TransResultImpl implements TranformationResult
   {
      private final DataValueKey key;
      private final Object value;
      private final Object src;
      
      TransResultImpl(DataValueKey key, Object value, Object src)
      {
         this.key = key;
         this.value = value;
         this.src = src;

      }
      @Override
      public DataValueKey getKey()
      {
         return key;
      }

      @Override
      public Object getValue()
      {
         return value;
      }

      @Override
      public Object getSource()
      {
         return src;
      }
   }
   
   public static interface TaskExecutionService
   {
      void execute(Runnable task);
   }

}
