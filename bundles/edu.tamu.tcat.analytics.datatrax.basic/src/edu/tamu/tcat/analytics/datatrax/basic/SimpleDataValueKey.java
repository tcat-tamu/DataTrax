package edu.tamu.tcat.analytics.datatrax.basic;

import java.util.Objects;
import java.util.UUID;

import edu.tamu.tcat.analytics.datatrax.DataValueKey;

public class SimpleDataValueKey implements DataValueKey
{
   
   private final Class<?> type;
   private final UUID sourceId;

   public SimpleDataValueKey(UUID sourceId, Class<?> type)
   {
      Objects.requireNonNull(sourceId);
      Objects.requireNonNull(type);
      
      this.sourceId = sourceId;
      this.type = type;
   }
   
   @Override
   public UUID getSourceId()
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
      
      return result;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (!DataValueKey.class.isInstance(obj))
         return false;
      
      DataValueKey key = DataValueKey.class.cast(obj);
      return type.equals(key.getType()) && sourceId.equals(key.getSourceId());
   }
   
   @Override
   public String toString()
   {
      return "DataValueKey: " + this.type.getName() + " (" + this.sourceId +")";
   }
}