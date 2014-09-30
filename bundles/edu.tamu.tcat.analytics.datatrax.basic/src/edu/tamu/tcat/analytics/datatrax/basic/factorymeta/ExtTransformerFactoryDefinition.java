package edu.tamu.tcat.analytics.datatrax.basic.factorymeta;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerFactory;
import edu.tamu.tcat.analytics.datatrax.config.FactoryConfigurationException;

/**
 * Wraps a transformer factory's plugin metadata declaration to provide an API for querying and 
 * and interacting with that factory. 
 *
 */
public class ExtTransformerFactoryDefinition implements TransformerFactory
{
   // TODO handle un-registration of the defining plugin 
   private final String id;
   private final String title;
   private final IConfigurationElement config;
   
   public ExtTransformerFactoryDefinition(IConfigurationElement e)
   {
      config = e;
      id = config.getAttribute("id");
      title = config.getAttribute("title");
   }
   
   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getTitle()
   {
      return title;
   }
   
   @Override
   public Class<?> getDeclaredSourceType()
   {
      String srcType = config.getAttribute("source_type");
      try 
      {
         return loadClass(srcType);
      } 
      catch (FactoryConfigurationException fce)
      {
         throw new IllegalStateException("Failed to retrieve source type [" + srcType + "] for transformer factory definition: " + this, fce);
      }
   }
   
   @Override
   public Class<?> getDeclaredOutputType()
   {
      String outType = config.getAttribute("output_type");
      try 
      {
         return loadClass(outType);
      } 
      catch (FactoryConfigurationException fce)
      {
         throw new IllegalStateException("Failed to retrieve output type [" + outType + "] for transformer factory definition: " + this, fce);
      }
   }
   
   /**
    * Indicates whether the defined transformer factory can accept input values of 
    * the supplied type. Specifically, this determines if the declared source data type 
    * associated with this factory is the same as or a super type of the supplied type.
    * 
    * @param type The Java type to check.
    * @return True if the defined factory can accept instances of the supplied type as inputs
    *       for processing. 
    * @throws IllegalStateException If there are configuration errors that prevent the 
    *       evaluation of the supplied type. Note that this includes the case when the bundle
    *       that supplies this factory is no longer available.
    */
   @Override
   public boolean canAccept(Class<?> type) throws FactoryConfigurationException
   {
      // TODO should this be a RuntimeException. The client cannot recover (in general) and 
      //      once the defn is instantiated and made available, the configuration should 
      //      be valid. The caveat is when the required bundle is still being loaded, but this should not be the case.

      String srcType = config.getAttribute("source_type");
      
      try 
      {
         Class<?> declaredSourceType = loadClass(srcType);
         return declaredSourceType.isAssignableFrom(type);
      }
      catch (FactoryConfigurationException ex)
      {
         throw new IllegalStateException("Invalid transformer factory configuration: " + this + ". This may indicate that the providing plugin is no longer available. ", ex);
      }
   }
   
   /**
    * Indicates whether the defined transformer factory produces results that can are compatible 
    * with the supplied type. Specifically, this determines if the declared output data type 
    * associated with this factory is the same as or a a subclass of the supplied type.
    * 
    * @param type The Java type to check.
    * @return True if the defined factory can accept instances of the supplied type as inputs
    *       for processing. 
    * @throws IllegalStateException If there are configuration errors that prevent the 
    *       evaluation of the supplied type. Note that this includes the case when the bundle
    *       that supplies this factory is no longer available.
    */
   @Override
   public boolean canProduce(Class<?> type) throws FactoryConfigurationException
   {
      String srcType = config.getAttribute("output_type");
      if (srcType == null || srcType.trim().isEmpty())
         throw new FactoryConfigurationException("Invalid factory configuration for " + this + ". No source_type defined.");
      
      Class<?> declaredOutputType = loadClass(srcType);
      return type.isAssignableFrom(declaredOutputType);
   }
   
   public <IN, OUT> Transformer<IN, OUT> instantiate() throws FactoryConfigurationException
   {
      try
      {
         @SuppressWarnings("unchecked") // type information must be known out of band.
         Transformer<IN, OUT> factory = (Transformer<IN, OUT>)config.createExecutableExtension("class");
         return factory;
      }
      catch (CoreException e)
      {
         String msg = "Failed to construct an instance of the requested transformation factory: " + this 
               + ". Could intantiate factory instance.";
         throw new IllegalStateException(msg, e);
      }
   }

   private Class<?> loadClass(String type) throws FactoryConfigurationException
   {
      IContributor contributor = config.getContributor();
      String symbolicName = contributor.getName();
      Bundle bundle = Platform.getBundle(symbolicName);
      if (bundle == null)
         throw new FactoryConfigurationException("Failed to load source bundle (" + symbolicName + ") for transformer factory " + this);
      
      try 
      {
         return bundle.loadClass(type);
      } 
      catch (ClassNotFoundException cnfe)
      {
         throw new FactoryConfigurationException("Failed to load class (" + type +") for transformer factory " + this, cnfe);
      }
      catch (IllegalStateException ise)
      {
         throw new FactoryConfigurationException("Unavailable source bundle (" + symbolicName + ") for transformer factory " + this, ise);
      }
   }

   @Override
   public String toString()
   {
      return title + " (" + id + ")";
   }
}