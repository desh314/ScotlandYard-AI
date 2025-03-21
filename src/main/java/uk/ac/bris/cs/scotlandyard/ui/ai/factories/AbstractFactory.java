package uk.ac.bris.cs.scotlandyard.ui.ai.factories;

import uk.ac.bris.cs.scotlandyard.ui.ai.MoveSelectingStrategy;

/**
 * Interface defining a Factory that produces a {@link MoveSelectingStrategy}.
 * Use to abstract away details and allow the use of a single {@link MoveSelectingStrategy} interface.
 */
public interface AbstractFactory {
    /**
     *
     * @return A {@link MoveSelectingStrategy}
     */
    MoveSelectingStrategy getMoveSelectingStrategy();
}
