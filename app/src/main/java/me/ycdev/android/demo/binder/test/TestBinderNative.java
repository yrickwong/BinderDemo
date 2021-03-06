package me.ycdev.android.demo.binder.test;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import me.ycdev.android.demo.binder.utils.AppLogger;

public class TestBinderNative extends Binder implements ITestBinder {
    private static final String TAG = "TestBinderNative";

    public static ITestBinder asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }

        ITestBinder binder = (ITestBinder) obj.queryLocalInterface(DESCRIPTOR);
        if (binder != null) {
            AppLogger.i(TAG, "asInterface, local binder found");
            return binder;
        }

        return new TestBinderProxy(obj);
    }

    public TestBinderNative() {
        attachInterface(this, DESCRIPTOR);
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        AppLogger.i(TAG, "onTransact: " + code);
        switch (code) {
            case INCREASE_TRANSACTION: {
                data.enforceInterface(DESCRIPTOR);
                int value = data.readInt();
                int result = increase(value);
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            }

            case PRINT_TRANSACTION: {
                data.enforceInterface(DESCRIPTOR);
                String value = data.readString();
                print(value);
                reply.writeNoException();
                return true;
            }

            case TEST_CRASH_TRANSACTION: {
                data.enforceInterface(DESCRIPTOR);
                String result = testCrash();
                reply.writeNoException();
                reply.writeString(result);
                return true;
            }

            case TEST_CRASH2_TRANSACTION: {
                data.enforceInterface(DESCRIPTOR);
                try {
                    String result = testCrash2();
                    reply.writeNoException();
                    reply.writeString(result);
                } catch (Exception e) {
                    reply.writeException(new SecurityException(e));
                }
                return true;
            }
        }
        return super.onTransact(code, data, reply, flags);
    }

    @Override
    public int increase(int value) {
        AppLogger.i(TAG, "native increate invoked: " + value);
        return value + 1;
    }

    @Override
    public void print(String value) {
        AppLogger.i(TAG, "native print invoked: " + value);
    }

    @Override
    public String testCrash() {
        throw new RuntimeException("test crash in IBinder impl");
    }

    @Override
    public String testCrash2() throws RemoteException {
        throw new RuntimeException("test crash2 in IBinder impl");
    }
}


class TestBinderProxy implements ITestBinder {
    private static final String TAG = "TestBinderProxy";

    private IBinder mRemote;

    public TestBinderProxy(IBinder remote) {
        mRemote = remote;
    }

    @Override
    public IBinder asBinder() {
        return mRemote;
    }

    @Override
    public int increase(int value) throws RemoteException {
        AppLogger.i(TAG, "proxy increase invoked: " + value);

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(value);
            mRemote.transact(INCREASE_TRANSACTION, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override
    public void print(String value)  throws RemoteException {
        AppLogger.i(TAG, "proxy print invoked: " + value);

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeString(value);
            mRemote.transact(PRINT_TRANSACTION, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override
    public String testCrash() throws RemoteException {
        AppLogger.i(TAG, "proxy testCrash invoked");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            mRemote.transact(TEST_CRASH_TRANSACTION, data, reply, 0);
            reply.readException();
            return reply.readString();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override
    public String testCrash2() throws RemoteException {
        AppLogger.i(TAG, "proxy testCrash2 invoked");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            mRemote.transact(TEST_CRASH2_TRANSACTION, data, reply, 0);
            reply.readException();
            return reply.readString();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}