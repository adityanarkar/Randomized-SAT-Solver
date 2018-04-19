# Randomized-SAT-Solver

Requirements:

1. Groovy 2.4.13

To run the solver:

1. cd /path/to/Solver
2. groovy solver.groovy <cnf filename> <max-tries>

I have implemented this randomized solver by implementing PPZ algorithm with following heuristic:

If there is a variable for which assignment has to be choosen randomly, then rather than choosing randomly, assignment will be choosen based on probability.

There is a 50% probability that truth assignment will be choosen randomly, and there is a 50% probability that truth assignment will be choosen based on maximum occurances heuristic.

If a positive literal for variable occurs more than that of negation of it, then for that variable false truth assignment will be assigned. By doing so, we have higher probability to have a unit clause for remaining variables of a permutation.
