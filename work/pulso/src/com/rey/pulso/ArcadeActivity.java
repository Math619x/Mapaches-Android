package com.rey.pulso;

import android.app.Activity;
import android.os.*;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.util.Random;
import java.util.UUID;

public class ArcadeActivity extends Activity {
    private final int BG=0xfff7f3e5,CARD=0xffe8ead8,TEXT=0xff304d3d,MUTED=0xff6a7962,GREEN=0xff426e4d,RED=0xffb75448;
    private final ArcadeEngine game=new ArcadeEngine(new Random());
    private SharedPreferences prefs;
    private LinearLayout root,screen;
    private TextView points,health,progress,audioStatus;
    private EditText nickInput;
    private Board board;
    private AmbientMusic music;
    private SoundEffects effects;
    private boolean foreground,musicEnabled,effectsEnabled;
    private int character,map,item,selectedMode,selectedLevel;
    private String page="home",audioBack="home",sessionId=UUID.randomUUID().toString(),resultKey="",lastHud="";
    private ScoreTable resultScores;
    private String friendName="Tu amigo",friendPhrase="¡Me escapé!";
    private ContentAccess access;
    private boolean selectionUpdate;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private String key(int m,int l){return "arcade_"+m+"_"+l;}
    private String tableKey(String k){return "scores_"+k;}
    private ScoreTable table(String k){return ScoreTable.decode(prefs.getString(tableKey(k),""));}
    private void saveTable(String k,ScoreTable t){prefs.edit().putString(tableKey(k),t.encode()).apply();}
    private int best(String k){ScoreTable t=table(k);int high=t.entries().isEmpty()?0:t.entries().get(0).score;return Math.max(high,prefs.getInt("best_"+k,0));}
    private String characterName(){return character==8?friendName:GameOptions.CHARACTERS[character];}
    private String sessionLabel(){return ArcadeEngine.MODES[game.mode]+" · "+ArcadeEngine.LEVELS[game.level];}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);prefs=getSharedPreferences("pulso",MODE_PRIVATE);
        String legacyKey=key(0,1);
        if(!prefs.contains(tableKey(legacyKey))){
            ScoreTable legacy=ScoreTable.decode(prefs.getString("leaderboard_v1",""));
            int old=prefs.getInt("best",0);if(legacy.entries().isEmpty()&&old>0)legacy.add("legacy","ANTERIOR",old);
            prefs.edit().putString(tableKey(legacyKey),legacy.encode()).putInt("best_"+legacyKey,old).apply();
        }
        access=ContentAccess.decodeDemo(true,prefs.getString("store_demo_entitlements_v1",""));
        character=GameOptions.valid(prefs.getInt("character",0),9);map=GameOptions.valid(prefs.getInt("map",0),5);item=GameOptions.valid(prefs.getInt("item",0),5);validateSelection();
        selectedMode=GameOptions.valid(prefs.getInt("arcade_mode",0),3);selectedLevel=GameOptions.valid(prefs.getInt("arcade_level",1),3);
        friendName=prefs.getString("friend_name","Tu amigo");friendPhrase=prefs.getString("friend_phrase","¡Me escapé!");
        musicEnabled=prefs.getBoolean("music",true);effectsEnabled=prefs.getBoolean("effects",true);
        music=new AmbientMusic(this);music.setVolume(prefs.getInt("music_volume",60)/100f);
        music.setListener(new AmbientMusic.StatusListener(){public void changed(String s){if(audioStatus!=null)audioStatus.setText(s);}});
        effects=new SoundEffects(this);effects.setEnabled(effectsEnabled);setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener(){public WindowInsets onApplyWindowInsets(View v,WindowInsets i){v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i.consumeSystemWindowInsets();}});
        setContentView(root);root.requestApplyInsets();
        if(state!=null&&state.getBoolean("session")&&game.restore(state.getByteArray("engine"))&&game.playing){sessionId=state.getString("sessionId",sessionId);showPause();}else{game.playing=false;showHome();}
    }
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private void begin(String name){
        saveNick();View focused=getCurrentFocus();if(focused!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focused.getWindowToken(),0);
        nickInput=null;audioStatus=null;board=null;page=name;root.removeAllViews();syncMusic();
        screen=new LinearLayout(this);screen.setOrientation(1);screen.setPadding(dp(22),dp(16),dp(22),dp(20));root.addView(screen,new LinearLayout.LayoutParams(-1,-1));lastHud="";
    }
    private void gap(int n){screen.addView(new View(this),new LinearLayout.LayoutParams(1,dp(n)));}
    private void title(String s){TextView t=text(s,34,TEXT);t.setTypeface(Typeface.DEFAULT,1);screen.addView(t);gap(12);}
    private void body(String s){TextView t=text(s,15,MUTED);t.setLineSpacing(dp(3),1);screen.addView(t);gap(14);}
    private void brand(){screen.addView(text("UN MUNDO DE TRAVIESOS",12,GREEN));gap(16);}
    private Button makeButton(String s,boolean primary,View.OnClickListener click){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(primary?BG:TEXT);GradientDrawable bg=new GradientDrawable();bg.setColor(primary?GREEN:CARD);bg.setCornerRadius(dp(17));b.setBackground(bg);b.setOnClickListener(click);return b;}
    private void button(String s,boolean primary,View.OnClickListener click){screen.addView(makeButton(s,primary,click),new LinearLayout.LayoutParams(-1,dp(54)));gap(11);}
    private void scroll(){ScrollView s=new ScrollView(this);s.setFillViewport(true);root.removeView(screen);s.addView(screen);root.addView(s,new LinearLayout.LayoutParams(-1,-1));}
    private Spinner picker(String label,String[] values,int selected,final AdapterView.OnItemSelectedListener listener){
        screen.addView(text(label,14,TEXT));Spinner spinner=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);spinner.setAdapter(adapter);spinner.setSelection(selected);screen.addView(spinner,new LinearLayout.LayoutParams(-1,dp(52)));gap(10);spinner.setOnItemSelectedListener(listener);return spinner;
    }
    private abstract class Selection implements AdapterView.OnItemSelectedListener {public void onNothingSelected(AdapterView<?> p){}}
    private View.OnClickListener homeClick(){return new View.OnClickListener(){public void onClick(View v){showHome();}};}
    private View.OnClickListener playClick(){return new View.OnClickListener(){public void onClick(View v){startGame();}};}
    private View.OnClickListener exitClick(){return new View.OnClickListener(){public void onClick(View v){exitGame();}};}
    private View.OnClickListener soundClick(){return new View.OnClickListener(){public void onClick(View v){showAudio(page);}};}
    private void showHome(){
        begin("home");brand();screen.addView(new Hero(),new LinearLayout.LayoutParams(-1,dp(190)));gap(20);title("Mapaches\ny compañía");
        body(characterName()+" · "+GameOptions.MAPS[map]+"\n"+ArcadeEngine.MODES[selectedMode]+" · "+ArcadeEngine.LEVELS[selectedLevel]);
        screen.addView(text("RÉCORD DE ESTA CATEGORÍA · "+best(key(selectedMode,selectedLevel)),15,GREEN));gap(22);
        button("¡A jugar!",true,playClick());button("Modo y dificultad",false,new View.OnClickListener(){public void onClick(View v){showRules();}});
        button("Personalizar partida",false,new View.OnClickListener(){public void onClick(View v){showCustomize();}});
        button("Tienda · vista previa",false,new View.OnClickListener(){public void onClick(View v){showStore();}});
        button("Mejores 10",false,new View.OnClickListener(){public void onClick(View v){showScores(false);}});
        button("Música y efectos",false,soundClick());button("Cómo jugar",false,new View.OnClickListener(){public void onClick(View v){showHelp();}});button("Salir de la aplicación",false,exitClick());scroll();
    }
    private void showRules(){
        begin("rules");brand();title("Elige tu reto.");
        picker("Modo",ArcadeEngine.MODES,selectedMode,new Selection(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){selectedMode=n;prefs.edit().putInt("arcade_mode",n).apply();}});
        picker("Dificultad",ArcadeEngine.LEVELS,selectedLevel,new Selection(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){selectedLevel=n;prefs.edit().putInt("arcade_level",n).apply();}});
        body("CLÁSICO\nUn personaje a la vez, tres vidas y una vida extra cada 20 puntos.");
        body("CONTRARRELOJ\n60 segundos exactos de juego. Los fallos rompen la racha, pero no terminan la partida. No se usan vidas ni se añaden segundos.");
        body("MADRIGUERAS\nSeis agujeros. Al avanzar pueden asomarse hasta dos personajes simultáneamente. Tres vidas y vida extra cada 20 puntos.");
        body("Relajado da más tiempo y menos trampas. Difícil acelera el juego y aumenta las trampas. Cada combinación tiene su propia tabla de marcas.");
        button("Jugar con esta selección",true,playClick());button("Inicio",false,homeClick());scroll();
    }
    private void startGame(){validateSelection();saveNick();nickInput=null;sessionId=UUID.randomUUID().toString();game.start(selectedMode,selectedLevel);showGame();}
    private void validateSelection(){character=access.safeSelection(ContentAccess.CHARACTER,character);map=access.safeSelection(ContentAccess.MAP,map);item=access.safeSelection(ContentAccess.ITEM,item);prefs.edit().putInt("character",character).putInt("map",map).putInt("item",item).apply();}
    private void saveAccess(){prefs.edit().putString("store_demo_entitlements_v1",access.encodeDemo()).apply();}
    private void showGame(){
        begin("playing");LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        points=text("",28,TEXT);points.setTypeface(Typeface.DEFAULT,1);header.addView(points,new LinearLayout.LayoutParams(0,-2,1));header.addView(makeButton("Pausa",false,new View.OnClickListener(){public void onClick(View v){showPause();}}),new LinearLayout.LayoutParams(dp(86),dp(46)));screen.addView(header);gap(7);
        health=text("",16,RED);screen.addView(health);gap(6);progress=text("",12,MUTED);screen.addView(progress);gap(12);
        board=new Board();screen.addView(board,new LinearLayout.LayoutParams(-1,0,1));gap(10);TextView hint=text(characterName()+": toca · "+GameOptions.ITEMS[item]+": evita",12,MUTED);hint.setGravity(Gravity.CENTER);screen.addView(hint);updateHud();
    }
    private void updateBest(){String k=game.key();if(game.score>prefs.getInt("best_"+k,best(k)))prefs.edit().putInt("best_"+k,game.score).apply();}
    private void updateHud(){
        String hud=game.score+"/"+game.lives+"/"+((game.timeLeft+999)/1000)+"/"+game.streak+"/"+game.bestStreak;
        if(hud.equals(lastHud)||points==null)return;lastHud=hud;points.setText(game.score+" puntos");
        health.setText(game.mode==ArcadeEngine.TIMED?"◷ "+((game.timeLeft+999)/1000)+" s   ·   Racha "+game.streak:"♥ "+game.lives+" vidas   ·   Racha "+game.streak);
        progress.setText(game.mode==ArcadeEngine.TIMED?sessionLabel()+" · Mejor racha "+game.bestStreak:sessionLabel()+" · Vida en "+(20-game.score%20)+" puntos");
    }
    private void showPause(){begin("paused");brand();title("Toma un\nrespiro.");body(sessionLabel()+"\n"+game.score+" puntos · Mejor racha "+game.bestStreak+(game.mode==ArcadeEngine.TIMED?"\nQuedan "+((game.timeLeft+999)/1000)+" segundos":"\n"+game.lives+" vidas"));button("Continuar",true,new View.OnClickListener(){public void onClick(View v){showGame();}});button("Música y efectos",false,soundClick());button("Terminar partida",false,new View.OnClickListener(){public void onClick(View v){game.end();showResult();}});button("Salir de la aplicación",false,exitClick());scroll();}
    private void saveNick(){if(nickInput!=null&&resultScores!=null){String nick=ScoreTable.cleanNick(nickInput.getText().toString());resultScores.rename(sessionId,nick);saveTable(resultKey,resultScores);prefs.edit().putString("last_nick",nick).apply();}}
    private void saveResult(){updateBest();String k=game.key();ScoreTable t=table(k);if(t.add(sessionId,prefs.getString("last_nick","MAPACHE"),game.score))saveTable(k,t);}
    private void stat(String label,String value){LinearLayout row=new LinearLayout(this);row.setPadding(dp(12),dp(9),dp(12),dp(9));TextView l=text(label,15,MUTED);row.addView(l,new LinearLayout.LayoutParams(0,-2,1));row.addView(text(value,16,TEXT));screen.addView(row);}
    private void showResult(){
        saveNick();saveResult();begin("result");resultKey=game.key();resultScores=table(resultKey);brand();title("¡Vaya pandilla!");body(sessionLabel()+" · "+game.finishReason);
        TextView total=text(String.valueOf(game.score),74,GREEN);total.setTypeface(Typeface.DEFAULT,1);screen.addView(total);body("PERSONAJES ATRAPADOS");
        stat("Aciertos",String.valueOf(game.hits));stat("Personajes escapados",String.valueOf(game.escaped));stat("Trampas tocadas",String.valueOf(game.trapsTouched));stat("Mejor racha",game.bestStreak+" seguidos");stat("Tiempo de juego",String.format(java.util.Locale.getDefault(),"%.1f s",game.playedMs/1000.0));
        if(game.mode!=ArcadeEngine.TIMED)stat("Vidas extra ganadas",String.valueOf(game.bonusLives));gap(18);
        int rank=resultScores.rank(sessionId);if(rank>0){
            body("¡Puesto "+rank+" en esta categoría!\nEscribe tu nick (hasta 12 caracteres):");
            nickInput=new EditText(this);nickInput.setSingleLine(true);nickInput.setTextSize(19);nickInput.setTextColor(TEXT);nickInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});nickInput.setText(resultScores.find(sessionId).nick);nickInput.setSelectAllOnFocus(true);screen.addView(nickInput,new LinearLayout.LayoutParams(-1,dp(52)));gap(10);
            button("Guardar nick",false,new View.OnClickListener(){public void onClick(View v){saveNick();nickInput.setText(resultScores.find(sessionId).nick);((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nickInput.getWindowToken(),0);nickInput.clearFocus();Toast.makeText(ArcadeActivity.this,"Marca guardada",Toast.LENGTH_SHORT).show();}});
        }
        button("Volver a jugar",true,playClick());button("Ver mejores 10",false,new View.OnClickListener(){public void onClick(View v){showScores(true);}});button("Inicio",false,homeClick());button("Salir de la aplicación",false,exitClick());scroll();
    }
    private void showScores(final boolean fromResult){
        begin("scores");brand();title("Salón de\nla fama");body("Marcas separadas por modo y dificultad. En empates se conserva primero la marca anterior.");
        final int[] category={fromResult?game.mode:selectedMode,fromResult?game.level:selectedLevel};final LinearLayout rows=new LinearLayout(this);rows.setOrientation(1);
        picker("Modo",ArcadeEngine.MODES,category[0],new Selection(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){category[0]=n;renderRows(rows,category[0],category[1]);}});
        picker("Dificultad",ArcadeEngine.LEVELS,category[1],new Selection(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){category[1]=n;renderRows(rows,category[0],category[1]);}});
        screen.addView(rows);renderRows(rows,category[0],category[1]);gap(18);button(fromResult?"Volver al resultado":"Inicio",true,new View.OnClickListener(){public void onClick(View v){if(fromResult)showResult();else showHome();}});scroll();
    }
    private void renderRows(LinearLayout rows,int m,int l){
        rows.removeAllViews();ScoreTable t=table(key(m,l));if(t.entries().isEmpty()){rows.addView(text("Todavía no hay marcas en esta categoría.",15,MUTED));return;}
        int position=1;for(ScoreTable.Entry entry:t.entries()){
            LinearLayout row=new LinearLayout(this);row.setPadding(dp(10),dp(14),dp(10),dp(14));row.setGravity(Gravity.CENTER_VERTICAL);if(position%2==1)row.setBackgroundColor(CARD);
            row.addView(text(String.format(java.util.Locale.ROOT,"%02d",position++),15,MUTED),new LinearLayout.LayoutParams(dp(34),-2));TextView nick=text(entry.nick,17,TEXT);nick.setSingleLine(true);nick.setEllipsize(TextUtils.TruncateAt.END);row.addView(nick,new LinearLayout.LayoutParams(0,-2,1));row.addView(text(String.valueOf(entry.score),21,GREEN));rows.addView(row);
        }
    }
    private void showCustomize(){
        begin("customize");brand();title("A tu gusto.");final Hero preview=new Hero();screen.addView(preview,new LinearLayout.LayoutParams(-1,dp(195)));gap(18);
        body("Dos opciones gratuitas por categoría. Los candados tienen vista previa en la tienda. Todos los modos y dificultades siguen incluidos.");
        gatedPicker("Personaje",GameOptions.CHARACTERS,ContentAccess.CHARACTER,character,preview);
        gatedPicker("Escenario",GameOptions.MAPS,ContentAccess.MAP,map,preview);
        gatedPicker("Objeto que debes evitar",GameOptions.ITEMS,ContentAccess.ITEM,item,preview);
        if(access.available(ContentAccess.CHARACTER,8)){
            body("También puedes poner un apodo y una frase para el personaje de tu amigo. La frase aparece cuando se escapa.");
            friendField("Apodo de tu amigo",friendName,12,true);friendField("Frase al escapar",friendPhrase,28,false);
        }
        button("Ver paquetes y vistas previas",false,new View.OnClickListener(){public void onClick(View v){showStore();}});
        button("Guardar y volver",true,homeClick());scroll();
    }
    private String[] labeled(String[] names,int category){String[] labels=new String[names.length];for(int i=0;i<names.length;i++)labels[i]=names[i]+(access.available(category,i)?i<2?" · Gratis":" · Prueba desbloqueada":" · 🔒");return labels;}
    private void gatedPicker(String label,String[] names,final int category,int selected,final Hero preview){
        final Spinner[] holder=new Spinner[1];
        holder[0]=picker(label,labeled(names,category),selected,new Selection(){public void onItemSelected(AdapterView<?> parent,View view,int n,long id){
            if(selectionUpdate)return;
            if(!access.available(category,n)){
                int prior=category==0?character:category==1?map:item;selectionUpdate=true;holder[0].setSelection(prior);selectionUpdate=false;showLocked(category,n);return;
            }
            if(category==0)character=n;else if(category==1)map=n;else item=n;
            prefs.edit().putInt("character",character).putInt("map",map).putInt("item",item).apply();preview.invalidate();
        }});
    }
    private String contentName(int category,int index){return (category==0?GameOptions.CHARACTERS:category==1?GameOptions.MAPS:GameOptions.ITEMS)[index];}
    private void showLocked(final int category,final int index){
        new android.app.AlertDialog.Builder(this).setTitle(contentName(category,index)+" · Bloqueado")
            .setMessage("Puedes verlo en la tienda sin cambiar tu selección. Esta versión es una demostración y no realiza cobros.")
            .setPositiveButton("Ver vista previa",new android.content.DialogInterface.OnClickListener(){public void onClick(android.content.DialogInterface d,int which){showStorePreview(category,index);}})
            .setNegativeButton("Seguir con lo gratuito",null).show();
    }
    private void showStore(){
        begin("store");brand();title("La tienda de\nlos traviesos.");
        body("DEMOSTRACIÓN · SIN COBROS\nAquí puedes ver el contenido y probar desbloqueos locales. No se conecta con Google Play ni solicita dinero.");
        body("INCLUIDO GRATIS\nMapache y Conejo · Jardín y Granja · Bote de basura y Bota vieja. Los tres modos y las tres dificultades están incluidos.");
        for(int i=0;i<ContentAccess.PACKS.length;i++){
            final String pack=ContentAccess.PACKS[i];final int packIndex=i;
            TextView heading=text(ContentAccess.PACK_NAMES[i],22,TEXT);heading.setTypeface(Typeface.DEFAULT,1);screen.addView(heading);gap(10);body(ContentAccess.PACK_DETAILS[i]);
            if(i<3)button("Ver contenido y vistas previas",false,new View.OnClickListener(){public void onClick(View v){showCatalog(packIndex);}});
            else button("Explorar el catálogo completo",false,new View.OnClickListener(){public void onClick(View v){showCatalog(0);}});
            if(access.demoOwned(pack))body("DESBLOQUEADO DE PRUEBA · guardado solo en este teléfono");
            else button("Desbloqueo de prueba · sin cobro",true,new View.OnClickListener(){public void onClick(View v){confirmDemo(pack);}});
            gap(18);
        }
        body("Las pruebas se conservan al cerrar la app. No son compras ni se restauran desde una cuenta de Google. Puedes volver al catálogo gratuito con el botón de abajo.");
        button("Restablecer candados de prueba",false,new View.OnClickListener(){public void onClick(View v){resetDemo();}});button("Personalizar partida",false,new View.OnClickListener(){public void onClick(View v){showCustomize();}});button("Inicio",false,homeClick());scroll();
    }
    private void confirmDemo(final String pack){
        new android.app.AlertDialog.Builder(this).setTitle("Desbloqueo de prueba — no realiza cobros")
            .setMessage("Habilitará este paquete únicamente para probarlo en este teléfono. No se efectuará ninguna compra y no se utilizará una tarjeta.")
            .setPositiveButton("Desbloquear prueba",new android.content.DialogInterface.OnClickListener(){public void onClick(android.content.DialogInterface d,int which){access.unlockDemo(pack);saveAccess();Toast.makeText(ArcadeActivity.this,"Prueba desbloqueada · no se realizó ningún cobro",Toast.LENGTH_LONG).show();showStore();}})
            .setNegativeButton("Cancelar",null).show();
    }
    private void resetDemo(){
        new android.app.AlertDialog.Builder(this).setTitle("Volver al contenido gratuito")
            .setMessage("Se restablecerán los candados de demostración. Tus puntuaciones, nicks y preferencias de audio se conservarán.")
            .setPositiveButton("Restablecer prueba",new android.content.DialogInterface.OnClickListener(){public void onClick(android.content.DialogInterface d,int which){access.resetDemo();saveAccess();validateSelection();showStore();}})
            .setNegativeButton("Cancelar",null).show();
    }
    private void showCatalog(final int category){
        begin("catalog");brand();title(category==0?"La pandilla.":category==1?"Otros rincones.":"¡No los toques!");
        picker("Categoría",new String[]{"Personajes","Escenarios","Objetos"},category,new Selection(){public void onItemSelected(AdapterView<?> p,View v,int n,long id){if(n!=category)showCatalog(n);}});
        body("Toca una opción para ver cómo se dibuja. La vista previa no cambia tu personaje ni el escenario de la partida.");
        for(int i=0;i<ContentAccess.count(category);i++){
            final int index=i;String status=i<2?"Gratis":access.available(category,i)?"Desbloqueado de prueba":"🔒 Bloqueado";
            button(contentName(category,i)+" · "+status,false,new View.OnClickListener(){public void onClick(View v){showStorePreview(category,index);}});
        }
        button("Volver a la tienda",true,new View.OnClickListener(){public void onClick(View v){showStore();}});scroll();
    }
    private void showStorePreview(final int category,final int index){
        begin("preview");brand();title(contentName(category,index));
        int a=category==0?index:character,m=category==1?index:map,t=category==2?index:item;
        screen.addView(new StorePreview(a,m,t),new LinearLayout.LayoutParams(-1,dp(240)));gap(18);
        body("Solo vista previa: tu selección para jugar no se modifica.");
        if(access.available(category,index)){
            body(index<2?"INCLUIDO GRATIS":"DISPONIBLE COMO DESBLOQUEO DE PRUEBA");
            button("Usar esta opción",true,new View.OnClickListener(){public void onClick(View v){if(!access.available(category,index)){showLocked(category,index);return;}if(category==0)character=index;else if(category==1)map=index;else item=index;validateSelection();showCustomize();}});
        }else{
            body("CONTENIDO BLOQUEADO\nPuedes probar el paquete correspondiente sin realizar ningún pago.");
            button("Desbloqueo de prueba · sin cobro",true,new View.OnClickListener(){public void onClick(View v){confirmDemo(ContentAccess.packFor(category));}});
        }
        button("Ver más contenido",false,new View.OnClickListener(){public void onClick(View v){showCatalog(category);}});button("Volver a la tienda",false,new View.OnClickListener(){public void onClick(View v){showStore();}});scroll();
    }
    private void friendField(String label,String value,int max,final boolean name){
        screen.addView(text(label,14,TEXT));final EditText input=new EditText(this);input.setTextColor(TEXT);input.setSingleLine(true);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});input.setText(value);screen.addView(input,new LinearLayout.LayoutParams(-1,dp(52)));gap(14);
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){String v=s.toString().trim();if(name){friendName=v.isEmpty()?"Tu amigo":v;prefs.edit().putString("friend_name",friendName).apply();}else{friendPhrase=v.isEmpty()?"¡Me escapé!":v;prefs.edit().putString("friend_phrase",friendPhrase).apply();}}public void afterTextChanged(Editable s){}});
    }
    private void syncMusic(){if(music!=null)music.setActive(musicEnabled&&foreground&&!page.equals("paused")&&!page.equals("exiting"));}
    private void showAudio(final String back){
        audioBack=back;begin("audio");brand();title("Música y\npequeños sonidos.");
        audioStatus=text(music.status(),14,GREEN);screen.addView(audioStatus);gap(16);
        final Button musicToggle=makeButton(musicEnabled?"Silenciar música":"Activar música",false,new View.OnClickListener(){public void onClick(View v){musicEnabled=!musicEnabled;prefs.edit().putBoolean("music",musicEnabled).apply();syncMusic();((Button)v).setText(musicEnabled?"Silenciar música":"Activar música");}});screen.addView(musicToggle,new LinearLayout.LayoutParams(-1,dp(54)));gap(12);
        final SeekBar volume=new SeekBar(this);volume.setMax(100);volume.setProgress(prefs.getInt("music_volume",60));screen.addView(volume,new LinearLayout.LayoutParams(-1,dp(52)));
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int n,boolean user){if(user){prefs.edit().putInt("music_volume",n).apply();music.setVolume(n/100f);}}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        button("Probar música",true,new View.OnClickListener(){public void onClick(View v){musicEnabled=true;prefs.edit().putBoolean("music",true).apply();if(prefs.getInt("music_volume",60)==0){prefs.edit().putInt("music_volume",60).apply();volume.setProgress(60);music.setVolume(.6f);}musicToggle.setText("Silenciar música");music.retry();((android.media.AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,android.media.AudioManager.ADJUST_SAME,android.media.AudioManager.FLAG_SHOW_UI);}});
        final Button fxToggle=makeButton(effectsEnabled?"Efectos: activados":"Efectos: desactivados",false,new View.OnClickListener(){public void onClick(View v){effectsEnabled=!effectsEnabled;prefs.edit().putBoolean("effects",effectsEnabled).apply();effects.setEnabled(effectsEnabled);((Button)v).setText(effectsEnabled?"Efectos: activados":"Efectos: desactivados");}});screen.addView(fxToggle,new LinearLayout.LayoutParams(-1,dp(54)));gap(12);
        button("Probar efectos",false,new View.OnClickListener(){public void onClick(View v){effectsEnabled=true;prefs.edit().putBoolean("effects",true).apply();effects.setEnabled(true);fxToggle.setText("Efectos: activados");final Handler h=new Handler(Looper.getMainLooper());int[] events={ArcadeEngine.HIT,ArcadeEngine.HURT,ArcadeEngine.BONUS};for(int i=0;i<events.length;i++){final int e=events[i];h.postDelayed(new Runnable(){public void run(){if(foreground&&page.equals("audio"))effects.play(e,false);}},i*450);}}});
        body("Los aciertos, fallos, rachas, vidas extra y el final tienen sonidos distintos. Se pueden silenciar sin quitar la música. Usa también el volumen multimedia del teléfono.");
        button("Volver",false,new View.OnClickListener(){public void onClick(View v){if(back.equals("paused"))showPause();else showHome();}});scroll();
    }
    private void showHelp(){begin("help");brand();title("Así se juega.");body("Toca cualquier parte del personaje o su agujero: la hitbox conserva el tamaño máximo mientras sale o se esconde. Cada personaje suma un punto una sola vez.");body("Evita el objeto elegido. Las rachas se celebran a los 5, 10, 20… aciertos seguidos, pero no multiplican los puntos. Escapar o tocar una trampa rompe la racha.");body("En Clásico y Madrigueras empiezas con tres vidas y ganas una cada 20 puntos. En Contrarreloj juegas 60 segundos sin límite de fallos; no se usan vidas ni tiempo extra.");body("En Madrigueras hay seis agujeros y pueden salir dos personajes. Captura ambos antes de que se escondan. Cada uno tiene su barra de tiempo.");body("Elige Relajado, Normal o Difícil en Modo y dificultad. Consulta tus aciertos, escapes, trampas y mejor racha al terminar. Cada categoría guarda sus mejores diez nicks.");body("Los datos son locales: no se sincronizan ni sobreviven a desinstalar o borrar los datos de la app.");button("Entendido",true,homeClick());scroll();}
    private void exitGame(){saveNick();if(game.playing){game.end();saveResult();}page="exiting";foreground=false;music.setActive(false);effects.pause();finishAndRemoveTask();}
    @Override protected void onResume(){super.onResume();foreground=true;syncMusic();if(board!=null){board.last=0;board.invalidate();}}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused&&foreground)syncMusic();}
    @Override protected void onPause(){foreground=false;syncMusic();effects.pause();saveNick();if(game.playing)updateBest();if(page.equals("playing"))showPause();super.onPause();}
    @Override protected void onDestroy(){music.release();effects.release();super.onDestroy();}
    @Override public void onBackPressed(){if(page.equals("playing"))showPause();else if(page.equals("paused"))showGame();else if(page.equals("audio")&&audioBack.equals("paused"))showPause();else if(!page.equals("home"))showHome();else exitGame();}
    @Override protected void onSaveInstanceState(Bundle out){saveNick();super.onSaveInstanceState(out);out.putBoolean("session",game.playing);out.putByteArray("engine",game.snapshot());out.putString("sessionId",sessionId);}

    private final class Hero extends View {
        private final ArcadeArt art=new ArcadeArt();
        Hero(){super(ArcadeActivity.this);setContentDescription("Vista previa del personaje elegido");}
        @Override protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density;float w=getWidth()/d,h=getHeight()/d;long now=SystemClock.uptimeMillis();float a=(now%4200)/4200f;float v=Math.max(0,Math.min(1,Math.min(a/.15f,(1-a)/.15f)));v=v*v*(3-2*v);c.save();c.scale(d,d);art.background(c,w,h,map);art.item(c,w*.78f,h*.66f,25,now,false,item);art.character(c,w*.42f,h*.61f,49,v,now,character,a>.6f?1:0);c.restore();if(foreground&&(page.equals("home")||page.equals("customize")))postInvalidateDelayed(33);}
    }
    private final class StorePreview extends View {
        private final ArcadeArt art=new ArcadeArt();private final int a,m,t;
        StorePreview(int a,int m,int t){super(ArcadeActivity.this);this.a=a;this.m=m;this.t=t;setContentDescription("Vista previa de contenido del catálogo");}
        @Override protected void onDraw(Canvas c){float d=getResources().getDisplayMetrics().density,w=getWidth()/d,h=getHeight()/d;long now=SystemClock.uptimeMillis();float age=(now%4200)/4200f,v=Math.max(0,Math.min(1,Math.min(age/.15f,(1-age)/.15f)));v=v*v*(3-2*v);c.save();c.scale(d,d);art.background(c,w,h,m);art.item(c,w*.78f,h*.66f,25,now,false,t);art.character(c,w*.42f,h*.61f,49,v,now,a,age>.6f?1:0);c.restore();if(foreground&&page.equals("preview"))postInvalidateDelayed(33);}
    }
    private final class Board extends View {
        private final ArcadeArt art=new ArcadeArt();private final Paint p=new Paint(3);
        private final float density=getResources().getDisplayMetrics().density;
        private long last,effectEnd;private int effectEvent;private float effectX,effectY;private String effectText="";private boolean effectMilestone;
        Board(){super(ArcadeActivity.this);setContentDescription("Toca los personajes activos y evita la trampa");}
        @Override protected void onSizeChanged(int w,int h,int ow,int oh){game.setBounds(w/density,h/density);}
        private void event(int e){
            if(e==ArcadeEngine.NONE)return;
            updateBest();updateHud();effects.play(e,game.milestone);effectEvent=e;effectMilestone=game.milestone;effectX=game.eventX;effectY=game.eventY;effectEnd=SystemClock.uptimeMillis()+850;
            effectText=e==ArcadeEngine.BONUS?"+1 VIDA":game.milestone?"¡"+game.eventStreak+" SEGUIDOS!":e==ArcadeEngine.HIT?"+1":game.cause==1?(character==8?friendPhrase:"¡Me escapé!"):"¡Cuidado!";
            if(e==ArcadeEngine.FINISHED)post(new Runnable(){public void run(){if(page.equals("playing"))showResult();}});invalidate();
        }
        private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setAlpha(255);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);long now=SystemClock.uptimeMillis();if(foreground&&page.equals("playing")){if(last!=0)event(game.advance(now-last));last=now;updateHud();}
            c.save();c.scale(density,density);art.background(c,game.width,game.height,map);
            if(game.mode==ArcadeEngine.BURROWS)for(ArcadeEngine.Slot s:game.slots)art.hole(c,s.x,s.y,game.radius,character);
            for(ArcadeEngine.Slot s:game.slots)if(s.active){
                art.character(c,s.x,s.y,game.radius,s.visibility(),now,character,s.reaction);
                if(game.mode==ArcadeEngine.BURROWS&&!s.exiting){float half=game.radius*1.15f,barY=s.y+game.radius*1.55f;fill(0xffa8b991);c.drawRoundRect(s.x-half,barY,s.x+half,barY+4,2,2,p);fill(s.remaining<s.duration*.25f?RED:GREEN);c.drawRoundRect(s.x-half,barY,s.x-half+2*half*Math.max(0,(float)s.remaining/s.duration),barY+4,2,2,p);}
            }
            if(game.trap)art.item(c,game.trapX,game.trapY,game.radius,now,effectEvent==ArcadeEngine.HURT&&now<effectEnd,item);
            if(game.mode!=ArcadeEngine.BURROWS){float fraction=game.mode==ArcadeEngine.TIMED?game.timeLeft/60000f:Math.max(0,(float)game.slots[0].remaining/Math.max(1,game.slots[0].duration));fill(0xffafc6a1);c.drawRoundRect(18,12,game.width-18,18,3,3,p);fill(fraction<.25f?RED:GREEN);c.drawRoundRect(18,12,18+(game.width-36)*fraction,18,3,3,p);}
            if(now<effectEnd){
                float age=1-(effectEnd-now)/850f;boolean good=effectEvent==ArcadeEngine.HIT||effectEvent==ArcadeEngine.BONUS;
                if(good)art.leaves(c,effectX,effectY,game.radius,age);
                fill(good?GREEN:RED);p.setTextSize(effectMilestone?20:17);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);
                float x=Math.max(65,Math.min(game.width-65,effectX)),y=Math.max(44,Math.min(game.height-20,effectY-45-age*12));
                String caption=effectText;while(p.measureText(caption)>game.width-24&&caption.length()>1)caption=caption.substring(0,caption.length()-2)+"…";
                p.setColor(BG);p.setAlpha(235);c.drawRoundRect(x-p.measureText(caption)/2-9,y-22,x+p.measureText(caption)/2+9,y+7,9,9,p);p.setAlpha(255);p.setColor(good?GREEN:RED);c.drawText(caption,x,y,p);
            }
            c.restore();if(foreground&&page.equals("playing")&&game.playing)postInvalidateOnAnimation();
        }
        @Override public boolean onTouchEvent(MotionEvent e){if(!foreground||!page.equals("playing"))return false;if(e.getActionMasked()==MotionEvent.ACTION_DOWN||e.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN){long now=SystemClock.uptimeMillis();if(last!=0)event(game.advance(now-last));last=now;if(!game.playing)return true;int index=e.getActionIndex();event(game.tap(e.getX(index)/density,e.getY(index)/density));performClick();}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
