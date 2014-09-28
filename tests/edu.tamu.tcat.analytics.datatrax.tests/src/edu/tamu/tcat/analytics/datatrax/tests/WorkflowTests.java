package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.FactoryConfiguration;
import edu.tamu.tcat.analytics.datatrax.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.WorkflowConfigurationException;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowConfigBuilderImpl;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowFactoryImpl;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;
import edu.tamu.tcat.analytics.image.integral.datatrax.BufferedImageAdapter;
import edu.tamu.tcat.dia.binarization.sauvola.FastSauvolaTransformer;
import edu.tamu.tcat.dia.segmentation.cc.twopass.CCAnalyzer;

public class WorkflowTests
{

   // TODO create a Mock registry that we can use to return decorated filter 
   //      implementations to support testing and performance analysis 
   private ExtPointTranformerFactoryRegistry registry;

   public WorkflowTests()
   {
   }

   @Before
   public void setup()
   {
      registry = new ExtPointTranformerFactoryRegistry();
      registry.activate();
   }
   
   @After
   public void teardown()
   {
      registry.dispose();
      
   }
   
   private FactoryConfiguration createCfg(String id, Map<String, Object> params)
   {
      if (params == null)
         params = new HashMap<String, Object>();
      
      FactoryConfiguration cfg = new FactoryConfiguration();
      cfg.factoryId = id;
      cfg.configData = params;
      
      return cfg;
   }
   
   private WorkflowConfiguration buildDefaultConfiguration() throws WorkflowConfigurationException 
   {
      WorkflowConfigBuilderImpl builder = new WorkflowConfigBuilderImpl(registry);
      builder.setTitle("Config Test");
      builder.setDescription("A builder for use in testing builder creation");
      
      builder.append(createCfg(BufferedImageAdapter.EXTENSION_ID, null));
      builder.append(createCfg(FastSauvolaTransformer.EXTENSION_ID, null));
      builder.append(createCfg(CCAnalyzer.EXTENSION_ID, null));
//      builder.append(colorizer);
      
      return builder.build();
   }
   
   // TODO test configuration parameters
   
   @Test
   public void testWorkflowBuilder() throws WorkflowConfigurationException
   {
      WorkflowConfiguration cfg = buildDefaultConfiguration();
      // TODO test name
      assertNotNull("No workflow configuration was built", cfg);
      assertEquals("Unexpected number of factories configured", 3, cfg.factories.size());
      
      assertEquals("Cannot find BufferedImageAdapter", BufferedImageAdapter.EXTENSION_ID, cfg.factories.get(0).factoryId);
      assertEquals("Cannot find FastSauvolaTransformer", FastSauvolaTransformer.EXTENSION_ID, cfg.factories.get(1).factoryId);
      assertEquals("Cannot find CCAnalyzer", CCAnalyzer.EXTENSION_ID, cfg.factories.get(2).factoryId);
   }
   
   @Test
   public void testWorkflowCreation() throws WorkflowConfigurationException
   {
      WorkflowConfiguration cfg = buildDefaultConfiguration();
      
      WorkflowFactoryImpl factory = new WorkflowFactoryImpl();
      factory.bindFactoryRegistry(registry);
      
      DataTransformWorkflow<?, ?> workflow = factory.create(cfg);
      assertNotNull("Failed to create workflow", workflow);
   }
   
   @Test
   public void testWorkflowExecution()
   {
      
   }
   
   @Test
   public void testWorkflowConfigSerialization()
   {
      
   }
}
