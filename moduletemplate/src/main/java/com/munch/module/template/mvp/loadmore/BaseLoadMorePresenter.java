package com.munch.module.template.mvp.loadmore;

import com.munch.module.template.mvp.BasePresenter;
import com.munch.module.template.mvp.IView;
import com.munch.module.template.net.NetConfig;

/**
 * Created by Munch on 2019/7/29 17:28
 */
public class BaseLoadMorePresenter<V extends IView> extends BasePresenter<V> implements ILoadMorePresenter<V> {

    private int mPage = NetConfig.PAGE_FIRST;
    private final static int ROWS = NetConfig.PAGE_ROWS;

    @Override
    public void loadMore() {
        mPage++;
    }

    @Override
    public void refresh() {
        mPage = NetConfig.PAGE_FIRST;
    }
}
