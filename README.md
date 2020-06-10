# Squava
From [BoardGameGeek](https://boardgamegeek.com/boardgame/112745/squava):
> Squava is played on a 5x5 square board. 2 players (White and Black) alternate turns placing a pawn of their colour on an empty space of the board.
> Players strive to make a line of 4 pieces of their colour, but lose if they make a line of 3 before doing so. Lines can be orthogonal or diagonal.

Squava is your typical two-player connection game with a twist. The addition of a rule that prevents each player from creating a certain structure is innovative, as can allow players to set up very sophisticated lines. In this document I will refer to the first player as black and the second player as white.

When I first started playing this game with others, I though that black would have an advantage as they are able to lead white into forcing sequences. After further analysis, I saw that the black's move can be targeted by white to force black into undesired positions. Moreover, the small board means that games can transition into an endgame, where players have to calculate whether they have more available moves than their opponent. This looks advantageous for white, as white will end up with less moves than black. From my perspective, it was unclear which player was better from the beginning.

However, it seems that the simplest solution is indeed the correct solution, as black is able to force a win given perfect play. Out of the 6 opening moves, three have winnings lines, and the other three have refutations. 

In the source folder there is the Agent.java abstract class and an AlphaBetaAgent.java class which were used to create the solution database. The Board.java class stores the position with bitboard and has some other miscellaneous methods. There is a basic UI built with a simple JFrame template, which allows you to test out the solution. To test out a certain opening click on the file menu button and select an opening from the dropdown list. The default opening is set to 6.

To initialize the database, the program takes all the values from the states.txt file in the resources folder and stores them in a hashmap with the state as the key and the move to be played as the value. At every state when it is black's turn, the program sees whether or not the states exists in the hashmap. If it does exists, the program plays the value, and if it does not exist, the program plays a move calculated from the AlphaBetaAgent. States that are not recorded in the database are all the one move winning plays. 

Much of the inspiration for this project comes from [bediger4000's github repository](https://github.com/bediger4000/squava), where he creates agents in Golang that are able to play fairly well. Squava is beginner-friendly game to test out some of the search techniques that programmers have refined over the years, and is a very nice introduction into game-searching along with other similar games. 
