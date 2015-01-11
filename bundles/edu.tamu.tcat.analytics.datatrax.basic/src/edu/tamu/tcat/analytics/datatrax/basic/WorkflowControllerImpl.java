package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.WorkflowController;
import edu.tamu.tcat.analytics.datatrax.WorkflowObserver;
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
   
   // executor for tasks submitted by individual workflows
   private ExecutorService taskExector;
   
   // executor for running the workflow over a single input data
   private ExecutorService workflowExectorService;
   
   private final DataValueKey inputKey;
   private final WorkflowConfiguration config;
   private final Set<ConfiguredTransformer> transformers;

   private WorkflowControllerImpl(WorkflowConfiguration config, Set<ConfiguredTransformer> transformers)
   {
      // TODO add
      this.config = config;
      this.transformers = transformers;
      this.inputKey = config.getInputKey();
      
      taskExector = Executors.newCachedThreadPool();
      workflowExectorService = Executors.newCachedThreadPool();
   }
   
   public static WorkflowControllerImpl create(WorkflowConfiguration config) throws TransformerConfigurationException
   {
      // TODO probably need to tune these to prevent poor thread usage. Need to investigate work-stealing 
      
      Set<ConfiguredTransformer> transformers = new HashSet<>();
      Collection<TransformerConfiguration> tConfigs = config.getTransformers();
      for (TransformerConfiguration cfg : tConfigs)
      {
         TransformerRegistration registration = cfg.getRegistration();
         Transformer transformer = registration.instantiate();
         transformer.configure(getParams(cfg));                         // TODO create a parameter bag or something similar?
         
         transformers.add(new ConfiguredTransformer(cfg, transformer));
      }
      
      return new WorkflowControllerImpl(config, transformers);
   }
   
   private static Map<String, Object> getParams(TransformerConfiguration cfg)
   {
      Map<String, Object> params = new HashMap<>();
      for (String key : cfg.getDefinedParameters())
      {
         params.put(key, cfg.getParameter(key));
      }
      
      return params;
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

   @Override
   public <X> void process(X sourceData, ResultsCollector collector)
   {
      @SuppressWarnings("resource")    // temporary reference
      final WorkflowControllerImpl exec = this;
      workflowExectorService.submit(new Runnable()
      {
         @Override
         public void run()
         {
            // FIXME check to ensure this hasn't been closed
            
            WorkflowExecutor workflow = new WorkflowExecutor(exec.inputKey, exec::execute, transformers);
            workflow.process(sourceData, collector);
         }
      });
   }

   @Override
   public AutoCloseable addListener(WorkflowObserver ears)
   {
      // TODO Auto-generated method stub
      return null;
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
   private static class WorkflowExecutor
   {
      private final UUID id;
      private final DataValueKey inputKey;
      private final ExecutionContext context;
      private final Collection<TransformerController> controllers;

      private Set<ConfiguredTransformer> transformers;

      public WorkflowExecutor(DataValueKey inputKey, TaskExecutionService exec, Set<ConfiguredTransformer> transformers)
      {
         this.inputKey = inputKey;
         this.id = UUID.randomUUID();
         this.context = new ExecutionContext();
         
         this.controllers = new HashSet<>();
         for (ConfiguredTransformer cfgTransformer : transformers)
         {
            TransformerConfiguration cfg = cfgTransformer.cfg;
            Transformer transformer = cfgTransformer.transformer;
            
            TransformerController controller = new TransformerController(transformer, cfg, exec, context);
            controller.activate();
            
            AutoCloseable registration = context.registerListener(controller.getKeys(), controller::dataAvailable);
            controller.setListenerRegistration(registration);
            
            controllers.add(controller);
         }
      }

      void process(Object data, ResultsCollector collector)
      {
         // TODO stitch results collector to data output handlers
         context.put(inputKey, data);
      }
   }
   
   
   public static interface TaskExecutionService
   {
      void execute(Runnable task);
   
   }


}
