package edu.tamu.tcat.analytics.datatrax.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.tamu.tcat.analytics.datatrax.InvalidTransformerConfiguration;
import edu.tamu.tcat.analytics.datatrax.Transformer;

public class MockTransformer implements Transformer<String, String>
{

   public MockTransformer()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public Class<String> getSourceType()
   {
      return String.class;
   }

   @Override
   public Class<String> getOutputType()
   {
      return String.class;
   }

   @Override
   public void configure(Map<String, Object> data) throws InvalidTransformerConfiguration
   {
   }

   @Override
   public Map<String, Object> getConfiguration()
   {
      return new HashMap<String, Object>();
   }

   @Override
   public Runnable create(final Supplier<String> source, final Consumer<String> sink)
   {
      return new Runnable()
      {
         
         @Override
         public void run()
         {
            String s = source.get();
            if (s == null)
               throw new IllegalStateException("Invalid string. Null value.");
            
            sink.accept("Hello " + s);
            
         }
      };
   }

}
