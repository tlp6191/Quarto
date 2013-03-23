import java.util.*;
public class Revised{
	public static void main(String args[]){
		
	}
}
class Game{
	boolean player, set;
	/*These are all the sets we are keeping for whether we have already solved a given state. We are mostly keeping these so we can write it out to the disk
	We are only keeping the states with 4,8,or 12 pieces on the board
	as a balance between the work and memory of keeping these sets, and 
	the desire to have data to analyze and be able to exit a branch quickly. 	
	We can actually keep the result in the same set as the game state itself
	by utilizing the fact that the first piece in the upper left corner is 
	always 0000. So, to store that player 1 won the game, we start the int
	with 10. To store that player 2 won, we store 01. To store a tie, 11.
	We are doing this because I'm incredible scared of the amount of memory 
	we need to store these things. 
	*/
	/*Optomization, add later.
	public static Set<Long> FourPiece=new HashSet<Long>();
	public static Set<Long> EightPiece=new HashSet<Long>();
	public static Set<Long> TwelvePiece=new HashSet<Long>();*/
	public Set<byte> pieces;
	public byte[][] board;
	//public byte chosen;
	
	Game(byte piece, byte placement, Game previous){
		//Generates a new game state based on the placement.
	}
	Game(){
		pieces=new LinkedHashSet<Long>();
		board=new byte[4][4];
	}
	int winner(){
		if(done()){
			return result();
		}
		int winner=-1;
		//For every piece
			//For every placement
			//winner=best of(winner, new Game(piece,placement,this).winner())
			
	}
}
