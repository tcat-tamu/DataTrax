package edu.tamu.tcat.analytics.datatrax;

import java.util.function.Consumer;

import edu.tamu.tcat.analytics.datatrax.config.FactoryConfigurationException;

/**
 * Provides a structure for registering configurable algorithms ({@link Transformer}s) that 
 * transform input data instances of a particular type into output data.   
 *
 */
public interface TransformerFactoryRegistration
{
   // TODO Rename to TransformerRegistration - provides meta-informatation about a TransformerFactory (currently named Transformer)
   // NOTE Instantiation of a transformer is an implementation detail (see deprecated method below). 
   /**
    * @return The unique identifier used to register this {@code TransformerFactory} with a 
    *       {@link TransformerRegistry}. Will not be <code>null</code>.  
    */
   String getId();

   /**
    * @return The title of this {@code TransformerFactory} for display purposes.
    */
   String getTitle();

   /**
    * @return The Java type of objects that can be used as input to the {@link Transformer} 
    *    instances returned by this factory. Note that instances of the declared source type 
    *    and all sub-types must be accepted and correctly handled by the returned 
    *    {@code Transformer} instances.
    */
   Class<?> getDeclaredSourceType();
   
   /**
    * @return The Java type of objects that will be generated as output from the 
    *    {@link Transformer} instances returned by this factory. Note that the returned 
    *    {@code Transformer} instances will be required to supply output to a {@link Consumer} 
    *    that accepts instances of the declared output type or any super type. 
    */
   Class<?> getDeclaredOutputType();
   
   /**
    * Tests whether this {@code TransformerFactory} can accept input values of the supplied type.
    * 
    * @param type The Java type to test.
    * @return {@code true} if the supplied type is the same as or a sub-type of the declared 
    *    source type.
    * @throws FactoryConfigurationException If there are errors in the configuration of this 
    *    {@link TransformerFactoryRegistration} that prevent the evaluation of this method. Note that this
    *    could be caused by a invalid registration metadata or by errors in the application's 
    *    (for example, if a class provided by an OSGi bundle is no longer available to the OSGi 
    *    class loader).  
    */
   boolean canAccept(Class<?> type) throws FactoryConfigurationException;

   /**
    * Tests whether the output of this {@code TransformerFactory} can be offered to a 
    * {@link Consumer} that accepts objects of the supplied type. 
    * 
    * @param type The Java type to test
    * @return {@code true} if the supplied type is the same as or a super-type of the declared 
    *    output type.
    * @throws FactoryConfigurationException If there are errors in the configuration of this 
    *    {@link TransformerFactoryRegistration} that prevent the evaluation of this method. Note that this
    *    could be caused by a invalid registration metadata or by errors in the application's 
    *    (for example, if a class provided by an OSGi bundle is no longer available to the OSGi 
    *    class loader).  
    */
   boolean canProduce(Class<?> type) throws FactoryConfigurationException;
   
   /**
    * Creates an instance of the {@link Transformer} defined by this factory. Note that the 
    * returned {@code Transformer} can be configured and used independently of any 
    * {@link Transformer} returned by this factory.
    * <p>
    * Note that the type parameters {@code IN} and {@code OUT} must pass the 
    * {@link #canAccept(Class)} and {@link #canProduce(Class)} type check methods respectively 
    * in order to maintain type safety the returned {@link Transformer}. Failure to ensure this
    * will almost certainly result in a {@link ClassCastException} upon use.  
    * 
    * @return A instance of the {@link Transformer} defined by this factory.
    * @throws FactoryConfigurationException If there are errors in the configuration of this
    *    {@link TransformerFactoryRegistration} that prevent the instantiation of the {@link Transformer}.  
    *    Note that this could be caused by a invalid registration metadata or by errors in the 
    *    application's (for example, if a class provided by an OSGi bundle is no longer 
    *    available to the OSGi class loader).  
    */
   @Deprecated
   Transformer instantiate() throws FactoryConfigurationException;

}
