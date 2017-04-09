/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.berkeley.ground.dao.models.cassandra;

import edu.berkeley.ground.dao.models.EdgeFactory;
import edu.berkeley.ground.dao.versions.cassandra.CassandraItemFactory;
import edu.berkeley.ground.dao.versions.cassandra.CassandraVersionHistoryDagFactory;
import edu.berkeley.ground.db.CassandraClient;
import edu.berkeley.ground.db.CassandraResults;
import edu.berkeley.ground.db.DbClient;
import edu.berkeley.ground.db.DbDataContainer;
import edu.berkeley.ground.exceptions.*;
import edu.berkeley.ground.model.models.Edge;
import edu.berkeley.ground.model.models.EdgeVersion;
import edu.berkeley.ground.model.models.Tag;
import edu.berkeley.ground.model.versions.GroundType;
import edu.berkeley.ground.model.versions.VersionHistoryDag;
import edu.berkeley.ground.util.IdGenerator;

import static edu.berkeley.ground.dao.models.cassandra.ElementOps.verifyNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CassandraEdgeFactory extends EdgeFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraEdgeFactory.class);
  private final CassandraClient dbClient;
  private final CassandraVersionHistoryDagFactory versionHistoryDagFactory;
  private final CassandraItemFactory itemFactory;
  private CassandraEdgeVersionFactory edgeVersionFactory;

  private final IdGenerator idGenerator;

  /**
   * Constructor for Cassandra edge factory.
   *
   * @param itemFactory a CassandraItemFactory singleton
   * @param dbClient the Cassandra client
   * @param idGenerator a unique ID generator
   * @param versionHistoryDagFactory a CassandraVersionHistoryDAGFactory singleton
   */
  public CassandraEdgeFactory(CassandraItemFactory itemFactory,
                              CassandraClient dbClient,
                              IdGenerator idGenerator,
                              CassandraVersionHistoryDagFactory versionHistoryDagFactory) {
    this.dbClient = dbClient;
    this.itemFactory = itemFactory;
    this.idGenerator = idGenerator;
    this.edgeVersionFactory = null;
    this.versionHistoryDagFactory = versionHistoryDagFactory;
  }

  public void setEdgeVersionFactory(CassandraEdgeVersionFactory edgeVersionFactory) {
    this.edgeVersionFactory = edgeVersionFactory;
  }

  /**
   * Creates and persists a new edge.
   *
   * @param name the name of the edge
   * @param sourceKey the user generated unique key for the edge
   * @param fromNodeId the id of the originating node for this edg
   * @param toNodeId the id of the destination node for this edg
   * @param tags tags on this edge
   * @return the created edge
   * @throws GroundException an error while creating or persisting the edge
   */
  @Override
  public Edge create(String name,
                     String sourceKey,
                     long fromNodeId,
                     long toNodeId,
                     Map<String, Tag> tags) throws GroundException {

    verifyItemNotExists(sourceKey);

    long uniqueId = this.idGenerator.generateItemId();

    this.itemFactory.insertIntoDatabase(uniqueId, tags);

    List<DbDataContainer> insertions = new ArrayList<>();
    insertions.add(new DbDataContainer("name", GroundType.STRING, name));
    insertions.add(new DbDataContainer("item_id", GroundType.LONG, uniqueId));
    insertions.add(new DbDataContainer("from_node_id", GroundType.LONG, fromNodeId));
    insertions.add(new DbDataContainer("to_node_id", GroundType.LONG, toNodeId));
    insertions.add(new DbDataContainer("source_key", GroundType.STRING, sourceKey));

    this.dbClient.insert("edge", insertions);

    LOGGER.info("Created edge " + name + ".");
    return EdgeFactory.construct(uniqueId, name, sourceKey, fromNodeId, toNodeId, tags);
  }

  @Override
  public Edge retrieveFromDatabase(String sourceKey) throws GroundException {
    return this.retrieveByPredicate("source_key", sourceKey, GroundType.STRING);
  }

  @Override
  public Edge retrieveFromDatabase(long id) throws GroundException {
    return this.retrieveByPredicate("item_id", id, GroundType.LONG);
  }

  private Edge retrieveByPredicate(String fieldName, Object value, GroundType valueType)
      throws GroundException {

    List<DbDataContainer> predicates = new ArrayList<>();
    predicates.add(new DbDataContainer(fieldName, valueType, value));

    CassandraResults resultSet = this.dbClient.equalitySelect("edge", DbClient.SELECT_STAR, predicates);
    verifyNotEmpty(resultSet, Edge.class, fieldName, value);

    long id = resultSet.getLong("item_id");
    long fromNodeId = resultSet.getLong("from_node_id");
    long toNodeId = resultSet.getLong("to_node_id");

    String name = resultSet.getString("name");
    String sourceKey = resultSet.getString("source_key");

    Map<String, Tag> tags = this.itemFactory.retrieveFromDatabase(id).getTags();

    LOGGER.info("Retrieved edge " + value + ".");
    return EdgeFactory.construct(id, name, sourceKey, fromNodeId, toNodeId, tags);
  }

  /**
   * Update this edge with a new version.
   *
   * @param itemId the item id of the edge
   * @param childId the id of the new child
   * @param parentIds the ids of any parents of the child
   * @throws GroundException an unexpected error during the update
   */
  @Override
  public void update(long itemId, long childId, List<Long> parentIds) throws GroundException {
    this.itemFactory.update(itemId, childId, parentIds);
    parentIds = parentIds.stream().filter(x -> x != 0).collect(Collectors.toList());

    for (long parentId : parentIds) {
      EdgeVersion currentVersion = this.edgeVersionFactory.retrieveFromDatabase(childId);
      EdgeVersion parentVersion = this.edgeVersionFactory.retrieveFromDatabase(parentId);
      Edge edge = this.retrieveFromDatabase(itemId);

      long fromNodeId = edge.getFromNodeId();
      long toNodeId = edge.getToNodeId();

      long fromEndId = -1;
      long toEndId = -1;

      if (parentVersion.getFromNodeVersionEndId() == -1) {
        // update from end id
        VersionHistoryDag dag = this.versionHistoryDagFactory.retrieveFromDatabase(fromNodeId);
        fromEndId = (long) dag.getParent(currentVersion.getFromNodeVersionStartId()).get(0);
      }

      if (parentVersion.getToNodeVersionEndId() == -1) {
        // update to end id
        VersionHistoryDag dag = this.versionHistoryDagFactory.retrieveFromDatabase(toNodeId);
        toEndId = (long) dag.getParent(currentVersion.getToNodeVersionStartId()).get(0);
      }

      if (fromEndId != -1 || toEndId != -1) {
        this.edgeVersionFactory.updatePreviousVersion(parentId, fromEndId, toEndId);
      }
    }
  }

  @Override
  public void truncate(long itemId, int numLevels) throws GroundException {
    this.itemFactory.truncate(itemId, numLevels, "edge");
  }
}
