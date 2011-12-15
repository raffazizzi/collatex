package eu.interedition.collatex.implementation.graph;

import com.google.common.io.Files;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static eu.interedition.collatex.implementation.graph.GraphRelationshipType.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(GraphFactory.class);

  private Resolver<IWitness> witnessResolver = new DefaultResolver<IWitness>();
  private Resolver<Token> tokenResolver = new DefaultResolver<Token>();
  private EmbeddedGraphDatabase database;
  private Node variantGraphs;
  private Node editGraphs;

  public GraphFactory() throws IOException {
    this(Files.createTempDir(), true);
  }

  public GraphFactory(File dbStorageDirectory) throws IOException {
    this(dbStorageDirectory, false);
  }

  public GraphFactory(File dbStorageDirectory, final boolean deleteAfterUsage) throws IOException {
    final File dbDirectory = dbStorageDirectory.getCanonicalFile();

    LOG.debug("Creating variant graph database in {} (deleteAfterUsage = {})", dbDirectory, deleteAfterUsage);
    database = new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath());
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        database.shutdown();
        if (deleteAfterUsage && dbDirectory.isDirectory()) {
          try {
            Files.deleteRecursively(dbDirectory);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }));

    final Transaction tx = database.beginTx();
    try {
      final Node referenceNode = database.getReferenceNode();
      final Relationship vgRel = referenceNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING);
      if (vgRel == null) {
        referenceNode.createRelationshipTo(variantGraphs = database.createNode(), VARIANT_GRAPH);
      } else {
        variantGraphs = vgRel.getEndNode();
      }

      final Relationship egRel = referenceNode.getSingleRelationship(EDIT_GRAPH, OUTGOING);
      if (egRel == null) {
        referenceNode.createRelationshipTo(editGraphs = database.createNode(), VARIANT_GRAPH);
      } else {
        editGraphs = egRel.getEndNode();
      }
      tx.success();
    } finally {
      tx.finish();
    }
  }

  public EmbeddedGraphDatabase getDatabase() {
    return database;
  }

  public void setWitnessResolver(Resolver<IWitness> witnessResolver) {
    this.witnessResolver = witnessResolver;
  }

  public void setTokenResolver(Resolver<Token> tokenResolver) {
    this.tokenResolver = tokenResolver;
  }

  public VariantGraph newVariantGraph() {
    final VariantGraph graph = new VariantGraph(database, witnessResolver, tokenResolver);
    graph.init(VariantGraphVertex.createWrapper(graph), VariantGraphEdge.createWrapper(graph), database.createNode(), database.createNode());

    variantGraphs.createRelationshipTo(graph.getStart().getNode(), START_END);
    graph.getEnd().getNode().createRelationshipTo(variantGraphs, START_END);

    return graph;
  }

  public EditGraph newEditGraph() {
    final EditGraph graph = new EditGraph(database, witnessResolver, tokenResolver);
    graph.init(EditGraphVertex.createWrapper(graph), EditGraphEdge.createWrapper(graph), database.createNode(), database.createNode());

    editGraphs.createRelationshipTo(graph.getStart().getNode(), START_END);
    graph.getEnd().getNode().createRelationshipTo(editGraphs, START_END);

    return graph;
  }

  public void delete(EditGraph eg) {
    eg.getStart().getNode().getSingleRelationship(START_END, INCOMING).delete();
    eg.getEnd().getNode().getSingleRelationship(START_END, OUTGOING).delete();
    for (EditGraphVertex v : eg.vertices()) {
      for (EditGraphEdge e : v.incoming()) {
        e.delete();
      }
      v.delete();
    }
  }
}