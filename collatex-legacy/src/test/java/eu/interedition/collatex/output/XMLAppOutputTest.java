package eu.interedition.collatex.output;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class XMLAppOutputTest {

  private static WitnessBuilder builder;

  @BeforeClass
  public static void setUp() {
    builder = new WitnessBuilder();
  }

  private String collateWitnessStrings(final String a, final String b, final String c) {
    final Witness w1 = builder.build("A", a);
    final Witness w2 = builder.build("B", b);
    final Witness w3 = builder.build("C", c);
    final WitnessSet set = new WitnessSet(w1, w2, w3);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    return table.toXML();
  }

  /**
   * The first example from #6 (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without witness C for now)
   */
  @Test
  public void testSimpleSubstitutionOutput() {
    final String xml = collateWitnessStrings("the black cat and the black mat", "the black dog and the black mat", "the black dog and the black mat");
    Assert.assertEquals("<collation><seg>the black <app><rdg wit=\"#A\">cat</rdg><rdg wit=\"#B #C\">dog</rdg></app> and the black mat</seg></collation>", xml);
  }

  /**
   * Second example from #6. Tests addition, deletion and multiple words in one variant 
   */
  @Test
  public void testSimpleAddDelOutput() {
    final String xml = collateWitnessStrings("the black cat on the white table", "the black saw the black cat on the table", "the black saw the black cat on the table");
    Assert.assertEquals(
        "<collation><seg>the black <app><rdg wit=\"#A\"/><rdg wit=\"#B #C\">saw the black</rdg></app> cat on the <app><rdg wit=\"#A\">white</rdg><rdg wit=\"#B #C\"/></app> table</seg></collation>",
        xml);
  }

  @Test
  public void testMultiSubstitutionOutput() {
    final String xml = collateWitnessStrings("the black cat and the black mat", "the big white dog and the black mat", "the big white dog and the black mat");
    Assert.assertEquals("<collation><seg>the <app><rdg wit=\"#A\">black cat</rdg><rdg wit=\"#B #C\">big white dog</rdg></app> and the black mat</seg></collation>", xml);
  }

  // Additional unit tests (not present in ticket #6)
  @Test
  public void testAllWitnessesEqual() {
    final Witness w1 = builder.build("the black cat");
    final Witness w2 = builder.build("the black cat");
    final Witness w3 = builder.build("the black cat");
    final WitnessSet set = new WitnessSet(w1, w2, w3);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "<collation><seg>the black cat</seg></collation>";
    Assert.assertEquals(expected, table.toXML());
  }

  // Note: There are some problems with whitespace here!
  @Test
  public void testAWordMissingAtTheEnd() {
    final Witness w1 = builder.build("A", "the black cat");
    final Witness w2 = builder.build("B", "the black cat");
    final Witness w3 = builder.build("C", "the black");
    final WitnessSet set = new WitnessSet(w1, w2, w3);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "<collation><seg>the black <app><rdg wit=\"#A #B\">cat</rdg><rdg wit=\"#C\"/></app></seg></collation>";
    Assert.assertEquals(expected, table.toXML());
  }

  // Note: There might be some problems with whitespace here!
  @Test
  public void testCrossVariation() {
    final Witness w1 = builder.build("A", "the black cat");
    final Witness w2 = builder.build("B", "the white and black cat");
    final Witness w3 = builder.build("C", "the white cat");
    final WitnessSet set = new WitnessSet(w1, w2, w3);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "<collation><seg>the <app><rdg wit='#A'/><rdg wit='#B #C'>white</rdg></app> <app><rdg wit='#A #C'/><rdg wit='#B'>and</rdg></app> <app><rdg wit='#A #B'>black</rdg><rdg wit='#C'/></app> cat</seg></collation>"
        .replaceAll("\\'", "\\\"");
    Assert.assertEquals(expected, table.toXML());
  }

  // Note: There might be some problems with whitespace here!
  @Test
  public void testAddition() {
    final Witness w1 = builder.build("A", "the black cat");
    final Witness w2 = builder.build("B", "the white and black cat");
    final WitnessSet set = new WitnessSet(w1, w2);
    final AlignmentTable2 table = AlignmentTableCreator.createAlignmentTable(set);
    final String expected = "<collation><seg>the <app><rdg wit=\"#A\"/><rdg wit=\"#B\">white and</rdg></app> black cat</seg></collation>";
    Assert.assertEquals(expected, table.toXML());
  }

  //  @Test
  //  public void testNearMatches() {
  //    Witness w1 = builder.build("A", "the black cat");
  //    Witness w2 = builder.build("B", "the blak cat");
  //    Witness w3 = builder.build("C", "the black cat");
  //    WitnessSet set = new WitnessSet(w1, w2, w3);
  //    AlignmentTable2 table = set.createAlignmentTable();
  //    String expected = "<collation>the black cat</collation>";
  //    Assert.assertEquals(expected, table.toXML());
  //  }
}