package edu.tamu.tcat.analytics.datatrax.basic.factorymeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.Transformer;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistry;

/**
 *  
 *
 */
public class ExtPointTranformerFactoryRegistry implements TransformerRegistry
{

   public static final String EXT_POINT_ID = "edu.tamu.tcat.analytics.datatrax.transformer";

   private final ConcurrentMap<String, ExtTransformerFactoryDefinition> factoryDefinitions = new ConcurrentHashMap<>();

   private RegistryEventListener ears;
   
   public ExtPointTranformerFactoryRegistry()
   {
   }

   /**
    * Initializes this {@link ExtPointTranformerFactoryProvider}. This must be called during 
    * initial configuration in order to load {@link Transformer} plugins.
    */
   public void activate()
   {
      // TODO do async and wait until ready.
      loadExtensions();
   }
   
   public void dispose()
   {
      // unregister listener
      if (ears != null)
      {
         IExtensionRegistry registry = Platform.getExtensionRegistry();
         registry.removeListener(ears);
      }
      
      // clear all loaded factory definitions
      factoryDefinitions.clear();
   }
   
   /**
    * Loads all currently registered extensions of the the DataTrax Transformers extension 
    * point and attaches a listener to the {@link IExtensionRegistry} that will be notified 
    * when new extensions become available or loaded extensions are removed.
    * 
    * <p>
    * This method is typically called by {@link #activate()} when the {@code ExtPointTranformerFactoryProvider} 
    * is registered as an OSGi service or when the provider is manually activated.
    */
   private void loadExtensions() 
   {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      ears = new RegistryEventListener();
      registry.addListener(ears, EXT_POINT_ID);
      
      // register any currently loaded transformers
      IExtension[] extensions = registry.getExtensionPoint(EXT_POINT_ID).getExtensions();
      for (IExtension ext : extensions)
      {
         ears.parseExtension(ext);
      }
   }
   
   @Override
   public Collection<String> getTransformers()
   {
      return new ArrayList<String>(factoryDefinitions.keySet());
   }

   @Override
   public boolean isRegistered(String id)
   {
      ExtTransformerFactoryDefinition factory = factoryDefinitions.get(id);
      return (factory != null);
   }
   
   @Override
   public ExtTransformerFactoryDefinition getFactory(String id) throws FactoryUnavailableException 
   {
      ExtTransformerFactoryDefinition factory = factoryDefinitions.get(id);
      if (factory == null)
      {
         throw new FactoryUnavailableException("No factory is registered for id [" + id + "]");
      }
      
      return factory;
   }

   @Override
   public <X> Collection<String> getCompatibleFactories(Class<X> sourceType)
   {
      Collection<String> matches = new HashSet<>();
      for (ExtTransformerFactoryDefinition defn : factoryDefinitions.values())
      {
         if (defn.canAccept(sourceType))
            matches.add(defn.getId());
      }
      
      return Collections.unmodifiableCollection(matches);
   }

   @Override
   public <X> Collection<String> getProducingFactories(Class<X> outputType)
   {
      Collection<String> matches = new HashSet<>();
      for (ExtTransformerFactoryDefinition defn : factoryDefinitions.values())
      {
         if (defn.canProduce(outputType))
            matches.add(defn.getId());
      }
      
      return Collections.unmodifiableCollection(matches);
   }
   
   private class RegistryEventListener implements IRegistryEventListener
   {

      @Override
      public void added(IExtension[] extensions)
      {
         for (IExtension ext : extensions)  
         {
            parseExtension(ext);
         }
      }

      private void parseExtension(IExtension ext)
      {
         IConfigurationElement[] elements = ext.getConfigurationElements();
         
         for (IConfigurationElement e : elements)
         {
            ExtTransformerFactoryDefinition configuration = new ExtTransformerFactoryDefinition(e);
            factoryDefinitions.putIfAbsent(configuration.getId(), configuration);
            
            // TODO log duplicate registration
         }
      }

      @Override
      public void removed(IExtension[] extensions)
      {
         for (IExtension ext : extensions)
         {
            IConfigurationElement[] elements = ext.getConfigurationElements();
            
            for (IConfigurationElement e : elements)
            {
               String id = e.getAttribute("id");
               factoryDefinitions.remove(id);
            }
         }
      }

      @Override
      public void added(IExtensionPoint[] extensionPoints)
      {
         // no-op
      }

      @Override
      public void removed(IExtensionPoint[] extensionPoints)
      {
         // no-op
      }
      
   }
}
