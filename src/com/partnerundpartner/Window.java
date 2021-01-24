package com.partnerundpartner;

import processing.core.PApplet;

public class Window extends PApplet {
	private int width;
	private int height;

	public Window(String title, int width, int height) {
		this.width = width;
		this.height = height;

		//Open window
		String[] processingArgs = {title};
		PApplet.runSketch(processingArgs, this);
	}

	@Override
	public void settings() {
		//Called once at program start
		size(width, height);
	}

	@Override
	public void draw() {
		//Called 60 times per second
		//background(64);
		ellipse(mouseX, mouseY, 50, 50);
	}

	@Override
	public void mousePressed(){
		rect(mouseX, mouseY, 50, 50);
	}
}