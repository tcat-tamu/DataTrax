package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.basic.ExecutionContext;
import edu.tamu.tcat.analytics.datatrax.basic.SimpleDataValueKey;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;

public class ExecutionContextTests
{
   // TODO create a Mock registry that we can use to return decorated filter 
   //      implementations to support testing and performance analysis 
   private static ExtPointTranformerFactoryRegistry registry;
   
   @BeforeClass
   public static void setup()
   {
      registry = new ExtPointTranformerFactoryRegistry();
      registry.activate();
   }
   
   @AfterClass
   public static void teardown()
   {
      registry.dispose();
      
   }
   
   /**
    * Tests basic interaction of test and get methods. Ensures that data can be set 
    * and retrieved. 
    * 
    * <p>
    * Note that optimizations may elect not to store data values for which there are 
    * no listeners.
    */
   @Test
   public void testPutGet()
   {
      String testValue = "Hello World";
      DataValueKey testKey = new SimpleDataValueKey(UUID.randomUUID(), String.class);
      
      ExecutionContext context = new ExecutionContext();
      
      context.put(testKey, testValue);
      String result = (String)context.get(testKey);
      
      assertEquals("Result value does not match supplied value", testValue, result);
   }
   
   /**
    * Tests that listeners are notified when data arrives that they are registered for.
    * @throws InterruptedException 
    */
   @Test
   public void testListenerNotification() throws InterruptedException
   {
      String testValue = "Hello World";
      DataValueKey testKey = new SimpleDataValueKey(UUID.randomUUID(), String.class);
      
      ExecutionContext context = new ExecutionContext();
      Set<DataValueKey> keys = new HashSet<>();
      keys.add(testKey);
      
      final AtomicReference<String> result = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);
      context.registerListener(keys, evt -> {
         result.set((String)evt.getValue());
         latch.countDown();
      });

      context.put(testKey, testValue);
      
      latch.await(2, TimeUnit.SECONDS);
      
      assertEquals("Result value does not match supplied value", testValue, result.get());
   }
   
   /**
    * Tests that listeners are not notified if their registrations are closed.
    * @throws Exception 
    */
   @Test
   public void testListenerUnregistration() throws Exception
   {
      String testValue = "Hello World";
      DataValueKey testKey = new SimpleDataValueKey(UUID.randomUUID(), String.class);
      
      ExecutionContext context = new ExecutionContext();
      Set<DataValueKey> keys = new HashSet<>();
      keys.add(testKey);
      
      final AtomicReference<String> result = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);
      AutoCloseable reg = context.registerListener(keys, evt -> {
         result.set((String)evt.getValue());
         latch.countDown();
      });

      reg.close();
      context.put(testKey, testValue);
      
      // HACK: note that this only verifies that the listener is not called within two seconds.
      assertFalse("Event listener was triggered", latch.await(2, TimeUnit.SECONDS));
   }
   
   /**
    * If a listener is registered after data arrives, tests that it is fired without waiting 
    * for new data to arrive.
    * @throws InterruptedException 
    */
   @Test
   public void testImmediateFire() throws InterruptedException
   {
      String testValue = "Hello World";
      DataValueKey testKey = new SimpleDataValueKey(UUID.randomUUID(), String.class);
      
      ExecutionContext context = new ExecutionContext();
      Set<DataValueKey> keys = new HashSet<>();
      keys.add(testKey);
      
      // add value
      context.put(testKey, testValue);

      // add listener
      final AtomicReference<String> result = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);
      context.registerListener(keys, evt -> {
         result.set((String)evt.getValue());
         latch.countDown();
      });

      latch.await(2, TimeUnit.SECONDS);
      
      assertEquals("Result value does not match supplied value", testValue, result.get());
   }
   
   /**
    * Test that duplicate values are ignored silently. 
    */
   public void testDuplicateAdd() throws InterruptedException
   {
      String testValue = "Hello World";
      String testValue2 = "Goodbye World";
      DataValueKey testKey = new SimpleDataValueKey(UUID.randomUUID(), String.class);
      
      ExecutionContext context = new ExecutionContext();
      Set<DataValueKey> keys = new HashSet<>();
      keys.add(testKey);
      
      final AtomicReference<String> result = new AtomicReference<>();
      
      // test that the value is fired once
      CountDownLatch singlLatch = new CountDownLatch(1);
      context.registerListener(keys, evt -> {
         result.set((String)evt.getValue());
         singlLatch.countDown();
      });
      
      // test that the value is not fired twice 
      CountDownLatch doubleLatch = new CountDownLatch(2);
      context.registerListener(keys, evt -> {
         result.set((String)evt.getValue());
         doubleLatch.countDown();
      });

      context.put(testKey, testValue);
      context.put(testKey, testValue2);
      
      singlLatch.await(2, TimeUnit.SECONDS);
      
      assertEquals("Result value does not match supplied value", testValue, result.get());
      assertFalse("Event notification fired twice", doubleLatch.await(2, TimeUnit.SECONDS));
      
      // ensure that the correct value is in the context after second put
      assertEquals("Unexpected value found in context", testValue, (String)context.get(testKey));
   }
   
   
}
