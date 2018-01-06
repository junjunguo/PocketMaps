package com.junjunguo.pocketmaps.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import org.oscim.core.GeoPoint;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.fragments.MessageDialog;
import com.junjunguo.pocketmaps.fragments.MyAddressAdapter;
import com.junjunguo.pocketmaps.geocoding.AddressLoc;
import com.junjunguo.pocketmaps.geocoding.GeocoderGlobal;
import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;
import com.junjunguo.pocketmaps.util.Variable;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class GeocodeActivity  extends AppCompatActivity implements OnClickListener
{
  private static final String FAV_PROP_FILE = "Favourites.properties";
  private static final String SEL_FROM = "Location from";
  private static final String SEL_TO = "Location to";
  private static final String SEL_CUR = "Current location";
  public static final String ENGINE_OSM = "OpenStreetMap";
  public static final String ENGINE_GOOGLE = "Google Maps";
  public static final String ENGINE_OFFLINE = "Offline";
  private static OnClickAddressListener callbackListener;
  private static GeoPoint[] locations;
  private static Properties favourites;
  Spinner geoSpinner;
  EditText txtLocation;
  Button okButton;
  boolean statusLoading = false;
  
  /** Set pre-settings.
   *  @param newCallbackListener The Callback listener, called on selected Address.
   *  @param newLocations The [0]=start [1]=end and [2]=cur location used on Favourites, or null. **/
  public static void setPre(OnClickAddressListener newCallbackListener, GeoPoint[] newLocations)
  {
    callbackListener = newCallbackListener;
    locations = newLocations;
  }
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if (locations == null)
    {
      showSearchEngine();
    }
    else
    {
      final RecyclerView recView = showAddresses(new ArrayList<Address>());
      final MyAddressAdapter recAdapter = (MyAddressAdapter)recView.getAdapter();
      OnItemClickListener l = new OnItemClickListener()
      {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
          Address curAddr = recAdapter.remove(position);
          favourites.remove(curAddr.getAddressLine(0));
          
          String mapDir = Variable.getVariable().getMapsFolder().getParent();
          String propFile = new File(mapDir,FAV_PROP_FILE).getPath();
          try(FileOutputStream fos = new FileOutputStream(propFile))
          {
            favourites.store(fos, "List of favourites");
          }
          catch (IOException e)
          {
            logUser("Unable to store favourites");
          }
        }
      };
      startFavAsync(recAdapter);
      MainActivity.addDeleteItemHandler(this, recView, l);
    }
  }
  
  private void showSearchEngine()
  {
    setContentView(R.layout.activity_geocode);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
    adapter.add(ENGINE_OSM);
    adapter.add(ENGINE_GOOGLE);
    adapter.add(ENGINE_OFFLINE);
    geoSpinner = (Spinner) findViewById(R.id.geoSpinner);
    geoSpinner.setAdapter(adapter);
    okButton = (Button) findViewById(R.id.geoOk);
    txtLocation = (EditText) findViewById(R.id.geoLocation);
    okButton.setOnClickListener(this);
  }
  
  private void showFavAdd()
  {
    if (locations[0] == null && locations[1] == null && locations[2] == null)
    {
      logUser("Select a location first!");
    }
    setContentView(R.layout.activity_address_add);
    Button okButton = (Button) findViewById(R.id.addrOk);
    EditText addr1 = (EditText) findViewById(R.id.addrLine1);
    EditText addr2 = (EditText) findViewById(R.id.addrLine2);
    EditText addr3 = (EditText) findViewById(R.id.addrLine3);
    EditText addr4 = (EditText) findViewById(R.id.addrLine4);
    Spinner sp = (Spinner) findViewById(R.id.addrSpinner);
    
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
    if (locations[0] != null) { adapter.add(SEL_FROM); }
    if (locations[1] != null) { adapter.add(SEL_TO); }
    if (locations[2] != null) { adapter.add(SEL_CUR); }
    sp.setAdapter(adapter);
    okButton.setOnClickListener(createAddAddrClickListener(sp, addr1, addr2, addr3, addr4));
  }
  
  private OnClickListener createAddAddrClickListener(Spinner sp,
      final EditText addr1, final EditText addr2,
      final EditText addr3, final EditText addr4)
  {
    final GeoPoint loc;
    if (sp.getSelectedItem().toString().equals(SEL_FROM)) { loc = locations[0]; }
    else if (sp.getSelectedItem().toString().equals(SEL_TO)) { loc = locations[1]; }
    else { loc = locations[2]; }
    OnClickListener l = new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        new AsyncTask<Void, Void, Void>()
        {
          @Override
          protected Void doInBackground(Void... params)
          {
            Address addr = new Address(Locale.getDefault());
            addr.setAddressLine(0, addr1.getText().toString());
            addr.setAddressLine(1, addr2.getText().toString());
            addr.setAddressLine(2, addr3.getText().toString());
            addr.setAddressLine(3, addr4.getText().toString());
            addr.setLatitude(loc.getLatitude());
            addr.setLongitude(loc.getLongitude());
            AddressLoc.addToProp(favourites, addr);
            String mapDir = Variable.getVariable().getMapsFolder().getParent();
            String propFile = new File(mapDir,FAV_PROP_FILE).getPath();
            try(FileOutputStream fos = new FileOutputStream(propFile))
            {
              favourites.store(fos, "List of favourites");
            }
            catch (IOException e)
            {
              logUser("Unable to store favourites");
            }
            return null;
          }
        }.execute();
        GeocodeActivity.this.finish();
      }
    };
    return l;
  }

  @Override protected void onResume()
  {
    super.onResume();
    if (locations!=null)
    {
      MessageDialog.showMsg(this, "addressDeleteMsg", R.string.swipe_out, true);
    }
  }
  
  @Override protected void onDestroy()
  {
    super.onDestroy();
    GeocoderGlobal.stopRunningActions();
  }
  
  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.geoOk)
    {
      log("Selected: Search location");
      startSearchAsync();
    }
  }

  private void startSearchAsync()
  {
    if (statusLoading)
    {
      logUser(okButton.getText().toString());
      return;
    }
    statusLoading = true;
    okButton.setText(R.string.loading_dotdotdot);
    final String engine = geoSpinner.getSelectedItem().toString();
    final String geoLocation = txtLocation.getText().toString();
    new AsyncTask<Void, Void, List<Address>>()
    {
      @Override
      protected List<Address> doInBackground(Void... params)
      {
        Locale locale = Locale.getDefault();
        GeocoderGlobal geoc = new GeocoderGlobal(locale);
        Context appContext = GeocodeActivity.this.getApplicationContext();
        if (engine.equals(ENGINE_OSM)) { return geoc.find_osm(appContext, geoLocation); }
        if (engine.equals(ENGINE_GOOGLE)) { return geoc.find_google(appContext, geoLocation); }
        if (engine.equals(ENGINE_OFFLINE)) { return geoc.find_local(appContext, geoLocation); }
        return null;
      }
      @Override
      protected void onPostExecute(List<Address> resp)
      {
        if (resp==null)
        {
          logUser(engine + " search is not present!");
          okButton.setText(R.string.search_location);
          statusLoading = false;
        }
        else
        {
          if (resp.size()==0)
          {
            logUser("No addresses found");
            okButton.setText(R.string.search_location);
            statusLoading = false;
          }
          else
          {
            showAddresses(resp);
          }
        }
      }
    }.execute();
  }
  
  private void startFavAsync(final MyAddressAdapter adapter)
  {
    String mapDir = Variable.getVariable().getMapsFolder().getParent();
    final String propFile = new File(mapDir,"Favourites.properties").getPath();
    if (!new File(propFile).exists())
    {
      favourites = new Properties();
      return;
    }
    new AsyncTask<Void, Void, List<Address>>()
    {
      String errMsg;
      @Override
      protected List<Address> doInBackground(Void... params)
      {
        ArrayList<Address> result = new ArrayList<Address>();
        if (favourites == null)
        {
          favourites = new Properties();
          try (FileInputStream fis = new FileInputStream(propFile))
          {
            favourites.load(fis);
          }
          catch (IOException e)
          {
            errMsg = "Error while loadiong favourites (file)";
            return null;
          }
        }
        boolean hasError = false;
        for (Entry<Object, Object> entry : favourites.entrySet())
        {
          Address addr = AddressLoc.readFromPropEntry(entry);
          if (addr == null) { hasError = true; }
          else { result.add(addr); }
        }
        if (hasError) { errMsg ="Error while loading favourites (properties)"; }
        return result;
      }
      @Override
      protected void onPostExecute(List<Address> resp)
      {
        if (errMsg!=null) { logUser(errMsg); }
        if (resp != null)
        {
          adapter.addAll(resp);
        }
      }
    }.execute();
  }

  private RecyclerView showAddresses(List<Address> list)
  {
    setContentView(R.layout.activity_addresses);
    OnClickAddressListener l = new OnClickAddressListener()
    {
      @Override
      public void onClick(Address addr)
      {
        log("Address selected: " + addr);
        GeocodeActivity.this.finish();
        if (callbackListener!=null)
        {
          callbackListener.onClick(addr);
        }
      }
    };
    MyAddressAdapter adapter = new MyAddressAdapter(list, l);
    RecyclerView listView = (RecyclerView) findViewById(R.id.my_addr_recycler_view);
    listView.setHasFixedSize(true);

    // use a linear layout manager
    LinearLayoutManager layoutManager = new LinearLayoutManager(this.getApplicationContext());
    layoutManager.setOrientation(LinearLayout.VERTICAL);
    listView.setLayoutManager(layoutManager);
    listView.setItemAnimator(new DefaultItemAnimator());
    listView.setAdapter(adapter);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.my_addr_add_fab);
    if (locations==null)
    {
      fab.setVisibility(View.INVISIBLE);
    }
    else
    {
      fab.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View v)
        {
          log("Plus selected!");
          showFavAdd();
        }
      });
    }
    return listView;
  }
  
  private void log(String str)
  {
    Log.i(GeocodeActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(GeocodeActivity.class.getName(), str);
    Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
  }
}

