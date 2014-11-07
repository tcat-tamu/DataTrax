package edu.tamu.tcat.analytics.datatrax;

/**
 * Immutable identifier for the source of a data object within a data workflow. This uniquely 
 * identifies the transformer that produced the data instance and the Java type of the object.
 * It is used principally to define the stiching from the outputs of one transformer to the 
 * input pin of another transformer. 
 */
public interface DataValueKey
{
   /**
    * @return The unique identifier for the {@link TransformerFactory} instance that is responsible for 
    *       creating a data value. Will not be {@code null}. Must be unique within the context
    *       of a configured workflow.
    */
   String getTransformerId();
   
   /**
    * @return The Java type of object associated with this key.
    */
   <X> Class<X> getType();
}
