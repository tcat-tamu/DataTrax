package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistry;
import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfigEditor;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;

public class SimpleTransformerConfig implements TransformerConfiguration, TransformerConfigEditor
{

   public static SimpleTransformerConfig instantiate(TransformerRegistry registry, TransformerConfigData data) throws FactoryUnavailableException
   {
      // check invariants. These values must be supplied.
      Objects.requireNonNull(data.transformerId, "Invalid initial configuration data: no transformer id supplied.");
      Objects.requireNonNull(data.registrationId, "Invalid initial configuration: no transformer registration supplied.");
      
      // ensure that all pins have been set
      TransformerRegistration registration = registry.getRegistration(data.registrationId);
      for (DataInputPin pin : registration.getDeclaredInputs())
      {
         if (!data.inputs.containsKey(pin.label))
            data.inputs.put(pin.label, null);
      }
      
      return new SimpleTransformerConfig(data, registration);
   }
   
   private TransformerConfigData data;
   private TransformerRegistration registration;

   private SimpleTransformerConfig(TransformerConfigData data, TransformerRegistration reg)
   {
      this.data = data;
      this.registration = reg;
   }

   @Override
   public UUID getId()
   {
      return data.transformerId;
   }

   @Override
   public TransformerRegistration getRegistration()
   {
      return registration;
   }

   @Override
   public Set<String> getDefinedParameters()
   {
      return data.params.keySet();
   }

   @Override
   public Object getParameter(String key)
   {
      return data.params.get(key);
   }

   @Override
   public int getIntParameter(String key)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public double getDoubleParameter(String key)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String getStringParameter(String key)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<DataInputPin> getDefinedInputs()
   {
      Set<DataInputPin> pins = new HashSet<>();
      data.inputs.keySet().forEach(
            name -> pins.add(this.registration.getDeclaredInput(name)));
      
      return Collections.unmodifiableSet(pins);
   }

   private boolean hasInputPin(DataInputPin pin)
   {
      // TODO seem like we need a more intelligent check than this.
      return data.inputs.containsKey(pin.label);
   }
   
   @Override
   public UUID getDataSource(DataInputPin pin)
   {
      if (!hasInputPin(pin))
         throw new IllegalArgumentException("Cannot retrieve data source. Invalid input pin [" + pin.label + "]");
      
      return data.inputs.get(pin.label);
   }

   /**
    * Removes the stitching for any input values provided by the indicated transformer.
    * 
    * @param transformerId The transformer whose output should no longer be used as input for 
    *    this transformer.
    */
   public void removeInput(UUID transformerId)
   {
      if (!data.inputs.containsValue(transformerId))
         return;
      
      for (String key : data.inputs.keySet())
      {
         UUID id = data.inputs.get(key);
         if (transformerId.equals(id))
            data.inputs.remove(key);
      }
   }

   @Override
   public Class<?> getOutputType()
   {
      return registration.getDeclaredOutputType();
   }

   @Override
   public void setParameter(String param, Object val)
   {
      // TODO sanity check
      data.params.put(param, val);
   }
   
   private void validateDataSource(DataInputPin pin, DataValueKey source) throws TransformerConfigurationException
   {
      Class<?> required = pin.type;
      Class<?> provided = source.getType();
      
      if (!required.isAssignableFrom(provided))
      {
//         TransformerRegistration reg = source.getRegistration();
         throw new TransformerConfigurationException("Incompatible data source. "
               + "The supplied data source [" + source + "] "
               + "does not match the input pin [" + pin.label + "]. "
               + "Required [" + required + "]. Provided [" + provided + "]");
      }
   }

   @Override
   public void setDataSource(DataInputPin pin, TransformerConfiguration source) throws TransformerConfigurationException
   {
      Class<?> required = pin.type;
      Class<?> provided = source.getOutputType();
      
      if (!required.isAssignableFrom(provided))
      {
//         TransformerRegistration reg = source.getRegistration();
         throw new TransformerConfigurationException("Incompatible data source. "
               + "The supplied data source [" + source.getId() + "] "
               + "does not match the input pin [" + pin.label + "]. "
               + "Required [" + required + "]. Provided [" + provided + "]");
      }
      
      data.inputs.put(pin.label, source.getId());
   }
   
   @Override
   public void setDataSource(DataInputPin pin, DataValueKey source) throws TransformerConfigurationException
   {
      validateDataSource(pin, source);
      
      data.inputs.put(pin.label, source.getSourceId());
   }

   @Override
   public TransformerConfiguration getConfiguration()
   {
      return new SimpleTransformerConfig(TransformerConfigData.create(data), registration);
   }
}
