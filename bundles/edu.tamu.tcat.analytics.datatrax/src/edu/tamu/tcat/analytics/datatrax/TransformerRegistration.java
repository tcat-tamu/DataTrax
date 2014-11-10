package edu.tamu.tcat.analytics.datatrax;

import java.util.Set;
import java.util.function.Consumer;

import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.FactoryConfigurationException;

/**
 * Provides a structure for registering configurable algorithms ({@link Transformer}s) that 
 * transform input data instances of a particular type into output data.   
 *
 */
public interface TransformerRegistration
{
   // NOTE Instantiation of a transformer is an implementation detail (see deprecated method below). 
   /**
    * @return The unique identifier used to register this {@code TransformerFactory} with a 
    *       {@link TransformerRegistry}. Will not be <code>null</code>.  
    */
   String getId();

   /**
    * @return The title of the registered {@code Transformer} for display purposes.
    */
   String getTitle();
   
   /**
    * @return A description of the registered {@link Transformer} for display purposes.
    */
   String getDescription();

   /**
    * @return The input data objects that this transformer operates on. 
    */
   Set<DataInputPin> getDeclaredInputs();
   
   /**
    * @return The Java type of objects that will be generated as output by the registered 
    *    {@link Transformer}. 
    */
   Class<?> getDeclaredOutputType();
   
   /**
    * Tests whether this {@code TransformerFactory} can accept input values of the supplied type.
    * 
    * @param type The Java type to test.
    * @return {@code true} if the supplied type is the same as or a sub-type of the declared 
    *    source type.
    * @throws FactoryConfigurationException If there are errors in the configuration of this 
    *    {@link TransformerRegistration} that prevent the evaluation of this method. Note that this
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
    *    {@link TransformerRegistration} that prevent the evaluation of this method. Note that this
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
    *    {@link TransformerRegistration} that prevent the instantiation of the {@link Transformer}.  
    *    Note that this could be caused by a invalid registration metadata or by errors in the 
    *    application's (for example, if a class provided by an OSGi bundle is no longer 
    *    available to the OSGi class loader).  
    */
   Transformer instantiate() throws FactoryConfigurationException;

}
