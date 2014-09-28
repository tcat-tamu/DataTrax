package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.Transformer;

public class WorkflowImpl<IN, OUT> implements DataTransformWorkflow<IN, OUT>
{

   // TODO monitor performance of different transformers
   // TODO provide integrated 
   
   private final List<Transformer<?, ?>> transformers;
   
   public WorkflowImpl(List<Transformer<?, ?>> transformers)
   {
      this.transformers = transformers;;
   }

   @Override
   public Class<?> getSourceType()
   {
      Transformer<?, ?> transformer = transformers.get(0);
      return transformer.getSourceType();
   }

   @Override
   public Class<?> getOutputType()
   {
      Transformer<?, ?> transformer = transformers.get(transformers.size() - 1);
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

   public void process(Supplier<IN> input, Consumer<OUT> ears)
   {
   }
}
