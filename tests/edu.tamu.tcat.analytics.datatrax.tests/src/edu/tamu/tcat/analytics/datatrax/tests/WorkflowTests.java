package edu.tamu.tcat.analytics.datatrax.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;
import edu.tamu.tcat.analytics.datatrax.FactoryUnavailableException;
import edu.tamu.tcat.analytics.datatrax.ResultsCollector;
import edu.tamu.tcat.analytics.datatrax.TransformerConfigurationException;
import edu.tamu.tcat.analytics.datatrax.basic.SimpleDataValueKey;
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
   public void testOutputDefinition() throws Exception
   {
      WorkflowConfigurationBuilder builder = new WorkflowConfigBuilderImpl(registry);
      builder.setTitle(DEFAULT_TITLE);
      builder.setDescription(DEFAULT_DESC);
      builder.setInputType(BufferedImage.class);
      
      // create thresholder configurations
      DataValueKey inputKey = builder.getInputKey();
      TransformerConfiguration integralImageAdapter = createIntegralImageAdapter(builder, inputKey);
      TransformerConfiguration thresholder = createThresholder(builder, integralImageAdapter);
      TransformerConfiguration ccAnalyzer = createCCAnalyzer(builder, thresholder);
      TransformerConfiguration ccWriter = createCCWriter(builder, inputKey, ccAnalyzer);
      
      // declare (valid) outputs
      builder.registerOutput(ccWriter.getId());
      builder.registerOutput(ccAnalyzer.getId());
      SimpleDataValueKey ccAnalyzerOutputPin = new SimpleDataValueKey(ccAnalyzer.getId(), ccAnalyzer.getOutputType());
      SimpleDataValueKey ccWriterOutputPin = new SimpleDataValueKey(ccWriter.getId(), ccWriter.getOutputType());
      
      try
      {
         builder.registerOutput(UUID.randomUUID());
         assertFalse("Attempt to register output for unknown transformer succeeded", true);
      } catch (WorkflowConfigurationException wce)
      {
         // expected case.
      }
      
      WorkflowConfiguration build = builder.build();
      Set<DataValueKey> outputs = build.getDeclaredOutputs();
      
      assertTrue("Did not find data output for CC Analyzer", outputs.contains(ccAnalyzerOutputPin));
      assertTrue("Did not find data output for CC Writer", outputs.contains(ccWriterOutputPin));
   }
   
   @Test
   public void testWorkflowExecution() throws IOException, WorkflowConfigurationException, InterruptedException, FactoryUnavailableException, TransformerConfigurationException
   {
      WorkflowConfigurationBuilder builder = new WorkflowConfigBuilderImpl(registry);
      builder.setTitle(DEFAULT_TITLE);
      builder.setDescription(DEFAULT_DESC);
      
      WorkflowControllerImpl workflow = createCCAnalyzerWorkflow(builder);
      
      Path dataDir = Paths.get("C:\\dev\\git\\citd.dia\\tests\\edu.tamu.tcat.dia.binarization.sauvola.test\\res");
      Path imagePath = dataDir.resolve("00000009.jp2");
      assertTrue("Source image does not exist. " + imagePath.toString(), Files.exists(imagePath));
      
      BufferedImage image = ImageIO.read(imagePath.toFile());
      
      CountDownLatch latch = new CountDownLatch(2);
      List<Exception> errors = new CopyOnWriteArrayList<>();
      Map<DataValueKey, Object> outputs = new HashMap<>();
      AtomicReference<BufferedImage> colorizedImage = new AtomicReference<>();
      
      long start = System.currentTimeMillis();
      workflow.process(image, new ResultsCollector()
      {
         
         @Override
         public <X> void handleResult(TranformationResult result)
         {
            outputs.put(result.getKey(), result.getValue());
            if (result.getValue() instanceof BufferedImage)
               colorizedImage.set((BufferedImage)result.getValue());
            
            latch.countDown();
         }
         
         @Override
         public void handleError(TransformationError error)
         {
            Exception ex = error.getException();
            errors.add(ex);
            ex.printStackTrace();
         }
         
         @Override
         public void finished()
         {
            // TODO alternatively, we could just wait until processing is done
            System.out.println("Done.");
         }
      });
      
      if (latch.await(2, TimeUnit.HOURS))
      {
         System.out.println("Time Elapsed: " + (System.currentTimeMillis() - start));
         
         Path output = dataDir.resolve("outputs").resolve("00000009.jpg");
         Files.createDirectories(output);
         ImageIO.write(colorizedImage.get(), "jpg", output.toFile());
      }
      else 
      {
         assertFalse("Failed to recieve results.", true);
      }
   }

   /**
    * Constructs a simple workflow that accepts  a buffered image and generates a set 
    * of connected components along with colorized image that displays those CCs.
    * 
    * @param builder
    * @return
    * 
    * @throws FactoryUnavailableException
    * @throws WorkflowConfigurationException
    * @throws TransformerConfigurationException
    */
   private WorkflowControllerImpl createCCAnalyzerWorkflow(WorkflowConfigurationBuilder builder) 
         throws FactoryUnavailableException, WorkflowConfigurationException, TransformerConfigurationException
   {
      builder.setInputType(BufferedImage.class);
      
      // create thresholder configurations
      DataValueKey inputKey = builder.getInputKey();
      TransformerConfiguration integralImageAdapter = createIntegralImageAdapter(builder, inputKey);
      TransformerConfiguration thresholder = createThresholder(builder, integralImageAdapter);
      TransformerConfiguration ccAnalyzer = createCCAnalyzer(builder, thresholder);
      TransformerConfiguration ccWriter = createCCWriter(builder, inputKey, ccAnalyzer);
      
      // declare outputs
      builder.registerOutput(ccWriter.getId());
      builder.registerOutput(ccAnalyzer.getId());
      
      WorkflowConfiguration configuration = builder.build();
      WorkflowControllerImpl workflow = WorkflowControllerImpl.create(configuration);
      
      return workflow;
   }

   private TransformerConfiguration createCCWriter(WorkflowConfigurationBuilder builder, DataValueKey imageModelProvider, TransformerConfiguration ccSetProvider) throws FactoryUnavailableException, WorkflowConfigurationException, TransformerConfigurationException
   {
      TransformerConfigEditor editor;
      ExtTransformerFactoryDefinition ccWriterReg = registry.getRegistration(CCWriter.EXTENSION_ID);
      
      editor = builder.createTransformer(ccWriterReg);
      editor.setDataSource(ccWriterReg.getDeclaredInput(CCWriter.CONNECTED_COMPONENTS_PIN), ccSetProvider);
      editor.setDataSource(ccWriterReg.getDeclaredInput(CCWriter.MODEL_PIN), imageModelProvider);
      TransformerConfiguration ccWriter = editor.getConfiguration();
      return ccWriter;
   }

   private TransformerConfiguration createCCAnalyzer(WorkflowConfigurationBuilder builder, TransformerConfiguration binaryImageSource) throws FactoryUnavailableException, WorkflowConfigurationException, TransformerConfigurationException
   {
      TransformerConfigEditor editor;
      ExtTransformerFactoryDefinition ccReg = registry.getRegistration(CCAnalyzer.EXTENSION_ID);
      
      editor = builder.createTransformer(ccReg);
      editor.setDataSource(ccReg.getDeclaredInput(CCAnalyzer.BINARY_IMAGE_PIN), binaryImageSource);
      TransformerConfiguration ccAnalyzer = editor.getConfiguration();
      return ccAnalyzer;
   }

   private TransformerConfiguration createThresholder(WorkflowConfigurationBuilder builder, TransformerConfiguration integralImageDataSource) throws FactoryUnavailableException, WorkflowConfigurationException, TransformerConfigurationException
   {
      TransformerConfigEditor editor;
      ExtTransformerFactoryDefinition sauvolaReg = registry.getRegistration(FastSauvolaTransformer.EXTENSION_ID);
      
      editor = builder.createTransformer(sauvolaReg);
      editor.setDataSource(sauvolaReg.getDeclaredInput(FastSauvolaTransformer.INTEGRAL_IMAGE_PIN), integralImageDataSource);
      TransformerConfiguration thresholder = editor.getConfiguration();
      return thresholder;
   }

   private TransformerConfiguration createIntegralImageAdapter(WorkflowConfigurationBuilder builder, DataValueKey sourceData) throws WorkflowConfigurationException, TransformerConfigurationException, FactoryUnavailableException
   {
      ExtTransformerFactoryDefinition buffImageReg = registry.getRegistration(BufferedImageAdapter.EXTENSION_ID);

      TransformerConfigEditor editor = builder.createTransformer(buffImageReg);
      editor.setDataSource(buffImageReg.getDeclaredInput(BufferedImageAdapter.IMAGE_PIN), builder.getInputKey());
      TransformerConfiguration integralImageAdapter = editor.getConfiguration();
      
      return integralImageAdapter;
   }
   
//   @Test
   public void testWorkflowConfigSerialization()
   {
      
   }
   

}
