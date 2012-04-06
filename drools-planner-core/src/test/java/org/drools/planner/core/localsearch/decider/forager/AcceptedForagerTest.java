/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.localsearch.decider.forager;

import java.util.List;
import java.util.Random;

import org.drools.planner.core.domain.solution.SolutionDescriptor;
import org.drools.planner.core.localsearch.LocalSearchSolverPhaseScope;
import org.drools.planner.core.localsearch.LocalSearchStepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.localsearch.decider.deciderscorecomparator.NaturalDeciderScoreComparatorFactory;
import org.drools.planner.core.move.DummyMove;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.buildin.simple.DefaultSimpleScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.buildin.simple.SimpleScoreDefinition;
import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
import org.drools.planner.core.score.director.drools.DroolsScoreDirectorFactory;
import org.drools.planner.core.solver.DefaultSolverScope;
import org.drools.planner.core.testdata.domain.TestdataSolution;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class AcceptedForagerTest {

    @Test
    public void testPickMoveMaxScoreOfAll() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyType.NEVER, Integer.MAX_VALUE);
        ((AcceptedForager) forager).setDeciderScoreComparatorFactory(new NaturalDeciderScoreComparatorFactory()); // TODO
        LocalSearchSolverPhaseScope localSearchSolverPhaseScope = createLocalSearchSolverPhaseScope();
        forager.phaseStarted(localSearchSolverPhaseScope);
        LocalSearchStepScope localSearchStepScope = createStepScope(localSearchSolverPhaseScope);
        forager.beforeDeciding(localSearchStepScope);
        // Pre conditions
        MoveScope a = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-20), true);
        MoveScope b = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-1), true);
        MoveScope c = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-20), false);
        MoveScope d = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-300), true);
        MoveScope e = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-2), true);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertFalse(forager.isQuitEarly());
        forager.addMove(e);
        assertFalse(forager.isQuitEarly());
        MoveScope pickedScope = forager.pickMove(localSearchStepScope);
        // Post conditions
        assertSame(b, pickedScope);
        List<Move> topList = forager.getTopList(3);
        assertTrue(topList.contains(a.getMove())); // Because c is not accepted
        assertTrue(topList.contains(b.getMove()));
        assertFalse(topList.contains(c.getMove()));
        assertFalse(topList.contains(d.getMove()));
        assertTrue(topList.contains(e.getMove()));
        forager.phaseEnded(localSearchSolverPhaseScope);
    }

    @Test
    public void testPickMoveFirstBestScoreImproving() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyType.FIRST_BEST_SCORE_IMPROVING, Integer.MAX_VALUE);
        ((AcceptedForager) forager).setDeciderScoreComparatorFactory(new NaturalDeciderScoreComparatorFactory()); // TODO
        LocalSearchSolverPhaseScope localSearchSolverPhaseScope = createLocalSearchSolverPhaseScope();
        forager.phaseStarted(localSearchSolverPhaseScope);
        LocalSearchStepScope localSearchStepScope = createStepScope(localSearchSolverPhaseScope);
        forager.beforeDeciding(localSearchStepScope);
        // Pre conditions
        MoveScope a = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-1), false);
        MoveScope b = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-20), true);
        MoveScope c = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-300), true);
        MoveScope d = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-1), true);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertTrue(forager.isQuitEarly());
        // Post conditions
        MoveScope pickedScope = forager.pickMove(localSearchStepScope);
        assertSame(d, pickedScope);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b.getMove()));
        assertTrue(topList.contains(d.getMove()));
        forager.phaseEnded(localSearchSolverPhaseScope);
    }

    @Test
    public void testPickMoveFirstLastStepScoreImproving() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyType.FIRST_LAST_STEP_SCORE_IMPROVING, Integer.MAX_VALUE);
        ((AcceptedForager) forager).setDeciderScoreComparatorFactory(new NaturalDeciderScoreComparatorFactory()); // TODO
        LocalSearchSolverPhaseScope localSearchSolverPhaseScope = createLocalSearchSolverPhaseScope();
        forager.phaseStarted(localSearchSolverPhaseScope);
        LocalSearchStepScope localSearchStepScope = createStepScope(localSearchSolverPhaseScope);
        forager.beforeDeciding(localSearchStepScope);
        // Pre conditions
        MoveScope a = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-1), false);
        MoveScope b = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-300), true);
        MoveScope c = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-4000), true);
        MoveScope d = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-20), true);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertTrue(forager.isQuitEarly());
        // Post conditions
        MoveScope pickedScope = forager.pickMove(localSearchStepScope);
        assertSame(d, pickedScope);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b.getMove()));
        assertTrue(topList.contains(d.getMove()));
        forager.phaseEnded(localSearchSolverPhaseScope);
    }

    @Test @Ignore
    public void testPickMoveRandomly() {
        // Setup
        Forager forager = new AcceptedForager(PickEarlyType.NEVER, 1);
        ((AcceptedForager) forager).setDeciderScoreComparatorFactory(new NaturalDeciderScoreComparatorFactory()); // TODO
        LocalSearchSolverPhaseScope localSearchSolverPhaseScope = createLocalSearchSolverPhaseScope();
        forager.phaseStarted(localSearchSolverPhaseScope);
        LocalSearchStepScope localSearchStepScope = createStepScope(localSearchSolverPhaseScope);
        forager.beforeDeciding(localSearchStepScope);
        // Pre conditions
        MoveScope a = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-20), false);
        MoveScope b = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-1), true);
        MoveScope c = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-1), false);
        MoveScope d = createMoveScope(localSearchStepScope, DefaultSimpleScore.valueOf(-20), true);
        // Do stuff
        forager.addMove(a);
        assertFalse(forager.isQuitEarly());
        forager.addMove(b);
        assertFalse(forager.isQuitEarly());
        forager.addMove(c);
        assertFalse(forager.isQuitEarly());
        forager.addMove(d);
        assertTrue(forager.isQuitEarly());
        // Post conditions
        MoveScope pickedScope = forager.pickMove(localSearchStepScope);
        assertSame(d, pickedScope);
        List<Move> topList = forager.getTopList(2);
        assertTrue(topList.contains(b.getMove()));
        assertTrue(topList.contains(d.getMove()));
        forager.phaseEnded(localSearchSolverPhaseScope);
    }

    private LocalSearchSolverPhaseScope createLocalSearchSolverPhaseScope() {
        DefaultSolverScope solverScope = new DefaultSolverScope();
        LocalSearchSolverPhaseScope localSearchSolverPhaseScope = new LocalSearchSolverPhaseScope(solverScope);
        DroolsScoreDirectorFactory scoreDirectorFactory = new DroolsScoreDirectorFactory();
        scoreDirectorFactory.setSolutionDescriptor(TestdataSolution.buildSolutionDescriptor());
        scoreDirectorFactory.setScoreDefinition(new SimpleScoreDefinition());
        solverScope.setScoreDirector(scoreDirectorFactory.buildScoreDirector());
        solverScope.setWorkingRandom(new Random() {
            public double nextDouble() {
                return 0.2;
            }
        });
        solverScope.setBestScore(DefaultSimpleScore.valueOf(-10));
        LocalSearchStepScope lastLocalSearchStepScope = new LocalSearchStepScope(localSearchSolverPhaseScope);
        lastLocalSearchStepScope.setScore(DefaultSimpleScore.valueOf(-100));
        localSearchSolverPhaseScope.setLastCompletedLocalSearchStepScope(lastLocalSearchStepScope);
        return localSearchSolverPhaseScope;
    }

    private LocalSearchStepScope createStepScope(LocalSearchSolverPhaseScope localSearchSolverPhaseScope) {
        LocalSearchStepScope localSearchStepScope = new LocalSearchStepScope(localSearchSolverPhaseScope);
        return localSearchStepScope;
    }

    public MoveScope createMoveScope(LocalSearchStepScope localSearchStepScope, Score score, boolean accepted) {
        MoveScope moveScope = new MoveScope(localSearchStepScope);
        moveScope.setMove(new DummyMove());
        moveScope.setScore(score);
        moveScope.setAccepted(accepted);
        return moveScope;
    }

}
