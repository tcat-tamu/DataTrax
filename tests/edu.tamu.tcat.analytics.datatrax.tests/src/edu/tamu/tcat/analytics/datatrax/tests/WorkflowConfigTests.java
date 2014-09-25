package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.TransformerFactory;
import edu.tamu.tcat.analytics.datatrax.TransformerFactoryRegistry;
import edu.tamu.tcat.analytics.datatrax.tests.internal.Activator;
import edu.tamu.tcat.osgi.services.util.ServiceHelper;

public class WorkflowConfigTests
{

   public WorkflowConfigTests()
   {
      // TODO Auto-generated constructor stub
   }

   @Test
   public void testLookupFactory() throws FactoryUnavailableException
   {
      try (ServiceHelper helper = new ServiceHelper(Activator.getDefault().getContext()))
      {
         TransformerFactoryRegistry registry = helper.waitForService(TransformerFactoryRegistry.class, 10_000);
         TransformerFactory factory = registry.getFactory("edu.tamu.tcat.analytics.datatrax.tests.helloworld");
         assertNotNull("Failed to retrieve hello world factory.", factory);
         
         assertTrue("Cannot accept String.class", factory.canAccept(String.class));
         assertTrue("Cannot produce String.class", factory.canProduce(String.class));
         assertFalse("Can accept Object.class", factory.canAccept(Object.class));
         assertTrue("Cannot produce Object.class", factory.canProduce(Object.class));
         
//         Collection<TransformerFactory> factories = registry.getFactories();
//         for (TransformerFactory f : factories)
//         {
//            System.out.println(f.getTitle());
//         }
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
