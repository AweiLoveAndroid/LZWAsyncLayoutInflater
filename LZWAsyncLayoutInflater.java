/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lzw.mvvm_demo.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.util.Pools;
import androidx.core.view.LayoutInflaterCompat;
import androidx.core.view.LayoutInflaterFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Helper class for inflating layouts asynchronously. To use, construct
 * an instance of {@link LZWAsyncLayoutInflater} on the UI thread and call
 * {@link #inflate(int, ViewGroup, OnInflateFinishedListener)}. The
 * {@link OnInflateFinishedListener} will be invoked on the UI thread
 * when the inflate request has completed.
 *
 * <p>This is intended for parts of the UI that are created lazily or in
 * response to user interactions. This allows the UI thread to continue
 * to be responsive & animate while the relatively heavy inflate
 * is being performed.
 *
 * <p>For a layout to be inflated asynchronously it needs to have a parent
 * whose {@link ViewGroup#generateLayoutParams(AttributeSet)} is thread-safe
 * and all the Views being constructed as part of inflation must not create
 * any {@link Handler}s or otherwise call {@link Looper#myLooper()}. If the
 * layout that is trying to be inflated cannot be constructed
 * asynchronously for whatever reason, {@link LZWAsyncLayoutInflater} will
 * automatically fall back to inflating on the UI thread.
 *
 * <p>NOTE that the inflated View hierarchy is NOT added to the parent. It is
 * equivalent to calling {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
 * with attachToRoot set to false. Callers will likely want to call
 * {@link ViewGroup#addView(View)} in the {@link OnInflateFinishedListener}
 * callback at a minimum.
 *
 * <p>This inflater does not support setting a {@link LayoutInflater.Factory}
 * nor {@link LayoutInflater.Factory2}. Similarly it does not support inflating
 * layouts that contain fragments.
 *
 * <p>?????????AsyncLayoutInflater????????????</p>
 * <p> 1???????????????View?????????????????????Handler????????????Looper.myLooper()???????????????????????????????????????Looper.prepare()???</p>
 * <p> 2????????????????????????View??????????????????parent??????AsyncLayoutInflater????????????LayoutInflater.inflate(int, ViewGroup, false)???
 * ???????????????????????????parent View??????????????????????????????????????????</p>
 * <p> 3???AsyncLayoutInflater???????????????LayoutInflater.Factory??????LayoutInflater.Factory2???</p>
 * <p> 4???????????????????????????10??????????????????????????????10????????????????????????????????????</p>
 * <p> 5?????????????????????????????????inflate??????????????????????????????layout?????????????????????????????????</p>
 * <p>6????????????fragment</p>
 *
 * <p>????????????</p>
 * <p>???????????????2???1.?????????????????????View?????????????????????parent???</p>
 * <p>???????????????3???2.????????????LayoutInflater.Factory??????LayoutInflater.Factory2</p>
 * <p>???????????????4?????????5???3.??????????????????????????????????????????</p>
 * <p>4..layoutinflater?????????????????????????????????view??????????????????</p>
 *
 */
public class LZWAsyncLayoutInflater {
    private static final String TAG = "LZWAsyncLayoutInflater";

    private Pools.SynchronizedPool<InflateRequest> mRequestPool = new Pools.SynchronizedPool<>(10);

    BasicInflater mInflater;
    Handler mHandler;
    Dispather mDispatcher;

    public LZWAsyncLayoutInflater(@NonNull Context context) {
        mInflater = new BasicInflater(context);
        mHandler = new Handler(mHandlerCallback);
        mDispatcher = new Dispather();
    }

    @UiThread
    public void inflate(@LayoutRes int resid, @Nullable ViewGroup parent,
                        @NonNull OnInflateFinishedListener callback) {
        if (callback == null) {
            throw new NullPointerException("callback argument may not be null!");
        }
        InflateRequest request = obtainRequest();
        request.setInflater(this);
        request.setResid(resid);
        request.setParent(parent);
        addViews(parent, mInflater.getCurrentChildView());
        request.setCallback(callback);
        // ?????????????????????????????????????????????
        mDispatcher.enqueue(request);
    }

    /** TODO: ???????????????????????????Bug?????????
     * ??????????????????View?????????parent?????????????????????????????????Bug??????
     * @param parent
     * @param child
     */
    private void addViews(ViewGroup parent, View child) {
        if (parent != null && child != null) {
            parent.addView(child);
        }
    }

    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            InflateRequest request = (InflateRequest) msg.obj;
            if (request.getView() == null) {
                View view = request.getInflater().mInflater.inflate( request.getResid(), request.getParent(), false);
                request.setView(view);
            }
            request.getCallback().onInflateFinished(request.getView(), request.getResid(), request.getParent());
            releaseRequest(request);
            return true;
        }
    };

    private static class Dispather {

        //????????????CPU????????????
        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        //?????????????????????????????????2-4??????,???????????????CPU??????
        private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
        //???????????????????????????????????? CPU?????? * 2 + 1
        private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
        //???????????????????????????????????????30s
        private static final int KEEP_ALIVE_SECONDS = 30;

        private static final ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "AsyncLayoutInflatePlus #" + mCount.getAndIncrement());
            }
        };

        //LinkedBlockingQueue ?????????????????????????????????Integer.MAX_VALUE
        private static final BlockingQueue<Runnable> sPoolWorkQueue =
                new LinkedBlockingQueue<Runnable>();

        /**
         * An {@link Executor} that can be used to execute tasks in parallel.
         */
        public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR;

        static {
            Log.i(TAG, "static initializer: " + " CPU_COUNT = " + CPU_COUNT + " CORE_POOL_SIZE = " + CORE_POOL_SIZE + " MAXIMUM_POOL_SIZE = " + MAXIMUM_POOL_SIZE);
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                    sPoolWorkQueue, sThreadFactory);
            threadPoolExecutor.allowCoreThreadTimeOut(true);
            THREAD_POOL_EXECUTOR = threadPoolExecutor;
        }

        public void enqueue(InflateRequest request) {
            THREAD_POOL_EXECUTOR.execute((new InflateRunnable(request)));

        }

    }

    private static class InflateRunnable implements Runnable {
        private InflateRequest request;
        private boolean isRunning;

        public InflateRunnable(InflateRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                View view = request.getInflater().mInflater.inflate( request.getResid(), request.getParent(), false);
                request.setView(view);
//                request.view = request.inflater.mInflater.inflate(request.resid, request.parent, false);

            } catch (RuntimeException ex) {
                // Probably a Looper failure, retry on the UI thread
                Log.w(TAG, "Failed to inflate resource in the background! Retrying on the UI"
                        + " thread", ex);
            }
            Message.obtain(request.getInflater().mHandler, 0, request).sendToTarget();
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    private static class InflateRequest {

        private LZWAsyncLayoutInflater inflater;
        private ViewGroup parent;
        private int resid;
        private View view;
        private OnInflateFinishedListener callback;

        InflateRequest() {
        }

        public LZWAsyncLayoutInflater getInflater() {
            return inflater;
        }

        public void setInflater(LZWAsyncLayoutInflater inflater) {
            this.inflater = inflater;
        }

        public ViewGroup getParent() {
            return parent;
        }

        public void setParent(ViewGroup parent) {
            this.parent = parent;
        }

        public int getResid() {
            return resid;
        }

        public void setResid(int resid) {
            this.resid = resid;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public OnInflateFinishedListener getCallback() {
            return callback;
        }

        public void setCallback(OnInflateFinishedListener callback) {
            this.callback = callback;
        }

        @Override
        public String toString() {
            return "InflateRequest{" +
                    "inflater=" + inflater +
                    ", parent=" + parent +
                    ", resid=" + resid +
                    ", view=" + view +
                    ", callback=" + callback +
                    '}';
        }
    }

    public interface OnInflateFinishedListener {
        void onInflateFinished(@NonNull View view, @LayoutRes int resid, @Nullable ViewGroup parent);
    }


    // TODO: ????????????????????????????????????????????????parenet
    private static class BasicInflater extends LayoutInflater {
        private static final String[] sClassPrefixList = {
                "android.widget.",
                "android.webkit.",
                "android.app."
        };

        private View currentChild;

        BasicInflater(Context context) {
            super(context);

            if (context instanceof AppCompatActivity) {
                AppCompatDelegate appCompatDelegate = ((AppCompatActivity) context).getDelegate();
                // ??????setFactory?????????AppCompatTextView?????????
                if (appCompatDelegate instanceof LayoutInflater.Factory) {
                    LayoutInflaterCompat.setFactory(this, new BaseLayoutInflaterFactory(appCompatDelegate));
                }else if (appCompatDelegate instanceof LayoutInflater.Factory2) { // ??????setFactory2?????????AppCompatTextView?????????
                    LayoutInflaterCompat.setFactory2(this, (LayoutInflater.Factory2) appCompatDelegate);
                }
            }
        }

        @Override
        public LayoutInflater cloneInContext(Context newContext) {
            return new BasicInflater(newContext);
        }

        @Override
        protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
            //??????view??????
            long startTime = System.currentTimeMillis();

            for (String prefix : sClassPrefixList) {
                try {
                    currentChild = createView(name, prefix, attrs);
                    if (currentChild != null) {
                        return currentChild;
                    }
                } catch (ClassNotFoundException e) {
                    // In this case we want to let the base class take a crack
                    // at it.
                }
            }
            //??????view??????????????????view??????????????????????????????
            long totalTimes = System.currentTimeMillis() - startTime;
            Log.d(TAG, "???????????????" + name + "?????????" + totalTimes);
            return super.onCreateView(name, attrs);
        }

        /**
         * ?????????????????????View
         * @return
         */
        public View getCurrentChildView(){
            return currentChild;
        }

    }

    static class BaseLayoutInflaterFactory implements LayoutInflaterFactory {

        private AppCompatDelegate mAppCompatDelegate;

        BaseLayoutInflaterFactory(AppCompatDelegate appCompatDelegate) {
            this.mAppCompatDelegate = appCompatDelegate;
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            // TODO:????????????????????????new?????????View
            //  ????????????????????????????????????????????????View
            AppCompatDelegate delegate = mAppCompatDelegate;
            View view = delegate.createView(parent, name, context, attrs);

            // ???????????????????????????????????????MyCustomView???????????????????????????????????????????????????
//            if ( view!= null && (view instanceof MyCustomView))  {
//                ((MyCustomView) view).setTextColor(Color.RED);
//            }
            return view;
        }
    }


    public InflateRequest obtainRequest() {
        InflateRequest obj = mRequestPool.acquire();
        if (obj == null) {
            obj = new InflateRequest();
        }
        return obj;
    }

    public void releaseRequest(InflateRequest obj) {
        obj.setCallback(null);
        obj.setInflater(null);
        obj.setParent(null);
        obj.setResid(0);
        obj.setView(null);
        mRequestPool.release(obj);
    }


    /**
     * ??????????????????????????????releaseRequest???????????????????????????
     */
    public void cancel() {
        mHandler.removeCallbacksAndMessages(null);
        mHandlerCallback = null;
    }

}
