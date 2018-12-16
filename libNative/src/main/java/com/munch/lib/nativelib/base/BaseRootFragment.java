package com.munch.lib.nativelib.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.munch.lib.nativelib.mpvpack.IView;
import com.munch.lib.nativelib.helper.ObjectHelper;

/**
 * Created by Munch on 2018/12/16.
 */
public abstract class BaseRootFragment<T> extends Fragment implements IView<T>, IViewExpand {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int resId = getLayoutResId(savedInstanceState);
        if (0 != resId) {
            return inflater.inflate(resId, container);
        }
        View view = getLayoutView(savedInstanceState);
        if (ObjectHelper.isNonEmpty(view)) {
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view, savedInstanceState);
        initListener();
        start();
    }


    public void initView(View view, Bundle bundle) {

    }

}
