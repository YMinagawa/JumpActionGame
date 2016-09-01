package jp.techacademy.yoshihiro.minagawa.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by ym on 16/08/31.
 */
public class Player extends GameObject{

    //横幅、高さ
    public static final float PLAYER_WIDTH = 1.0f;
    public static final float PLAYER_HEIGHT = 1.0f;

    //状態(ジャンプ中、落ちている最中)
    public static final int PLAYER_STATE_JUMP = 0;
    public static final int PLAYER_STATE_FALL  = 1;

    //速度(ジャンプ時の速度、横方向への速度)
    public static final float PLAYER_JUMP_VELOCITY = 11.0f;
    public static final float PLAYER_MOVE_VELOCITY = 20.0f;

    //状態を保持するmState
    int mState;

    public Player(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(PLAYER_WIDTH, PLAYER_HEIGHT);
        mState = PLAYER_STATE_FALL;
    }


    //Screenのrenderメソッドから呼び出されることを想定してるupdateメソッド
    //位置情報とJUMP、FALLの状態が変わる
    public void update(float delta, float accelX){

        //重力をプレイヤーの速度に加算し、速度から位置
        velocity.add(0, GameScreen.GRAVITY * delta);
        velocity.x = -accelX/10 * PLAYER_MOVE_VELOCITY;
        setPosition(getX() + velocity.x*delta, getY() + velocity.y*delta);

        //y方向の速度が正(上方向)のときのSTATEからPLAYER_STATE_JUMPでなければ、PLAYER_STATE_JUMPにする
        if(velocity.y > 0){
            if(mState != PLAYER_STATE_JUMP){
                mState = PLAYER_STATE_JUMP;
            }
        }

        //y方向の速度が負(下方向)のときにSTATEからPLAYER_STATE_FALLでなければ、PLAYER_STATE_FALLにする
        if(velocity.y < 0){
            if(mState != PLAYER_STATE_FALL){
                mState = PLAYER_STATE_FALL;
            }
        }

        //画面の橋まで来たら反対側に移動させる
        if(getX() + PLAYER_WIDTH/2 < 0){
            setX(GameScreen.WORLD_WIDTH - PLAYER_WIDTH/2);
        }else if(getX() + PLAYER_WIDTH/2 > GameScreen.WORLD_WIDTH){
            setX(0);
        }

    }

    public void hitStep(){
        velocity.y = PLAYER_JUMP_VELOCITY;
        mState = PLAYER_STATE_JUMP;
    }


}
