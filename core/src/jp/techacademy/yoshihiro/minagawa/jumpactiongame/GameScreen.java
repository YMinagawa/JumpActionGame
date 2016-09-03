package jp.techacademy.yoshihiro.minagawa.jumpactiongame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
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
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_READY = 0;  //ゲーム開始前
    static final int GAME_STATE_PLAYING = 1;  //ゲーム中
    static final int GAME_STATE_GAMEOVER = 2;  //ゴールか落下してゲーム終了

    //重力
    static final float GRAVITY = -12;

    private JumpActionGame mGame;

    Sprite mBg;      //スプライトはコンピューターの処理の負荷を上げずに高速に描画する仕組み
    OrthographicCamera mCamera; //
    OrthographicCamera mGuiCamera;
    FitViewport mViewPort; //ゲームの世界の物理的なディスプレイを届けるためのオブジェクト
    FitViewport mGuiViewPort;

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    Ufo mUfo;
    Player mPlayer;

    float mHeightSoFar; //プレイヤーが地面からどれだけ離れたかを保持する
    int mGameState;
    Vector3 mTouchPoint; //タッチされた座標を保持するメンバ変数mTouchPointを定義
    BitmapFont mFont;
    int mScore;
    int mHighScore;
    Preferences mPrefs;

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

        //GUI用のカメラを設定する
        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        //メンバ変数の初期化
        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3(); //タッチされた座標を保持するメンバ変数mTouchPointを定義
        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false);
        mFont.getData().setScale(0.8f);
        mScore = 0;

        //ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.yoshihiro.minagawa.jumpactiongame;");
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0);

        createStage();
    }

    @Override
    //毎フレームの描画を行う(1/60 s毎に自動に呼び出される)
    //オブジェクトの状態(座標など)をアップデートするupdateメソッドの呼び出しとオブジェクトの描画を行う
    //踏み台と星はListで保持しているので順番に取り出す
    public void render(float delta) {

        //それぞれの状態をupdte
        update(delta);

        //下2つは画面の描画する準備
        //glClearColorメソッドは画面をクリアにする時の色を赤、緑、青、透過で指定する。
        //glClear(GL20.GL_COLOR_BUFFER_BIT)で実際にその色をクリア(塗りつぶし)をする。
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //カメラの中心を超えたらカメラを上に移動させる
        //つまりキャラが画面の上半分には絶対に行かない
        if(mPlayer.getY() > mCamera.position.y){
            mCamera.position.y = mPlayer.getY();
        }


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

        //スコア表示
        mGuiCamera.update();
        mGame.batch.setProjectionMatrix(mGuiCamera.combined);
        mGame.batch.begin();
        mFont.draw(mGame.batch, "HighScore: " + mHighScore, 16, GUI_HEIGHT - 15);
        mFont.draw(mGame.batch, "Score: " + mScore, 16, GUI_HEIGHT-35);
        mGame.batch.end();

    }

    @Override
    public void resize(int width, int height){
        mViewPort.update(width, height);
        mGuiViewPort.update(width, height);
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

    private void updateReady(){
        if(Gdx.input.justTouched()){
            mGameState = GAME_STATE_PLAYING;
        }
    }

    //updatePlayingメソッドではタッチされたら、その座標が画面の左か右かを判断する。
    //タッチされた座標はGdx.input.getXorYで取得
    //mTouchPointにsetメソッドで設定。
    //Vector3クラスはx,yだけでなくZ軸を保持するメンバ変数zを持っているため、3つめの引数は0
    //画面の右側が叩かれたら右に加速、左であれば左に加速

    private void updatePlaying(float delta){

        float accel = 0;

        if(Gdx.input.isTouched()){

            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            Rectangle left = new Rectangle(0, 0, GUI_WIDTH/2, GUI_HEIGHT);
            Rectangle right = new Rectangle(GUI_WIDTH/2, 0, GUI_WIDTH/2, GUI_HEIGHT);

            if(left.contains(mTouchPoint.x, mTouchPoint.y)){
                accel = 5.0f;
            }

            if(right.contains(mTouchPoint.x, mTouchPoint.y)){
                accel = -5.0f;
            }

        }

        //Step
        for(int i = 0; i < mSteps.size(); i++){
            mSteps.get(i).update(delta);
        }

        //Player
        if(mPlayer.getY() <= Player.PLAYER_HEIGHT/2){
            mPlayer.hitStep();
        }

        mPlayer.update(delta, accel);
        mHeightSoFar = Math.max(mPlayer.getY(), mHeightSoFar);

        //当たり判定を行う
        checkCollision();

        //ゲームオーバーか判断する
        checkGameOver();

    }

    private void updateGameOver(){
        if (Gdx.input.justTouched()) {
            mGame.setScreen(new ResultScreen(mGame, mScore));
        }
    }


    //衝突したかどうかの判定をするメソッド
    //矩形同士が重なっているかはSpriteクラスのgetBoundingRectangleメソッドで
    //スプライトの矩形をあらわすRectangleを取得します。
    //GameObjectクラスはSpriteクラスを継承しているののでgetBoundingRectangleを呼び出せる
    //Rectangleクラスのoverlapsメソッドに当たり判定を行いたい相手のRectangleを指定する。
    //戻り値がtrueであれば重なっている。
    private void checkCollision(){

        //UFO(ゴールとのあたり判定)
        if(mPlayer.getBoundingRectangle().overlaps(mUfo.getBoundingRectangle())){
            mGameState = GAME_STATE_GAMEOVER;
            return;
        }

        //Starとの当たり判定
        for(int i = 0; i < mStars.size(); i++){
            Star star = mStars.get(i);

            if(star.mState == Star.STAR_NONE){
                continue;
            }

            if(mPlayer.getBoundingRectangle().overlaps(star.getBoundingRectangle())){
                star.get();
                mScore++;
                if(mScore > mHighScore){
                    mHighScore = mScore;
                    mPrefs.putInteger("HIGHSCORE", mHighScore);
                    mPrefs.flush();
                }
                break;
            }
        }

        //Stepとの当たり判定
        //上昇中はStepとの当たり判定を確認しない
        if(mPlayer.velocity.y > 0){
            return;
        }

        for(int i = 0; i < mSteps.size(); i++){
            Step step = mSteps.get(i);

            if(step.mState == Step.STEP_STATE_VANISH){
                continue;
            }

            if(mPlayer.getY() > step.getY()){
                if(mPlayer.getBoundingRectangle().overlaps(step.getBoundingRectangle())){
                    mPlayer.hitStep();
                    if(mRandom.nextFloat() > 0.5f){
                        step.vanish();
                    }
                    break;
                }
            }
        }
    }

    private void checkGameOver(){
        if(mHeightSoFar-CAMERA_HEIGHT/2 > mPlayer.getY()){
            Gdx.app.log("JampActionGame", "GAMEOVER");
            mGameState = GAME_STATE_GAMEOVER;
        }
    }
}
