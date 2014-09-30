package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowConfigBuilderImpl;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowFactoryImpl;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;
import edu.tamu.tcat.analytics.datatrax.config.FactoryConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationException;
import edu.tamu.tcat.analytics.image.integral.datatrax.BufferedImageAdapter;
import edu.tamu.tcat.dia.binarization.datatrax.BinaryImageWriter;
import edu.tamu.tcat.dia.binarization.sauvola.FastSauvolaTransformer;
import edu.tamu.tcat.dia.segmentation.cc.ConnectComponentSet;
import edu.tamu.tcat.dia.segmentation.cc.twopass.CCAnalyzer;
import edu.tamu.tcat.dia.segmentation.cc.twopass.CCWriter;

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
      
      @SuppressWarnings("rawtypes")
      DataTransformWorkflow workflow = factory.create(cfg);
      assertNotNull("Failed to create workflow", workflow);
      assertEquals("Mismatched workflow source type", BufferedImage.class, workflow.getSourceType());
      assertEquals("Mismatched workflow output type", ConnectComponentSet.class, workflow.getOutputType());
   }
   
   @Test
   public void testWorkflowExecution() throws IOException, WorkflowConfigurationException, InterruptedException
   {
      Path dataDir = Paths.get("C:\\dev\\git\\citd.dia\\tests\\edu.tamu.tcat.dia.binarization.sauvola.test\\res");
      
      FastSauvolaTransformer thresholder = new FastSauvolaTransformer();
      thresholder.setK(0.3);
      Path imagePath = dataDir.resolve("shipbuilding-treatise.jpg");
      
      final BufferedImage image = ImageIO.read(Files.newInputStream(imagePath, StandardOpenOption.READ));
      Path outputPath = dataDir.resolve("output/shipbuilding-treatise-bin.png");
      Map<String, Object> binWriterParams = new HashMap<>();
      binWriterParams.put("model", image);
      binWriterParams.put("path", outputPath);
      binWriterParams.put("format", "png");
      
      Map<String, Object> ccWriterParams = new HashMap<>();
      outputPath = dataDir.resolve("output/shipbuilding-treatise-cc.png");
      ccWriterParams.put("model", image);
      ccWriterParams.put("path", outputPath);
      ccWriterParams.put("format", "png");
      
      WorkflowConfigBuilderImpl builder = new WorkflowConfigBuilderImpl(registry);
      builder.setTitle("Config Test");
      builder.setDescription("A builder for use in testing builder creation");
      
      builder.append(createCfg(BufferedImageAdapter.EXTENSION_ID, null));
      builder.append(createCfg(FastSauvolaTransformer.EXTENSION_ID, null));
      builder.append(createCfg(BinaryImageWriter.EXTENSION_ID, binWriterParams));
      builder.append(createCfg(CCAnalyzer.EXTENSION_ID, null));
      builder.append(createCfg(CCWriter.EXTENSION_ID, ccWriterParams));
      

      WorkflowConfiguration cfg = builder.build();
      
      WorkflowFactoryImpl factory = new WorkflowFactoryImpl();
      factory.bindFactoryRegistry(registry);
      
      DataTransformWorkflow<BufferedImage, ConnectComponentSet> workflow = factory.create(cfg);
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<ConnectComponentSet> ccRef = new AtomicReference<>();
      
      workflow.process(() -> image, (ConnectComponentSet cc) -> {
         ccRef.set(cc);
         latch.countDown();
      });
      
      latch.await(10, TimeUnit.SECONDS);
      ConnectComponentSet cc = ccRef.get();
      assertNotNull("No connected components returned", cc);
      int size = cc.listLabels().size();
      assertTrue("Surpisingly few connected components returned [" + size + "]", size > 500);
   }
   
//   @Test
   public void testWorkflowConfigSerialization()
   {
      
   }
   

}
