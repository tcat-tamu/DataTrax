package edu.tamu.tcat.analytics.datatrax;

import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationBuilder;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowDescription;

/**
 * Service for managing {@link WorkflowConfiguration} instances and for
 *
 */
public interface DataTraxFacade
{
   /**
    * Obtain a builder for use in creating a new {@link WorkflowConfiguration}.  
    * 
    * @return A new {@link WorkflowConfigurationBuilder} for constructing {@link WorkflowConfiguration}
    *    instances that can be managed using this {@link DataTraxFacade}. This ensures that the 
    *    constructed {@code WorkflowConfiguration} instances are configured with the proper 
    *    {@link TransformerRegistry} and return {@code WorkflowConfiguration} instances that 
    *    can be properly serialized and restored by this {@code DataTraxFacade}.
    */
   WorkflowConfigurationBuilder createConfiguration();
   
   /**
    * Obtain a builder for use in editing an existing {@link WorkflowConfiguration}.
    * 
    * @param config
    * @return
    */
   WorkflowConfigurationBuilder editConfiguration(WorkflowConfiguration config);
   
   /**
    * Retrieve the identified {@link WorkflowConfiguration}.
    * 
    * @param id The unique identifier of the configuration to return.
    * @return The identified configuration.
    * 
    * @throws WorkflowException If there is no configuration associated with the 
    *    supplied id. 
    */
   WorkflowConfiguration getConfiguration(UUID id) throws WorkflowException;
   
   /**
    * @return A set containing descriptive information for all workflows defined by this
    *    configuration manager.
    */
   Set<WorkflowDescription> listWorkflows();
   
   /**
    * Saves the supplied {@link WorkflowConfiguration} in a storage system configured for this
    * {@link DataTraxFacade}. 
    * 
    * @param config The configuration instance to save.
    * @throws IllegalStateException If the supplied configuration object is not of a type 
    *       that can be serialized using this {@link DataTraxFacade}/
    */
   void saveWorkflowConfiguratoin(WorkflowConfiguration config);
   
   /**
    * Creates a new {@link WorkflowConfiguration} for the specified {@link WorkflowConfiguration}. 
    * This is the primary access point for clients to instantiate a new {@code WorkflowController}
    * for use in processing data.
    *  
    * @param config
    * @return
    */
   WorkflowController createWorkflow(WorkflowConfiguration config);
   
   /**
    * @return The configured {@link TransformerRegistry}. This will be the registry that is used
    *       by all system components associated with this facade.  
    */
   TransformerRegistry getTranformerRegistry();
}
