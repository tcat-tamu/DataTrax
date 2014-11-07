package edu.tamu.tcat.analytics.datatrax.basic.refactor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class WorkflowController 
{

   public WorkflowController()
   {
      // TODO Auto-generated constructor stub
   }

   public <X> Future<X> execute(Callable<X> transformationTask)
   {
      throw new UnsupportedOperationException();
   }
}
