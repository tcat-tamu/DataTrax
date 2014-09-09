package edu.tamu.tcat.analytics.datatrax;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generates a runnable that accepts input data of type {@code IN}, performs some data analysis
 * and/or transformation and produces output of type {@code OUT}. Implementations of 
 * {@code TransformerFactory} are typically registered as extensions by their implementing 
 * bundle. 
 *
 * @param <IN>
 * @param <OUT>
 */
public interface TransformerFactory<IN, OUT>
{
   
   /**
    * @return A type token for the input data type. 
    */
   Class<IN> getSourceType();
   
   /**
    * @return A type token for the output data type. 
    */
   Class<OUT> getOutputType();
   
   void configure(Map<String, Object> data) throws InvalidTransformerConfiguration;
   
   Map<String, Object> getConfiguration();
   
   Runnable create(Supplier<IN> source, Consumer<OUT> sink);
   // TODO need to provide sub-interface to supplier/consumer that will supply 
   //      pass-through context data for the purpose (for example) of annotation the image 
   //      source or writing intermediate results


}
