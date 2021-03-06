/**
 * @author Adam Chappell, Joel Lyles, Taylor Phebus, Forrest Stallings
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;;
import java.io.*;

public class Quarto
{
	public static void main(String args[])
	{
		/*
		Testing game:
		0|0|2|c 
		0|4|0|0
		9|0|0|6
		7|d|0|b
		*/
		// This is the only place we want to declare an executor so that
		// we can control the number of threads in the entire system
		ExecutorService e = Executors.newCachedThreadPool();
		try{
			Game.out= new PrintWriter(new BufferedWriter(new FileWriter(args[0])));
			Game.threadP=Double.parseDouble(args[1]);
		}catch(IOException ex){
			ex.printStackTrace();
			System.exit(1);
		}
		BlockingQueue<Integer> q = new ArrayBlockingQueue<Integer>(1);
		if (args.length > 2)
		{
			byte pieces[] = new byte[16];
			for (int i = 0; i < 16; i++)
			{
				pieces[i] = (byte) Integer.parseInt(args[i+2]);
			}
			if(args.length>=19&&args[18].equals("N")){
				Set<Byte> constPieces=new LinkedHashSet<Byte>();
				for(int i=0; i<16; i++){
					constPieces.add((byte)i);
				}
				Runnable worker1 = new Game(pieces, q, e, constPieces);
				e.execute(worker1);
				int winner1 = Utils.waitAndGetResult(q);
				System.out.println(winner1);
				e.shutdown();
				System.out.println("Complete");
				
			}
			else{
			Set<Byte> constPieces1=new LinkedHashSet<Byte>();
			Set<Byte> constPieces2=new LinkedHashSet<Byte>();
			Set<Byte> constPieces3=new LinkedHashSet<Byte>();
			Set<Byte> constPieces4=new LinkedHashSet<Byte>();
			constPieces1.add((byte)0);
			constPieces1.add((byte)1);
			constPieces1.add((byte)2);
			constPieces1.add((byte)3);
			Runnable worker1 = new Game(pieces, q, e, constPieces1);
			e.execute(worker1);
			//int winner1 = Utils.waitAndGetResult(q);
			//System.out.println(winner1);
			constPieces2.add((byte)4);
			constPieces2.add((byte)5);
			constPieces2.add((byte)6);
			constPieces2.add((byte)7);
			Runnable worker2 = new Game(pieces, q, e, constPieces2);
			e.execute(worker2);
			constPieces3.add((byte)8);
			constPieces3.add((byte)9);
			constPieces3.add((byte)10);
			constPieces3.add((byte)11);
			Runnable worker3 = new Game(pieces, q, e, constPieces3);
			e.execute(worker3);
			constPieces4.add((byte)12);
			constPieces4.add((byte)13);
			constPieces4.add((byte)14);
			constPieces4.add((byte)15);
			Runnable worker4 = new Game(pieces, q, e, constPieces4);
			e.execute(worker4);
			
			for(int i=0; i<4; i++){
			int winner = Utils.waitAndGetResult(q);
			System.out.println(winner);
			}
			}
			//Game g=new Game(pieces);
			//g.print();
			//System.out.println(g.winner());
			e.shutdown();
			System.out.println("Complete");
		} else
		{
			Runnable worker = new Game(q, e);
			e.execute(worker);
			int winner = Utils.waitAndGetResult(q);
			System.out.println(winner);
			//System.out.println(new Game().winner());
			e.shutdown();
		}
	}
}

class Game implements Runnable
{
	public static  PrintWriter out ;
	public static double threadP;
	public static Map<Long,Integer> seenStates=new ConcurrentHashMap<Long, Integer>();
	public static synchronized void write(){
		count.lazySet(0);
		try{
			out.println(System.currentTimeMillis()+ " "+((ThreadPoolExecutor)(exec)).getActiveCount()+" "+seenStates.size());
			out.flush();
			System.err.println(System.currentTimeMillis()+ " "+((ThreadPoolExecutor)(exec)).getActiveCount()+" "+seenStates.size());
		}catch(Exception e){e.printStackTrace();System.exit(1);}
	}
	public static AtomicInteger count=new AtomicInteger();
	/*Possible draw state (For testing):
		0|1|2|c 
		3|4|5|8
		9|a|f|6
		7|d|0|b
		0000|0001|0010|1100
		0011|0100|0101|1000
		1001|1010|1111|0110
		0111|1101|0000|1011
	*/
	boolean player; //, set; set was a variable for the twist.
	static ExecutorService exec;
	BlockingQueue<Integer> parentQueue;

	/**
	 * These are all the sets we are keeping for whether we have already solved a given state. 
	 * We are mostly keeping these so we can write it out to the disk
	 * We are only keeping the states with 4,8,or 12 pieces on the board
	 * as a balance between the work and memory of keeping these sets, and 
	 * the desire to have data to analyze and be able to exit a branch quickly. 	
	 * We can actually keep the result in the same set as the game state itself
	 * by utilizing the fact that the first piece in the upper left corner is 
	 * always 0000. So, to store that player 1 won the game, we start the int
	 * with 10. To store that player 2 won, we store 01. To store a tie, 11.
	 * We are doing this because I'm incredible scared of the amount of memory 
	 * we need to store these things. 
	*/

	public void run()
	{
		// TODO Review this
		if(!constrained){
			Utils.putAndWaitForSpace(this.parentQueue, this.winner());
		}
		else{
			Utils.putAndWaitForSpace(this.parentQueue, this.winConstrained(constrainedPieces));
		}
			
	}

	/*Optomization, add later.
	public static Set<Long> FourPiece=new HashSet<Long>();
	public static Set<Long> EightPiece=new HashSet<Long>();
	public static Set<Long> TwelvePiece=new HashSet<Long>();*/
	public Set<Byte> pieces;
	public Set<Byte> constrainedPieces;
	public boolean constrained=false;
	public byte[][] board;
	protected byte CalculatedResult = -2;
	final boolean DEBUG = false;

	public Long toLong()
	{
		long r = (long) 0;
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				r += board[i][j];
				r = r << 4;
			}
		}
		r = r >> 4;
		return r;
	}

	void debugp(String s)
	{
		if (DEBUG)
		{
			System.out.println(s);
		}
	}

	void print()
	{
		debugp("" + Long.toHexString(toLong()));
		System.out.println(player ? 2 : 1);
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				System.out.print(Integer.toHexString(((Byte) board[i][j]).intValue()) + "|");
			}
			System.out.println("");
		}
		System.out.flush();
	}

	public void transform()
	{
		sortByRow();
		sortByCol();
		
		//sorting alone can't cover cases like this
		//[0][ ][ ][ ]		[0][ ][ ][ ]
		//[ ][1][ ][ ]		[ ][1][4][ ]
		//[ ][4][6][ ]		[ ][ ][6][7]
		//[ ][ ][7][9]		[ ][ ][ ][9]
		if(needsReflecting()){
			reflect();
		}
	}
	
	private void sortByRow(){
		boolean finished  = false;
		
		while(!finished){
			finished = true;
			
			for(int j = 1; j<board[0].length - 1;j++){
				if(smallestElementInRow(j) > smallestElementInRow(j+1)){
					swapRows(j, j+1);
					finished = false;
				}
			}
		}
	}
	
	private void swapRows(int a, int b){
		for(int x = 0; x<4;x++){
			byte temp = board[a][x];
			board[a][x] = board[b][x];
			board[b][x] = temp;
		}
	}
	
	private byte smallestElementInRow(int row){
		byte min = Byte.MAX_VALUE;
		
		for(int x=0; x<board[row].length;x++){
			if(board[row][x] < min && board[row][x]!=0){
				min = board[row][x];
			}
		}
		
		return min;
	}
	
	private void sortByCol(){
		boolean finished  = false;
		
		while(!finished){
			finished = true;
			
			for(int j = 1; j<board.length - 1;j++){
				if(smallestElementInCol(j) > smallestElementInCol(j+1)){
					swapCols(j, j+1);
					finished = false;
				}
			}
		}
	}
	
	private void swapCols(int a, int b){
		for(int x = 0; x<4;x++){
			byte temp = board[x][a];
			board[x][a] = board[x][b];
			board[x][b] = temp;
		}
	}

	private byte smallestElementInCol(int col){
		byte min = Byte.MAX_VALUE;
		
		for(int x=0; x<board.length;x++){
			if(board[x][col] < min && board[x][col]!=0){
				min = board[x][col];
			}
		}
		
		return min;
	}
	
	
	//for game states that are diagonally symmetric we need
	//to decide on a common way of representing them
	private boolean needsReflecting(){
		for(int row = 1; row<4;row++){
			for(int col = 0; col < row; col++){
				if(board[row][col] < board[col][row]){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void reflect(){
		for(int row = 0; row<4;row++){
			for(int col = row; col<4;col++){
				byte temp = board[row][col];
				board[row][col] = board[col][row];
				board[col][row] = temp;
			}
		}
	}
	
	public Game(){//used for testing
		board = new byte[4][4];
	}
	
	Game(byte[] piecesArr, BlockingQueue<Integer> queue, ExecutorService e, Set<Byte> constraints)
	{//Pass in 16 bytes. The first better be 0.
		this.parentQueue = queue;
		this.constrainedPieces=constraints;
		this.constrained=true;
		this.exec = e;
		board = new byte[4][4];
		int num0 = 0;

		if (piecesArr[0] != 0)
		{
			System.err.println("Incorrect game state constructor");
		}
		int k = 0;
		pieces = new LinkedHashSet<Byte>();
		for (byte i = 0; i < 16; i++)
		{
			pieces.add(i);
		}
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				if (piecesArr[k] == 0)
				{
					num0++;//Used to determine the player number
				}
				pieces.remove(piecesArr[k]);
				board[i][j] = piecesArr[k];
				k++;
			}
		}
		if ((num0 % 2) == 1)
		{
			player = false;
		}
		transform();
	}

	//public byte chosen; chosen was a variable for the twist
	Game(byte piece, int i, int j, Game previous, BlockingQueue<Integer> queue, ExecutorService e)
	{
		this.parentQueue = queue;
		this.exec = e;
		//Generates a new game state based on the placement.
		board = new byte[4][4];
		for (int I = 0; I < 4; I++)
		{
			for (int J = 0; J < 4; J++)
			{
				board[I][J] = previous.board[I][J];
			}
		}
		board[i][j] = piece;
		player = !previous.player;
		pieces = new LinkedHashSet<Byte>();
		pieces.addAll(previous.pieces);
		pieces.remove(piece);
		//TODO: Transform
		transform();
	}

	/**
	 * Instantiation of a new Game.
	 * Creates the pieces and board.
	 */
	Game(BlockingQueue<Integer> queue, ExecutorService e)
	{
		this.parentQueue = queue;
		this.exec = e;
		player = false;//Start with P2, since the 0 piece was placed by P1
		pieces = new LinkedHashSet<Byte>();
		board = new byte[4][4];
		for (byte i = 1; i < 16; i++)
		{
			pieces.add(i);
		}
		transform();
	}

	/**
	 * Decides the winner.
	 * @return The ID of the winner, or 0 for tie.
	 */
	int winner()
	{
		if (DEBUG)
		{
			print();
		}
		if(seenStates.containsKey(toLong())){
			int x=count.incrementAndGet();
			if(x==1000000){
				write();
			}
			return seenStates.get(toLong());
		}
		if (done())
		{
			if (DEBUG)
			{
				print();
				System.out.println("Player = " + (player ? 2 : 1) + " Winner = " + CalculatedResult);
			}
			int x=count.incrementAndGet();
			if(x==1000000){
				write();
			}
			return CalculatedResult;
		}
		//TODO: Look up the winner in the hashMaps.
		//TODO: With some propability p, divide into threads.
		int winner = 3;
		byte nextPiece;
		Iterator<Byte> i = pieces.iterator();
		nextMoves: while (i.hasNext())
		{
			nextPiece = i.next();
			debugp("Next piece = " + nextPiece);
			//For every piece
			//For every placement
			BlockingQueue<Integer> newQueue = new ArrayBlockingQueue<Integer>(16);
			for (int j = 0; j < 4; j++)
			{
				for (int k = 0; k < 4; k++)
				{
					if (!(j == 0 && k == 0) && board[j][k] == 0)
					{//Don't overwrite the top left.
					//out.println(System.currentTimeMillis()+ " "+((ThreadPoolExecutor)(exec)).getActiveCount());

						if (Math.random() < threadP && ((ThreadPoolExecutor)(exec)).getQueue().size()<8)
						{
							Runnable worker = new Game(nextPiece, j, k, this, newQueue, this.exec);
							this.exec.execute(worker);
						} else
						{
							new Game(nextPiece, j, k, this, newQueue, this.exec).run();
						}
						/*
						int winTemp = new Game(nextPiece, j, k, this).winner();
						//winner=best of(winner, new Game(piece,placement,this).winner())
						//TODO: Store result.
						if (winTemp == 1 && !player)
						{
							debugp("Breaking for P1Win");
							winner = 1;
							break nextMoves;
						}
						if (winTemp == 2 && player)
						{
							debugp("Breaking for P2Win");
							winner = 2;
							break nextMoves;
						}
						winner = Math.min(winner, winTemp);
						//Take the tie if that's an option.
						 */
					}
				}
			}

			int winTemp = Utils.waitAndGetResult(newQueue);
			//winner=best of(winner, new Game(piece,placement,this).winner())
			//TODO: Store result.
			if (winTemp == 1 && !player)
			{
				debugp("Breaking for P1Win");
				winner = 1;
				break nextMoves;
			}
			if (winTemp == 2 && player)
			{
				debugp("Breaking for P2Win");
				winner = 2;
				break nextMoves;
			}
			winner = Math.min(winner, winTemp);
		}
		if (winner == 3)
		{
			System.err.println("Incorrect Winner Assigned");
		}
		//TODO: Store result.
		debugp("Player = " + (player ? 2 : 1) + " Winner = " + winner);
		//if(pieces.size()==12||pieces.size()==8||pieces.size()==4){
			seenStates.put(toLong(), winner);
		//}
		int x=count.incrementAndGet();
		if(x==1000000){
			write();
		}
		return winner;
	}

/**
	 * Decides the winner, with the constraint that the first piece chosen
	 *.must be from the list of pieces passed in.
	 * @return The ID of the winner, or 0 for tie.
	 */
	int winConstrained(Set possiblePieces)
	{
		if (DEBUG)
		{
			print();
		}
		debugp("Win with constraint"); 
		if(seenStates.containsKey(toLong())){
			int x=count.incrementAndGet();
			if(x==1000000){
				write();
			}
			return seenStates.get(toLong());
		}
		if (done())
		{
			if (DEBUG)
			{
				print();
				System.out.println("Player = " + (player ? 2 : 1) + " Winner = " + CalculatedResult);
			}
			int x=count.incrementAndGet();
			if(x==1000000){
				write();
			}
			return CalculatedResult;
		}
		//TODO: Look up the winner in the hashMaps.
		//TODO: With some propability p, divide into threads.
		int winner = 3;
		byte nextPiece; 
		Set<Byte> piecesToChoose = new LinkedHashSet<Byte>();
		piecesToChoose.addAll(pieces);
		piecesToChoose.retainAll(possiblePieces);
		System.out.println("Pieces to choose from: "+piecesToChoose.size());
		Iterator<Byte> i = piecesToChoose.iterator();
		while(i.hasNext()){
			System.out.print(i.next());
		}System.out.println("");
		
		i = piecesToChoose.iterator();
		nextMoves: while (i.hasNext())
		{
			nextPiece = i.next();
			debugp("Next piece = " + nextPiece);
			//For every piece
			//For every placement
			BlockingQueue<Integer> newQueue = new ArrayBlockingQueue<Integer>(16);
			for (int j = 0; j < 4; j++)
			{
				for (int k = 0; k < 4; k++)
				{
					if (!(j == 0 && k == 0) && board[j][k] == 0)
					{//Don't overwrite the top left.
					//out.println(System.currentTimeMillis()+ " "+((ThreadPoolExecutor)(exec)).getActiveCount());

						if (Math.random() < threadP && ((ThreadPoolExecutor)(exec)).getQueue().size()<8)
						{
							Runnable worker = new Game(nextPiece, j, k, this, newQueue, this.exec);
							this.exec.execute(worker);
						} else
						{
							new Game(nextPiece, j, k, this, newQueue, this.exec).run();
						}
						/*
						int winTemp = new Game(nextPiece, j, k, this).winner();
						//winner=best of(winner, new Game(piece,placement,this).winner())
						//TODO: Store result.
						if (winTemp == 1 && !player)
						{
							debugp("Breaking for P1Win");
							winner = 1;
							break nextMoves;
						}
						if (winTemp == 2 && player)
						{
							debugp("Breaking for P2Win");
							winner = 2;
							break nextMoves;
						}
						winner = Math.min(winner, winTemp);
						//Take the tie if that's an option.
						 */
					}
				}
			}

			int winTemp = Utils.waitAndGetResult(newQueue);
			//winner=best of(winner, new Game(piece,placement,this).winner())
			//TODO: Store result.
			if (winTemp == 1 && !player)
			{
				debugp("Breaking for P1Win");
				winner = 1;
				break nextMoves;
			}
			if (winTemp == 2 && player)
			{
				debugp("Breaking for P2Win");
				winner = 2;
				break nextMoves;
			}
			winner = Math.min(winner, winTemp);
		}
		if (winner == 3)
		{
			System.err.println("Incorrect Winner Assigned");
		}
		//TODO: Store result.
		debugp("Player = " + (player ? 2 : 1) + " Winner = " + winner);
		//if(pieces.size()==12||pieces.size()==8||pieces.size()==4){
			seenStates.put(toLong(), winner);
		//}
		int x=count.incrementAndGet();
		if(x==1000000){
			write();
		}
		return winner;
	}



	/**
	 * Figures out if the algorithm is done.
	 * @return TRUE if done, FALSE otherwise.
	 */
	boolean done()
	{
		//If there is a winner
		//Return true
		CalculatedResult = result();
		if (CalculatedResult > 0)
			return true;
		if (pieces.size() == 0)
			return true;
		//If out of pieces
		//Return true
		//Return false
		return false;

	}

	/**
	 * The result of the current gamestate?
	 * @return 0 for tie, 1 for player 1 wins, 2 for player 2 wins
	 * -1 for inconsistent gamestate
	 */
	byte result()
	{
		byte mask1 = 0x01;
		byte mask2 = 0x02;
		byte mask3 = 0x04;
		byte mask4 = 0x08;
		//For all rows
		rows: for (int i = 0; i < 4; i++)
		{
			//Are all the pieces in this row placed
			for (int j = 0; j < 4; j++)
			{
				if (board[i][j] == 0 && !(j == 0 && i == 0))
				{
					continue rows;
				}
			}
			//For all attributes
			//Are the cols all 0?
			byte temp1 = 0;
			byte temp2 = 0;
			byte temp3 = 0;
			byte temp4 = 0;
			for (int j = 0; j < 4; j++)
			{
				temp1 += board[i][j] & mask1;
				temp2 += board[i][j] & mask2;
				temp3 += board[i][j] & mask3;
				temp4 += board[i][j] & mask4;
			}
			if (temp1 == 0 || temp2 == 0 || temp3 == 0 || temp4 == 0)
			{
				debugp("Returning Winner-0-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
			}
			temp1 = mask1;
			temp2 = mask2;
			temp3 = mask3;
			temp4 = mask4;
			//Are the cols all 1?
			for (int j = 0; j < 4; j++)
			{
				temp1 = (byte) (temp1 & board[i][j]);
				temp2 = (byte) (temp2 & board[i][j]);
				temp3 = (byte) (temp3 & board[i][j]);
				temp4 = (byte) (temp4 & board[i][j]);
			}
			if (temp1 == 0x01 || temp2 == 0x02 || temp3 == 0x04 || temp4 == 0x08)
			{
				debugp("Returning Winner-1-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
			}
		}
		//For all cols
		cols: for (int i = 0; i < 4; i++)
		{
			//Are all the pieces in this row placed
			for (int j = 0; j < 4; j++)
			{
				if (board[j][i] == 0 && !(j == 0 && i == 0))
				{
					continue cols;
				}
			}
			//For all attributes
			//Are the cols all 0?
			byte temp1 = 0;
			byte temp2 = 0;
			byte temp3 = 0;
			byte temp4 = 0;
			for (int j = 0; j < 4; j++)
			{
				temp1 += board[j][i] & mask1;
				temp2 += board[j][i] & mask2;
				temp3 += board[j][i] & mask3;
				temp4 += board[j][i] & mask4;
			}
			if (temp1 == 0 || temp2 == 0 || temp3 == 0 || temp4 == 0)
			{
				debugp("Returning ColWinner-0-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
			}
			temp1 = mask1;
			temp2 = mask2;
			temp3 = mask3;
			temp4 = mask4;
			//Are the cols all 1?
			for (int j = 0; j < 4; j++)
			{
				temp1 = (byte) (temp1 & board[j][i]);
				temp2 = (byte) (temp2 & board[j][i]);
				temp3 = (byte) (temp3 & board[j][i]);
				temp4 = (byte) (temp4 & board[j][i]);
			}
			if (temp1 == 0x01 || temp2 == 0x02 || temp3 == 0x04 || temp4 == 0x08)
			{
				debugp("Returning ColWinner-1-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
			}
		}

		//Otherwise, return 0 if we are out of pieces.
		if (pieces.size() == 0)
		{
			//System.err.println("Draw");
			return 0;
		}
		return -1;
	}
}

// Various wrappers for concurrency
class Utils
{
	public static Integer waitAndGetResult(BlockingQueue<Integer> q)
	{
		try
		{
			return q.take();
		} catch (InterruptedException e)
		{
			System.err.println("InterruptedException in Utils.waitAndGetResult: " + e.toString());
		}
		return -1;
	}

	public static void putAndWaitForSpace(BlockingQueue<Integer> q, Integer e)
	{
		try
		{
			q.put(e);
		} catch (InterruptedException e1)
		{
			System.err.println("InterruptedException in Utils.putAndWaitForSpace: " + e.toString());
		}
	}
}
