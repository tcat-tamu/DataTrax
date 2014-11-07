package edu.tamu.tcat.analytics.datatrax.basic.refactor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutionService
{
   <X> Future<X> execute(Callable<X> transformationTask);

}
