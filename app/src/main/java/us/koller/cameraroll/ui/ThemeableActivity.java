package us.koller.cameraroll.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import us.koller.cameraroll.R;
import us.koller.cameraroll.data.Settings;
import us.koller.cameraroll.util.Constants;

public abstract class ThemeableActivity extends BaseActivity {

//    Theme theme = null;

    public int backgroundColor;
    public int toolbarColor;
    public int textColorPrimary;
    public int textColorSecondary;
    public int accentColor;
    public int accentTextColor;

    private ColorDrawable statusBarOverlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings settings = Settings.getInstance(getApplicationContext());
        Constants.THEMES theme = settings.getTheme();
        int THEMETYPE = 0;
        switch (theme){
            case DARK:
                THEMETYPE = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case LIGHT:
                THEMETYPE = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case SYSTEM:
                THEMETYPE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(THEMETYPE);
        //todo: do themeing BS

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        int windowColor = -1;
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT){
            windowColor = typedValue.data;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(windowColor);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ViewGroup rootView = findViewById(R.id.root_view);

        checkTags(rootView);

//        onThemeApplied(theme);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupTaskDescription();
        }
    }

    //systemUiFlags need to be reset to achieve transparent status- and NavigationBar
    void setSystemUiFlags() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void readTheme(Context context) {
//        Settings settings = Settings.getInstance(context);
//        theme = settings.getThemeInstance(this);

        backgroundColor = ContextCompat.getColor(context, R.color.color_bg);
        toolbarColor = ContextCompat.getColor(context, R.color.colorPrimary_toolbar);
        textColorPrimary = ContextCompat.getColor(context, R.color.textColorPrimary);
        textColorSecondary = ContextCompat.getColor(context, R.color.textColorSecondary);
        accentColor = ContextCompat.getColor(context, R.color.colorAccent);
        accentTextColor = ContextCompat.getColor(context, R.color.colorAccent_text);
    }

    //static Method to call, when adding a view dynamically in order to get Theme applied
    public static void checkTags(ViewGroup viewGroup) {
        setViewBgColors(viewGroup);

        setViewTextColors(viewGroup);
    }

    private static void setViewTextColors(ViewGroup vg) {
        if (vg == null) {
            return;
        }

        //find views
        String TAG_TEXT_PRIMARY = vg.getContext().getString(R.string.theme_text_color_primary);
        ArrayList<View> viewsPrimary = findViewsWithTag(TAG_TEXT_PRIMARY, vg);

        int textColorPrimary = ContextCompat.getColor(vg.getContext(), R.color.textColorPrimary);
        for (int i = 0; i < viewsPrimary.size(); i++) {
            View v = viewsPrimary.get(i);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(textColorPrimary);
            } else if (v instanceof ImageView) {
                ((ImageView) v).setColorFilter(textColorPrimary);
            }
        }

        String TAG_TEXT_SECONDARY = vg.getContext().getString(R.string.theme_text_color_secondary);
        ArrayList<View> viewsSecondary = findViewsWithTag(TAG_TEXT_SECONDARY, vg);

        int textColorSecondary = ContextCompat.getColor(vg.getContext(), R.color.textColorSecondary);
        for (int i = 0; i < viewsSecondary.size(); i++) {
            View v = viewsSecondary.get(i);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(textColorSecondary);
            } else if (v instanceof ImageView) {
                ((ImageView) v).setColorFilter(textColorSecondary);
            }
        }
    }

    private static void setViewBgColors(ViewGroup vg) {
        if (vg == null) {
            return;
        }

        //find views
        String TAG = vg.getContext().getString(R.string.theme_bg_color);
        ArrayList<View> views = findViewsWithTag(TAG, vg);

        int backgroundColor = ContextCompat.getColor(vg.getContext(), R.color.colorPrimary_toolbar);
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setBackgroundColor(backgroundColor);
        }
    }

    private static ArrayList<View> findViewsWithTag(String TAG, ViewGroup rootView) {
        return findViewsWithTag(TAG, rootView, new ArrayList<View>());
    }

    private static ArrayList<View> findViewsWithTag(String TAG, ViewGroup rootView,
                                                    ArrayList<View> views) {
        Object tag = rootView.getTag();
        if (tag != null && tag.equals(TAG)) {
            views.add(rootView);
        }

        for (int i = 0; i < rootView.getChildCount(); i++) {
            View v = rootView.getChildAt(i);
            tag = v.getTag();
            if (tag != null && tag.equals(TAG)) {
                views.add(v);
            }

            if (v instanceof ViewGroup) {
                findViewsWithTag(TAG, (ViewGroup) v, views);
            }
        }

        return views;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupTaskDescription() {
        int color = getTaskDescriptionColor();

        Bitmap overviewIcon = getIcon();

        setTaskDescription(new ActivityManager.TaskDescription(
                getString(R.string.task_description_label),
                overviewIcon, color));
        overviewIcon.recycle();
    }

    public Bitmap getIcon() {
        //getting the app icon as a bitmap
        Drawable icon = getApplicationInfo().loadIcon(getPackageManager());
        Bitmap overviewIcon = Bitmap.createBitmap(icon.getIntrinsicWidth(),
                icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overviewIcon);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);
        return overviewIcon;
    }

    public int getTaskDescriptionColor() {
        return ContextCompat.getColor(this, R.color.colorPrimary);
    }

    public int getStatusBarColor() {
        float darken = 0.96f;
        return Color.argb(
                (int) (Color.alpha(toolbarColor) * darken),
                (int) (Color.red(toolbarColor) * darken),
                (int) (Color.green(toolbarColor) * darken),
                (int) (Color.blue(toolbarColor) * darken));
    }

    public void addStatusBarOverlay(final Toolbar toolbar) {
        int statusBarColor = getStatusBarColor();
        statusBarOverlay = new ColorDrawable(statusBarColor);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                statusBarOverlay.setBounds(new Rect(0, 0,
                        toolbar.getWidth(), toolbar.getPaddingTop()));
                toolbar.getOverlay().add(statusBarOverlay);
            }
        });
    }

    public ColorDrawable getStatusBarOverlay() {
        return statusBarOverlay;
    }
}