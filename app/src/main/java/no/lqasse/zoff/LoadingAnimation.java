package no.lqasse.zoff;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lassedrevland on 03.07.15.
 */
public class LoadingAnimation extends View {
    private static final int FPS = 25;
    private static final int FRAME_DELAY_MILLIS = 1000 / FPS;
    private static final int ROTATIONS_PER_SECOND = 1;
    private static final int ROTATION_PER_FRAME = (360/ROTATIONS_PER_SECOND)/FPS;
    private float rotation = 0;
    private Bitmap icon;


    private Paint paint = new Paint();
    Matrix matrix;
    private boolean isScaled = false;

    boolean isVisible = true;
    int color = Color.BLACK;


    private Runnable animator = new Runnable() {
        @Override
        public void run() {
           invalidate();
            postDelayed(this, FRAME_DELAY_MILLIS);


        }
    };
    public LoadingAnimation(Context context, AttributeSet attrs) {


        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LoadingAnimation,
                0, 0);

        try {
            isVisible = a.getBoolean(R.styleable.LoadingAnimation_visible,true);
            color = a.getColor(R.styleable.LoadingAnimation_iconColor,Color.BLACK);
        } finally {
            a.recycle();
        }

        icon  = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
        if (isVisible){
            post(animator);
        }
        matrix = new Matrix();
        matrix.setRotate(ROTATION_PER_FRAME);
        paint.setColor(color);
        ColorFilter filter = new LightingColorFilter(Color.BLACK, 0);
        paint.setColorFilter(filter);




    }

    private void hide(){

        isVisible = false;
    }

    private void show(){
        isVisible = true;
        post(animator);

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == VISIBLE) {
            show();
        } else if (visibility == INVISIBLE){
            hide();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isVisible){
            if (!isScaled){
                icon = Bitmap.createScaledBitmap(icon,canvas.getWidth(),canvas.getHeight(),false);
            }


            matrix.setRotate(rotation,canvas.getHeight()/2,canvas.getWidth()/2);
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawBitmap(icon,matrix, paint);


            rotation+=ROTATION_PER_FRAME;
        } else {
            canvas.drawColor(Color.TRANSPARENT);

        }





    }




}
