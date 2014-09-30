package edu.tamu.tcat.analytics.datatrax;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A lightweight data vehicle for defining a sequence of data transformers to pass instance 
 * values through.  
 */
public class WorkflowConfiguration
{
   public UUID uuid;
   public String title = "New Workflow";
   public String description;
   
   /**
    * The Java type of the input instances that can be processed by this workflow configuration. 
    */
   public Class<?> sourceType = Object.class;
   
   public List<FactoryConfiguration> factories = new ArrayList<>();
   
   public WorkflowConfiguration()
   {
      uuid = UUID.randomUUID();
   }
}
