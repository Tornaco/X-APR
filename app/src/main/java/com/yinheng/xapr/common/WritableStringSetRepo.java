package com.yinheng.xapr.common;

import android.os.Handler;
import android.util.AtomicFile;
import android.util.Log;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public class WritableStringSetRepo implements SetRepo<String> {

    // Flush data too many times, may drain battery.
    private static final int FLUSH_DELAY = 5000;
    private static final int FLUSH_DELAY_FAST = 100;

    private Handler mHandler;
    private ExecutorService mExe;

    private AtomicFile mFile;

    public WritableStringSetRepo(File file, Handler handler, ExecutorService service) {
        this.mFile = new AtomicFile(file);
        this.mExe = service;
        this.mHandler = handler;

        try {
            if (!this.mFile.getBaseFile().exists()) {
                Files.createParentDirs(file);
            }
        } catch (IOException e) {
            AppLog.wtf("Fail createParentDirs for: " + file + "\n" + Log.getStackTraceString(e));
        }

        AppLog.boot("StringSetRepo: " + name() + ", comes up");

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

                if (!mFile.getBaseFile().exists()) {
                    AppLog.wtf("getBaseFile not exists, skip load: " + name());
                    return;
                }

                if (mFile.getBaseFile().isDirectory()) {
                    AppLog.wtf("getBaseFile isDirectory, clean up: " + name());
                    FileUtil.deleteDir(mFile.getBaseFile());
                    mFile.delete();
                }

                // A
                // B
                // C
                Set h = new HashSet();

                InputStreamReader fr = new InputStreamReader(mFile.openRead());
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    AppLog.verbose("Read of line: " + line);
                    h.add(line.trim());
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);

                mStorage.addAll(h);

            } catch (IOException e) {
                AppLog.wtf("Fail reload@IOException: " + mFile + "\n" + Log.getStackTraceString(e));
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
        AppLog.verbose("flush");
        synchronized (sync) {
            try {

                // com.android.mms
                // com.android.dialer
                Set<String> out = new HashSet<>();
                out.addAll(mStorage);

                FileOutputStream fos = mFile.startWrite();
                PrintWriter printWriter = new PrintWriter(fos);

                for (String line : out) {
                    printWriter.println(line);
                }

                printWriter.flush();
                mFile.finishWrite(fos);
                Closer.closeQuietly(printWriter);


            } catch (IOException e) {
                AppLog.wtf("Fail flush@IOException: " + mFile + "\n" + Log.getStackTraceString(e));
            }
        }
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
        AppLog.verbose("flush async");
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
        return Files.getNameWithoutExtension(mFile.getBaseFile().getPath());
    }

    @Override
    public int size() {
        return mStorage.size();
    }

}
