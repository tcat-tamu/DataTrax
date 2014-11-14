package edu.tamu.tcat.analytics.datatrax.basic.refactor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.tamu.tcat.analytics.datatrax.ResultsCollector;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.WorkflowController;
import edu.tamu.tcat.analytics.datatrax.WorkflowObserver;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;

public class WorkflowControllerImpl implements WorkflowController
{
   // executor for tasks submitted by individual workflows
   private ExecutorService taskExector;
   
   // executor for running the workflow over a single input data
   private ExecutorService workflowExectorService;
   
   private final WorkflowConfiguration config;
   private final Set<ConfiguredTransformer> transformers;

   public WorkflowControllerImpl(WorkflowConfiguration config, Set<ConfiguredTransformer> transformers)
   {
      // TODO add
      this.config = config;
      this.transformers = transformers;
      
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
         transformer.configure(getParams(cfg));
         
         transformers.add(new ConfiguredTransformer(cfg, transformer));
      }
      
      return new WorkflowControllerImpl(config, transformers);
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
            
            WorkflowExecutor workflow = new WorkflowExecutor(exec::execute, transformers);
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
   
   /**
    * Responsible for processing a single data instance through the workflow that has been 
    * instantiated by the {@link WorkflowControllerImpl} and for exporting the final data results 
    * defined in the {@code WorkflowConfiguration}. Upon instantiation, the WorkflowExecutor will create a single ExecutionContext that will be used to store the in-process results of different data transformations, along with a TransformerExecutionController corresponding to each Transformer declared in the WorkflowConfiguration. The data transformation process will be initiated by setting the initial source data instance into the ExecutionContext, thereby causing the TransformerExecutionControllers that rely only on this source data instance to activate.
   The WorkflowExecutor is supplied with a Java ExecutorService by the WorkflowController to be used when executing TransformerTasks.

    */
   private static class WorkflowExecutor
   {
      private final UUID id;
      private final ExecutionContext context;
      private final Collection<TransformerController> controllers;

      private Set<ConfiguredTransformer> transformers;

      public WorkflowExecutor(TaskExecutionService exec, Set<ConfiguredTransformer> transformers)
      {
         this.id = UUID.randomUUID();
         this.context = new ExecutionContext();
         
         this.controllers = new HashSet<>();
         for (ConfiguredTransformer cfgTransformer : transformers)
         {
            TransformerConfiguration cfg = cfgTransformer.cfg;
            Transformer transformer = cfgTransformer.transformer;
            
            TransformerController controller = new TransformerController(transformer, cfg, exec, context);
            AutoCloseable registration = context.registerListener(controller);
            controller.setListenerRegistration(registration);
            
            controllers.add(controller);
         }
      }

      void process(Object data, ResultsCollector collector)
      {
         // TODO stitch results collector to data output handlers
         context.put(null, data);
      }
   }
   
   
   public static interface TaskExecutionService
   {
      void execute(Runnable task);
   
   }


}
