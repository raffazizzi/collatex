package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public interface ITokenSequence {

  INormalizedToken getFirstToken();

  INormalizedToken getLastToken();

}