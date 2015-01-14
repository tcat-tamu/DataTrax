package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.DataTraxFacade;
import edu.tamu.tcat.analytics.datatrax.TransformerRegistry;
import edu.tamu.tcat.analytics.datatrax.WorkflowController;
import edu.tamu.tcat.analytics.datatrax.WorkflowException;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfiguration;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowConfigurationBuilder;
import edu.tamu.tcat.analytics.datatrax.config.WorkflowDescription;

public class DataTraxFacadeImpl implements DataTraxFacade
{

   public DataTraxFacadeImpl()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public WorkflowConfigurationBuilder createConfiguration()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public WorkflowConfigurationBuilder editConfiguration(WorkflowConfiguration config)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public WorkflowConfiguration getConfiguration(UUID id) throws WorkflowException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<WorkflowDescription> listWorkflows()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void saveWorkflowConfiguratoin(WorkflowConfiguration config)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public WorkflowController createWorkflow(WorkflowConfiguration config)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public TransformerRegistry getTranformerRegistry()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
