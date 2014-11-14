package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Objects;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;

public class SimpleDataValueKey implements DataValueKey
{
   
   private final Class<?> type;
   private final String sourceId;

   public SimpleDataValueKey(String sourceId, Class<?> type)
   {
      Objects.requireNonNull(sourceId);
      Objects.requireNonNull(type);
      
      this.sourceId = sourceId;
      this.type = type;
   }
   
   @Override
   public String getTransformerId()
   {
      return sourceId;
   }

   @Override
   public <X> Class<X> getType()
   {
      return (Class)type;
   }
   
   @Override
   public int hashCode()
   {
      int result = 47;
      
      result = 37 * result + type.hashCode();
      result = 37 * result + sourceId.hashCode();
      
      return super.hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (!DataValueKey.class.isInstance(obj))
         return false;
      
      DataValueKey key = DataValueKey.class.cast(obj);
      return type.equals(key.getType()) && sourceId.equalsIgnoreCase(key.getTransformerId());
   }
}