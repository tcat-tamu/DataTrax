package edu.tamu.tcat.analytics.datatrax.config;

import edu.tamu.tcat.analytics.datatrax.Transformer;

/**
 * Defines a data input source for a {@link Transformer}. {@code TransformerFactory}s 
 * operate on one or more input data objects. The {@code DataInputPin} provides a unique label
 * for a data input object, a textual description of the purpose or intended use of the 
 * supplied data for display, and the Java type (or supertype) of the expected data. 
 * Additionally, some data inputs may be optional, so a {@code DataInputPin} may have its 
 * required property set to false. 
 */
public class DataInputPin
{
   /**
    * A label that identifies this input pin within the scope of the associated {@link Transformer}. 
    * For convenience this is typically a semantically meaningful value. 
    */
   public String label;
   
   /**
    * A description of how the associated {@link Transformer} will use this input value for 
    * use in workflow authoring interfaces.
    */
   public String description;
   
   /**
    * The Java type of objects that are acceptable data sources for the associated input pin.
    */
   public Class<?> type;
   
   /**
    * Indicates whether this input value must be supplied. A value of {@code false} means that 
    * a workflow will be considered valid even if an input for this pin is not supplied. If an 
    * input value is stitched to this pin, a value for that source data must be supplied during 
    * execution before the associated {@link Transformer} will be invoked. 
    */
   public boolean required = true;
}
