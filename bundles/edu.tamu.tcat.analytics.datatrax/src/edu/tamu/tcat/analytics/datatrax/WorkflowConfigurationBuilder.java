package edu.tamu.tcat.analytics.datatrax;

import java.util.List;

/**
 * Used to build up a workflow configuration data object. 
 * 
 * <p>
 * Note that implementations are not required to be (and typically are not) thread-safe. 
 */
public interface WorkflowConfigurationBuilder
{

   void append(FactoryConfiguration config) throws WorkflowConfigurationException; 
   
   void append(List<FactoryConfiguration> configs) throws WorkflowConfigurationException;
   
   <IN, OUT> DataTransformWorkflow<IN, OUT> build();
   
}
