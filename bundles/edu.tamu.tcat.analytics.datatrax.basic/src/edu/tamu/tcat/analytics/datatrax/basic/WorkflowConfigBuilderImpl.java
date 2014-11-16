package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistration;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfigEditor;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationBuilder;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationException;

/**
 *
 * Note that this class is not thread safe. 
 */
public class WorkflowConfigBuilderImpl implements WorkflowConfigurationBuilder
{

   // TODO restore from existing configuration
   
   private final ExtPointTranformerFactoryRegistry registry;

   private UUID id;
   private String title;
   private String description;
   private Class<?> type;

   private Map<UUID, TransformerConfigEditor> transformerEditors = new HashMap<>();
   private Set<UUID> outputs = new HashSet<>();

   
   public WorkflowConfigBuilderImpl(ExtPointTranformerFactoryRegistry reg)
   {
      this.registry = reg;
//      this.cfg = new WorkflowConfiguration();
   }
   
   public static WorkflowConfigBuilderImpl create(ExtPointTranformerFactoryRegistry reg, WorkflowConfiguration config) throws WorkflowConfigurationException 
   {
      WorkflowConfigBuilderImpl impl = new WorkflowConfigBuilderImpl(reg);
      
      impl.id = config.getId();
      impl.title = config.getTitle();
      impl.description = config.getDescription();
      
      for (TransformerConfiguration tCfg : config.getTransformers())
      {
         appendEditor(reg, tCfg, impl);
      }
      
      return impl;
   }

   private static void appendEditor(ExtPointTranformerFactoryRegistry reg, TransformerConfiguration tCfg, WorkflowConfigBuilderImpl impl) throws WorkflowConfigurationException
   {
      try 
      {
         UUID tId = tCfg.getId();
         TransformerConfigData data = TransformerConfigData.create(tCfg);
         TransformerConfigEditor editor = SimpleTransformerConfig.instantiate(reg, data);

         impl.transformerEditors.put(tId, editor);
      }
      catch (FactoryUnavailableException fue)
      {
         throw new WorkflowConfigurationException("Failed to load configuration for transformer [" + tCfg.getId() + "]", fue);
      }
      catch (IllegalArgumentException iae)
      {
         throw new WorkflowConfigurationException("Invalid transformer id [" + tCfg.getId() + "]. Expected UUID.", iae);
      }
   }

   public static Map<UUID, String> checkConfiguration(ExtPointTranformerFactoryRegistry reg, WorkflowConfiguration config)
   {
      // TODO check the supplied configuration and determine if there are any configuration errors.
      
      throw new UnsupportedOperationException();
   }
   
   @Override
   public void setTitle(String title)
   {
      Objects.requireNonNull(title, "The workflow title cannot be null.");
      
      this.title = title;
   }
   
   @Override
   public void setDescription(String desc)
   {
      if (desc == null)
         desc = "";
      
      this.description = desc;
   }
   
   @Override
   public void setInputType(Class<?> cls)
   {
      this.type = cls;
   }
   
   @Override
   public Set<UUID> listTransformers()
   {
      return transformerEditors.keySet();
   }

   @Override
   public TransformerConfigEditor createTransformer(TransformerRegistration reg) throws WorkflowConfigurationException
   {
      if (!registry.isRegistered(reg.getId()))
         throw new WorkflowConfigurationException("Invalid transformer registration. The transformer '" + reg.getTitle() + "[" + reg.getId() + "] is not currently registered.");
      
      TransformerConfigData data = new TransformerConfigData();
      data.transformerId = UUID.randomUUID();
      data.registrationId = reg.getId();
      
      try
      {
         SimpleTransformerConfig editor = SimpleTransformerConfig.instantiate(registry, data);
         this.transformerEditors.put(data.transformerId, editor);
         return editor;
      }
      catch (FactoryUnavailableException e)
      {
         throw new WorkflowConfigurationException("Failed to create config editor:", e);
      }
   }

   @Override
   public TransformerConfigEditor editTransformer(UUID transfomerId) throws WorkflowConfigurationException
   {
      TransformerConfigEditor editor = this.transformerEditors.get(transfomerId);
      if (editor == null)
      {
         throw new WorkflowConfigurationException("No transformer with id [" + transfomerId + "] has been defined for this workflow [" + this.title + ": " + this.id + "]");
      }
      
      return editor;
   }

   @Override
   public WorkflowConfiguration build()
   {
      Set<TransformerConfiguration> transformers = new HashSet<>();
      for (TransformerConfigEditor editor : this.transformerEditors.values())
      {
         transformers.add(editor.getConfiguration());
      }
      
      return new WorkflowConfigImpl(id, title, description, type, transformers);
   }
   
   private static class WorkflowConfigImpl implements WorkflowConfiguration
   {
      private final UUID id;
      private final String title;
      private final String description;
      private final Class<?> type;
      private final Set<TransformerConfiguration> transformers;
      
      WorkflowConfigImpl(UUID id, String title, String description, Class<?> type, Set<TransformerConfiguration> transformers)
      {
         this.id = id;
         this.title = title;
         this.description = description;
         this.type = type;
         this.transformers = transformers;
         
      }

      @Override
      public UUID getId()
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
      public Class<?> getSourceType()
      {
         return type;
      }

      @Override
      public Collection<TransformerConfiguration> getTransformers()
      {
         return Collections.unmodifiableCollection(transformers);
      }
   }

}
