package edu.tamu.tcat.analytics.datatrax.config;

import edu.tamu.tcat.analytics.datatrax.TransformerFactory;

/**
 * Defines a data input source for a {@link TransformerFactory}. {@code TransformerFactory}s 
 * operate on one or more input data objects. The {@code DataInputPin} provides a unique label
 * for a data input object, a textual description of the purpose or intended use of the 
 * supplied data for display, and the Java type (or supertype) of the expected data. 
 * Additionally, some data inputs may be optional, so a {@code DataInputPin} may have its 
 * required property set to false. 
 */
public class DataInputPin
{
   public String label;
   
   public String description;
   
   public Class<?> type;
   
   /**
    * Indicates that this input value must be supplied. For optional data inputs, if a workflow
    * is configured with a transformer that will supply the required input 
    */
   public boolean required = true;
}
