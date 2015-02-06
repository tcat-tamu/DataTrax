package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistry;
import edu.tamu.tcat.analytics.datatrax.basic.SimpleTransformerConfig;
import edu.tamu.tcat.analytics.datatrax.basic.TransformerConfigData;
import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;
import edu.tamu.tcat.analytics.datatrax.tests.internal.Activator;
import edu.tamu.tcat.osgi.services.util.ServiceHelper;

public class ExtTranformerRegistryTests
{

   public ExtTranformerRegistryTests()
   {
      // TODO Auto-generated constructor stub
   }

   @Test
   public void testLookupFactory() throws FactoryUnavailableException
   {
      // TODO should test without appeal to OSGi
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         TransformerRegistry registry = helper.waitForService(TransformerRegistry.class, 10_000);
         TransformerRegistration registration = registry.getRegistration("edu.tamu.tcat.analytics.datatrax.tests.helloworld");
         assertNotNull("Failed to retrieve hello world factory.", registration);
         
         assertTrue("Cannot accept String.class", registration.canAccept(String.class));
         assertTrue("Cannot produce String.class", registration.canProduce(String.class));
         assertFalse("Can accept Object.class", registration.canAccept(Object.class));
         assertTrue("Cannot produce Object.class", registration.canProduce(Object.class));
      }
   }
   
   public static SimpleTransformerConfig buildConfig(TransformerRegistration registration) throws FactoryUnavailableException
   {
      TransformerConfigData data = new TransformerConfigData();
      data.transformerId = UUID.randomUUID();
      data.registrationId = registration.getId();
      data.inputs = new HashMap<>();
      data.outputType = String.class;
      data.params = new HashMap<>();
      
      // ensure that all pins have been set
      for (DataInputPin pin : registration.getDeclaredInputs())
      {
         if (!data.inputs.containsKey(pin.label))
            data.inputs.put(pin.label, null);
      }
      
      return new SimpleTransformerConfig(data, registration);
   }
   
   @Test
   public void testInvocation() throws Exception
   {
      // TODO should test without appeal to OSGi
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         TransformerRegistry registry = helper.waitForService(TransformerRegistry.class, 10_000);
         TransformerRegistration reg = registry.getRegistration("edu.tamu.tcat.analytics.datatrax.tests.helloworld");
         assertNotNull("Failed to retrieve hello world factory.", reg);

         TransformerConfiguration cfg = buildConfig(reg);
         Transformer hello = reg.instantiate(cfg);
         
         HashMap<String, Object> params = new HashMap<>();
         params.put("name", "Fred");
         Callable<?> task = hello.create((key) -> params.get(key));
         String msg = (String)task.call();
         assertEquals("Hello Fred", msg);
         
         
         params.put("salutation", "Goodbye");
         task = hello.create((key) -> params.get(key));
         msg = (String)task.call();
         assertEquals("Goodbye Fred", msg);
      }
   }
   
   @Test
   public void testConfigureFactory()
   {
      assertFalse("configure not implemented", false);
      
   }
   
   @Test
   public void testConfigSerialization()
   {
      assertFalse("serialize factory not implemented", false);
   }
   
}
