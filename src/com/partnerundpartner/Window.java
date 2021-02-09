package com.partnerundpartner;

import processing.core.PApplet;

import java.util.ArrayList;

public class Window extends PApplet {
	private final int width;
	private final int height;
	private final int scale = 80;

	public Window(String title, int width, int height) {
		this.width = width;
		this.height = height;

		//Open window
		String[] processingArgs = {title};
		PApplet.runSketch(processingArgs, this);
	}

	//Game variables
	PlayField ownField = new PlayField(7);
	PlayField enemyField = new PlayField(7);
	ArrayList<String> infoText = new ArrayList<>();

	@Override
	public void settings() {
		//Called once at program start
		size(width, height);

		infoText.add("1. Player hit A7");
		infoText.add("2. CPU hit B6");
		infoText.add("3. Player missed");
	}

	@Override
	public void draw() {
		//Called 60 times per second
		background(0, 119, 190);
		textSize(50);
		fill(24, 24, 24);
		stroke(48, 48, 48);

		String text = "Eigenes Feld";
		text(text, 280 - textWidth(text) / 2, 80);
		drawPlayField(0, 100, ownField.getMap());

		text = "Gegnerisches Feld";
		text(text, 1240 - textWidth(text) / 2, 80);
		drawPlayField(960, 100, enemyField.getMap());

		text = "Info";
		text(text, 760 - textWidth(text) / 2, 80);
		drawInfoSection(560, 100, 5, 7);

		drawInfoText(30);
	}

	private void drawPlayField(int startX, int startY, Ship.State[][] map) {
		int size = map.length;

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				rect(startX + x * scale, startY + y * scale, scale, scale);
			}
		}
	}

	private void drawInfoSection(int x, int y, int xSize, int ySize) {
		rect(x, y, xSize * scale, ySize * scale);
	}

	private void drawInfoText(int size) {
		pushStyle();
		fill(255);
		textSize(size);

		for (int i = 0; i < infoText.size(); i++) {
			String text = infoText.get(infoText.size() - 1 - i);

			int y = 100 + size + i * size;

			//Remove text from list if offscreen
			if(y > height - size){
				infoText.remove(infoText.size() - 1 - i);
				i--;
			}

			text(text, 760 - textWidth(text) / 2, y);
		}

		popStyle();
	}

	@Override
	public void mousePressed() {
		rect(mouseX, mouseY, 50, 50);
	}
}