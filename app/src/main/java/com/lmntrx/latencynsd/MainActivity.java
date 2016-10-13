package com.lmntrx.latencynsd;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;



public class MainActivity extends AppCompatActivity {
    static final int SocketServerPORT = 8080;  // Port should be fetched dynamically in real systems.// NSD Manager and service registration code
    private String SERVICE_NAME = "NSD";
    private String SERVICE_TYPE = "_http._tcp.";
    private NsdManager mNsdManager;

    private static final String TAG = "MainActivity";
//Initialize other variable that you will need


    boolean firstOpen;
    ServerSocket serverSocket;
    //ServerSocket mServerSocket;
    //ServerSocket mLocalPort;

    NsdManager.RegistrationListener mRegistrationListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.ResolveListener mResolveListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Fetch other Components


        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        Button hostBtn= (Button) findViewById(R.id.hostBTN);
        Button discoverBtn= (Button) findViewById(R.id.discoverBTN);

        hostBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                registerService(SocketServerPORT);

            }
        });

        discoverBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                Log.d("Entered Onclick","Entered Onlcick");


                initializeDiscoveryListener();
                mNsdManager.discoverServices(
                        SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);


            }
        });





    }
//};

    /*
    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        mServerSocket = new ServerSocket(0);

        // Store the chosen port.
        mLocalPort =  mServerSocket.getLocalPort();

    }

   */


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);


        initializeRegistrationListener();
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }


    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                String mServiceName = NsdServiceInfo.getServiceName();
                SERVICE_NAME = mServiceName;
                Toast.makeText(MainActivity.this, "Successfully registered",
                        Toast.LENGTH_LONG).show();
                Log.d("NsdserviceOnRegister", "Registered name : " + mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                             int errorCode) {

                Toast.makeText(MainActivity.this, "registration failed",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                // NsdManager.unregisterService() called and passed in this listener.
                Log.d("NsdserviceOnUnregister",
                        "Service Unregistered : " + serviceInfo.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                               int errorCode) {
                //Fail
            }
        };

    }


    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);


                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.

                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(SERVICE_NAME)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".

                    Log.d(TAG, "Same machine: " + SERVICE_NAME);

                    initializeResolveListener();
                    mNsdManager.resolveService(service, mResolveListener);

                }

                else if (service.getServiceName().contains("NSD")){

                    Log.d(TAG, "ENTERED RESOLVING IF CONDITION");
                    initializeResolveListener();
                    mNsdManager.resolveService(service, mResolveListener);
                }


            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.

                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.d(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                NsdServiceInfo mService = serviceInfo;

                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                int port = mService.getPort();
                InetAddress host = mService.getHost();

                Toast.makeText(MainActivity.this, "Successfully connected",
                        Toast.LENGTH_LONG).show();

                if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }


            }
        };
    }

    // NsdHelper's tearDown method
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

}