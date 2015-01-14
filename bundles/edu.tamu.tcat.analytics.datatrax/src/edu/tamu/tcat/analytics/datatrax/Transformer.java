package edu.tamu.tcat.analytics.datatrax;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.transform.TransformerFactory;

import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;

/**
 * Defines a configurable algorithm for converting input data instances into a single output. 
 * {@link TransformerFactory}s are the the main unit of operation that are stitched together 
 * to form a data transformation workflow. The workflow is defined by a {@link WorkflowConfiguration}. 
 * Data processing is managed by a {@link WorkflowController}. 
 * 
 *   
 * 
 * Rather than directly 
 * Generates a runnable that accepts input data of type {@code IN}, performs some data analysis
 * and/or transformation and produces output of type {@code OUT}. Implementations of 
 * {@code Transformer} are typically registered as extensions by their implementing 
 * bundle. 
 */
public interface Transformer
{

   /**
    * Provides configuration data to be used to parameterize this Transformer.
    * 
    * @param data
    * @throws TransformerConfigurationException
    */
   void configure(Map<String, Object> data) throws TransformerConfigurationException;
   
   /**
    * @return An object representing the currently configured state of this {@link Transformer}.
    *    The objects stored in the returned may should be of types that are easily serializable.
    *    Typically this means Object equivalents to Java primitives or POJOS composed of public
    *    fields that are Java primitives.
    */
   Map<String, Object> getConfiguration();
   
   /**
    * Returns a data processor (a {@link Callable}) that is used to process input data from
    * previously executed {@link Transformer}s or other data sources (e.g., source data 
    * supplied to the {@link WorkflowController}). The supplied {@link TransformerContext} 
    * allows the implementation to retrieve data object that correspond to the labels defined
    * by the registered input pins. These data objects will be available for retrieval when
    * the {@link #create(TransformerContext)} method is invoked and may be removed from the 
    * context once this method returns. 
    * 
    * <p>
    * The returned processor will be used only once and scheduled for execution by the 
    * {@link WorkflowController}. Consequently, it must not have side effects or dependencies
    * on data external data sources that may be changed. Specifically, it must not change any 
    * internal state associated with the {@link Transformer} or modify the supplied input data 
    * if those objects are mutable. It may depend on the internal state of the {@link Transformer},
    * (for example, on configuration parameters) or other external data sources only if all
    * required invariants are stable over time.  
    * 
    * @param ctx A data context object for use in retrieving any supplied source data.
    * @return A data processor that will be scheduled to run by the {@link WorkflowController}.
    */
   Callable<?> create(TransformerContext ctx);
}
