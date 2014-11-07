package edu.tamu.tcat.analytics.datatrax;

/**
 * Provides a container for a {@link Transformer} to use to retrieve input source data. 
 */
public interface TransformerContext
{
   /**
    * @param label The label of the input data to retrieve.
    * @return The value associated with the supplied input label.
    */
   <X> X getValue(String label);
}
