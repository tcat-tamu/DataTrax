package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.FactoryConfiguration;
import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.WorkflowConfigurationException;
import edu.tamu.tcat.analytics.datatrax.WorkflowFactory;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtTransformerFactoryDefinition;

/**
 * An implementation 
 *
 * <p>
 * Note that this class is not thread safe and is intended to be confined to a single thread
 * while constructing a {@link DataTransformWorkflow}. 
 *
 */
public class WorkflowFactoryImpl implements WorkflowFactory
{

   private ExtPointTranformerFactoryRegistry registry;
   
   public WorkflowFactoryImpl()
   {
      // TODO Auto-generated constructor stub
   }
   
   public void bindFactoryRegistry(ExtPointTranformerFactoryRegistry registry)
   {
      this.registry = registry;
   }
   
   
   @Override
   public <IN, OUT> DataTransformWorkflow<IN, OUT> create(WorkflowConfiguration config) throws WorkflowConfigurationException
   {
      // TODO figure out how to create the initial data source provider
      
      List<Transformer<?, ?>> transformers = new ArrayList<>();
      Class<?> sourceType = config.sourceType;    // the Java type that will be the input source of the next transformer
      for (FactoryConfiguration cfg : config.factories)
      {
         Transformer<?, ?> transformer = loadTransformer(cfg, sourceType);
         transformers.add(transformer);
         sourceType = transformer.getOutputType();
      }
      
      return new WorkflowImpl<>(Collections.unmodifiableList(transformers));
   }

   /**
    * 
    * @param config The configuration object that defines the transformer to be loaded.
    * @param expectInputType The input type that the defined transformer is expected to 
    *    accept as input. This will be the same as the output type of the preceding transformer 
    *    in the workflow pipeline being assembled.
    * @return The transformer as specified and configured by the supplied configuration definition. 
    * @throws WorkflowConfigurationException If there are configuration errors that prevent 
    *    the specified transformer from being loaded.
    * 
    */
   private Transformer<?, ?> loadTransformer(FactoryConfiguration config, Class<?> expectInputType) throws WorkflowConfigurationException
   {
      String factoryId = config.factoryId;
      if (!registry.isRegistered(factoryId))
         throw new WorkflowConfigurationException("Invalid worflow configuration. No factory is registered for [" + factoryId + "]");
            
      try
      {
         ExtTransformerFactoryDefinition factory = registry.getFactory(factoryId);
         if (!factory.canAccept(expectInputType))
            throw new WorkflowConfigurationException("Invalid worflow configuration. The "
                  + "requested transformer [" + factoryId + " - " + factory.getTitle() + "] "
                  + "does not accept input of type [" + expectInputType + "]. "
                  + "Requires [" + factory.getDeclaredSourceType() + "]");
         
         Transformer<Object, Object> transformer = factory.instantiate();
         transformer.configure(config.configData);
         
         return transformer;
      }
      catch (FactoryUnavailableException e)
      {
         throw new WorkflowConfigurationException("Invalid worflow configuration. A factory [" + factoryId + "] is not available", e);
      }
      catch (TransformerConfigurationException e)
      {
         throw new WorkflowConfigurationException("Invalid worflow configuration. A factory [" + factoryId + "] is not available", e);
      }
   }
   
}
