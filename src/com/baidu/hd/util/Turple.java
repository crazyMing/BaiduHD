package com.baidu.hd.util;

public class Turple<X, Y> {

	private X mX = null;
	private Y mY = null;
	
	public Turple(X x, Y y) {
		mX = x;
		mY = y;
	}

	public X getX() {
		return mX;
	}

	public void setX(X value) {
		this.mX = value;
	}

	public Y getY() {
		return mY;
	}

	public void setY(Y value) {
		this.mY = value;
	}
}
