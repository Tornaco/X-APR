package com.yinheng.xapr.common;

import android.os.Handler;
import android.util.Log;

import com.yinheng.xapr.hook.XposedLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import de.robv.android.xposed.SELinuxHelper;


/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public class ReadableStringSetRepo implements SetRepo<String> {

    // Flush data too many times, may drain battery.
    private static final int FLUSH_DELAY = 5000;
    private static final int FLUSH_DELAY_FAST = 100;

    private Handler mHandler;
    private ExecutorService mExe;

    private File mFile;

    public ReadableStringSetRepo(File file, Handler handler, ExecutorService service) {
        this.mFile = file;
        this.mExe = service;
        this.mHandler = handler;

        XposedLog.boot("StringSetRepo: " + name() + ", comes up");

        reload();
    }

    private final Set<String> mStorage = new HashSet<>();

    private final Object sync = new Object();

    @Override
    public Set<String> getAll() {
        return new HashSet<>(mStorage);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reload() {
        synchronized (sync) {
            try {

                boolean exist = SELinuxHelper.getAppDataFileService()
                        .checkFileExists(mFile.getAbsolutePath());

                if (!exist) {
                    XposedLog.wtf("File not exists, skip load: " + name());
                   // return;
                }

                // A
                // B
                // C
                Set h = new HashSet();

                InputStream is = SELinuxHelper.getAppDataFileService().getFileInputStream(mFile.getAbsolutePath());
                InputStreamReader fr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(fr);
                String line;

                while ((line = br.readLine()) != null) {
                    XposedLog.verbose("Read of line: " + line);
                    h.add(line.trim());
                }

                Closer.closeQuietly(fr);
                Closer.closeQuietly(is);
                Closer.closeQuietly(br);

                mStorage.addAll(h);

            } catch (IOException e) {
                XposedLog.wtf("Fail reload@IOException: " + mFile + "\n" + Log.getStackTraceString(e));
            }
        }
    }


    @Override
    public void reloadAsync() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                reload();
            }
        };
        if (mExe == null) {
            new Thread(r).start();
        } else {
            mExe.execute(r);
        }
    }

    @Override
    public void flush() {
        XposedLog.verbose("flush ignored");
    }

    private Runnable mFlusher = new Runnable() {
        @Override
        public void run() {
            flush();
        }
    };

    private Runnable mFlushCaller = new Runnable() {
        @Override
        public void run() {
            flushAsync();
        }
    };

    @Override
    public void flushAsync() {
        XposedLog.verbose("flush async");
        if (mExe == null) {
            new Thread(mFlusher).start();
        } else {
            mExe.execute(mFlusher);
        }
    }

    @Override
    public boolean add(String s) {
        if (s == null) return false;
        boolean added = mStorage.add(s);
        if (added && mHandler != null) {
            mHandler.removeCallbacks(mFlushCaller);
            mHandler.postDelayed(mFlushCaller, FLUSH_DELAY);
        }
        return added;
    }

    @Override
    public boolean remove(String s) {
        if (s == null) return false;
        boolean removed = mStorage.remove(s);
        if (removed && mHandler != null) {
            mHandler.removeCallbacks(mFlushCaller);
            mHandler.postDelayed(mFlushCaller, FLUSH_DELAY);
        }
        return removed;
    }

    @Override
    public void removeAll() {
        mStorage.clear();
        if (mHandler != null) {
            mHandler.removeCallbacks(mFlushCaller);
            mHandler.postDelayed(mFlushCaller, FLUSH_DELAY_FAST);
        }
    }

    @Override
    public boolean has(String s) {
        return s != null && mStorage.contains(s);
    }

    @Override
    public String name() {
        return (mFile.getPath());
    }

    @Override
    public int size() {
        return mStorage.size();
    }

}
