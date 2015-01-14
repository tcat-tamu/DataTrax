package edu.tamu.tcat.analytics.datatrax.config;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector;

public interface WorkflowConfiguration
{
   /**
    * @return A universally unique identifier for this workflow.
    */
   UUID getId();
   
   /**
    * @return A user supplied title for this workflow for display.
    */
   String getTitle();
   
   /**
    * @return A user-supplied description for this workflow for display. 
    */
   String getDescription();
   
   /**
    * @return The Java type of source data objects accepted by this workflow.
    */
   Class<?> getSourceType(); // TODO add support for multiple input types

   /**
    * @return The key for the initial input data object.
    */
   DataValueKey getInputKey();
   
   /**
    * @return The collection of transformers that have been configured for use in this workflow.
    */
   Collection<TransformerConfiguration> getTransformers();

   /**
    * @return A set of {@link DataValueKey}s for the values that should be exported from
    *       this workflow to a {@link ResultsCollector}.
    */
   Set<DataValueKey> getDeclaredOutputs();


}
