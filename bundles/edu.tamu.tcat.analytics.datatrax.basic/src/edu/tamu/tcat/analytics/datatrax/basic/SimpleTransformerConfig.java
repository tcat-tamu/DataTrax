package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.TransformerConfigurationException;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
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
      TransformerRegistration registration = registry.getFactory(data.registrationId);
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
      // TODO Auto-generated method stub
      return null;
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
      throw new UnsupportedOperationException();
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
   
   private void validateDataSource(DataInputPin pin, TransformerConfiguration source) throws TransformerConfigurationException
   {
      Class<?> required = pin.type;
      Class<?> provided = source.getOutputType();
      
      if (!required.isAssignableFrom(provided))
      {
         TransformerRegistration reg = source.getRegistration();
         throw new TransformerConfigurationException("Incompatible data source. "
               + "The output data for transformer [" + reg.getTitle() + "] "
               + "does not match the input pin [" + pin.label + "]. "
               + "Required [" + required + "]. Provided [" + provided + "]");
      }
   }

   @Override
   public void setDataSource(DataInputPin pin, TransformerConfiguration source) throws TransformerConfigurationException
   {
      validateDataSource(pin, source);
      
      data.inputs.put(pin.label, source.getId());
   }

   @Override
   public TransformerConfiguration getConfiguration()
   {
      return new SimpleTransformerConfig(TransformerConfigData.create(data), registration);
   }
}
