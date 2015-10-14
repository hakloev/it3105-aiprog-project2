package org.ntnu.it3105.ai;

import org.ntnu.it3105.game.Controller;

/**
 * Created by Aleksander Skraastad (myth) on 10/14/15.
 * <p/>
 * 2048-solver is licenced under the MIT licence.
 */
public interface Solver {
    void actuateNextMove();
    void solve();
    void shutdown();
}
