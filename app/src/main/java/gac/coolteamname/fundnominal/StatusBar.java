package gac.coolteamname.fundnominal;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by speccaN on 2016-05-10.
 */
public class StatusBar extends Activity {

    private Window mWindow;

    protected StatusBar(Window window){
        mWindow = window;
    }

    public void SetStatusbarColor(Context context, int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            switch (color){
                case 0:
                    mWindow.setStatusBarColor(ContextCompat.getColor(context, R.color.black));
                    break;
                case 1:
                    mWindow.setStatusBarColor(ContextCompat.getColor(context, R.color.LogoBackground));
                    break;
                default:
                    mWindow.setStatusBarColor(ContextCompat.getColor(context, R.color.black));
                    break;
            }
        }
    }
    public void HideStatusbar(){

        if (Build.VERSION.SDK_INT < 16){
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = mWindow.getDecorView();

            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
