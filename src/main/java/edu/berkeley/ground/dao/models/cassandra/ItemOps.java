package edu.berkeley.ground.dao.models.cassandra;

import edu.berkeley.ground.db.CassandraResults;
import edu.berkeley.ground.exceptions.GroundItemNotFoundException;
import edu.berkeley.ground.model.models.Edge;
import edu.berkeley.ground.model.versions.Item;

public class ItemOps {
  private ItemOps() {
    // Noop private constructor
  }

  public static void verifyNotEmpty(CassandraResults resultSet, Class<? extends Item> itemType, String fieldName, Object value) throws GroundItemNotFoundException {
    if (resultSet.isEmpty()) {
      throw new GroundItemNotFoundException(Edge.class, fieldName, value);
    }
  }


}
