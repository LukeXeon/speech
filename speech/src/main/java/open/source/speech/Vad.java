package open.source.speech;

import android.os.Process;

import androidx.annotation.RestrictTo;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ShortBuffer;

public final class Vad {

    private static final long[] ref = new long[1];

    private static final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    static {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        ((NativeAccessor) queue.remove()).stop();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private final NativeAccessor accessor = new NativeAccessor(this);

    public int start() {
        return accessor.start();
    }

    public void feed(ShortBuffer buffer) {
        accessor.feed(buffer);
    }

    public void stop() {
        accessor.stop();
    }

    private static class NativeAccessor extends PhantomReference<Object> {

        private long instance = 0;

        public NativeAccessor(Object referent) {
            super(referent, queue);
        }

        public synchronized int start() {
            if (instance != 0) {
                throw new IllegalStateException();
            }
            synchronized (ref) {
                ref[0] = 0;
                int r = nStart(ref);
                instance = ref[0];
                return r;
            }
        }

        public synchronized void feed(ShortBuffer buffer) {
            if (instance == 0) {
                throw new IllegalStateException();
            }
            nFeed(instance, buffer);
        }

        public synchronized void stop() {
            if (instance != 0) {
                nStop(instance);
                instance = 0;
            }
        }


    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static native int nStart(long[] ref);

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static native void nFeed(long instance, ShortBuffer buffer);

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static native void nStop(long instance);
}
