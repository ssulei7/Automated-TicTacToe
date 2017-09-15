package com.cs478.suleiman.cs478project4;

import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import java.util.Random;

/*No outside resources were used except the Lecture slides, and the code examples that were shown in class (specifically threads with handlers)*/
public class MainActivity extends AppCompatActivity {

    private GridLayout myBoard;                 //Gridlayout representing board as buttons
    private Button startButton;                 //Button that starts game
    private Handler playerOneHandler;           //Handler for player one
    private Handler playerTwoHandler;           //Handler for player two
    public boolean gameInProgress = false;      //Boolean that determines if game is in prog
    public PlayerOneThread t1;                  //Player ones thread
    public PlayerTwoThread t2;                  //Player two's thread
    public int playerOnesPieceCount = 3;
    public int playerTwosPieceCount = 3;
    public int playersOneLastMove;
    public int playerTwosLastMove;

    /*Static variables that are passed as messages*/
    public static final int UPDATE_UI = 0;
    public static final int PLAYERONE_TURN = 1;
    public static final int PLAYERTWO_TURN = 2;
    public static final int STOP_GAME = 4;

    /*Handler for the UI thread that handles notifying players of their turn, updating the UI, and stopping the game on the UI thread*/
    private Handler UIHandler = new Handler(){

        @Override
        public void handleMessage(Message m)
        {
            int player = m.arg1;
            int pos = m.arg2;
            String piece;
            if(player == 1)
                piece = "X";
            else
                piece = "O";
            switch (m.what)
            {
                case UPDATE_UI:
                    updateUI(player, pos, piece);
                    validateWin(player);
                    break;
                case PLAYERONE_TURN:
                    Message m2 = playerOneHandler.obtainMessage(PLAYERONE_TURN);
                    playerOneHandler.sendMessage(m2);
                    break;
                case PLAYERTWO_TURN:
                    Message m3 = playerTwoHandler.obtainMessage(PLAYERTWO_TURN);
                    playerTwoHandler.sendMessage(m3);
                    break;
                case STOP_GAME:
                    UIHandler.removeCallbacksAndMessages(null);
                    break;
            }
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find the start button
        startButton = (Button)findViewById(R.id.startButton);

        //Find the gridlayout containing the board
        myBoard = (GridLayout)findViewById(R.id.myBoard);


        //Create start buttons on click listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If game is NOT in progress
                if(gameInProgress == false) {

                    //Set gameinprogress to true
                    gameInProgress = true;
                    clearBoard();
                    //Create two threads and start them
                    t1 = new PlayerOneThread();
                    t2 = new PlayerTwoThread();
                    t1.start();
                    t2.start();
                    //Notify UI that it's the first players turn
                    Message m = UIHandler.obtainMessage(PLAYERONE_TURN);
                    UIHandler.sendMessage(m);
                }
                //Second time pressing, stop game
                else if(gameInProgress == true)
                {
                    stopGameForButton();
                }

            }
        });
    }

    /*Thread for player one that handles it's turn and what to do when the game has been stopped.*/
    public class PlayerOneThread extends Thread
    {

        @Override
        public void run() {
            Looper.prepare();
            playerOneHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {

                    switch(msg.what)
                    {
                        case PLAYERONE_TURN:
                            try{Thread.sleep(1000);}catch (InterruptedException e){System.out.println("Thread interrupted!");}
                            playerOnesMove();
                            break;
                        case STOP_GAME:
                            playerOneHandler.removeCallbacksAndMessages(null);
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    /*Thread for player two that handles it's turn and what to do when the game has been stopped*/
    public class PlayerTwoThread extends Thread
    {
        @Override
        public void run()
        {
            Looper.prepare();
            playerTwoHandler = new Handler()
            {
              @Override
              public void handleMessage(Message msg)
              {
                  switch(msg.what)
                  {
                      case PLAYERTWO_TURN:
                          try{Thread.sleep(1000);}catch (InterruptedException e){System.out.println("Thread interrupted!");}
                          playerTwosMove();
                          break;
                      case STOP_GAME:
                          playerTwoHandler.removeCallbacksAndMessages(null);
                          break;
                  }
              }
            };
            Looper.loop();
        }
    }


    /*Void method that handles player 1's moves. Player ones strategy is to always go for corners*/
    public void playerOnesMove()
    {
        //Create two messages: one to update the UI, and one to let the UI know it's player 2's turn
        Message m = UIHandler.obtainMessage(MainActivity.UPDATE_UI);
        Message m2 = UIHandler.obtainMessage(PLAYERTWO_TURN);

        //If we still have pieces, follow the strategy
        if(playerOnesPieceCount > 0)
        {
            if (((Button) myBoard.getChildAt(0)).getText().equals(" ")) {
                m.arg1 = 1;
                m.arg2 = 0;
                playerOnesPieceCount--;
                UIHandler.sendMessage(m);
                UIHandler.sendMessage(m2);
            } else if (((Button) myBoard.getChildAt(2)).getText().equals(" ")) {
                m.arg1 = 1;
                m.arg2 = 2;
                playerOnesPieceCount--;
                playersOneLastMove = 2;
                UIHandler.sendMessage(m);
                UIHandler.sendMessage(m2);
            } else if (((Button) myBoard.getChildAt(6)).getText().equals(" ")) {
                m.arg1 = 1;
                m.arg2 = 6;
                playerOnesPieceCount--;
                playersOneLastMove = 6;
                UIHandler.sendMessage(m);
                UIHandler.sendMessage(m2);
            }
        }
        //Otherwise, make a move based on the last piece and randomly select an empty spot
        else
        {
            Random rand = new Random();
            while(true)
            {
                int index = rand.nextInt(9) + 0;
                Button b = (Button)myBoard.getChildAt(index);
                if(b.getText().equals(" ") && index != playersOneLastMove)
                {
                    m.arg1 = 1;
                    m.arg2 = index;
                    UIHandler.sendMessage(m);
                    UIHandler.sendMessage(m2);
                    break;
                }
            }
        }
    }

    /*Void method that handles player 2's moves. Player two's strategy is to always make an upside down L*/
    public void playerTwosMove()
    {
        //Create two messages: one to update UI, one to notify main thread that player two's turn is valid
        Message m = UIHandler.obtainMessage(MainActivity.UPDATE_UI);
        Message m2 = UIHandler.obtainMessage(PLAYERONE_TURN);

        //If we have over 0 pieces, follow strategy
        if(playerTwosPieceCount > 0)
        {
            if (((Button) myBoard.getChildAt(4)).getText().equals(" ")) {
                m.arg1 = 2;
                m.arg2 = 4;
                playerTwosPieceCount--;
                playerTwosLastMove = 4;
                UIHandler.sendMessage(m);
                UIHandler.sendMessage(m2);
            } else if (((Button) myBoard.getChildAt(5)).getText().equals(" ")) {
                m.arg1 = 2;
                m.arg2 = 5;
                playerTwosPieceCount--;
                playerTwosLastMove = 5;
                UIHandler.sendMessage(m);
                UIHandler.sendMessage(m2);
            } else if (((Button) myBoard.getChildAt(7)).getText().equals(" ")) {
                m.arg1 = 2;
                m.arg2 = 7;
                playerTwosPieceCount--;
                playerTwosLastMove = 7;
                UIHandler.sendMessage(m);
                UIHandler.sendMessage(m2);
            }
        }
        //Otherwise, based on last piece placed, move piece to random empty spot
        else
        {
            Random rand = new Random();
            while(true)
            {
                int index = rand.nextInt(9) + 0;
                Button b = (Button)myBoard.getChildAt(index);
                if(b.getText().equals(" ") && index != playerTwosLastMove)
                {
                    m.arg1 = 2;
                    m.arg2 = index;
                    UIHandler.sendMessage(m);
                    UIHandler.sendMessage(m2);
                    break;
                }
            }
        }
    }

    /*Void method that updates the UI by setting the appropriate text based on position and player*/
    public void updateUI(int player,int pos, String piece)
    {
        //If we are player 1
        if(player == 1)
        {
            //If we still have over 0 pieces, change text based on strategy
            if(playerOnesPieceCount > 0) {
                Button b = (Button) myBoard.getChildAt(pos);
                b.setText(piece);
            }
            //Otherwise, change text based on random selection
            else
            {
                Button b1 = (Button)myBoard.getChildAt(playersOneLastMove);
                Button b2 = (Button)myBoard.getChildAt(pos);
                b1.setText(" ");
                b2.setText(piece);
                playersOneLastMove = pos;
            }
        }
        //Otherwise, we are player 2
        else
        {
            //If we still have over 0 pieces, change text based on strategy
            if(playerTwosPieceCount > 0) {
                Button b = (Button) myBoard.getChildAt(pos);
                b.setText(piece);
            }
            //Otherwise, change text based on random selection
            else
            {
                Button b1 = (Button)myBoard.getChildAt(playerTwosLastMove);
                Button b2 = (Button)myBoard.getChildAt(pos);
                b1.setText(" ");
                b2.setText(piece);
                playerTwosLastMove = pos;
            }
        }
    }

    /*Void method that determines whether a player has won a game or not, if they did it'll display a toast and stop threads*/
    public void validateWin(int player)
    {
        if(player == 1)
        {
            //Check all rows and cols for a valid win

            //Check first row
            if(((Button)myBoard.getChildAt(0)).getText().equals("X") && ((Button)myBoard.getChildAt(1)).getText().equals("X") && ((Button)myBoard.getChildAt(2)).getText().equals("X"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 1 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check second row
            else if(((Button)myBoard.getChildAt(3)).getText().equals("X") && ((Button)myBoard.getChildAt(4)).getText().equals("X") && ((Button)myBoard.getChildAt(5)).getText().equals("X"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 1 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check third row
            else if(((Button)myBoard.getChildAt(6)).getText().equals("X") && ((Button)myBoard.getChildAt(7)).getText().equals("X") && ((Button)myBoard.getChildAt(8)).getText().equals("X"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 1 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check first column
            else if(((Button)myBoard.getChildAt(0)).getText().equals("X") && ((Button)myBoard.getChildAt(3)).getText().equals("X") && ((Button)myBoard.getChildAt(6)).getText().equals("X"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 1 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check second column
            else if(((Button)myBoard.getChildAt(1)).getText().equals("X") && ((Button)myBoard.getChildAt(4)).getText().equals("X") && ((Button)myBoard.getChildAt(7)).getText().equals("X"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 1 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check third column
            else if(((Button)myBoard.getChildAt(2)).getText().equals("X") && ((Button)myBoard.getChildAt(5)).getText().equals("X") && ((Button)myBoard.getChildAt(8)).getText().equals("X"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 1 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            //Check all rows and cols for a valid win
            //Check first row
            if(((Button)myBoard.getChildAt(0)).getText().equals("O") && ((Button)myBoard.getChildAt(1)).getText().equals("O") && ((Button)myBoard.getChildAt(2)).getText().equals("O"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 2 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check second row
            else if(((Button)myBoard.getChildAt(3)).getText().equals("O") && ((Button)myBoard.getChildAt(4)).getText().equals("O") && ((Button)myBoard.getChildAt(5)).getText().equals("O"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 2 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check third row
            else if(((Button)myBoard.getChildAt(6)).getText().equals("O") && ((Button)myBoard.getChildAt(7)).getText().equals("O") && ((Button)myBoard.getChildAt(8)).getText().equals("O"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 2 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check first column
            else if(((Button)myBoard.getChildAt(0)).getText().equals("O") && ((Button)myBoard.getChildAt(3)).getText().equals("O") && ((Button)myBoard.getChildAt(6)).getText().equals("O"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 2 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check second column
            else if(((Button)myBoard.getChildAt(1)).getText().equals("O") && ((Button)myBoard.getChildAt(4)).getText().equals("O") && ((Button)myBoard.getChildAt(7)).getText().equals("O"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 2 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
            //Check third column
            else if(((Button)myBoard.getChildAt(2)).getText().equals("O") && ((Button)myBoard.getChildAt(5)).getText().equals("X") && ((Button)myBoard.getChildAt(8)).getText().equals("O"))
            {
                stopGameForWin();
                Toast.makeText(this, "Player 2 has won! Press start after toast disappears to play again!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*Void method that handles stopping the game when the start button is repressed!*/
    public void stopGameForButton()
    {

        //Create a message for the ui, and the players that the game has been declared over
        Message ui = UIHandler.obtainMessage(STOP_GAME);
        Message p1 = playerOneHandler.obtainMessage(STOP_GAME);
        Message p2 = playerTwoHandler.obtainMessage(STOP_GAME);

        //Send message to all handlers
        UIHandler.sendMessage(ui);
        playerOneHandler.sendMessage(p1);
        playerTwoHandler.sendMessage(p2);

        //If the players threads are still alive, interrupt them!
        if(t1.isAlive())
            t1.interrupt();
        if(t2.isAlive());
            t2.interrupt();


        //Clear the board of all pieces
        clearBoard();

        //Re-give pieces to each player
        playerOnesPieceCount = 3;
        playerTwosPieceCount = 3;

        //Set game in progress to false
        gameInProgress = false;

    }

    /*Void method similar to the method above, except doesn't clear the board so player can see ending board*/
    public void stopGameForWin()
    {

        //Create a message for the ui, and the players that the game has been declared over
        Message ui = UIHandler.obtainMessage(STOP_GAME);
        Message p1 = playerOneHandler.obtainMessage(STOP_GAME);
        Message p2 = playerTwoHandler.obtainMessage(STOP_GAME);

        //Send message to all handlers
        UIHandler.sendMessage(ui);
        playerOneHandler.sendMessage(p1);
        playerTwoHandler.sendMessage(p2);

        //If the players threads are still alive, interrupt them!
        if(t1.isAlive())
            t1.interrupt();
        if(t2.isAlive());
            t2.interrupt();


        //Re-give pieces to each player
        playerOnesPieceCount = 3;
        playerTwosPieceCount = 3;

        //Set game in progress to false
        gameInProgress = false;

    }

    /*Void method that clears the board of pieces, making all spots empty*/
    public void clearBoard()
    {
        for(int i = 0; i < myBoard.getChildCount(); i++)
        {
            Button b = (Button)myBoard.getChildAt(i);
            b.setText(" ");
        }
    }
}
