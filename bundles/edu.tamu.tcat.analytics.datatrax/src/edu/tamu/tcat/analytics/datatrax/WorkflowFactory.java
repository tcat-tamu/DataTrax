package edu.tamu.tcat.analytics.datatrax;

import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationException;

/**
 * Used to manage workflow configuration data and to generate workflow instances from 
 * existing configuration data. 
 *
 */
public interface WorkflowFactory
{
   
   <IN, OUT> DataTransformWorkflow<IN, OUT> create(WorkflowConfiguration config) throws WorkflowConfigurationException;
   
   // TODO provide access to configuration builders, defined configurations, etc. Provide support to ingest new configurations
}
