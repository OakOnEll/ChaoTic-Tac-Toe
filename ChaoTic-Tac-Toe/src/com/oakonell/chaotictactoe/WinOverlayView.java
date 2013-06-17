package com.oakonell.chaotictactoe;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter.Blur;
import android.util.AttributeSet;
import android.view.View;

public class WinOverlayView extends View {
	private final int OFFSET = 10; 
	private Paint linePaint;
	
	public enum WinStyle {
		ROW1, ROW2, ROW3, //
		COL1, COL2, COL3, //
		TOP_LEFT_DIAG, //
		TOP_RIGHT_DIAG; //
		
		public static WinStyle column(int x) {
			if (x ==0) return COL1;
			if (x ==1) return COL2;
			if (x ==2) return COL3;
			throw new RuntimeException("Invalid column " + x);
		}
		public static WinStyle row(int y) {
			if (y ==0) return ROW1;
			if (y ==1) return ROW2;
			if (y ==2) return ROW3;
			throw new RuntimeException("Invalid row " + y);
		}

	}

	private WinStyle style;

	public WinOverlayView(Context context) {
		super(context);
		createPaint();
	}

	public WinOverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createPaint();
	}

	public WinOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		createPaint();
	}

	private void createPaint() {
		linePaint = new Paint();
		linePaint.setColor(Color.BLUE);
		linePaint.setStrokeWidth(15);
		linePaint.setMaskFilter(new BlurMaskFilter(10, Blur.SOLID));
		linePaint.setStrokeCap(Paint.Cap.ROUND);
	}

	public void setWinStyle(WinStyle style) {
		this.style = style;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (style == null)
			return;

		// Drawing commands go here
		int height = getHeight();
		int width = getWidth();

		int startX;
		int startY;
		int stopX;
		int stopY;

		switch (style) {
		case COL1: {
			startY = OFFSET;
			stopY = height-OFFSET;
			startX = stopX = width / 6 ;
			break;
		}
		case COL2: {
			startY = OFFSET;
			stopY = height - OFFSET;
			startX = stopX = width / 2;
			break;
		}
		case COL3: {
			startY = OFFSET;
			stopY = height-OFFSET;
			startX = stopX = 5 * width / 6;
			break;
		}

		case ROW1: {
			startX = OFFSET;
			stopX = width-OFFSET;
			startY = stopY = height / 6;
			break;
		}
		case ROW2: {
			startX = OFFSET;
			stopX = width-OFFSET;
			startY = stopY = height / 2;
			break;
		}
		case ROW3: {
			startX = OFFSET;
			stopX = width-OFFSET;
			startY = stopY = 5 * height / 6;
			break;
		}

		case TOP_LEFT_DIAG: {
			startX = OFFSET;
			startY = OFFSET;
			stopX = width-OFFSET;
			stopY = height-OFFSET;
			break;
		}

		case TOP_RIGHT_DIAG: {
			startX = width-OFFSET;
			startY = OFFSET;
			stopX = OFFSET;
			stopY = height-OFFSET;
			break;
		}

		default:
			throw new RuntimeException("Invalid winStyle");
		}

		canvas.drawLine(startX, startY, stopX, stopY, linePaint);		
	}
}
