package edu.tamu.tcat.analytics.datatrax.config;

import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;

/**
 * Used to build up a workflow configuration data object. 
 * 
 * <p>
 * Note that implementations are not required to be (and typically are not) thread-safe. 
 */
public interface WorkflowConfigurationBuilder
{

   /**
    * Sets the title of the workflow.
    *  
    * @param title The title to be used for this workflow. May not be {@code null}.
    */
   void setTitle(String title);
   
   /**
    * Sets the description of the workflow.
    * 
    * @param desc The description to be set. If {@code null}, will be set as an empty string. 
    */
   void setDescription(String desc);
   
   /**
    * Sets the Java type of input values that will be processed by this workflow.
    * 
    * @param cls The Java type for input values.
    */
   void setInputType(Class<?> cls);

   /**
    * @return The {@link DataValueKey} that will be associated with input data instances 
    *    supplied to the workflow.
    */
   DataValueKey getInputKey();

   /**
    * @return The UUIDs of transformers that have been defined for this workflow.
    */
   Set<UUID> listTransformers();
   
   /**
    * Obtain a {@link TransformerConfigEditor} for the identified transformer configuration.
    * The returned editor can be used to directly modify the configuration for the associated 
    * transformer and all changes will be reflected immediately in the state of this workflow's 
    * configuration as returned by the {@link #build()} method. 
    * 
    * @param id The transformer configuration to be edited. Note that this is the unique 
    *       identifier for the specific transformer configuration associated with this workflow,
    *       not for the registration that defines a class of transformers. 
    * @return An editor for the identified transformer configuration. 
    * 
    * @throws WorkflowConfigurationException If the requested transformer has not be created 
    *       in the context of this builder.
    * @see #createTransformer(TransformerRegistration)
    */
   TransformerConfigEditor editTransformer(UUID id) throws WorkflowConfigurationException;
   
   /**
    * Create a new {@link TransformerConfiguration} within the scope of this workflow and 
    * return an {@link TransformerConfigEditor} for use in editing that configuration.   
    * 
    * @param reg The registration that declares the type of transformer to be configured.
    * @return An editor to update the newly created configuration.
    * 
    * @throws WorkflowConfigurationException If the supplied {@link TransformerRegistration} 
    *       is not defined by the registry associated with this 
    *       {@code WorkflowConfigurationBuilder}. 
    */
   TransformerConfigEditor createTransformer(TransformerRegistration reg) throws WorkflowConfigurationException;
   
   /**
    * Removes the configuration for the indicated transformer from this workflow. This will 
    * also remove the output of the indicated transformer from all input pins it has been 
    * connected to. Note that this action is irreversible and that, once removed, the 
    * associated transformer cannot be restored.
    * 
    * @param id The id of the transformer to be removed.
    */
   void removeTransformer(UUID id);
   
   /**
    * @return A set of ids for all transformers that are registered as outputs from this 
    *       workflow.
    */
   Set<UUID> listOutputs();
   
   /**
    * Registers a transformer in this workflow as supplying output values that should be 
    * passed to the {@link ResultsCollector} when the associated workflow is executed.
    * 
    * @param id The id of the transformer whose result should be collected as an
    *       output of this workflow.
    * @throws WorkflowConfigurationException If the identified transformer has not be created 
    *       in the context of this builder.
    */
   void registerOutput(UUID id) throws WorkflowConfigurationException;

   /**
    * Removes the indicated transformer from the set of transformers whose result values will 
    * be collected as part of the output values for this workflow.
    *  
    * @param id the id of the transformer to remove from the output set. Note that if this 
    *       transformer is not defined for this workflow or registered as an output value, 
    *       the method will complete successfully without modifying the set of registered outputs.
    */
   void removeOutput(UUID id);

   /**
    * 
    * @return The current state of this configuration. Note that this may be called multiple 
    *       times on the same {@code WorkflowConfigurationBuilder}. Each returned 
    *       {@link WorkflowConfiguration} instance will be detached from this builder so that
    *       subsequent updates to the builder do not impact the returned instance.
    */
   WorkflowConfiguration build();


   
}
