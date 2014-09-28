package edu.tamu.tcat.analytics.datatrax;

public interface DataTransformWorkflow<IN, OUT>
{
   // TODO almost certainly should remove the types.
   /**
    * @return The Java type of objects that can be used as input to this 
    *    {@code DataTransformWorkflow}. All values supplied to this 
    *    {@link DataTransformWorkflow} must be instances of this type or sub-types.  
    */
   Class<?> getSourceType();
   
   /**
    * @return The Java type of objects that will be generated as output from this 
    *    {@link DataTransformWorkflow}. The final result of this {@link DataTransformWorkflow} 
    *    will be a single object of this type (or a sub-type).  
    */
   Class<?> getOutputType();
   
   /**
    * Tests whether this {@code TransformerFactory} can accept input values of the supplied type.
    * 
    * @param type The Java type to test.
    * @return {@code true} if the supplied type is the same as or a sub-type of the declared 
    *    source type.
    */
   boolean canAccept(Class<?> type);

   /**
    * Tests whether the output of this {@code TransformerFactory} can be offered to a 
    * {@link Consumer} that accepts objects of the supplied type. 
    * 
    * @param type The Java type to test
    * @return {@code true} if the supplied type is the same as or a super-type of the declared 
    *    output type.
    */
   boolean canProduce(Class<?> type);
}
