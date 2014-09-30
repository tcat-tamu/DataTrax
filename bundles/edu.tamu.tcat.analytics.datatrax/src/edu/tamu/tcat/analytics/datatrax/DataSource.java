package edu.tamu.tcat.analytics.datatrax;

import java.util.function.Supplier;

public interface DataSource<T> extends Supplier<T>
{

   // TODO needs to supply an instance context that will be maintained throughout the 
   //      processing of this instance and that can be used to store transient data (i.e., 
   //      during the execution of a data transformation workflow) about intermediate data 
   //      results.
   
   // TODO needs to provide a per-object, persistent cache to store intermediate results and
   //      secondary outputs fo the transformation workflow. This is intented to be used to
   //      improve performance by caching intermediate results or data that might be shared by 
   //      multiple processes. 
   // NOTE that this *may* be used both to aggregate intermediate results and capture 
   //      information that is not easily transmitted through the pipeline. This use seems to be 
   //      a abuse of the notion of a cache. Perhaps we should provide a separate mechanism to
   //      handle this requirement.
}
