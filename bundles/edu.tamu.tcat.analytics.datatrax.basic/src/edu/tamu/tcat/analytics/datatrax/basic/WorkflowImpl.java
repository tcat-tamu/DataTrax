package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.tamu.tcat.analytics.datatrax.DataTransformWorkflow;
import edu.tamu.tcat.analytics.datatrax.InvalidTransformerConfiguration;

public class WorkflowImpl<IN, OUT> implements DataTransformWorkflow<IN, OUT>
{

   
   public WorkflowImpl()
   {
      // TODO Auto-generated constructor stub
   }
   
//   private static List<Transformer<?, ?>> loadFactories(WorkflowConfiguration config) 
//         throws InvalidTransformerConfiguration
//   {
//      List<Transformer<?, ?>> factories = new ArrayList<>();
//      List<TransformerConfiguration> elements = config.getTransformers();
//      for (TransformerConfiguration transCfg : elements)
//      {
//         Transformer<?, ?> factory = getTransformerFactory(transCfg.getTransformerId());
//         checkTypeCompatibility(factories, factory);
//         
//         factory.configure(transCfg.getConfiguration());
//         factories.add(factory);
//      }
//      
//      return factories;
//   }
   
//      FastSauvolaFactory thresholder = new TranformerFactory();
//      thresholder.setK(0.3);
////      thresholder.setWindowSize(15);
//      Path imagePath = dataDir.resolve("shipbuilding-treatise.jpg");
//      Path outputPath = dataDir.resolve("output/shipbuilding-treatise.png");
//      
//      final BufferedImage image = ImageIO.read(Files.newInputStream(imagePath, StandardOpenOption.READ));
//      
//      IntegralImageImpl iImage = IntegralImageImpl.create(image.getData());
//      Runnable runnable = thresholder.create(() -> { return iImage; }, (im) -> {
//         try
      

   /**
    * Checks to ensure that the source type of the candidate 
    * 
    * @param pipeline
    * @param tail
    * @throws InvalidTransformerConfiguration
    */
//   private static void checkTypeCompatibility(List<Transformer<?, ?>> pipeline, Transformer<?, ?> tail) throws InvalidTransformerConfiguration
//   {
//      if (pipeline.size() == 0)
//         return;
//      
//      Transformer<?, ?> previous = pipeline.get(pipeline.size() - 1);
//      Class<?> outType = previous.getOutputType();
//      Class<?> srcType = tail.getSourceType();
//      if (!srcType.isAssignableFrom(outType))
//      {
//         // TODO add description
//         throw new InvalidTransformerConfiguration();
//      }
//   }
//   
//   private static Transformer<?, ?> getTransformerFactory(String id)
//   {
//      // TODO implement me
//      return null;
//   }
   
   public void process(Supplier<IN> input, Consumer<OUT> ears)
   {
   }

}
