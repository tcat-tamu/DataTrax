package edu.tamu.tcat.analytics.datatrax.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerContext;

public class MockTransformer implements Transformer
{
   private static final String NAME = "name";
   private static final String SALUTATION = "salutation";

   public MockTransformer()
   {
      // TODO Auto-generated constructor stub
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
   public Callable<?> create(TransformerContext ctx)
   {
      final String prefix = (String)ctx.getValue(SALUTATION);
      final String name = (String)ctx.getValue(NAME);
      return new Callable<String>()
      {
         
         @Override
         public String call()
         {
            if (prefix != null)
               return prefix + " " + name;
            else 
               return "Hello " + name;
            
         }
      };
   }
}
