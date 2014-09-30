package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.TransformerFactory;

public class WorkflowImpl<IN, OUT> implements DataTransformWorkflow<IN, OUT>
{

   // TODO monitor performance of different transformers
   // TODO provide integrated 
   
   private final List<TransformerFactory<?, ?>> transformers;
   
   public WorkflowImpl(List<TransformerFactory<?, ?>> transformers)
   {
      this.transformers = transformers;;
   }

   @Override
   public Class<?> getSourceType()
   {
      TransformerFactory<?, ?> transformer = transformers.get(0);
      return transformer.getSourceType();
   }

   @Override
   public Class<?> getOutputType()
   {
      TransformerFactory<?, ?> transformer = transformers.get(transformers.size() - 1);
      return transformer.getOutputType();
   }

   @Override
   public boolean canAccept(Class<?> type)
   {
      return getSourceType().isAssignableFrom(type);
   }

   @Override
   public boolean canProduce(Class<?> type) 
   {
      return type.isAssignableFrom(getOutputType());
   }

   @Override
   @SuppressWarnings({ "rawtypes", "unchecked" }) // type safety is ensured by workflow construction
   public void process(Supplier<? extends IN> input, Consumer<? super OUT> ears)
   {
      List<Runnable> chain = new ArrayList<Runnable>();
      Supplier src = input;
      for (int i = 0; i < transformers.size(); i++)
      {
         TransformerFactory<?, ?> transformer = transformers.get(i);
         if (i < transformers.size() - 1) 
         {
            InOutShim shim = new InOutShim<>();
            chain.add(transformer.create(src, shim));
            src = shim;
         }
         else 
         {
            chain.add(transformer.create(src, (Consumer)ears));
         }
      }
      
      for (Runnable link : chain)
      {
         link.run();
      }
   }
}
