package fi.oulu.tol.vote50.voting;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class VotingOptionsView extends View {

	private Voting mVoting;
	public VotingOptionsView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setVoting(Voting mVoting) {
		this.mVoting = mVoting;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		canvas.drawPaint(paint);
		paint.setColor(Color.BLACK);
		paint.setTextSize(20);
		if (mVoting != null) {
			String text = this.mVoting.getmTitle();
			if (this.mVoting.isOpen()) {
				int i = 0;
				for (i = 0; i < this.mVoting.getOptions().size(); i++) {
					text = mVoting.getSpecifiedOption(i).getmText();
					if (i == this.mVoting.getmSelectedOption()) {
						paint.setStrokeWidth(2.0f);
						paint.setStyle(Paint.Style.STROKE);
						paint.setShadowLayer(5.0f, 10.0f, 10.0f, Color.GRAY);
					} else {
						paint.setStrokeWidth(1.0f);
						paint.setShadowLayer(0, 0, 0, Color.GRAY);
					}
					canvas.drawText(text, 40, 40 + 40 * i, paint);
				}
			}
			if (this.mVoting.isUpcoming()) {
				int i = 0;
				for (i = 0; i < this.mVoting.getOptions().size(); i++) {
					text = mVoting.getSpecifiedOption(i).getmText();
					paint.setStrokeWidth(1.0f);
					paint.setShadowLayer(0, 0, 0, Color.GRAY);
					canvas.drawText(text, 40, 40 + 40 * i, paint);
				}
			}
			if (this.mVoting.isClosed()) {
				int i = 0;
				for (i = 0; i < this.mVoting.getOptions().size(); i++) {
					text = mVoting.getSpecifiedOption(i).getmText();

					paint.setStrokeWidth(1.0f);
					paint.setShadowLayer(0, 0, 0, Color.GRAY);
					canvas.drawText(text, 40, 40 + 40 * i, paint);
				}
			}
		}
	}
}
