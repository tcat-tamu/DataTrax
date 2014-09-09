package edu.tamu.tcat.analytics.datatrax;

import java.util.Map;

public interface TransformerConfiguration
{

   String getTransformerId();
   
   Map<String, Object> getConfiguration();
}
