package edu.tamu.tcat.analytics.datatrax;

import java.util.Collection;


/**
 * A service that provides access to {@link Transformer} implementations that have been
 * registered with the framework.
 *
 */
public interface TransformerRegistry
{
   // TODO rename methods
   // TODO add listeners for update/removal events 


   /**
    * @return The ids of all transformers currently registered with the registry. Note that 
    *       the registry may be updated over time so subsequent calls may return different 
    *       results and transformers returned by this method may be subsequently removed.
    */
   Collection<String> getTransformers();     // TODO return string ids

   /**
    * Indicates whether a transformer is registered for the provided id.  
    * 
    * @param id The identifier for the transformer to evaluate. 
    * @return {@code true} if a transformer is registered with the provided id.
    * @deprecated This method is confusing. In general, since the registry can be updated at 
    *       any time, a call to this method does not indicate that a subsequent call to 
    *       {@link #getFactory(String)} or any other method will succeed. Consequently, this 
    *       method should not be used to prevent 
    */
   boolean isRegistered(String id);
   
   /**
    * Retrieves a transformer for the specified id. Note that, since the registry may be 
    * updated at any time, the F
    * 
    * @param id The id of the transformer to retrieve.
    * @return The data transformer registered under the supplied id. Will not be {@code null}. 
    * @throws FactoryUnavailableException If a transformer is not registered for the supplied 
    *       {@code id}.   
    * 
    */
   TransformerFactoryRegistration getFactory(String id) throws FactoryUnavailableException;    
   
   /**
    * Retrieves all transformers that can accept input data sources of the supplied type. Note 
    * that the returned transformers will accept input data of this type or any sub-type.
    * 
    * @param sourceType The type of objects that can be consumed by the returned transformers. 
    * @return Identifiers for all currently registered transformers that accept input object 
    *       of the specified type. Will not be {@code null}, may be empty.
    */
   <X> Collection<String> getCompatibleFactories(Class<X> sourceType);  
   
   /**
    * Retrieves all transformers that produce output data of the supplied type. The 
    * returned transformers will convert their inputs into instances of the supplied type or a 
    * sub-type.
    * 
    * @param outputType The type of objects to be produced by the returned transformers.  
    * @return All currently registered transformers that can convert data into the supplied 
    *       type. Will not be {@code null}, may be empty.
    */
   <X> Collection<String> getProducingFactories(Class<X> outputType);   
   
}
