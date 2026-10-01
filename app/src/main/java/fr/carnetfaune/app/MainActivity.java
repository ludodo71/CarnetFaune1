package fr.carnetfaune.app;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.location.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    final ArrayList<Observation> observations=new ArrayList<>();
    final String[] species={"Merle noir","Mésange charbonnière","Rougegorge familier","Hérisson d'Europe","Écureuil roux","Renard roux","Chevreuil","Lézard des murailles","Orvet fragile","Couleuvre helvétique","Blaireau européen","Lièvre d'Europe"};
    EditText place,count,notes; TextView gps;
    static class Observation {
        String species,place,notes,time; int count; double lat,lon;
        Observation(String s,String p,int c,String n,String t,double la,double lo){species=s;place=p;count=c;notes=n;time=t;lat=la;lon=lo;}
    }
    public void onCreate(Bundle b){super.onCreate(b);showHome();}
    TextView title(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(24);t.setTextColor(Color.rgb(35,80,45));t.setGravity(Gravity.CENTER);t.setPadding(8,20,8,20);return t;}
    Button btn(String s){Button b=new Button(this);b.setText(s);return b;}
    void showHome(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(24,16,24,24);l.addView(title("Carnet Faune"));Button n=btn("➕ Nouvelle observation"),h=btn("📋 Historique"),c=btn("📊 Statistiques par heure"),m=btn("🗺️ Ouvrir la carte");l.addView(n);l.addView(h);l.addView(c);l.addView(m);n.setOnClickListener(v->showNew());h.setOnClickListener(v->showHistory());c.setOnClickListener(v->showChart());m.setOnClickListener(v->openMap());setContentView(l);}
    void showNew(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(24,8,24,16);l.addView(title("Nouvelle observation"));Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,species));l.addView(sp);place=new EditText(this);place.setHint("Lieu");l.addView(place);count=new EditText(this);count.setHint("Nombre");count.setInputType(2);count.setText("1");l.addView(count);notes=new EditText(this);notes.setHint("Notes (météo, habitat, comportement...)");notes.setMinLines(3);l.addView(notes);gps=new TextView(this);gps.setText("GPS : non relevé");l.addView(gps);Button pos=btn("📍 Relever ma position"),save=btn("💾 Enregistrer"),back=btn("Retour");l.addView(pos);l.addView(save);l.addView(back);final double[] xy={0,0};pos.setOnClickListener(v->getLocation(xy));save.setOnClickListener(v->{int n=1;try{n=Integer.parseInt(count.getText().toString());}catch(Exception e){}String now=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.FRANCE).format(new Date());observations.add(new Observation(sp.getSelectedItem().toString(),place.getText().toString(),n,notes.getText().toString(),now,xy[0],xy[1]));Toast.makeText(this,"Observation enregistrée",Toast.LENGTH_SHORT).show();});back.setOnClickListener(v->showHome());setContentView(l);}
    void getLocation(double[] xy){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},42);return;}try{LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location x=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(x==null)x=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);if(x!=null){xy[0]=x.getLatitude();xy[1]=x.getLongitude();gps.setText(String.format(Locale.US,"GPS : %.5f, %.5f",xy[0],xy[1]));}else gps.setText("GPS : position indisponible");}catch(Exception e){gps.setText("GPS : erreur");}}
    void showHistory(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(20,10,20,20);l.addView(title("Historique"));if(observations.isEmpty()){TextView t=new TextView(this);t.setText("Aucune observation.");t.setTextSize(18);l.addView(t);}else for(Observation o:observations){TextView t=new TextView(this);t.setText(o.time+" — "+o.species+" ×"+o.count+"\n"+o.place+"\n"+o.notes+"\n");t.setTextSize(16);t.setPadding(0,12,0,12);l.addView(t);}Button b=btn("Retour");l.addView(b);b.setOnClickListener(v->showHome());setContentView(l);}
    void showChart(){setContentView(new HourChart(this,observations));}
    void openMap(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=observations")));}catch(Exception e){}}
    static class HourChart extends View{Paint p=new Paint(1);ArrayList<Observation>d;HourChart(Activity a,ArrayList<Observation>x){super(a);d=x;}protected void onDraw(Canvas c){c.drawColor(Color.WHITE);p.setTextSize(28);p.setColor(Color.rgb(35,80,45));c.drawText("Observations par heure",30,50,p);int[] h=new int[24];for(Observation o:d)try{h[Integer.parseInt(o.time.substring(11,13))]++;}catch(Exception e){}p.setTextSize(18);for(int i=0;i<24;i++){float x=30+(i%12)*55,y=100+(i/12)*130;p.setColor(Color.DKGRAY);c.drawText(i+"h",x,y,p);p.setColor(Color.rgb(46,125,50));c.drawRect(x,y+10,x+35,y+10+h[i]*12,p);p.setColor(Color.DKGRAY);c.drawText(""+h[i],x+10,y+35,p);}}}
}
