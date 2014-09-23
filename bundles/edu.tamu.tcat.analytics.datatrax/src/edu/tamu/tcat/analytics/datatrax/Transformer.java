package edu.tamu.tcat.analytics.datatrax;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Defines a configurable algorithm for creating data transformers. 
 * 
 * Rather than directly 
 * Generates a runnable that accepts input data of type {@code IN}, performs some data analysis
 * and/or transformation and produces output of type {@code OUT}. Implementations of 
 * {@code Transformer} are typically registered as extensions by their implementing 
 * bundle. 
 *
 * @param <IN>
 * @param <OUT>
 */
public interface Transformer<IN, OUT>
{
   // TODO might provide these as part of the metadata 
   /**
    * @return A type token for the input data type. 
    */
   Class<IN> getSourceType();
   
   /**
    * @return A type token for the output data type. 
    */
   Class<OUT> getOutputType();
   
   /**
    * 
    * @param data
    * @throws InvalidTransformerConfiguration
    */
   void configure(Map<String, Object> data) throws InvalidTransformerConfiguration;
   
   /**
    * 
    * @return
    */
   Map<String, Object> getConfiguration();
   
//   OUT transform(IN source) throws Exception;
   
   /**
    * NOTES:
    * - Returns a data processor (a Runnable) that can be used to process a single input data instance.
    * - This processor will be used only once.
    * - Must not have side effects (must not modify the source object if that object is mutable)
    *     --  May use the API provided by the DataSink and DataSource to access a provided cache
    *         or update metadata about the generated object
    * - We require the creation of a Runnable (as opposed to using a method like 
    *   {@code OUT transform(IN source)} in order to help clarify that the data processing must
    *   not modify the state of the transformer. That is, once configured, the transformer 
    *   must be thread-safe. 
    *  
    * @param source
    * @param sink
    * @return
    */
   Runnable create(Supplier<IN> source, Consumer<OUT> sink);
   
   // TODO need to provide sub-interface to supplier/consumer that will supply 
   //      pass-through context data for the purpose (for example) of annotation the image 
   //      source or writing intermediate results


}
