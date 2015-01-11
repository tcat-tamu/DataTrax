package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowConfigBuilderImpl;
import edu.tamu.tcat.analytics.datatrax.basic.WorkflowControllerImpl;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtPointTranformerFactoryRegistry;
import edu.tamu.tcat.analytics.datatrax.basic.factorymeta.ExtTransformerFactoryDefinition;
import edu.tamu.tcat.analytics.datatrax.config.DataInputPin;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfigEditor;
import edu.tamu.tcat.analytics.datatrax.config.TransformerConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationBuilder;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationException;
import edu.tamu.tcat.analytics.image.integral.datatrax.BufferedImageAdapter;
import edu.tamu.tcat.dia.binarization.sauvola.FastSauvolaTransformer;
import edu.tamu.tcat.dia.segmentation.cc.twopass.CCAnalyzer;
import edu.tamu.tcat.dia.segmentation.cc.twopass.CCWriter;

public class WorkflowTests
{

   private static final String DEFAULT_DESC = "A builder for use in testing builder creation";
   private static final String DEFAULT_TITLE = "Config Test";
   // TODO create a Mock registry that we can use to return decorated filter 
   //      implementations to support testing and performance analysis 
   private static ExtPointTranformerFactoryRegistry registry;

   public WorkflowTests()
   {
   }

   @BeforeClass
   public static void setup()
   {
      registry = new ExtPointTranformerFactoryRegistry();
      registry.activate();
   }
   
   @AfterClass
   public static void teardown()
   {
      registry.dispose();
      
   }
   
   private WorkflowConfiguration buildDefaultConfiguration() throws WorkflowConfigurationException, FactoryUnavailableException 
   {
      WorkflowConfigBuilderImpl builder = new WorkflowConfigBuilderImpl(registry);
      builder.setTitle(DEFAULT_TITLE);
      builder.setDescription(DEFAULT_DESC);
      builder.setInputType(BufferedImage.class);
      
      ExtTransformerFactoryDefinition buffImageReg = registry.getRegistration(BufferedImageAdapter.EXTENSION_ID);
      ExtTransformerFactoryDefinition sauvolaReg = registry.getRegistration(FastSauvolaTransformer.EXTENSION_ID);
      ExtTransformerFactoryDefinition ccReg = registry.getRegistration(CCAnalyzer.EXTENSION_ID);

      TransformerConfigEditor integralImAdapterEd = builder.createTransformer(buffImageReg);
      TransformerConfigEditor thresholderEd = builder.createTransformer(sauvolaReg);
      TransformerConfigEditor ccEd = builder.createTransformer(ccReg);
//      
//      thresholderEd.setDataSource(pin, integralImAdapterEd.getConfiguration());
//      ccEd.setDataSource(pin, thresholderEd.getConfiguration());
      
      return builder.build();
   }
   
   // TODO test configuration parameters
   
   private boolean containsTransformer(WorkflowConfiguration workflow, String regId)
   {
      for (TransformerConfiguration cfg : workflow.getTransformers())
      {
         if (cfg.getRegistration().getId().equals(regId))
            return true;
      }
      
      return false;
   }
   
   @Test
   public void testWorkflowBuilder() throws WorkflowConfigurationException, FactoryUnavailableException
   {
      WorkflowConfiguration cfg = buildDefaultConfiguration();
      
      assertNotNull("No workflow configuration was built", cfg);
      assertEquals(DEFAULT_TITLE, cfg.getTitle());
      assertEquals(DEFAULT_DESC, cfg.getDescription());
      
      assertEquals("Unexpected number of factories configured", 3, cfg.getTransformers().size());
      
      assertTrue("Cannot find BufferedImageAdapter", containsTransformer(cfg, BufferedImageAdapter.EXTENSION_ID));
      assertTrue("Cannot find FastSauvolaTransformer", containsTransformer(cfg, FastSauvolaTransformer.EXTENSION_ID));
      assertTrue("Cannot find CCAnalyzer", containsTransformer(cfg, CCAnalyzer.EXTENSION_ID));
   }
   
   @Test
   public void testWorkflowCreation() throws WorkflowConfigurationException, FactoryUnavailableException, TransformerConfigurationException
   {
      WorkflowConfiguration cfg = buildDefaultConfiguration();
      WorkflowControllerImpl workflowController = WorkflowControllerImpl.create(cfg);
   }
   
   DataInputPin getInputPin(ExtTransformerFactoryDefinition reg, String name) 
   {
      // HACK: this seems exceptionally inefficient
      for (DataInputPin pin : reg.getDeclaredInputs())
      {
         if (pin.label.equals(name))
            return pin;
      }
      
      throw new IllegalArgumentException("Undefined input pin [" + name + "]");
   }
   
   @Test
   public void testWorkflowExecution() throws IOException, WorkflowConfigurationException, InterruptedException, FactoryUnavailableException, TransformerConfigurationException
   {
      WorkflowConfigurationBuilder builder = new WorkflowConfigBuilderImpl(registry);
      builder.setTitle(DEFAULT_TITLE);
      builder.setDescription(DEFAULT_DESC);
      
      WorkflowControllerImpl workflow = createCCAnalyzerWorkflow(builder);
      
      
      Path dataDir = Paths.get("C:\\dev\\git\\citd.dia\\tests\\edu.tamu.tcat.dia.binarization.sauvola.test\\res");
      Path imagePath = dataDir.resolve("00000008.jp2");
      BufferedImage image = ImageIO.read(imagePath.toFile());
      
      CountDownLatch latch = new CountDownLatch(2);
      List<Exception> errors = new CopyOnWriteArrayList<>();
      Map<DataValueKey, Object> outputs = new HashMap<>();
      
      workflow.process(image, new ResultsCollector()
      {
         
         @Override
         public <X> void handleResult(TranformationResult result)
         {
            outputs.put(result.getKey(), result.getValue());
            latch.countDown();
         }
         
         @Override
         public void handleError(TransformationError error)
         {
            errors.add(error.getException());
         }
         
         @Override
         public void finished()
         {
            // TODO alternatively, we could just wait untill processing is done
            System.out.println("Done.");
         }
      });
      
      if (latch.await(2, TimeUnit.SECONDS))
      {
         // TODO evaluate results
      }
      else 
      {
         assertFalse("Failed to recieve results.", true);
      }
      
      // WIP execute configured workflow
      
//      
      
//      
//      FastSauvolaTransformer thresholder = new FastSauvolaTransformer();
//      thresholder.setK(0.3);
//      Path imagePath = dataDir.resolve("shipbuilding-treatise.jpg");
//      
//      final BufferedImage image = ImageIO.read(Files.newInputStream(imagePath, StandardOpenOption.READ));
//      Path outputPath = dataDir.resolve("output/shipbuilding-treatise-bin.png");
//      Map<String, Object> binWriterParams = new HashMap<>();
//      binWriterParams.put("model", image);
//      binWriterParams.put("path", outputPath);
//      binWriterParams.put("format", "png");
//      
//      Map<String, Object> ccWriterParams = new HashMap<>();
//      outputPath = dataDir.resolve("output/shipbuilding-treatise-cc.png");
//      ccWriterParams.put("model", image);
//      ccWriterParams.put("path", outputPath);
//      ccWriterParams.put("format", "png");
//      
//      WorkflowConfigBuilderImpl builder = new WorkflowConfigBuilderImpl(registry);
//      builder.setTitle(DEFAULT_TITLE);
//      builder.setDescription(DEFAULT_DESC);
//      
//      builder.append(createCfg(BufferedImageAdapter.EXTENSION_ID, null));
//      builder.append(createCfg(FastSauvolaTransformer.EXTENSION_ID, null));
//      builder.append(createCfg(BinaryImageWriter.EXTENSION_ID, binWriterParams));
//      builder.append(createCfg(CCAnalyzer.EXTENSION_ID, null));
//      builder.append(createCfg(CCWriter.EXTENSION_ID, ccWriterParams));
////      
//
//      WorkflowConfiguration cfg = builder.build();
//      
//      WorkflowFactoryImpl factory = new WorkflowFactoryImpl();
//      factory.bindFactoryRegistry(registry);
//      
//      DataTransformWorkflow<BufferedImage, ConnectComponentSet> workflow = factory.create(cfg);
//      final CountDownLatch latch = new CountDownLatch(1);
//      final AtomicReference<ConnectComponentSet> ccRef = new AtomicReference<>();
//      
//      workflow.process(() -> image, (ConnectComponentSet cc) -> {
//         ccRef.set(cc);
//         latch.countDown();
//      });
//      
//      latch.await(10, TimeUnit.SECONDS);
//      ConnectComponentSet cc = ccRef.get();
//      assertNotNull("No connected components returned", cc);
//      int size = cc.listLabels().size();
//      assertTrue("Surpisingly few connected components returned [" + size + "]", size > 500);
   }

   private WorkflowControllerImpl createCCAnalyzerWorkflow(WorkflowConfigurationBuilder builder) throws FactoryUnavailableException, WorkflowConfigurationException, TransformerConfigurationException
   {
      ExtTransformerFactoryDefinition buffImageReg = registry.getRegistration(BufferedImageAdapter.EXTENSION_ID);
      ExtTransformerFactoryDefinition sauvolaReg = registry.getRegistration(FastSauvolaTransformer.EXTENSION_ID);
      ExtTransformerFactoryDefinition ccReg = registry.getRegistration(CCAnalyzer.EXTENSION_ID);
      ExtTransformerFactoryDefinition ccWriter = registry.getRegistration(CCWriter.EXTENSION_ID);

      // construct a simple workflow that will accept a buffered image and generate a set 
      // of connected components and a colorized image that displays those CCs. 
      
      
      builder.setInputType(BufferedImage.class);
      
      // convert the input data to an integral image 
      TransformerConfigEditor editor = builder.createTransformer(buffImageReg);
      editor.setDataSource(buffImageReg.getDeclaredInput(BufferedImageAdapter.IMAGE_PIN), builder.getInputKey());
      TransformerConfiguration integralImageAdapter = editor.getConfiguration();
      
      // threshold the integral image
      editor = builder.createTransformer(sauvolaReg);
      editor.setDataSource(sauvolaReg.getDeclaredInput(FastSauvolaTransformer.INTEGRAL_IMAGE_PIN), integralImageAdapter);
      TransformerConfiguration thresholder = editor.getConfiguration();
      
      // perform CC analysis
      editor = builder.createTransformer(ccReg);
      editor.setDataSource(ccReg.getDeclaredInput(CCAnalyzer.BINARY_IMAGE_PIN), thresholder);
      TransformerConfiguration ccAnalyzer = editor.getConfiguration();
      
      editor = builder.createTransformer(ccWriter);
      editor.setDataSource(ccWriter.getDeclaredInput(CCWriter.CONNECTED_COMPONENTS_PIN), ccAnalyzer);
      editor.setDataSource(ccWriter.getDeclaredInput(CCWriter.MODEL_PIN), builder.getInputKey());
      
      // TODO declare outputs
      
      
      WorkflowConfiguration configuration = builder.build();
      WorkflowControllerImpl workflow = WorkflowControllerImpl.create(configuration);
      return workflow;
   }
   
//   @Test
   public void testWorkflowConfigSerialization()
   {
      
   }
   

}
