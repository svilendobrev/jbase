package com.svilendobrev.ui;

import com.svilendobrev.appbase.ActivityBase;

import android.view.View;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.os.Bundle;

import java.util.Random;

public class Splash {
    public static abstract class Base extends ActivityBase {

        boolean progressPending;
        boolean splashFinished;
        public View splash;

        @Override
        public void setContentView(int layoutResID) {
            super.setContentView( layoutResID);
            splash = findViewById( getSplashId());
            if (splash != null)
                showSplash();
        }

        public abstract int getSplashId();
        public abstract void showSplash();

        public Runnable hideSplash = new Runnable() {
            public void run() {
                splashFinished = true;
                splash.setVisibility( View.GONE);
                if (progressPending)
                    showProgress();
                    //handler.sendMessageAtFrontOfQueue( handler.obtainMessage( MSG_PROGRESS, true));
            }
        };

        @Override public void showProgress() {
            if (!splashFinished) {
                progressPending = true;
                return;
            }
            super.showProgress();
        }

        @Override public void hideProgress() {
            if (!splashFinished) {
                progressPending = false;
                return;
            }
            super.hideProgress();
        }
    }

    public static abstract class Simple extends Base {
        public void showSplash() {
            handler.postDelayed( hideSplash, 3000);
        }
    }

    public static abstract class RandomImage extends Base {

        public abstract int[] getSplashDrawables();

        public void showSplash() {
            int[] drawables = getSplashDrawables();
            Random generator = new Random();
            int index = generator.nextInt( drawables.length);
            ((ImageView) splash).setImageResource( drawables[ index]);
            handler.postDelayed( hideSplash, 3000);
        }
    }

    public static abstract class SingleImage extends RandomImage {

        public abstract int getSplashDrawable();

        public int[] getSplashDrawables() {
            return new int[] { getSplashDrawable() };
        }
    }

    public static abstract class Animated extends Base {

        public abstract Animation loadAnimation();
        public abstract void startAnimation();

        public void showSplash() {
            final Animation a = loadAnimation();
            a.setAnimationListener( new AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    handler.post( hideSplash);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });
            runUIupdater( new Runnable() { public void run() {
                startAnimation();
            }});
        }
    }

}


// vim:ts=4:sw=4:expandtab
