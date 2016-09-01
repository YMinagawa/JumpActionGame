package jp.techacademy.yoshihiro.minagawa.jumpactiongame;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by ym on 16/08/31.
 */
public class GameObject extends Sprite {

    public final Vector2 velocity; //xとyの速度を保持する

    public GameObject(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight){
        super(texture, srcX, srcY, srcWidth, srcHeight);
        velocity = new Vector2();
    }
}
