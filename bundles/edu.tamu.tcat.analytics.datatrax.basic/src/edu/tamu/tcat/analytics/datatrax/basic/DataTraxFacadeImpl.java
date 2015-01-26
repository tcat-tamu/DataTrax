package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.analytics.datatrax.DataTraxFacade;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistry;
import edu.tamu.tcat.analytics.datatrax.WorkflowController;
import edu.tamu.tcat.analytics.datatrax.WorkflowException;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationBuilder;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationException;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowDescription;

/**
 * 
 *
 */
public class DataTraxFacadeImpl implements DataTraxFacade
{

   private TransformerRegistry registry;
   private ConfigurationProperties props;

   public DataTraxFacadeImpl()
   {
   }

   public void setTransformerRegistry(TransformerRegistry registry)
   {
      this.registry = registry;
   }
   
   public void setConfiguration(ConfigurationProperties props)
   {
      this.props = props;
   }
   
   public void activate()
   {
      
   }
   
   public void deactivate()
   {
      
   }
   

   /**
    * Checks to ensure that the transformer registry is available, throwing an 
    * {@link IllegalStateException} if it is not.
    * 
    * TODO block for some short period of time to wait for the registry to become available
    */
   private void ensureRegistryAvailable()
   {
      if (registry == null)
      {
         throw new IllegalStateException("Transformer registry is not currently available");
      }
   }

   @Override
   public WorkflowConfigurationBuilder createConfiguration()
   {
      ensureRegistryAvailable();
      
      return new WorkflowConfigBuilderImpl(registry);
   }

   @Override
   public WorkflowConfigurationBuilder editConfiguration(WorkflowConfiguration config) throws WorkflowConfigurationException
   {
      ensureRegistryAvailable();
      
      return WorkflowConfigBuilderImpl.create(registry, config);
   }

   @Override
   public WorkflowConfiguration getConfiguration(UUID id) throws WorkflowException
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<WorkflowDescription> listWorkflows()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void saveWorkflowConfiguratoin(WorkflowConfiguration config)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public WorkflowController createWorkflow(WorkflowConfiguration config) throws WorkflowConfigurationException
   {
      try 
      {
         return WorkflowControllerImpl.create(config);
      } catch (Exception tce)
      {
         throw new WorkflowConfigurationException("Failed to instantatiate workflow for configuration '" + config.getTitle() + "' [" + config.getId() + "]", tce);
      }
   }

   @Override
   public TransformerRegistry getTranformerRegistry()
   {
      ensureRegistryAvailable();
      
      return registry;
   }

}
