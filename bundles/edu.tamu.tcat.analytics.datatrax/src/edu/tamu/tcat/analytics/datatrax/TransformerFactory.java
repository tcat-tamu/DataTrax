package edu.tamu.tcat.analytics.datatrax;

public interface TransformerFactory
{
   String getId();

   String getTitle();

   Class<?> getDeclaredSourceType();
   
   Class<?> getDeclaredOutputType();
   
   boolean canAccept(Class<?> type) throws FactoryConfigurationException;

   boolean canProduce(Class<?> type) throws FactoryConfigurationException;
   
   <IN, OUT> Transformer<IN, OUT> instantiate() throws FactoryConfigurationException;

}
