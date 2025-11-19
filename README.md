# Scotland Yard AI


Various algorithms developed to play Scotland Yard. 

![alt-text](https://github.com/desh314/ScotlandYard-AI/blob/8a3b43984950e5836d4671d7f9899e23b7630cba/MRXSIMS.png)


## Algorithms Implemented:

  - ### Minimax
    - Filtering and multi-threading to improve the depth of the search tree for minimax
  - ### Monte Carlo Tree Search (MCTS)
    - Implemented MCTS using a bidirectional tree data strucutre and a hylomorphism for the search. Used visitors to implement a catamorphism and anamorphisms for expansion and backpropogation. 
    - Multithreaded MCTS. Used a voting strategy to aggregate results of multiple searches
  - ### Partical Swarm Optimization
    - Used partical swarm optimization to create collaborative behaviour between several detectives and planning for better game play. 
    - Translated continous partical swarm optimization onto the discrete graph representation of the game.
    - Got comparable performance to minimax and MCTS with much less computation and time.

## Project Documentation:

[Results and key findings](https://drive.google.com/file/d/14HCROs98tYaQZbPGIvEDkNLh6KHDVkTk/view?usp=sharing)

