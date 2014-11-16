package edu.tamu.tcat.analytics.datatrax.config;

import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;

/**
 * Used to build up a workflow configuration data object. 
 * 
 * <p>
 * Note that implementations are not required to be (and typically are not) thread-safe. 
 */
public interface WorkflowConfigurationBuilder
{

   void setTitle(String title);
   
   void setDescription(String desc);
   
   void setInputType(Class<?> cls);

   Set<UUID> listTransformers();
   
   /**
    * 
    * @param id
    * @return
    * @throws WorkflowConfigurationException
    */
   TransformerConfigEditor editTransformer(UUID id) throws WorkflowConfigurationException;
   
   /**
    * 
    * @param reg
    * @return
    * @throws WorkflowConfigurationException 
    */
   TransformerConfigEditor createTransformer(TransformerRegistration reg) throws WorkflowConfigurationException;
   
   WorkflowConfiguration build();

   
}
