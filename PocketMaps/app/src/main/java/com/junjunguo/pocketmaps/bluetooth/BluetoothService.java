/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.junjunguo.pocketmaps.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 * Source: https://github.com/android/connectivity-samples
 */
public class BluetoothService {
    private final static Logger log = Logger.getLogger(BluetoothService.class.getName());

    private final BluetoothAdapter mAdapter;
    Handler mReceiver;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;
    private UUID mUuid;
    private String mName;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor.Prepares a new BluetoothChat session.
     *
     * @param activity The UI Activity Context
     * @param sName The name for this service.
     * @param uuid The unique uuid for this service.
     */
    public BluetoothService(Activity activity, String sName, UUID uuid) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mUuid = uuid;
        mName = sName;
    }

    public boolean isSupported()
    {
      return mAdapter!=null;
    }
    public boolean isEnabled()
    {
      return mAdapter.isEnabled();
    }

      /** Get the actual paired devices.
     * @return A list with deviceNames and deviceAddresses. */
    public Properties getPairedDevices()
    {
      Properties devs = new Properties();
      for (BluetoothDevice dev : mAdapter.getBondedDevices())
      {
        devs.put(dev.getName(), dev.getAddress());
      }
      return devs;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        log.fine("updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service.
     * <br>Specifically start AcceptThread to begin a session in listening (server) mode.
     * <br>Called by the Activity onResume()
     * @param receiver The Receiver, see BluetoothUtil.createFileReceiver().
     */
    public synchronized void startReceiver(Handler receiver) {
        mReceiver = receiver;
        log.fine("start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }

        // Update UI title
        updateUserInterfaceTitle();
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param deviceAddress The BluetoothDevice to connect
     * @param msgHandler The handler that sends empty messages of BluetoothUtil.MSG_XXX.
     */
    public void connect(String deviceAddress, Handler msgHandler)
    {
      connect(mAdapter.getRemoteDevice(deviceAddress), msgHandler);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param msgHandler The handler that sends empty messages of BluetoothUtil.MSG_XXX.
     */
    public synchronized void connect(BluetoothDevice device, Handler msgHandler) {
        log.log(Level.FINE, "connect to: {0}", device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, msgHandler);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     * @param socketType
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        log.log(Level.FINE, "connected, Socket Type: {0}", socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        updateUserInterfaceTitle();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        log.fine("stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        mState = STATE_NONE;
        updateUserInterfaceTitle();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @param len The length to write
     * @see ConnectedThread#write(byte[], int)
     * @return True on success.
     */
    public boolean write(byte[] out, int len) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return false;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        return r.write(out, len);
    }
    
     /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param str The Message to write
     * @see ConnectedThread#write(byte[], int)
     * @return True when ok.
     */
    public boolean write(String str) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return false;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        return r.write(str);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        mState = STATE_NONE;
        updateUserInterfaceTitle();
        log.warning("Connection failed.");
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        mState = STATE_NONE;
        updateUserInterfaceTitle();
        log.warning("Connection lost.");
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType = "Secure";

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(mName, mUuid);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            log.fine("Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    log.log(Level.SEVERE, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            log.info("END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            log.fine("Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType = "Secure";
        private Handler msgHandler;

        public ConnectThread(BluetoothDevice device, Handler msgHandler) {
          this.msgHandler = msgHandler;
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                    tmp = device.createRfcommSocketToServiceRecord(mUuid);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Socket Type: " + mSocketType + "create() failed", e);
                msgHandler.sendEmptyMessage(BluetoothUtil.MSG_FAILED);
                mmSocket = null;
                return;
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
          if (mmSocket == null) { return; }
            log.log(Level.INFO, "BEGIN mConnectThread SocketType: {0}", mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            msgHandler.sendEmptyMessage(BluetoothUtil.MSG_STARTED);

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    log.log(Level.SEVERE, "unable to close() " + mSocketType + " socket during connection failure", e2);
                }
                connectionFailed();
                msgHandler.sendEmptyMessage(BluetoothUtil.MSG_FAILED);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            msgHandler.sendEmptyMessage(BluetoothUtil.MSG_FINISH);

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            log.log(Level.FINE, "create ConnectedThread: {0}", socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                log.log(Level.SEVERE, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        @Override
        public void run() {
            log.info("BEGIN mConnectedThread");
            byte[] buffer = new byte[1024*4];
            long dataLength = -1;
            long dataWritten = 0;
            try
            {
              // Read header when connected
              if (mState == STATE_CONNECTED && mmSocket.isConnected())
              {
                if (! (mmInStream instanceof ObjectInputStream)) { mmInStream = new ObjectInputStream(mmInStream); }
                ObjectInputStream ois = (ObjectInputStream)mmInStream;
                String info = ois.readUTF();
                dataLength = BluetoothUtil.headerGetDataLength(info);
                Message msg = new Message();
                msg.what = BluetoothUtil.MSG_STARTED;
                msg.obj = info;
                mReceiver.sendMessage(msg);
              }
              // Keep listening to the InputStream while connected
              while (mState == STATE_CONNECTED && mmSocket.isConnected())
              {
                    // Read from the InputStream
                    int len = mmInStream.read(buffer);
                    if (len < 0) { break; }
                    float percent = ((float)dataWritten/dataLength) * 100.0f;
                    Message msg = new Message();
                    msg.what = BluetoothUtil.MSG_PROGRESS;
                    msg.obj = buffer;
                    msg.arg1 = (int)percent;
                    msg.arg2 = len;
                    mReceiver.sendMessage(msg);
                    dataWritten += len;
                    if (dataWritten >= dataLength) { break; }
                    buffer = new byte[1024*4]; // Dont override sent data-array.
              }
              mReceiver.sendEmptyMessage(BluetoothUtil.MSG_FINISH);
            } catch (IOException e) {
                    log.log(Level.SEVERE, "disconnected", e);
                    mReceiver.sendEmptyMessage(BluetoothUtil.MSG_FAILED);
                    connectionLost();
          }
        }
        
        public boolean write(String msg)
        {
          try
          {
            if (!(mmOutStream instanceof ObjectOutputStream))
            {
              mmOutStream = new ObjectOutputStream(mmOutStream);
            }
            ((ObjectOutputStream)mmOutStream).writeUTF(msg);
          }
          catch (IOException e)
          {
            log.log(Level.SEVERE, "Exception during obj-write", e);
            return false;
          }
          return true;
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public boolean write(byte[] buffer, int len) {
            try {
                mmOutStream.write(buffer, 0, len);
                mmOutStream.flush(); // Ensure all data is written.
            } catch (IOException e) {
                log.log(Level.SEVERE, "Exception during write", e);
                return false;
            }
            return true;
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, "close() of connect socket failed", e);
            }
        }
    }
}
