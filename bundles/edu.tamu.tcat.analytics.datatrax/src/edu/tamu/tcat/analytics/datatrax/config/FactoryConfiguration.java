package edu.tamu.tcat.analytics.datatrax.config;

import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.Transformer;

/**
 * A data vehicle for representing configuration information about a single 
 * {@link Transformer} within a workflow. A {@link DataTransformWorkflow} is defined, in part, 
 * by a sequence of {@code FactoryConfiguration}s that describe how to convert individual 
 * input data instances into output instances.
 *
 */
public class FactoryConfiguration
{
   public String factoryId;
   
   public Map<String, Object> configData = new HashMap<>();
}
