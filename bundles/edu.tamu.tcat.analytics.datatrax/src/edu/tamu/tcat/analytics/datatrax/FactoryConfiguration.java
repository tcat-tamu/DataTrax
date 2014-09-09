package edu.tamu.tcat.analytics.datatrax;

import java.util.Map;

/**
 * A data vehicle for representing configuration information about a single 
 * {@link TransformerFactory} within a workflow.
 *
 */
public class FactoryConfiguration
{
   public String factoryId;
   
   public Map<String, Object> configData;
}
