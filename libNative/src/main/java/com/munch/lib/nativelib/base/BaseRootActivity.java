package com.munch.lib.nativelib.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.munch.lib.nativelib.helper.ObjectHelper;
import com.munch.lib.nativelib.mpvpack.IBasePresenter;
import com.munch.lib.nativelib.mpvpack.IView;

/**
 * Created by  Munch on 2018/12/16.
 */
public abstract class BaseRootActivity<T, P extends IBasePresenter>
        extends AppCompatActivity implements IView<T>, IViewExpand {

    private P p;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBeforeSetContentView(savedInstanceState);
        int resId = getLayoutResId(savedInstanceState);
        if (0 != resId) {
            setContentView(resId);
        }
        View view = getLayoutView(savedInstanceState);
        if (ObjectHelper.isNonEmpty(view)) {
            setContentView(view);
        }
        initListener();
        start();
    }

    public void initBeforeSetContentView(Bundle bundle) {
    }

    public P bindPresenter(@NonNull P p) {
        this.p = p;
        return this.p;
    }

    @NonNull
    public P getPresenter() {
        return p;
    }

    @Override
    public View getLayoutView(@Nullable Bundle bundle) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setText(getClass().getCanonicalName());
        return textView;
    }
}
