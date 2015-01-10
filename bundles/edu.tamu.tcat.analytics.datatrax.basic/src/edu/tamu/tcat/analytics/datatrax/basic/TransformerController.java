package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerContext;
import edu.tamu.tcat.analytics.datatrax.basic.ExecutionContext.DataAvailableEvent;
import edu.tamu.tcat.analytics.datatrax.basic.ExecutionContext.DataValueListener;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowControllerImpl.TaskExecutionService;
import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;

/**
 * Manages the execution lifecycle of an individual {@link Transformer}. This controller is 
 * notified when new data values become available (via {@link #dataAvailable(DataValueKey, Object)}.
 * Once all data input values that have been stitched to input pins of the managed 
 * {@code Transformer} have been supplied, it will submit a {@link Runnable} to the 
 * {@link TaskExecutionService} for execution. Upon completion, it will provide the results
 * of the data transformation to the supplied {@link ExecutionContext}. 
 * 
 * <p>
 * The {@code TransformerController} is intended only for internal use by the DataTrax 
 * framework implementation. Specifically, the {@link WorkflowExecutor} creates a 
 * {@code TransformerController} for each configured {@code Transformer} and 
 * registers them as listeners on the {@code ExecutionContext}.
 */
public class TransformerController implements DataValueListener
{
   private AtomicInteger ct;
   
   private DataInputMap inputs = new DataInputMap();

   private final UUID id;
   private final Transformer transformer;
   private final TaskExecutionService exec;
   private final ExecutionContext context;

   private final SimpleDataValueKey resultKey;
   private AutoCloseable listenerRegistration;

   public TransformerController(Transformer transformer, TransformerConfiguration cfg, TaskExecutionService exec, ExecutionContext context)
   {
      this.id = UUID.randomUUID();
      this.transformer = transformer;
      this.exec = exec;
      this.context = context;
      
      for (DataInputPin pin : cfg.getDefinedInputs())
      {
         UUID dataSource = cfg.getDataSource(pin);
         SimpleDataValueKey key = new SimpleDataValueKey(dataSource, pin.type);
         
         inputs.define(key, pin.label);
      }
      
      resultKey = new SimpleDataValueKey(cfg.getId(), cfg.getOutputType());
      ct = new AtomicInteger(inputs.size());
   }
   
   // HACK: need a better way to do this - highly coupled. Better to listen to this transformer
   //       and close the listener reg externally once it is finished.
   public void setListenerRegistration(AutoCloseable listenerRegistration)
   {
      this.listenerRegistration = listenerRegistration;
   }

   @Override
   public Set<DataValueKey> getKeys()
   {
      return inputs.labels.keySet();
   }
   
   @Override
   public void dataAvailable(DataAvailableEvent e)
   {
      DataValueKey key = e.getKey();
      Object value = e.getValue();
      
      
      if (!inputs.isDefined(key))
         return;
      
      if (inputs.isSet(key))
         throw new IllegalStateException("A value has already ben set for [" + key + "]");
      
      inputs.setValue(key, value);
      
      if (ct.decrementAndGet() == 0)
      {
         execute();
      }
   }
   
   public void cancel()
   {
      try
      {
         if (this.listenerRegistration != null)
            this.listenerRegistration.close();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      throw new UnsupportedOperationException();
   }

   private void execute()
   {
      try
      {
         if (this.listenerRegistration != null)
            this.listenerRegistration.close();        // remove listener reg. No longer needed.
         
         TransformerExecutionTask task = new TransformerExecutionTask();
         exec.execute(task);
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }     
   }
   
   private class TransformerExecutionTask implements Runnable
   {
      public TransformerExecutionTask()
      {
      }
      
      @Override
      public void run() 
      {
         try 
         {
            // TODO notify about to execute
            Object result = transformer.create(inputs);
            // TODO ensure type safety
            // TODO notify execution complete
            context.put(resultKey, result);
         } 
         catch (Exception ex)
         {
            // FIXME add logging
            ex.printStackTrace();
         }
      }
   }
   
   /**
    * A convenience data structure that maps input data keys and data values to the 
    * corresponding input pin labels on the transformer. This is used to collect data objects 
    * as they are made available via the {@link ExecutionContext} and in turn supply them to 
    * a transformer via the {@link TransformerContext} API. 
    */
   private static class DataInputMap implements TransformerContext
   {
      // maps data keys to the input pins of a transformer
      private Map<DataValueKey, String> labels = new HashMap<>();    // effectively immutable after construction
      private Map<String, Object> values = new ConcurrentHashMap<>();
      
      public void define(DataValueKey key, String label)
      {
         labels.put(key, label);
      }
      
      public int size()
      {
         return labels.size();
      }
      
      /**
       * If the supplied key is defined is defined as an input to the associated transformer.
       * 
       * @param key The data input key to check.
       * @return
       */
      public boolean isDefined(DataValueKey key)
      {
         return labels.containsKey(key);
      }
      
      public String getLabel(DataValueKey key)
      {
         return labels.get(key);
      }
      
      /**
       * Indicates whether a value has been set for the specified key.
       * 
       * @param key The data input key to check.
       * @return True if a value has been supplied fot the label associated with this key.
       */
      public boolean isSet(DataValueKey key)
      {
         if (!isDefined(key))
            return false;

         String label = getLabel(key);
         return values.containsKey(label);
      }
      
      void setValue(DataValueKey key, Object value)
      {
         String label = labels.get(key);
         if (label == null)
            throw new IllegalArgumentException("Invalid input value [" + key + "]. Value does not mapt to a defined input for this transformer.");
       
         values.put(label, value);
      }
      
      @Override
      public Object getValue(String label)
      {
         // TODO check to ensure that the label is defined and that a value has been supplied.
         return values.get(label);
      }
   }
}