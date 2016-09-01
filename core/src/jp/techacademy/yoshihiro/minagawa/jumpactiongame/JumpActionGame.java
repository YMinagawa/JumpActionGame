package jp.techacademy.yoshihiro.minagawa.jumpactiongame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//GameクラスはScreenAdapterクラスと呼ばれる1画面に相当するクラスを設定している
//簡単に画面遷移を行える機能が付いている
public class JumpActionGame extends Game {

	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		//SpriteBatch()クラスは画像をGPUで効率的に描画するクラス
		batch = new SpriteBatch();

		//img = new Texture("badlogic.jpg");

		//GameScreenを表示する
		setScreen(new GameScreen(this));
	}

/*
	@Override
	//毎フレームの描画を行う
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}*/
}
