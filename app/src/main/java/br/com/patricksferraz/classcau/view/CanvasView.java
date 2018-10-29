package br.com.patricksferraz.classcau.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

public class CanvasView extends View {
    private Paint paint;
    private Rect rect;

    public CanvasView(Context context){
        super(context);
        //| Paint.LINEAR_TEXT_FLAG
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rect = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //paint.setColor(Color.argb(100,250,173,25)); // Color orange
        paint.setColor(Color.argb(155,0,0,0));
        paint.setStrokeWidth(3);

        double proporcion = getWidth()*CameraView.PROPORTION_TABLET_CAMERA_HEIGHT;
        double stripeBottomSize = (getWidth() - proporcion)/2;
        double stripeTopSize = proporcion + stripeBottomSize;
        canvas.drawRect(0,0, (float) stripeBottomSize, getHeight(),paint);
        canvas.drawRect((float) stripeTopSize,0, getWidth(),getHeight(),paint);

        String text = "Posicione a tábua de corte nos limites do retângulo claro";

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255,255,255));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, 1, bounds);

        canvas.rotate(90);
        canvas.translate((getHeight() + bounds.height()) >> 1, 0);

//        canvas.drawText(text, getWidth() >> 1,(getHeight() + bounds.height()) >> 1,  paint);
        canvas.drawText(text, 0, 0, paint);

    }
}
