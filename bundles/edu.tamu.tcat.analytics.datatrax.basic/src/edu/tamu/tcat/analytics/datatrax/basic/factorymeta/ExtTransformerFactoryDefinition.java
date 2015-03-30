package edu.tamu.tcat.analytics.datatrax.basic.factorymeta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.FactoryConfigurationException;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;

/**
 * Wraps a transformer factory's plugin metadata declaration to provide an API for querying and 
 * and interacting with that factory. 
 *
 */
public class ExtTransformerFactoryDefinition implements TransformerRegistration
{
   // TODO handle un-registration of the defining plugin 
   
   private final String id;
   private final String title;
   private final String description;
   private final Set<DataInputPin> inputPins;
   private final Class<?> outputType;
   
   private final IConfigurationElement config;

   /**
    * 
    * @param e The configuration element 
    * @throws FactoryConfigurationException If the supplied configuration cannot be processed.
    */
   public ExtTransformerFactoryDefinition(IConfigurationElement e)
   {
      config = e;
      id = config.getAttribute("id");
      title = config.getAttribute("title");
      description = config.getAttribute("description");
      
      IConfigurationElement[] children = config.getChildren("inputs");
      inputPins = loadInputPins(children);
      
      String outType = config.getAttribute("output_type");
      outputType = loadClass(config, outType);
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
   public String getDescription()
   {
      return description;
   }

   @Override
   public Set<DataInputPin> getDeclaredInputs()
   {
      return Collections.unmodifiableSet(inputPins);
   }

   @Override
   public DataInputPin getDeclaredInput(String name) 
   {
      // TODO we could use a map.
      for (DataInputPin pin : inputPins)
      {
         if (pin.label.equals(name))
            return pin;
      }
      
      throw new IllegalArgumentException("Undefined input pin [" + name + "]");
   }
   
   @Override
   public Class<?> getDeclaredOutputType()
   {
      return outputType;
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
   public boolean canAccept(Class<?> type) 
   {
      for (DataInputPin pin : inputPins)
      {
         if (pin.type.isAssignableFrom(type))
            return true;
      }
      
      return false;
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
      Class<?> declaredOutputType = getDeclaredOutputType();
      return type.isAssignableFrom(declaredOutputType);
   }
   
   @Override
   public Transformer instantiate(TransformerConfiguration cfg) throws FactoryConfigurationException
   {
      try
      {
         Transformer factory = (Transformer)config.createExecutableExtension("class");
         factory.configure(cfg.getDefinedParameters().parallelStream()
               .collect(Collectors.toMap(key -> key, key -> cfg.getParameter(key))));
         return factory;
      }
      catch (CoreException e)
      {
         String msg = "Failed to construct an instance of the requested transformation factory: " + this 
               + ". Could intantiate factory instance.";
         throw new IllegalStateException(msg, e);
      }
      catch (TransformerConfigurationException e)
      {
         throw new FactoryConfigurationException("Failed to configure transformer [" + config.getAttribute("class") + "]. Invalid configuration data.", e);
      }
   }
   
   @Override
   public String toString()
   {
      return title + " (" + id + ")";
   }

   private static Set<DataInputPin> loadInputPins(IConfigurationElement[] elems)
   {
      Set<DataInputPin> pins = new HashSet<>();
      for (IConfigurationElement elem : elems)  // should be one
      {
         IConfigurationElement[] pinEls = elem.getChildren();
         for (IConfigurationElement pinEl : pinEls)
         {
            pins.add(createDataInputPin(pinEl));
         }
      }
      
      return pins;
   }
   
   private static DataInputPin createDataInputPin(IConfigurationElement e) throws FactoryConfigurationException
   {
      DataInputPin pin = new DataInputPin();
      
      pin.label = e.getAttribute("label");
      pin.description = e.getAttribute("desription");
      String req = e.getAttribute("required");
      pin.required = Boolean.parseBoolean(req);
      pin.type = loadClass(e, e.getAttribute("type"));
      
      return pin;
   }

   private static Class<?> loadClass(IConfigurationElement config, String type) throws FactoryConfigurationException
   {
      IContributor contributor = config.getContributor();
      String symbolicName = contributor.getName();
      Bundle bundle = Platform.getBundle(symbolicName);
      if (bundle == null)
         throw new FactoryConfigurationException("Failed to load source bundle (" + symbolicName + ")");
      
      try 
      {
         return bundle.loadClass(type);
      } 
      catch (ClassNotFoundException cnfe)
      {
         throw new FactoryConfigurationException("Failed to load class (" + type +").", cnfe);
      }
      catch (IllegalStateException ise)
      {
         throw new FactoryConfigurationException("Unavailable source bundle (" + symbolicName + ").", ise);
      }
   }
}