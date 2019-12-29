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
import com.junjunguo.pocketmaps.util.Variable.VarType;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
  private enum EditType {ViewOnly, ViewEdit, EditOnly};
  private static OnClickAddressListener callbackListener;
  /** The locations "from" "to" and "current" used to set in Favourites. **/
  private static GeoPoint[] locations;
  private static String[] locNames;
  private static Properties favourites;
  Spinner geoSpinner;
  Spinner locSpinner;
  AutoCompleteTextView txtLocation;
  Button okButton;
  boolean statusLoading = false;
  boolean backToListViewOnly = false;
  List<Address> backToListData = null;
  
  /** Set pre-settings.
   *  @param newCallbackListener The Callback listener, called on selected Address.
   *  @param newLocations The [0]=start [1]=end and [2]=cur location used on Favourites, or null. **/
  public static void setPre(OnClickAddressListener newCallbackListener, GeoPoint[] newLocations, String[] newLocNames)
  {
    callbackListener = newCallbackListener;
    locations = newLocations;
    locNames = newLocNames;
  }
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    backToListData = null;
    if (locations == null)
    {
      showSearchEngine();
    }
    else // Favourites-AddressList
    {
      RecyclerView recView = showAddresses(new ArrayList<Address>(), false);
      startFavAsync((MyAddressAdapter)recView.getAdapter());
    }
  }
  
  private void addDeleteItemHandler(RecyclerView recView)
  {
    final MyAddressAdapter recAdapter = (MyAddressAdapter)recView.getAdapter();
    OnItemClickListener delL = new OnItemClickListener()
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
    MainActivity.addDeleteItemHandler(this, recView, delL);
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
    geoSpinner.setSelection(Variable.getVariable().getGeocodeSearchEngine());
    okButton = (Button) findViewById(R.id.geoOk);
    txtLocation = (AutoCompleteTextView) findViewById(R.id.geoLocation);
    String preText = ShowLocationActivity.locationSearchString;
    if (preText != null)
    {
      txtLocation.setText(preText);
      ShowLocationActivity.locationSearchString = null;
    }
    ArrayAdapter<String> autoAdapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1,
        Variable.getVariable().getGeocodeSearchTextList());
    txtLocation.setAdapter(autoAdapter);

    okButton.setOnClickListener(this);
  }
  
  /** When not editOnly then preAddress is needed. **/
  private void showFavAdd(EditType type, Address preAddress)
  {
    if (type == EditType.EditOnly &&
                locations[0] == null &&
                locations[1] == null &&
                locations[2] == null)
    {
      logUser("Select a location first!");
      return;
    }
    setContentView(R.layout.activity_address_add);
    Button okButton = (Button) findViewById(R.id.addrOk);
    TextView tv = (TextView) findViewById(R.id.addrText);
    ImageView iv = (ImageView) findViewById(R.id.addrShare);
    EditText addr[] = new EditText[5];
    addr[0] = (EditText) findViewById(R.id.addrLine1);
    addr[1] = (EditText) findViewById(R.id.addrLine2);
    addr[2] = (EditText) findViewById(R.id.addrLine3);
    addr[3] = (EditText) findViewById(R.id.addrLine4);
    addr[4] = (EditText) findViewById(R.id.addrLine5);
    TextView addrV[] = new TextView[5];
    addrV[0] = (TextView) findViewById(R.id.addrLineV1);
    addrV[1] = (TextView) findViewById(R.id.addrLineV2);
    addrV[2] = (TextView) findViewById(R.id.addrLineV3);
    addrV[3] = (TextView) findViewById(R.id.addrLineV4);
    addrV[4] = (TextView) findViewById(R.id.addrLineV5);
    locSpinner = (Spinner) findViewById(R.id.addrSpinner);

    GeoPoint loc;
    String oldLocName = null;
    if (type == EditType.ViewOnly)
    {
      okButton.setVisibility(View.INVISIBLE);
      locSpinner.setVisibility(View.INVISIBLE);
      fillText(addr, null, View.GONE);
      fillText(addrV, preAddress, View.VISIBLE);
      loc = new GeoPoint(preAddress.getLatitude(), preAddress.getLongitude());
      oldLocName = preAddress.getAddressLine(0);
      tv.setText(oldLocName);
      iv.setOnClickListener(createShareListener(loc));
    }
    else if (type == EditType.ViewEdit)
    {
      okButton.setText(R.string.edit);
      okButton.setTextColor(Color.BLUE);
      locSpinner.setVisibility(View.INVISIBLE);
      fillText(addr, preAddress, View.GONE);
      fillText(addrV, preAddress, View.VISIBLE);
      loc = new GeoPoint(preAddress.getLatitude(), preAddress.getLongitude());
      oldLocName = preAddress.getAddressLine(0);
      tv.setText(oldLocName);
      iv.setOnClickListener(createShareListener(loc));
    }
    else // EditOnly
    {
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
      if (locations[0] != null) { adapter.add(SEL_FROM); }
      if (locations[1] != null) { adapter.add(SEL_TO); }
      if (locations[2] != null) { adapter.add(SEL_CUR); }
      locSpinner.setAdapter(adapter);
      locSpinner.setOnItemSelectedListener(createSpinnerListener(addrV[1], addr[1], addrV[2], addr[2]));
      loc = getLocFromSpinner(locSpinner);
      iv.setVisibility(View.GONE);
    }
    okButton.setOnClickListener(createAddAddrClickListener(loc, type, addr, addrV, okButton, oldLocName));
  }
  
  private OnClickListener createShareListener(final GeoPoint loc)
  {
    OnClickListener c = new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // TODO: Test share-button!
        String uri = "geo:" + loc.getLatitude() + ","
            + loc.getLongitude();// + "?q=" + loc.getLatitude()
            //+ "," + loc.getLongitude();
        Intent shareI = new Intent(android.content.Intent.ACTION_SEND, Uri.parse(uri));
        //shareI.setType("text/plain");
        shareI.setType("application/geo");
        v.getContext().startActivity(shareI);
      }
    };
    return c;
  }

  private OnItemSelectedListener createSpinnerListener(final TextView editText1, final TextView editText2,
                                                       final TextView editText3, final TextView editText4)
  {
    OnItemSelectedListener l = new OnItemSelectedListener(){

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
      {
        String txt = getTxtFromSpinner(locSpinner);
        log("Spinner location-TXT: " + txt);
        editText1.setText(txt);
        editText2.setText(txt);
        editText3.setText(Variable.getVariable().getCountry());
        editText4.setText(Variable.getVariable().getCountry());
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    };
    return l;
  }

  private GeoPoint getLocFromSpinner(Spinner sp)
  {
    if (sp.getSelectedItem().toString().equals(SEL_FROM)) { return locations[0]; }
    else if (sp.getSelectedItem().toString().equals(SEL_TO)) { return locations[1]; }
    else { return locations[2]; }
  }
  
  private String getTxtFromSpinner(Spinner sp)
  {
    if (sp.getSelectedItem().toString().equals(SEL_FROM)) { return locNames[0]; }
    else if (sp.getSelectedItem().toString().equals(SEL_TO)) { return locNames[1]; }
    else { return "GPS"; }
  }
  
  /** Fill the text of preAddress, and set visibility.
   *  @param preAddress The address to fill out, may be null. **/
  private void fillText(TextView[] addr, Address preAddress, int visibility)
  {
    if (preAddress != null)
    {
      ArrayList<String> lines = AddressLoc.getLines(preAddress);
      for (int i=0; i<lines.size(); i++)
      {
        int arrIndex = i;
        if (arrIndex >= addr.length)
        { // Switch to multiLine
          arrIndex = addr.length-1;
        }
        TextView v = addr[arrIndex];
        String line = lines.get(i);
        if (line != null)
        {
          if (line.contains("\n"))
          { // Use MultiLine TextEdit
            v = addr[addr.length-1];
          }
          boolean isMultiLine = (v == addr[addr.length-1]);
          if (isMultiLine && !v.getText().toString().isEmpty())
          {
            v.setText(v.getText() + "\n");
          }
          v.setText(v.getText() + line);
        }
      }
    }
    for (TextView v : addr)
    {
      v.setVisibility(visibility);
    }
  }

  private OnClickListener createAddAddrClickListener(final GeoPoint loc, final EditType type,
                                            final EditText eaddr[], final TextView eaddrV[],
                                            final Button okButton, final String oldLocName)
  {
    OnClickListener l = new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if (okButton.getCurrentTextColor() == Color.BLUE)
        { // ViewEdit --> Switch to edit!
          okButton.setTextColor(Color.BLACK);
          okButton.setText(R.string.ok);
          fillText(eaddr, null, View.VISIBLE);
          fillText(eaddrV, null, View.GONE);
          return;
        }
        new AsyncTask<Void, Void, Void>()
        {
          @Override
          protected Void doInBackground(Void... params)
          {
            Address addr = new Address(Locale.getDefault());
            for (int i=0; i<eaddr.length; i++)
            {
              String line = eaddr[i].getText().toString();
              if (i == (eaddr.length-1) &&
                       (!line.isEmpty()) &&
                       (!line.contains("\n")))
              { // Force MultiLine!
                line = line + "\n";
              }
              addr.setAddressLine(i, line);
            }
            GeoPoint newLoc = loc;
            if (type == EditType.EditOnly)
            {
              newLoc = getLocFromSpinner(locSpinner);
            }
            addr.setLatitude(newLoc.getLatitude());
            addr.setLongitude(newLoc.getLongitude());
            AddressLoc.addToProp(favourites, addr, oldLocName);
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
    if (locations!=null && favourites!=null && !favourites.isEmpty())
    {
      MessageDialog.showMsg(this, "addressDeleteMsg", R.string.swipe_out, true);
    }
  }
  
  @Override protected void onDestroy()
  {
    super.onDestroy();
    GeocoderGlobal.stopRunningActions();
    backToListData = null;
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
  
  @Override
  public void onBackPressed()
  {
    if (backToListData == null)
    {
        super.onBackPressed();
    }
    else
    {
      showAddresses(backToListData, backToListViewOnly);
      backToListData = null;
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
    boolean bSetEn = Variable.getVariable().setGeocodeSearchEngine(geoSpinner.getSelectedItemPosition());
    boolean bSetTx = Variable.getVariable().addGeocodeSearchText(geoLocation);
    if (bSetEn || bSetTx) { Variable.getVariable().saveVariables(VarType.Geocode); }
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
            showAddresses(resp, true);
          }
        }
      }
    }.execute();
  }
  
  private void startFavAsync(final MyAddressAdapter adapter)
  {
    String mapDir = Variable.getVariable().getMapsFolder().getParent();
    final String propFile = new File(mapDir,"Favourites.properties").getPath();
    boolean loadProp = false;
    if (!new File(propFile).exists())
    {
      favourites = new Properties();
      return;
    }
    else if (favourites == null)
    {
      favourites = new Properties();
      loadProp = true;
    }
    final boolean loadPropFinal = loadProp;
    new AsyncTask<Void, Void, List<Address>>()
    {
      String errMsg;
      @Override
      protected List<Address> doInBackground(Void... params)
      {
        ArrayList<Address> result = new ArrayList<Address>();
        if (loadPropFinal)
        {
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

  private RecyclerView showAddresses(final List<Address> list, final boolean viewOnly)
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
    OnClickAddressListener detL = new OnClickAddressListener()
    {
      @Override
      public void onClick(Address addr)
      {
        log("Address details selected: " + addr);
        backToListData = list;
        backToListViewOnly = viewOnly;
        if (viewOnly)
        {
          GeocodeActivity.this.showFavAdd(EditType.ViewOnly, addr);
        }
        else
        {
          GeocodeActivity.this.showFavAdd(EditType.ViewEdit, addr);
        }
      }
    };
    MyAddressAdapter adapter = new MyAddressAdapter(list, l, detL);
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
          showFavAdd(EditType.EditOnly, null);
        }
      });
    }
    if (!viewOnly) { addDeleteItemHandler(listView); };
    return listView;
  }
  
  private void log(String str)
  {
    Log.i(GeocodeActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(GeocodeActivity.class.getName(), str);
    try
    {
      Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
}

