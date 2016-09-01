package jp.techacademy.yoshihiro.minagawa.jumpactiongame;

/**
 * Created by ym on 16/08/31.
 */

import com.badlogic.gdx.graphics.Texture;

//触れると得点を獲得する星のstarクラスを作成する
//
public class Star extends GameObject{

    //横幅、高さ
    public static final float STAR_WIDTH = 0.0f;
    public static final float STAR_HEIGHT = 0.0f;

    //状態
    public static final int STAR_EXIST = 0;
    public static final int STAR_NONE = 0;

    int mState;

    public Star(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight){
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(STAR_WIDTH, STAR_HEIGHT);
        mState = STAR_EXIST;
    }

    //獲得されると消える
    //getAlphaで透明に
    public void get(){
        mState = STAR_NONE;
        setAlpha(0);
    }
}
