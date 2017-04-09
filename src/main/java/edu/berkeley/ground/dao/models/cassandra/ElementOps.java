package edu.berkeley.ground.dao.models.cassandra;

import edu.berkeley.ground.db.CassandraResults;
import edu.berkeley.ground.exceptions.GroundElementNotFoundException;
import edu.berkeley.ground.model.models.Edge;
import edu.berkeley.ground.model.versions.Item;

public class ElementOps {
  private ElementOps() {
    // Noop private constructor
  }

  public static void verifyNotEmpty(CassandraResults resultSet, Class<? extends Item> itemType, String fieldName, Object value) throws GroundElementNotFoundException {
    if (resultSet.isEmpty()) {
      throw new GroundElementNotFoundException(Edge.class, fieldName, value);
    }
  }


}
