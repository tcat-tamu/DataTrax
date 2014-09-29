package edu.tamu.tcat.analytics.datatrax;

import java.util.Collection;


/**
 * A service that provides access to {@link Transformer} implementations that have been
 * registered with the framework.
 *
 */
public interface TransformerFactoryRegistry
{

   Collection<TransformerFactory> getFactories();     // TODO return string ids

   /**
    * Indicates whether a factory is registered for the provided id. Note that the id under whci 
    * 
    * @param id 
    * @return
    */
   boolean isRegistered(String id);
   
   /**
    * 
    * @param id The id of the factory to retrieve.
    * @return The factory registered under the supplied id. Will not be {@code null}. 
    * @throws FactoryUnavailableException If a factory is not registered for the supplied 
    *       {@code id}. Call {@link #isRegistered(String)} to check if the factory is configured
    *       prior to calling this method.
    * 
    */
   TransformerFactory getFactory(String id) throws FactoryUnavailableException;     // should return what is currently the Transformer
   
   // TODO getRegistration(String id) -- should return what is currently the TransformerFactory
   /**
    * Retrieves all factories that can accept input data sources of the supplied type. Note 
    * that the returned factories will accept input data of this type or any sub-type.
    * 
    * @param sourceType The type of objects that can be consumed by the returned factories. 
    * @return All currently registered factories that will process data of the supplied 
    *       type. Will not be null, may be empty.
    */
   <X> Collection<TransformerFactory> getCompatibleFactories(Class<X> sourceType);  // TODO return string ids
   
   /**
    * Retrieves all factories that produce output data of the supplied type. Note that the 
    * returned factories will convert their inputs into instances of the supplied type or a 
    * sub-type.
    * 
    * @param outputType The type of objects to be produced by the returned factories.  
    * @return All currently registered factories that can convert data into the supplied type. 
    *       Will not be null, may be empty.
    */
   <X> Collection<TransformerFactory> getProducingFactories(Class<X> outputType);   // TODO return string ids

   
   

}
