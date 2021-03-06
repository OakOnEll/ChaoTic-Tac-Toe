/*
 * Copyright (C) 2011 Gustavo Claramunt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oakonell.chaotictactoe.utils;

import java.util.ArrayList;
import java.util.Random;

import com.oakonell.chaotictactoe.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Region;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class ExplodeView extends View {
	private static final int SLICES_WIDTH = 10;
	private static final int SLICES_HEIGHT = 11;

	private static final int STATUS_NORMAL = 0;
	private static final int STATUS_EXPLODING = 1;
	private static final int STATUS_EXPLODED = 2;

	private ArrayList<BitmapFragment> fragments;
	private Bitmap mBackedBitmap;
	private Canvas mBackedCanvas;
	private Interpolator anim;
	private int mAnimDuration = 2000;
	private long mStartTime;
	private int mStatus = STATUS_NORMAL;
	private Camera mCamera;

	private static class BitmapFragment {
		Bitmap bmp;
		int sourceX;
		int sourceY;
		int destX;
		int destY;
		int rotX;
		int rotY;
		int rotZ;
		int totalW;
		int totalH;
		Region.Op op;
		Path triangle;
		private static Random rnd;

		private BitmapFragment(Bitmap bmp, int sourceX, int sourceY,
				int totalW, int totalH, Region.Op op, Path tri) {
			this.bmp = bmp;
			this.sourceX = sourceX;
			this.sourceY = sourceY;
			this.op = op;
			this.triangle = tri;
			this.totalH = totalH;
			this.totalW = totalW;
			if (rnd == null)
				rnd = new Random(SystemClock.uptimeMillis());
		}

		public void prepare(int xOrigin, int yOrigin) {
			final int x = sourceX;
			final int y = sourceY;
			final int h = totalH;
			final int w = totalW;
			final int sliceW = bmp.getWidth();
			final int sliceH = bmp.getHeight();
			int distH = (x + (SLICES_WIDTH / 2)) - (xOrigin);
			int distV = (y + (SLICES_HEIGHT / 2)) - (yOrigin);

			Path tri = new Path();
			tri.moveTo(0, 0);
			for (float jj = 0; jj <= 1; jj += .2) {
				tri.lineTo(rnd.nextInt(sliceW), sliceH * jj);
			}
			tri.lineTo(0, sliceH);
			tri.close();

			// ADD 2 times each part for 2 pieces
			destX = distH > 0 ? (x + (rnd.nextInt(w - xOrigin))) : (x - (rnd
					.nextInt(xOrigin)));
			destY = distV > 0 ? (y + (rnd.nextInt(h - yOrigin))) : (y - (rnd
					.nextInt(yOrigin)));
			rotX = rnd.nextInt(360);
			rotY = rnd.nextInt(360);
			rotZ = rnd.nextInt(360);
		}
	}

	public ExplodeView(Context context) {
		this(context, null);
	}

	public ExplodeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ExplodeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundColor(0xFFFF0000);
		init(context);
	}

	public void setImage(int res) {
		recycle();
		Random rnd = new Random(SystemClock.uptimeMillis());

		Bitmap original = BitmapFactory.decodeResource(getContext()
				.getResources(), res);
		final int w = original.getWidth();
		final int h = original.getHeight();
		final int sliceW = w / SLICES_WIDTH;
		final int sliceH = h / SLICES_HEIGHT;
		mBackedBitmap = Bitmap.createBitmap(sliceW, sliceH,
				Bitmap.Config.ARGB_8888);
		mBackedCanvas = new Canvas(mBackedBitmap);
		fragments = new ArrayList<BitmapFragment>(SLICES_WIDTH * SLICES_HEIGHT);
		for (int i = 0; i < SLICES_WIDTH; i++) {
			int x = i * sliceW;
			for (int j = 0; j < SLICES_HEIGHT; j++) {
				int y = j * sliceH;
				Bitmap part = Bitmap.createBitmap(original, x, y, sliceW,
						sliceH);
				Path tri = new Path();
				tri.moveTo(0, 0);
				for (float jj = 0; jj <= 1; jj += .2) {
					tri.lineTo(rnd.nextInt(sliceW), sliceH * jj);
				}
				tri.lineTo(0, sliceH);
				tri.close();

				fragments.add(new BitmapFragment(part, x, y, w, h,
						Region.Op.DIFFERENCE, tri));
				fragments.add(new BitmapFragment(part, x, y, w, h,
						Region.Op.REPLACE, tri));
			}
		}
		original.recycle();
		original = null;
	}

	private void init(Context context) {
		setImage(R.drawable.bomb_icon_1068);

		anim = new AccelerateDecelerateInterpolator();
		mCamera = new Camera();
	}

	public void explode(float x, float y) {
		for (int i = 0; i < fragments.size(); i++) {
			fragments.get(i).prepare(Math.round(x), Math.round(y));
		}
		mStatus = STATUS_EXPLODING;
		mStartTime = -1;
		postInvalidate();
	}

	public void reset() {
		mStatus = STATUS_NORMAL;
		postInvalidate();
	}
	
	public void recycle() {
		if (fragments == null) return;
		for (BitmapFragment each : fragments) {
			each.bmp.recycle();
		}
		fragments.clear();
	}

	@Override
	public void draw(Canvas canvas) {
		if (mStatus == STATUS_EXPLODED) {
			return;
		}
		if (mStatus == STATUS_NORMAL) {
			super.draw(canvas);
		}
		// exploding
		final Canvas backed = mBackedCanvas;
		for (int i = 0; i < fragments.size(); i++) {
			BitmapFragment part = fragments.get(i);
			int drawX = part.sourceX;
			int drawY = part.sourceY;
			int rotateX = 0;
			int rotateY = 0;
			int rotateZ = 0;
			if (mStatus == STATUS_EXPLODING) {
				if (mStartTime == -1)
					mStartTime = SystemClock.uptimeMillis();
				final long delayedTime = SystemClock.uptimeMillis()
						- mStartTime;
				int diffX = part.destX - part.sourceX;
				int diffY = part.destY - part.sourceY;
				float interpolation = anim.getInterpolation((float) delayedTime
						/ (float) mAnimDuration);
				drawX = part.sourceX + (Math.round(diffX * interpolation));
				drawY = part.sourceY + (Math.round(diffY * interpolation));
				rotateX = Math.round(part.rotX * interpolation);
				rotateY = Math.round(part.rotY * interpolation);
				rotateZ = Math.round(part.rotZ * interpolation);
				postInvalidate();
				if (delayedTime >= mAnimDuration)
					mStatus = STATUS_EXPLODED;
			}
			backed.drawColor(0, PorterDuff.Mode.CLEAR);
			backed.save();
			backed.clipPath(part.triangle, part.op);
			backed.drawBitmap(part.bmp, 0, 0, null);
			backed.restore();

			canvas.save();
			canvas.translate(drawX, drawY);
			mCamera.save();
			mCamera.rotateX(rotateX);
			mCamera.rotateY(rotateY);
			mCamera.rotateZ(rotateZ);
			mCamera.applyToCanvas(canvas);
			mCamera.restore();
			canvas.drawBitmap(mBackedBitmap, 0, 0, null);
			canvas.restore();
		}
		if (mStatus == STATUS_EXPLODED) {
			if (onPostExplode != null) {
				onPostExplode.postExplosion(this);
			}
		}
	}

	private OnPostExplosion onPostExplode;

	public void setOnPostExplode(OnPostExplosion onPostExplode) {
		this.onPostExplode = onPostExplode;
	}

	public interface OnPostExplosion {
		public void postExplosion(ExplodeView view);
	}
}
