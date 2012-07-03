package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchTableTest extends AbstractTest {

  // helper method
  private void assertIslandEquals(int leftRow, int leftColumn, int rightRow, int rightColumn, Island island) {
    Coordinate leftEnd = island.getLeftEnd();
    assertEquals(leftRow, leftEnd.getRow());
    assertEquals(leftColumn, leftEnd.getColumn());
    Coordinate rightEnd = island.getRightEnd();
    assertEquals(rightRow, rightEnd.getRow());
    assertEquals(rightColumn, rightEnd.getColumn());
  }

  @Test
  public void testTableCreationAbAcAbc() {
    SimpleWitness[] witnesses = createWitnesses("a b", "a c", "a b c");
    VariantGraph graph = collate(witnesses[0], witnesses[1]);
    MatchTable table = MatchTable.create(graph, witnesses[2]);
    assertVertexEquals("a", table.at(0, 0));
    assertVertexEquals("b", table.at(1, 1));
    assertVertexEquals("c", table.at(2, 1));
  }

  @Test
  public void testTableCreationAbcabCab() {
    SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable table = MatchTable.create(graph, witnesses[1]);
    assertVertexEquals("a", table.at(1, 0));
    assertVertexEquals("b", table.at(2, 1));
    assertVertexEquals("c", table.at(0, 2));
    assertVertexEquals("a", table.at(1, 3));
    assertVertexEquals("b", table.at(2, 4));
  }
  
  @Test
  public void testTableCreationAbcabAbcab() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTable table = MatchTable.create(vg, sw[1], new EqualityTokenComparator());
    assertEquals(5, table.columnList().size());
    assertEquals(5, table.rowList().size());
    assertVertexEquals("a", table.at(0, 0));
    assertVertexEquals("a", table.at(0, 3));
    assertVertexEquals("b", table.at(1, 1));
    assertVertexEquals("b", table.at(1, 4));
    assertVertexEquals("c", table.at(2, 2));
    assertVertexEquals("a", table.at(3, 0));
    assertVertexEquals("a", table.at(3, 3));
    assertVertexEquals("b", table.at(4, 1));
    assertVertexEquals("b", table.at(4, 4));
  }

  @Test
  public void testIslandDetectionAbcabCab() {
    SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable table = MatchTable.create(graph, witnesses[1]);
    List<Island> islands = Lists.newArrayList(table.getIslands());
    assertEquals(2, islands.size());
    Collections.sort(islands);
    Island island = islands.get(1);
    assertIslandEquals(0, 2, 2, 4, island);
  }

  @Test
  public void testIslandDetectionXabcabXcab() {
    SimpleWitness[] witnesses = createWitnesses("x a b c a b", "x c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable table = MatchTable.create(graph, witnesses[1]);
    List<Island> islands = Lists.newArrayList(table.getIslands());
    assertEquals(3, islands.size());
    Collections.sort(islands);
    Island island = islands.get(0);
    assertIslandEquals(0, 0, 0, 0, island);
  }

}
