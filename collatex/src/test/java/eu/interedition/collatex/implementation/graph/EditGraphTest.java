package eu.interedition.collatex.implementation.graph;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.graph.EditGraph;
import eu.interedition.collatex.implementation.graph.EditGraphEdge;
import eu.interedition.collatex.implementation.graph.EditGraphLinker;
import eu.interedition.collatex.implementation.graph.EditGraphVertex;
import eu.interedition.collatex.implementation.graph.EditGraphVisitor;
import eu.interedition.collatex.implementation.graph.EditOperation;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EditGraphTest extends AbstractTest {

  @Test
  public void testUsecase1() {
    final IWitness[] w = createWitnesses("The black cat", "The black and white cat");
    final VariantGraph graph = merge(w[0]);
    EditGraphLinker linker = new EditGraphLinker(graphFactory);
    Map<Token, Token> link = linker.link(VariantGraphWitnessAdapter.create(graph), w[1], new EqualityTokenComparator());
    assertEquals(3, link.size());
  }

  @Test
  public void testGapsEverythingEqual() {
    // All the witness are equal
    // There are choices to be made however, since there is duplication of tokens
    // Optimal alignment has no gaps
    final IWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    assertNumberOfGaps(0, graphFactory.newEditGraph().build(w[0], w[1], new EqualityTokenComparator()));
  }

  @Test
  public void testGapsOmission() {
    // There is an omission
    // Optimal alignment has 1 gap
    // Note: there are two paths here that contain 1 gap
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    assertNumberOfGaps(1, graphFactory.newEditGraph().build(w[0], w[1], new EqualityTokenComparator()));
  }

  @Test
  public void testRemoveChoicesThatIntroduceGaps() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EditGraph eg = graphFactory.newEditGraph().build(w[0], w[1], new EqualityTokenComparator());
    assertShortestPathVertices(eg, "the", "black", "cat");
  }

  //When there are multiple paths with the same minimum number of gaps
  //do a second pass that tries to find the longest common sequence
  @Test
  public void testTryToFindMinimumAmountOfSequences() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = graphFactory.newEditGraph().build(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    Matches matches = Matches.between(w[0], w[1], comparator);
    EditGraph dGraph2 = visitor.removeChoicesThatIntroduceGaps(matches);
    Map<EditGraphVertex, Integer> determineMinSequences = visitor.determineMinSequences(dGraph2);
    // asserts
    Iterator<EditGraphVertex> dgVerticesIterator = dGraph2.vertices().iterator();
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(2), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
    assertEquals(new Integer(1), determineMinSequences.get(dgVerticesIterator.next()));
  }

  @Test
  public void testShortestPathOneOmissionRepetition() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = graphFactory.newEditGraph().build(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);
    Matches matches = Matches.between(w[0], w[1], comparator);
    List<EditGraphEdge> edges = visitor.getShortestPath(matches);
    assertEquals(EditOperation.GAP, edges.get(0).getEditOperation()); // The ideal path should start with a gap
    assertEquals(EditOperation.NO_GAP, edges.get(1).getEditOperation());
    assertEquals(EditOperation.NO_GAP, edges.get(2).getEditOperation());
    assertEquals(EditOperation.NO_GAP, edges.get(3).getEditOperation());
    assertEquals(4, edges.size());
  }

  // TODO
  // All the witness are equal
  // There should only be one valid path through this decision graph
  @Ignore
  @Test
  public void testShortestPathEverythingEqual() {
    final IWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    EqualityTokenComparator comparator = new EqualityTokenComparator();
    EditGraph dGraph = graphFactory.newEditGraph().build(w[0], w[1], comparator);
    EditGraphVisitor visitor = new EditGraphVisitor(dGraph);

    Matches matches = Matches.between(w[0], w[1], comparator);
    List<EditGraphEdge> path = visitor.getShortestPath(matches);
    // we expect 8 edges
    // they all should have weight 0
    Iterator<EditGraphEdge> edges = path.iterator();
    assertEquals(new Integer(0), edges.next().getEditOperation());
  }

  protected static void assertShortestPathVertices(EditGraph dGraph, String... vertices) {
    final Iterator<EditGraphEdge> shortestPath = shortestPathIn(dGraph).iterator();
    shortestPath.next(); // skip start vertex

    int vc = 0;
    for (String vertex : vertices) {
      assertTrue("Shortest path to short", shortestPath.hasNext());
      assertEquals(vertex + "[" + (vc++) + "]", vertex, ((SimpleToken) shortestPath.next().from().getWitness()).getNormalized());
    }
  }

  protected static Iterable<EditGraphEdge> shortestPathIn(EditGraph eg) {
    final Iterable<EditGraphEdge> shortestPath = eg.shortestPath(0);
    assertTrue("Shortest path exists", !Iterables.isEmpty(shortestPath));
    return shortestPath;
  }

  protected static void assertNumberOfGaps(int expected, EditGraph eg) {
    int numberOfGaps = 0;
    for (EditGraphEdge e : shortestPathIn(eg)) {
      if (e.getEditOperation() == EditOperation.GAP) {
        numberOfGaps++;
      }
    }
    assertEquals(expected, numberOfGaps);
  }

  //  the -> The
  //  the -> the
  //  black -> black
  //  cat -> cat
  //  cat -> cat
  // bij een decision tree zou de black wegvallen
  // we maken er een graaf van, dan krijgen we twee cirkels als het ware
  // shortest path
  // bij elke vertex bijhouden wat de minimum weight daar is
  // bij elke edge bijhouden of hij deel uitmaakt van het shortest path
  // dan zou het mogelijk moeten zijn om meerdere shortest paths 
  // te reconstrueren

  // ik kan nu een nieuwe graph maken waarbij ik alle vertices en edges die niet kleiner 
  // of gelijk de minimum weight zijn deleten
  // maar of dat echt nodig is
  // is nog maar de vraag
  
  
  
  // we moeten bijhouden welk pad we gelopen hebben in de vorm van edges
  // ook moeten we bijhouden welke stappen we nog moeten zetten
  // daar twijfel ik tussen de vertices en de edges
  // aangezien je wil recursen bij meerdere vertices..
  // en dan dus een bepaalde edge meegeven om te doen...
  // laten we initialisen met een bepaalde edge
//  DGVertex start = graph.getStartVertex();
//  List<List<DGEdge>> initialpaths = Lists.newArrayList();
//  initialpaths.add(new ArrayList<DGEdge>());
//  List<List<DGEdge>> paths = traverseVertex(initialpaths, graph, vertexToMinWeight, minGaps, start);
//  System.out.println(paths);

  //  DGEdge[] bla = new DGEdge[] { new DGEdge(start, start, minGaps), new DGEdge(start, start, minGaps) };
  // we kunnen natuurlijk de graph in een tree converten
  // door strategies de vertex te dupliceren
  // dan kun je alle paden vinden door de leaf nodes af te lopen
  // een dag zou meerdere start nodes kunne hebben
  // daar is er geen algoritme voor
  // maar mijn dag heeft maar 1 start node..
  // aargh
  
  
  // het moet wel met een graph want anders wordt het nix
  // in een nromale decision tree schuif je dan die ene optie in de bij de andere
  // dan ben ik echter de kost kwijt
  // of je kunt in dit geval zeggen dat die case niet bestaat
  // maar das niet echt mooi
  
//
//
//
//private List<List<DGEdge>> traverseVertex(List<List<DGEdge>> paths, DecisionGraph graph,
//    Map<DGVertex, Integer> vertexToMinWeight, int minGaps, DGVertex source) {
//  Set<DGEdge> outgoingEdges = graph.outgoingEdgesOf(source);
//  // hier moeten we kijken hoeveel outgoingEdges source heeft
//  // 0 == we zijn klaar; return de paden gewoon zoals ze zijn
//  // 1 == vul het huidige path gewoon aan en we zijn klaar
//  // >1 == maak extra paden aan in de list!
//  // hier het aantal outgoing edges checken werkt niet,
//  // want er kunnen er een aantal onzichtbaar zijn..
//  
//  for (DGEdge edge : outgoingEdges) {
//    DGVertex targetVertex = edge.getTargetVertex();
//    if (vertexToMinWeight.get(targetVertex) <= minGaps) {
//      // we willen dit path bewandelen
//      for (List<DGEdge> path : paths) {
//        path.add(edge);
//      }
//      traverseVertex(paths, graph, vertexToMinWeight, minGaps, targetVertex);
//    }
//  }
//  return paths;
//}

}