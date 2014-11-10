package edu.tamu.tcat.analytics.datatrax;

/**
 * Provides a container for a {@link Transformer} to use to retrieve input source data. When
 * a transformer is invoked by the {@link WorkflowController}, it will be supplied with an
 * instance of a {@code TransformerContext} that will provide the data input values that have 
 * been stitched to the appropriate input pins in that specific workflow configuration.  
 */
public interface TransformerContext
{
   /**
    * @param label The label of the input data to retrieve.
    * @return The value associated with the supplied input label.
    */
   <X> X getValue(String label);
}
