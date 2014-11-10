package edu.tamu.tcat.analytics.datatrax.config;

import java.util.List;

/**
 * Used to build up a workflow configuration data object. 
 * 
 * <p>
 * Note that implementations are not required to be (and typically are not) thread-safe. 
 */
public interface WorkflowConfigurationBuilder
{

   void append(TransformerConfigData config) throws WorkflowConfigurationException; 
   
   void append(List<TransformerConfigData> configs) throws WorkflowConfigurationException;
   
   WorkflowConfiguration build();
   
}
