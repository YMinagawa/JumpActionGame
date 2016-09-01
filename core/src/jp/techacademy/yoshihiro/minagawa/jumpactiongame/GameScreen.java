package jp.techacademy.yoshihiro.minagawa.jumpactiongame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ym on 16/08/31.
 */

//各画面に相当するScreenはScreenAdapterクラスを継承する
public class GameScreen extends ScreenAdapter {


    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;
    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15*20; //20画面分登れば終了

    static final int GAME_STATE_READY = 0;  //ゲーム開始前
    static final int GAME_STATE_PLAYING = 1;  //ゲーム中
    static final int GAME_STATE_GAMEOVER = 2;  //ゴールか落下してゲーム終了

    //重力
    static final float GRAVITY = -12;

    private JumpActionGame mGame;

    Sprite mBg;      //スプライトはコンピューターの処理の負荷を上げずに高速に描画する仕組み
    OrthographicCamera mCamera; //
    FitViewport mViewPort; //ゲームの世界の物理的なディスプレイを届けるためのオブジェクト

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    Ufo mUfo;
    Player mPlayer;

    float mHeightSoFar; //プレイヤーが地面からどれだけ離れたかを保持する
    int mGameState;
    Vector3 mTouchPoint; //タッチされた座標を保持するメンバ変数mTouchPointを定義

    //コンストラクタでは引数で受け取ったJumpActionGameクラスのオブジェクトをメンバ変数に格納する。
    public GameScreen(JumpActionGame game){

        mGame = game;

        //背景の準備
        Texture bgTexture = new Texture("back.png");
        //TextureRegionで切り出す時は原点は左上
        mBg = new Sprite(new TextureRegion(bgTexture,0,0,540,810));
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        mBg.setPosition(0, 0);

        //カメラ、ViewPortを生成、設定する (端末に依らず縦横比を保ったままにするため)
        //カメラとViewPortのサイズを最初に同じ値にする
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera);

        //メンバ変数の初期化
        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3(); //タッチされた座標を保持するメンバ変数mTouchPointを定義

        createStage();

    }

    @Override
    //毎フレームの描画を行う(1/60 s毎に自動に呼び出される)
    //オブジェクトの状態(座標など)をアップデートするupdateメソッドの呼び出しとオブジェクトの描画を行う
    //踏み台と星はListで保持しているので順番に取り出す
    public void render(float delta) {

        //下2つは画面の描画する準備
        //glClearColorメソッドは画面をクリアにする時の色を赤、緑、青、透過で指定する。
        //glClear(GL20.GL_COLOR_BUFFER_BIT)で実際にその色をクリア(塗りつぶし)をする。
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //どの端末でも縦横比を保ったままできる限り大きく表示するためにカメラとビューポートを使用する
        //カメラの座標をアップデート(計算)し(setProjectionMatrix(mCamera.combined))、スプライトの表示に反映させる
        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        //スプライトなどの描画はbeginとendの間で行う
        mGame.batch.begin();

        //背景
        //原点は左下
        mBg.setPosition(mCamera.position.x-CAMERA_WIDTH/2, mCamera.position.y-CAMERA_HEIGHT/2);
        mBg.draw(mGame.batch);

        //Step
        for(int i = 0; i < mSteps.size(); i++){
            mSteps.get(i).draw(mGame.batch);
        }

        //Star
        for(int i = 0; i < mStars.size(); i++){
            mStars.get(i).draw(mGame.batch);
        }

        //UFO
        mUfo.draw(mGame.batch);

        //Player
        mPlayer.draw(mGame.batch);

        mGame.batch.end();
    }

    @Override
    public void resize(int width, int height){
        mViewPort.update(width, height);
    }

    //ステージを作成する
    private void createStage(){

        //テクスチャ準備
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");

        //StepとStarをゴールの高さまでに配置していく
        float y = 0;

        float maxJumpHeight = Player.PLAYER_JUMP_VELOCITY*Player.PLAYER_JUMP_VELOCITY/(2*-GRAVITY);

        while(y < WORLD_HEIGHT -5){
            int type = mRandom.nextFloat() > 0.8f ? Step.STEP_TYPE_MOVING:Step.STEP_TYPE_STATIC;
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);

            Step step = new Step(type, stepTexture, 0, 0, 144, 36);
            step.setPosition(x, y);
            mSteps.add(step);

            if(mRandom.nextFloat() > 0.6f){
                Star star = new Star(starTexture, 0, 0, 72, 72);
                star.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Star.STAR_HEIGHT + mRandom.nextFloat() * 3);
                mStars.add(star);
            }

            y += (maxJumpHeight - 0.5f);
            y -= mRandom.nextFloat() * (maxJumpHeight/3);

        }

        //Playerを配置
        mPlayer = new Player(playerTexture, 0, 0, 72, 72);
        mPlayer.setPosition(WORLD_WIDTH/2 - mPlayer.getWidth()/2, Step.STEP_HEIGHT);

        //ゴールのUFOを配置
        mUfo = new Ufo(ufoTexture, 0, 0, 72, 72);
        mUfo.setPosition(WORLD_WIDTH/2 - Ufo.UFO_WIDTH/2, y);

    }

    //それぞれのオブジェクト(Player, star, step, ufo)の状態をアップデートする
    private void update(float delta){
        switch (mGameState){
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
        }
    }

}
