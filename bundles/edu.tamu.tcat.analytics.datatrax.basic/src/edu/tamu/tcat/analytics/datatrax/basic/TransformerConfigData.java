package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;

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
   public UUID transformerId;
   public Map<String, Object> params = new HashMap<>();
   public Map<String, UUID> inputs = new HashMap<>();    // map of input pin labels to transformers supply that value
   public Class<?> outputType;

   public TransformerConfigData()
   {
      
   }

   /**
    * Creates a {@code TransformerConfigData} data vehicle as a shallow copy an existing 
    * data vehicle. All values will be copied into the new structure such that changes to the 
    * previous data vehicle will not be reflected in the retured object. Any mutable values 
    * for parameters, however, will not be copied so direct modification of these object will
    * be reflected in both the old and new {@code TransformerConfigData} instances. 
    * 
    * @param data The config data to be copied.
    */
   public static TransformerConfigData create(TransformerConfigData data)
   {
      TransformerConfigData result = new TransformerConfigData();

      result.registrationId = data.registrationId;
      result.transformerId = data.transformerId;
      result.params = new HashMap<>(data.params);
      result.inputs = new HashMap<>(data.inputs);
      result.outputType = data.outputType;
      
      return result;
   }
   
   /**
    * Creates a {@code TransformerConfigData} data vehicle that is initialized based on an 
    * existing {@link TransformerConfiguration} instance. 
    * 
    * @param cfg The configuration to copy
    * @return A new data vehicle containing the supplied configuration.
    */
   public static TransformerConfigData create(TransformerConfiguration cfg)
   {
      TransformerConfigData data = new TransformerConfigData();
      data.registrationId = cfg.getRegistration().getId();
      data.transformerId = cfg.getId();
      Set<String> paramKeys = cfg.getDefinedParameters();
      for (String key : paramKeys)
      {
         data.params.put(key, cfg.getParameter(key));
      }
      
      Set<DataInputPin> pins = cfg.getDefinedInputs();
      for (DataInputPin pin : pins)
      {
         data.inputs.put(pin.label, cfg.getDataSource(pin));
      }
      
      data.outputType = cfg.getOutputType();
      
      return data;
   }
}