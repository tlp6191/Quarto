import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;


public class QuartoTest {

	Game testGame = new Game();
	
	@Test
	public void toLongTestAllF() {
		
		testGame.board=new byte[4][4];
		for(int x = 0; x<4;x++){
			for(int y = 0; y<4; y++){
				testGame.board[x][y] = 0xF;
			}
		}
		
		assertEquals(-1, (long)testGame.toLong());
	}
	
	
	@Test
	public void toLongTestAllZero() {

		testGame.board=new byte[4][4];
		for(int x = 0; x<4;x++){
			for(int y = 0; y<4; y++){
				testGame.board[x][y] = 0x0;
			}
		}
		
		assertEquals(0, (long)testGame.toLong());
	}
	
	@Test
	public void toLongTestIncrement() {
		int count = 0;
		
		testGame.board=new byte[4][4];
		for(int x = 0; x<4;x++){
			for(int y = 0; y<4; y++){
				testGame.board[x][y] = (byte)(count++);
			}
		}
		
		assertEquals(Long.parseLong("0123456789abcdef", 16), (long)testGame.toLong());
	}
	
	@Test
	public void toLongTestDecrement() {
		int count = 15;
		
		testGame.board=new byte[4][4];
		for(int x = 0; x<4;x++){
			for(int y = 0; y<4; y++){
				testGame.board[x][y] = (byte)(count--);
			}
		}
		
		assertEquals((long)Long.decode("-81985529216486896"), (long)testGame.toLong());//0xfedcba9876543210 = -81985529216486896
	}
	
	@Test
	public void toLongTestAssorted() {
		
		testGame.board=new byte[4][4];
		for(int x = 0; x<4;x++){
			for(int y = 0; y<4; y++){
				testGame.board[x][y] = (byte)(x+y);
			}
		}
		
		assertEquals(Long.parseLong("0123123423453456", 16), (long)testGame.toLong());
	}
	
	@Test
	public void transformTest3Piece(){
		Game testGame1 = new Game();
		testGame1.board = new byte[4][4];
		testGame1.board[0][0] = 0;
		
		testGame1.board[1][1] = 1;
		testGame1.board[2][2] = 3;
		testGame1.board[3][2] = 5;
		
		testGame1.transform();
		
		Game testGame2 = new Game();
		testGame2.board = new byte[4][4];
		testGame2.board[0][0] = 0;
		
		testGame2.board[3][2] = 1;
		testGame2.board[1][3] = 3;
		testGame2.board[2][3] = 5;
		
		testGame2.transform();
		
		assertArrayEquals(testGame1.board, testGame2.board);
	}
	

	@Test
	public void transformTest5Piece(){
		Game testGame1 = new Game();
		testGame1.board = new byte[4][4];
		testGame1.board[0][0] = 0;
		
		testGame1.board[1][1] = 1;
		testGame1.board[1][3] = 6;
		testGame1.board[2][3] = 2;
		testGame1.board[3][1] = 7;
		testGame1.board[3][3] = 9;
		
		testGame1.transform();
		
		Game testGame2 = new Game();
		testGame2.board = new byte[4][4];
		testGame2.board[0][0] = 0;
		
		testGame2.board[1][2] = 7;
		testGame2.board[1][3] = 9;
		testGame2.board[2][2] = 1;
		testGame2.board[2][3] = 6;
		testGame2.board[3][3] = 2;
		
		testGame2.transform();
		
		assertArrayEquals(testGame1.board, testGame2.board);
	}
	
	@Test
	public void transformTest6Piece(){
		Game testGame1 = new Game();
		testGame1.board = new byte[4][4];
		testGame1.board[0][0] = 0;
		
		testGame1.board[1][1] = 1;
		testGame1.board[1][2] = 7;
		testGame1.board[1][3] = 5;
		testGame1.board[2][2] = 9;
		testGame1.board[2][3] = 6;
		testGame1.board[3][1] = 4;
		
		testGame1.transform();
		
		Game testGame2 = new Game();
		testGame2.board = new byte[4][4];
		testGame2.board[0][0] = 0;
		
		testGame2.board[1][2] = 6;
		testGame2.board[1][3] = 9;
		testGame2.board[2][1] = 1;
		testGame2.board[2][2] = 5;
		testGame2.board[2][3] = 7;
		testGame2.board[3][1] = 4;
		
		testGame2.transform();
		
		assertArrayEquals(testGame1.board, testGame2.board);
	}
	
	@Test
	public void transformTestReflect(){
		Game testGame1 = new Game();
		testGame1.board = new byte[4][4];
		testGame1.board[0][0] = 0;
		
		testGame1.board[1][1] = 1;
		testGame1.board[2][1] = 4;
		testGame1.board[2][2] = 6;
		testGame1.board[3][2] = 7;
		testGame1.board[3][3] = 9;
	
		
		testGame1.transform();
		
		Game testGame2 = new Game();
		testGame2.board = new byte[4][4];
		testGame2.board[0][0] = 0;
		
		testGame2.board[1][1] = 1;
		testGame2.board[1][2] = 4;
		testGame2.board[2][2] = 6;
		testGame2.board[2][3] = 7;
		testGame2.board[3][3] = 9;
	
		
		testGame2.transform();
		
		assertArrayEquals(testGame1.board, testGame2.board);
	}
}
