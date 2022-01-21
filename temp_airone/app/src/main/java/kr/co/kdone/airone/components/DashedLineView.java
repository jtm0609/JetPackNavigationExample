package kr.co.kdone.airone.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;
import kr.co.kdone.airone.R;

public class DashedLineView extends View {
    private float density;
    private PathEffect effects;
    private Paint paint;
    private Path path;

    public DashedLineView(Context context) {
        super(context);
        init(context);
    }

    public DashedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DashedLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.paint = new Paint();
        this.paint.setStyle(Style.STROKE);
        this.paint.setStrokeWidth(10.0f);
        this.paint.setColor(context.getResources().getColor(R.color.colorAccent));
        this.path = new Path();
        this.effects = new DashPathEffect(new float[]{14.0f, 10.0f, 14.0f, 10.0f}, 3.0f);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setPathEffect(this.effects);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        if (measuredHeight <= measuredWidth) {
            this.path.moveTo(0.0f, 0.0f);
            this.path.lineTo((float) measuredWidth, 0.0f);
            canvas.drawPath(this.path, this.paint);
            return;
        }
        this.path.moveTo(0.0f, 0.0f);
        this.path.lineTo(0.0f, (float) measuredHeight);
        canvas.drawPath(this.path, this.paint);
    }
}