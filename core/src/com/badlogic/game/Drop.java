package com.badlogic.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import org.w3c.dom.css.Rect;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;

	// precisamos saber quando a ultima gota spawnou (nanosegundos)
	private long lastDropTime;

	@Override
	public void create () {
		// carrega as imagens da gota e do balde, cada uma com 64x64
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// carrega o efeito sonoro da gota e o som continuo da chuva
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// começa a tocar a musica de fundo continuamente
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		// sistemas de coordenadas especificado pela camera
		camera.setToOrtho(false,800,480);

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0.2f, 1);
		// atualizando a camera (boa pratica)
		camera.update();
		//diz ao spritebatch para usar a coordanada especificada pela camera
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// verifica se o usuario pressiona um botao do mouse e centraliza o balde ao redor da posicao horizontalmente
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos); // transforma as coordenadas do toque em nossa cordenada da camera
			bucket.x = touchPos.x - 64/2;
		}

		// checa se estamos pressionando as teclas laterais e move o balde
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		// checa se o balde nao passou dos limites das paredes (horizontalmente)
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		for(Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext();) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) iter.remove();

			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
