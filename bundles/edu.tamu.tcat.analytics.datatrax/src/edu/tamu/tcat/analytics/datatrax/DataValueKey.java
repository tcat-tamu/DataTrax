package edu.tamu.tcat.analytics.datatrax;

import java.util.UUID;

/**
 * Immutable identifier for the source of a data object within a data workflow. This uniquely 
 * identifies the transformer or other data source that produced the data instance and the 
 * Java type of the object. It is used principally to define the stiching from the outputs of 
 * one transformer to the input pin of another transformer. 
 */
public interface DataValueKey
{
   /**
    * @return The unique identifier for the {@link Transformer} instance or other data source
    *       that is responsible for creating a data value. Will not be {@code null}. Must be 
    *       unique within the context of a configured workflow.
    */
   UUID getSourceId();
   
   /**
    * @return The Java type of object associated with this key.
    */
   <X> Class<X> getType();
}
