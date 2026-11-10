package top.niunaijun.blackbox.fake.delegate;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

/**
 * updated by alex5402 on 4/2/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * TFNQw5HgWUS33Ke1eNmSFTwoQySGU7XNsK (USDT TRC20)
 */
public final class ServiceConnectionDelegate extends Binder {
    private static final String DESCRIPTOR = "android.app.IServiceConnection";
    private static final Map<IBinder, IServiceConnection> connections = new HashMap<>();
    private final IBinder original;
    private final ComponentName target;

    private ServiceConnectionDelegate(IBinder original, ComponentName target) {
        this.original = original;
        this.target = target;
        // Let the device framework supply its own IServiceConnection.Proxy.
        attachInterface(null, DESCRIPTOR);
    }

    public static synchronized IServiceConnection getDelegate(IBinder binder) {
        return connections.get(binder);
    }

    public static synchronized void forget(IBinder binder, IServiceConnection expected) {
        if (connections.get(binder) == expected) connections.remove(binder);
    }

    public static synchronized IServiceConnection createProxy(IServiceConnection base, Intent intent) {
        IBinder binder = base.asBinder();
        IServiceConnection existing = connections.get(binder);
        if (existing != null) return existing;
        IServiceConnection proxy = IServiceConnection.Stub.asInterface(
                new ServiceConnectionDelegate(binder, intent.getComponent()));
        try {
            binder.linkToDeath(() -> forget(binder, proxy), 0);
            connections.put(binder, proxy);
        } catch (RemoteException ignored) {
            // The framework will observe the original binder's death on dispatch.
        }
        return proxy;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code < FIRST_CALL_TRANSACTION || code > LAST_CALL_TRANSACTION) {
            return super.onTransact(code, data, reply, flags);
        }
        if (code != FIRST_CALL_TRANSACTION || target == null) {
            return original.transact(code, data, reply, flags);
        }
        // connected starts with a typed ComponentName. Preserve the device's
        // interface header and every subsequent field, including binder objects.
        data.enforceInterface(DESCRIPTOR);
        int componentStart = data.dataPosition();
        if (data.readInt() != 0) ComponentName.CREATOR.createFromParcel(data);
        int tail = data.dataPosition();
        Parcel forwarded = Parcel.obtain();
        try {
            forwarded.appendFrom(data, 0, componentStart);
            forwarded.writeInt(1);
            target.writeToParcel(forwarded, 0);
            forwarded.appendFrom(data, tail, data.dataSize() - tail);
            forwarded.setDataPosition(0);
            return original.transact(code, forwarded, reply, flags);
        } finally {
            forwarded.recycle();
        }
    }
}
