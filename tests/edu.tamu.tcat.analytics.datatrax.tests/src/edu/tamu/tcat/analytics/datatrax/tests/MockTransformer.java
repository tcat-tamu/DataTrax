package edu.tamu.tcat.analytics.datatrax.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerFactory;

public class MockTransformer implements TransformerFactory<String, String>
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
   public void configure(Map<String, Object> data) throws TransformerConfigurationException
   {
   }

   @Override
   public Map<String, Object> getConfiguration()
   {
      return new HashMap<String, Object>();
   }

   @Override
   public Runnable create(final Supplier<? extends String> source, final Consumer<? super String> sink)
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
