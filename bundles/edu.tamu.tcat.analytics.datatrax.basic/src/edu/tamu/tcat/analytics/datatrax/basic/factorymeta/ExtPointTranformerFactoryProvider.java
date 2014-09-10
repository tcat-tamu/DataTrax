package edu.tamu.tcat.analytics.datatrax.basic.factorymeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;

import edu.tamu.tcat.analytics.datatrax.TransformerFactoryProvider;

public class ExtPointTranformerFactoryProvider implements TransformerFactoryProvider
{

   public static final String EXT_POINT_ID = "edu.tamu.tcat.analytics.datatrax.transformfactories";

   private final ConcurrentMap<String, TransformerFactoryDefinition> factoryDefinitions = new ConcurrentHashMap<>();

   private RegistryEventListener ears;
   
   public ExtPointTranformerFactoryProvider()
   {
   }

   public void activate()
   {
      
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
    * Loads all 
    */
   public void loadConfiguration() 
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
   
   public Collection<TransformerFactoryDefinition> getFactories()
   {
      return new ArrayList<TransformerFactoryDefinition>(factoryDefinitions.values());
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
            TransformerFactoryDefinition configuration = new TransformerFactoryDefinition(e);
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
