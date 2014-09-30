package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerFactoryRegistration;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtTransformerFactoryDefinition;
import edu.tamu.tcat.analytics.datatrax.config.FactoryConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationException;

public class WorkflowConfigBuilderImpl
{

   private WorkflowConfiguration cfg;
   private final ExtPointTranformerFactoryRegistry registry;
   
   public WorkflowConfigBuilderImpl(ExtPointTranformerFactoryRegistry reg)
   {
      this.registry = reg;
      this.cfg = new WorkflowConfiguration();
   }

   public void setTitle(String title)
   {
      Objects.requireNonNull(title, "The workflow title cannot be null.");
      
      cfg.title = title;
   }
   
   public void setDescription(String desc)
   {
      if (desc == null)
         desc = "";
      
      cfg.description = desc;
   }
   
   public void append(FactoryConfiguration config) throws WorkflowConfigurationException
   {
      ExtTransformerFactoryDefinition candidate = getFactory(config);
      checkTypeCompatibility(candidate);
      
      // TODO test supplied configuration.
      try 
      {
         Transformer<Object, Object> transformer = candidate.instantiate();
         transformer.configure(config.configData);
      } 
      catch (Exception ex)
      {
         throw new WorkflowConfigurationException("Cannot append transformer factory: " 
               + candidate.getTitle() + "[" + candidate.getId() + "]. Failed to instantiate and configure instance.", ex);
         
      }
      
      cfg.factories.add(config);
   }

   /**
    * Checks to ensure that the source type of a candidate transformer factory is compatible 
    * with the output type of the final factory in the growing workflow configuration.
    *   
    * @param candidate The candidate factory to append
    * @throws WorkflowConfigurationException If the supplied candidate cannot be appended to 
    *    the current workflow.
    */
   private void checkTypeCompatibility(TransformerFactoryRegistration candidate) throws WorkflowConfigurationException
   {
      if (size() != 0)
      {
         TransformerFactoryRegistration tail = getTail();
         if (!candidate.canAccept(tail.getDeclaredOutputType()))
            throw new WorkflowConfigurationException("Cannot append transformer factory: " 
                  + candidate.getTitle() + "[" + candidate.getId() + "]. Declared input type [" + candidate.getDeclaredSourceType() + "] "
                  + "is not compatible with preceeding factory's output type [" + tail.getDeclaredOutputType() +"]");
      }
   }
   
   // TODO add API for splitting and merging workflow configurations
   // TODO add methods to insert elements in the middle of a workflow.

   public void append(List<FactoryConfiguration> configs) throws WorkflowConfigurationException
   {
      // TODO Auto-generated method stub
      
   }

   /**
    * 
    * @return A {@link Collection} of all registered factories that can be appended to the 
    *       current workflow. 
    */
   public Collection<TransformerFactoryRegistration> listValidFactories()
   {
      return registry.getCompatibleFactories(getTail().getDeclaredOutputType());
   }

   public WorkflowConfiguration build()
   {
      cfg.sourceType = getHead().getDeclaredSourceType();
      
      return cfg;
   }

   public Class<?> getSourceType()
   {
      if (size() == 0)
         return Object.class;
      
      return getHead().getDeclaredOutputType();
   }

   /**
    * @return The Java type of the output produced by the final transformer in the workflow.
    */
   public Class<?> getTailType()
   {
      if (size() == 0)
         return Object.class;
      
      return getTail().getDeclaredOutputType();
   }
   
   public int size()
   {
      return cfg.factories.size();
   }

   /**
    * 
    * @return The first {@link TransformerFactoryRegistration} in the current workflow configuration. Will
    *    not be {@code null}. 
    * @throws IllegalStateException If no factories have been added to the current configuration
    *    or if the defined factory cannot be retrieved from the registry. 
    */
   private TransformerFactoryRegistration getHead()
   {
      return getFactory(0);
   }
   
   /**
    * 
    * @return The final {@link TransformerFactoryRegistration} in the current workflow configuration. Will
    *    not be {@code null}. 
    * @throws IllegalStateException If no factories have been added to the current configuration 
    *    or if the defined factory cannot be retrieved from the registry. 
    */
   private TransformerFactoryRegistration getTail()
   {
      return getFactory(size() - 1);
   }

   private ExtTransformerFactoryDefinition getFactory(int ix)
   {
      if (ix < 0 || ix >= size())
         throw new ArrayIndexOutOfBoundsException("Cannot retrieve transformers factory at position [" + ix + "]");
      
      return getFactory(cfg.factories.get(ix));
   }
   
   private ExtTransformerFactoryDefinition getFactory(FactoryConfiguration config)
   {
      try 
      {
         return registry.getFactory(config.factoryId);
      } 
      catch (FactoryUnavailableException ex)
      {
         throw new IllegalStateException("Invalid configuration. Cannot retrieve the transformer factory [" + config.factoryId + "]", ex);
      }
   }

   // TODO add insert method
}
