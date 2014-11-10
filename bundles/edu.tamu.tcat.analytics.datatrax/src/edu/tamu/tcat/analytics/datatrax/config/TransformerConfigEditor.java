package edu.tamu.tcat.analytics.datatrax.config;

import javax.xml.transform.TransformerConfigurationException;


/**
 * Used to edit a {@link TransformerConfiguration} instance.
 */
public interface TransformerConfigEditor
{
   /**
    * Sets a configuration parameter for the associated transformer.
    * 
    * @param param The parameter to set.
    * @param obj The value to be set.
    */
   void setParameter(String param, Object val);
   
   /**
    * Defines the data source whose output value should be used as the input for a specific
    * data input pin on the associated transformer.
    * 
    * @param pin The input pin the supplied data source will be connected to
    * @param source The configuration instance for the transformer whose output should be 
    *       supplied as input to the associated transformer. 
    * @throws TransformerConfigurationException 
    */
   void setDataSource(DataInputPin pin, TransformerConfiguration source) throws TransformerConfigurationException;
   
   /**
    * @return The configuration instance represented by the current state of this editor.
    */
   TransformerConfiguration getConfiguration();

}
