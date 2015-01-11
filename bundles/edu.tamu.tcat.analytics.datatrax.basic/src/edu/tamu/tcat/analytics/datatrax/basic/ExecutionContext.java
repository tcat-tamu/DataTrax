package edu.tamu.tcat.analytics.datatrax.basic;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.WorkflowController;

/**
 *  Used by the {@link WorkflowController} to store the results of data transformation 
 *  operations. The notification system implemented by the {@link ExecutionContext} drives 
 *  the execution of new tasks. 
 */
public class ExecutionContext
{
   private final static Logger contextLogger = Logger.getLogger("edu.tamu.tcat.datatrax.ex_context");
   
   private final Map<DataValueKey, Object> values = new ConcurrentHashMap<>();
   // TODO need JavaType for this thing.
   private final Map<DataValueKey, List<Consumer<DataAvailableEvent>>> listeners = new ConcurrentHashMap<>();
   
   public ExecutionContext()
   {
   }
   
   /**
    * Register a listener that will be notified once a data instance becomes available.
    * 
    * @param ears The listener for data events.
    * @return A handle that allows for the attached listener to be closed.
    * @deprecated Use {@link #registerListener(Set, Consumer)} instead
    */
   public AutoCloseable registerListener(final DataValueListener ears)
   {
      final Set<DataValueKey> keys = new HashSet<>(ears.getKeys());
      return registerListener(keys, (obj) -> ears.dataAvailable(obj));
   }
   
   /**
    * Registers a listener that will be notified whenever data become available for any of the 
    * supplied keys.
    * 
    * @param keys The keys that identify data values to listen for.
    * @param ears The listener for data events.
    * @return A handle that allows for the attached listener to be closed.
    */
   public AutoCloseable registerListener(final Set<DataValueKey> keys, final Consumer<DataAvailableEvent> ears)
   {
      synchronized (this)
      {
         for (DataValueKey key : keys)
         {
            addListener(key, ears);
         }
      }
      
      return () -> removeListener(keys, ears);
   }
   
   private void removeListener(Set<DataValueKey> keys, Consumer<DataAvailableEvent> ears)
   {
      synchronized (this)
      {
         for (DataValueKey key : keys)
         {
            List<Consumer<DataAvailableEvent>> list = listeners.get(key);
            if (list != null && !list.contains(ears))
            {
               list.remove(ears);
            }
         } 
      }
   }
   
   private void addListener(DataValueKey key, Consumer<DataAvailableEvent> ears)
   {
      // if already present, fire immediately. otherwise, add to all defined keys
      if (values.containsKey(key))
      {
         DataAvailableEvent event = new DataAvailableEvent(key, values.get(key));
         ears.accept(event);
         return;
      }
      
      List<Consumer<DataAvailableEvent>> pending = listeners.get(key);
      if (pending == null)
      {
         pending = new CopyOnWriteArrayList<>();
         listeners.put(key, pending);
      }
      
      pending.add(ears);
   }
      

   public void put(DataValueKey key, Object value)
   {
      synchronized (this)
      {
         checkValueType(key, value);
         
         if (values.putIfAbsent(key, value) != null)
         {
            contextLogger.warning("Attempt to supply duplicate value for key [" + key + "]. The supplied value [" + value + "] was ignored.");
            return;
         }
      
         notifyDataAvailable(key, value);
      }
   }

   private void notifyDataAvailable(DataValueKey key, Object value)
   {
      DataAvailableEvent e = new DataAvailableEvent(key, value);
      List<Consumer<DataAvailableEvent>> ears = listeners.get(key);
      for (Consumer<DataAvailableEvent> ear : ears)
      {
         try 
         {
            ear.accept(e);
         }
         catch (Exception ex)
         {
            contextLogger.log(Level.WARNING, "Error attempting to notify listener of new data available for [" + key + "]", ex);
         }
      }
      
      ears.clear();
   }

   private void checkValueType(DataValueKey key, Object value)
   {
      if (!key.getType().isInstance(value))
      {
         String errMsg = "Invalid value for [{0}]. Value type [{1}] does not match expected type [{2}]";
         String msg = MessageFormat.format(errMsg, key, value.getClass(), key.getType());
         
         contextLogger.warning(msg);
         throw new IllegalArgumentException(msg);
      }
   }
   
   public Object get(DataValueKey key)
   {
      synchronized (this)
      {
         return values.get(key);
      }
   }
   
   public void close()
   {
      // TODO find all autoclosable items and close them
   }
   
   /**
    *  @deprecated To be replaced once {@link ExecutionContext#registerListener(DataValueListener)}
    *       is no longer used. 
    *
    */
   @Deprecated
   public static interface DataValueListener 
   {
      /**
       * 
       * @return A set of keys to listen for. Typically
       */
      Set<DataValueKey> getKeys();
      
      /**
       * Called when data is made available to the {@link ExecutionContext}.
       *  
       * @param key The {@link DataValueKey} that defines  
       * @param value
       */
      void dataAvailable(DataAvailableEvent e);

   }
   
   public static class DataAvailableEvent 
   {
   
      // TODO extend this to provide additional detail about the execution environment.
      
      private final DataValueKey key;
      private final Object value;
      
      DataAvailableEvent(DataValueKey key, Object value)
      {
         this.key = key;
         this.value = value;
      }
      
      public DataValueKey getKey() 
      {
         return key;
      }
      
      public Object getValue()
      {
         return value;
      }
   }
}
