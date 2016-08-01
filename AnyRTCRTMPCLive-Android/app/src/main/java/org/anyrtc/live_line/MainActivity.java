package org.anyrtc.live_line;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.anyrtc.adapter.LiveHosterAdapter;
import org.anyrtc.rtmpc_hybird.RTMPCHybird;
import org.anyrtc.utils.LiveItemBean;
import org.anyrtc.utils.RTMPCHttpSDK;
import org.anyrtc.utils.RecyclerViewUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;

/**
 * Demo 主页面
 */
public class MainActivity extends AppCompatActivity implements RecyclerViewUtil.RefreshDataListener, RecyclerViewUtil.ScrollingListener, BGAOnItemChildClickListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private RecyclerViewUtil mRecyclerViewUtils;
    private List<LiveItemBean> listLive;

    private LiveHosterAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_swipe_refresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        ((TextView) findViewById(R.id.txt_version)).setText(getString(R.string.str_version).replace("${VERSION}", getVersion()));

        setTitle(R.string.str_title);
        listLive = new ArrayList<LiveItemBean>();
        /**
         * 初始化RTMPC引擎
         */
        RTMPCHybird.Inst().Init(getApplicationContext());
        RTMPCHybird.Inst().InitEngineWithAnyrtcInfo("teameetingtest", "meetingtest", "OPJXF3xnMqW+7MMTA4tRsZd6L41gnvrPcI25h9JCA4M", "c4cd1ab6c34ada58e622e75e41b46d6d");
        /**
         * 私有云配置， 不是私有云时不需要
         */
        //RTMPCHybird.Inst().ConfigServerForPriCloud("192.168.7.207", 9060);

        {
            /**
             * 获取直播列表
             */
            RTMPCHttpSDK.GetLiveList(this, RTMPCHybird.Inst().GetHttpAddr(), "teameetingtest", "meetingtest",
                    "c4cd1ab6c34ada58e622e75e41b46d6d", mRTMPCHttpCallback);
        }

        mAdapter = new LiveHosterAdapter(this, mRecyclerView);
        mAdapter.setOnItemChildClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerViewUtils = new RecyclerViewUtil();
        mRecyclerViewUtils.init(this, mSwipeRefreshLayout, mRecyclerView, mAdapter, this);
        mRecyclerViewUtils.beginRefreshing();//第一次自动加载一次
        mRecyclerViewUtils.setScrollingListener(this);
        mRecyclerViewUtils.setPullUpRefreshEnable(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void OnBtnClicked(View btn) {
        if (btn.getId() == R.id.btn_start_live) {
            Intent it = new Intent(this, PreStartLiveActivity.class);
            startActivity(it);
        }
    }

    private RTMPCHttpSDK.RTMPCHttpCallback mRTMPCHttpCallback = new RTMPCHttpSDK.RTMPCHttpCallback() {
        @Override
        public void OnRTMPCHttpOK(String strContent) {
            mRecyclerViewUtils.endRefreshing();
            try {
                listLive.clear();
                JSONObject liveJson = new JSONObject(strContent);
                JSONArray liveList = liveJson.getJSONArray("LiveList");
                JSONArray memberList = liveJson.getJSONArray("LiveMembers");
                for (int i = 0; i < liveList.length(); i++) {
                    LiveItemBean bean = new LiveItemBean();
                    JSONObject itemJson = new JSONObject(liveList.getString(i));
                    bean.setmHosterId(itemJson.getString("hosterId"));
                    bean.setmRtmpPullUrl(itemJson.getString("rtmp_url"));
                    bean.setmHlsUrl(itemJson.getString("hls_url"));
                    bean.setmLiveTopic(itemJson.getString("topic"));
                    bean.setmAnyrtcId(itemJson.getString("anyrtcId"));
                    bean.setmMemNumber(memberList.getInt(i));
                    listLive.add(bean);
                }
                mAdapter.setDatas(listLive);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void OnRTMPCHttpFailed(int code) {

        }
    };

    @Override
    public void onRefresh() {
        /**
         * 下拉刷新直播列表
         */
        RTMPCHttpSDK.GetLiveList(this, RTMPCHybird.Inst().GetHttpAddr(), "teameetingtest", "meetingtest",
                "c4cd1ab6c34ada58e622e75e41b46d6d", mRTMPCHttpCallback);
    }

    @Override
    public boolean loadMore() {
        return false;
    }

    @Override
    public void scroll(boolean scrollState) {

    }

    @Override
    public void onItemChildClick(ViewGroup viewGroup, View view, int i) {
        Intent it = new Intent(this, GuestActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("rtmp_url", listLive.get(i).getmRtmpPullUrl());
        bundle.putString("anyrtcId", listLive.get(i).getmAnyrtcId());
        bundle.putString("guestId", "GuestID");
        bundle.putString("userData", new JSONObject().toString());
        bundle.putString("topic", listLive.get(i).getmLiveTopic());
        it.putExtras(bundle);
        startActivity(it);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}


