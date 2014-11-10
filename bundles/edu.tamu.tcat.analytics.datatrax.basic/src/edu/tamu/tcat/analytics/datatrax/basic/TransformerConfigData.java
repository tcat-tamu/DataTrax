package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.analytics.datatrax.Transformer;

/**
 * A data vehicle for representing configuration information about a single 
 * {@link Transformer} within a workflow. A {@link DataTransformWorkflow} is defined, in part, 
 * by a sequence of {@code FactoryConfiguration}s that describe how to convert individual 
 * input data instances into output instances.
 *
 */
public class TransformerConfigData
{
   public String registrationId;
   public String transformerId;
   public Map<String, Object> params = new HashMap<>();
   public Map<String, String> inputs = new HashMap<>();
   public Class<?> outputType;

   /**
    * Copy constructor makes a deep copy of the data so that the source object can be modified
    * without affecting the new config data.
    * 
    * @param data The config data to be copied.
    */
   public TransformerConfigData(TransformerConfigData data)
   {
      this.registrationId = data.registrationId;
      this.transformerId = data.transformerId;
      this.params = new HashMap<>(data.params);
      this.inputs = new HashMap<>(data.inputs);
      this.outputType = data.outputType;
   }
   
   public TransformerConfigData()
   {
      
   }
}