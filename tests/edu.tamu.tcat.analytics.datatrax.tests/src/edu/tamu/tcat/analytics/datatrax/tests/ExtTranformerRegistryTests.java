package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.Callable;

import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistry;
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
         TransformerRegistration registration = registry.getFactory("edu.tamu.tcat.analytics.datatrax.tests.helloworld");
         assertNotNull("Failed to retrieve hello world factory.", registration);
         
         assertTrue("Cannot accept String.class", registration.canAccept(String.class));
         assertTrue("Cannot produce String.class", registration.canProduce(String.class));
         assertFalse("Can accept Object.class", registration.canAccept(Object.class));
         assertTrue("Cannot produce Object.class", registration.canProduce(Object.class));
      }
   }
   
   @Test
   public void testInvocation() throws Exception
   {
      // TODO should test without appeal to OSGi
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         TransformerRegistry registry = helper.waitForService(TransformerRegistry.class, 10_000);
         TransformerRegistration reg = registry.getFactory("edu.tamu.tcat.analytics.datatrax.tests.helloworld");
         assertNotNull("Failed to retrieve hello world factory.", reg);
         
         Transformer hello = reg.instantiate();
         HashMap<String, String> params = new HashMap<>();
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
