package tw.com.lr.moodbox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket mBluetoothSocket = null;
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 2;

    public void Reg(Activity mContext)
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)mContext).startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
// Otherwise, setup the chat session
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ((Activity)mContext).registerReceiver(mReceiver, filter);
    }

    private void resetConnection()
    {
        if (mInputStream != null) {
            try {mInputStream.close();} catch (Exception e) {}
            mInputStream = null;
        }

        if (mOutputStream != null) {
            try {mOutputStream.close();} catch (Exception e) {}
            mOutputStream = null;
        }

        if (mBluetoothSocket != null) {
            try {mBluetoothSocket.close();} catch (Exception e) {}
            mBluetoothSocket = null;
        }
    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

//            if(intent.getAction())
//            {
//                Status = -1;
//            }
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                resetConnection();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(this.getClass().getName(), "device.getName = " + device.getName());
                Toast.makeText(getApplicationContext(), device.getName(), Toast.LENGTH_SHORT).show();

                if(device.getName()==null)
                    return;

                if (device.getName().equals("HC-05")){
                    try {

                        Log.d(this.getClass().getName(), "Connect success = " + device.getName());

                        mBluetoothAdapter.cancelDiscovery();

                        mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
                        mBluetoothSocket.connect();

                        Status = 1;

                        String message = "Device Connected";
                        mOutputStream = mBluetoothSocket.getOutputStream();

                        mOutputStream.write(message.getBytes());
                        mOutputStream.flush();

                    } catch (IOException e) {
                        Log.d(this.getClass().getName(),"Exception = " + e.getMessage());
                    }
                }
            }
        }
    };

    ImageView Red = null;
    ImageView Orange = null;
    ImageView Green = null;
    Button sendBtn = null;
    EditText edTxt = null;

    static int Status = -1;
    static int Osw = 0;

    Handler mHandler = new Handler();
    Timer mTimer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Reg(this);

        Red = (ImageView)findViewById(R.id.red);
        Orange = (ImageView)findViewById(R.id.orange);
        Green = (ImageView)findViewById(R.id.green);
        sendBtn = (Button)findViewById(R.id.sendbtn);
        edTxt = (EditText)findViewById(R.id.edtext);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edTxt.getText().toString();

                if(msg.equals("1"))
                {
                    for(int i=0;i<10;i++) {
                        try {
                            mOutputStream.write(msg.getBytes());
                            mOutputStream.flush();

                            edTxt.setText("");

                            if (mInputStream == null)
                                mInputStream = mBluetoothSocket.getInputStream();

                            if (mBluetoothSocket != null && mInputStream != null) {
                                Log.d("Main", "waiting read");

                                final int r = mInputStream.read();
                                Log.d("Main", String.valueOf(r));

                                Toast.makeText(MainActivity.this, "heart = " + String.valueOf(r), Toast.LENGTH_SHORT).show();

                            }

                        } catch (Exception e) {
                            Log.d("Main", "Exception = " + e.getMessage());
                        }
                    }
                    return;
                }
                if(Status==1)
                {
                    try {
                        mOutputStream.write(msg.getBytes());
                        mOutputStream.flush();
                        edTxt.setText("");
                    }catch (Exception ex)
                    {
                        edTxt.setText("exception = " + ex.getMessage());
                        Status = -1;
                    }
                }
            }
        });

        mBluetoothAdapter.startDiscovery();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Thread.sleep(500);
                            if(mBluetoothSocket == null)
                            {
                                Log.d("Main", "socket is null");
                                Status = -1;
                                Thread.sleep(500);
                                return;
                            }
                            if(mBluetoothSocket!=null && !mBluetoothSocket.isConnected())
                            {
                                Log.d("Main", "socket connect fail");
                                Status = -1;
                            }

                            switch (Status) {
                                case -1:
                                    Green.setVisibility(View.INVISIBLE);
                                    Red.setVisibility(View.VISIBLE);
                                    if ((Osw++) % 2 == 0)
                                        Orange.setVisibility(View.INVISIBLE);
                                    else
                                        Orange.setVisibility(View.VISIBLE);


                                    if(!mBluetoothAdapter.isDiscovering()) {
                                        mBluetoothAdapter.startDiscovery();
                                    }

                                    break;
                                case 1:
                                    if ((Osw++) % 2 == 0)
                                        Green.setVisibility(View.INVISIBLE);
                                    else
                                        Green.setVisibility(View.VISIBLE);

                                    Orange.setVisibility(View.INVISIBLE);
                                    Red.setVisibility(View.INVISIBLE);

                                    break;
                            }

                            if(Osw>500) Osw = 0;

                        } catch (Exception e) {
                            Log.d("Main", "Exception = " + e.getMessage());
                        }
                    }
                });
            }
        },500,500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
