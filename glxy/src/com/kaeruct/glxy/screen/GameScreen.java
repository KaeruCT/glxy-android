package com.kaeruct.glxy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kaeruct.glxy.GlxyGame;
import com.kaeruct.glxy.actor.SettingsDialog;
import com.kaeruct.glxy.actor.Universe;
import com.kaeruct.glxy.model.Settings.Setting;

public class GameScreen extends Screen {
	private final Universe universe;
	private final SettingsDialog settingsDialog;
	
	public GameScreen (GlxyGame gg) {
		super(gg);
		final int padX = 10;
		final int padY = 5;
		
		universe = new Universe();
		
		// set up widgets
		final ButtonGroup rbg = new ButtonGroup();
		final TextButton b1 = new TextButton("Small", skin, "toggle");
		b1.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				universe.setParticleRadius(universe.minRadius);
			}
		});
		final TextButton b2 = new TextButton("Medium", skin, "toggle");
		b2.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				universe.setParticleRadius(universe.minRadius + universe.maxRadius/4);
			}
		});
		final TextButton b3 = new TextButton("Large", skin, "toggle");
		b3.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				universe.setParticleRadius(universe.minRadius + universe.maxRadius/2);
			}
		});

		rbg.add(b1);
		rbg.add(b2);
		rbg.add(b3);
		
		final Label l2 = new Label("Count: 000", skin);
		universe.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				String c = String.format("%3s", universe.getParticleCount()+"").replace(' ', '0');
				l2.setText("Count: "+c);
			}
		});
		
		settingsDialog = new SettingsDialog(universe, skin, padX, padY);
		settingsDialog.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!universe.settings.get(Setting.TRAILS)) {
					universe.clearTrails();
				}
			}
		});
		final Texture settingsTexture = new Texture(Gdx.files.internal("data/gear.png"));
		final TextureRegion settingsImage = new TextureRegion(settingsTexture); 
		final ImageButton t4 = new ImageButton(new TextureRegionDrawable(settingsImage));
		t4.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				settingsDialog.show(stage);
			}
		});
		
		final TextButton panButton = new TextButton("Pan", skin, "toggle");
		panButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				universe.panning = panButton.isChecked();
			}
		});
		panButton.pad(padY, padX, padY, padX);
		panButton.setChecked(universe.panning);
		
		final TextButton stickyButton = new TextButton("Sticky", skin, "toggle");
		stickyButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				universe.addSticky = stickyButton.isChecked();
			}
		});
		universe.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				stickyButton.setChecked(universe.addSticky);
			}
		});
		stickyButton.pad(padY, padX, padY, padX);

		b1.pad(padY, padX, padY, padX);
		b2.pad(padY, padX, padY, padX);
		b3.pad(padY, padX, padY, padX);
		
		// set up table layout
		table.add(universe).expand().fill().colspan(7).row();
		table.add(l2).pad(4).fillX().expandX();
		
		table.add(b1).left().pad(4);
		table.add(b2).center().pad(4);
		table.add(b3).right().pad(4);		
		table.add(stickyButton).right().expandX().pad(4);
		
		table.add(panButton).right().pad(4);
		table.add(t4).right().pad(4);
		
		// set up input
		InputMultiplexer im = new InputMultiplexer(stage, universe.gestureDetector);
		Gdx.input.setInputProcessor(im);
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		if (Gdx.input.isKeyPressed(Keys.MENU)) {
			settingsDialog.show(stage);
		}
		if (Gdx.input.isKeyPressed(Keys.BACK)) {
			if (settingsDialog.isShowing()) {
				settingsDialog.hide();
			} else {
				game.setScreen("MainMenuScreen");
			}
		}
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		universe.resize();
	}
	@Override
	public void dispose() {
		universe.dispose();
	}
}
