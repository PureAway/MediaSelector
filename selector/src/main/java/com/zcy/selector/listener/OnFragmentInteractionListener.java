package com.zcy.selector.listener;

/**
 * PreViewItemFragment 和  BasePreViewActivity 通信的接口 ，为了方便拿到 PhotoView 的点击事件
 */
public interface OnFragmentInteractionListener {
    /**
     * PhotoView 被点击了
     */
    void onClick();
}
