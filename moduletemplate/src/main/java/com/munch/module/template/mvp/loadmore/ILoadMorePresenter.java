package com.munch.module.template.mvp.loadmore;

import com.munch.module.template.mvp.IPresenter;
import com.munch.module.template.mvp.IView;

/**
 * Created by Munch on 2019/7/29 17:13
 */
public interface ILoadMorePresenter<V extends IView> extends IPresenter<V> {

    void loadMore();

    void refresh();

}
