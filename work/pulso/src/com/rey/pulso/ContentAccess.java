package com.rey.pulso;

import java.util.*;

/** Demo entitlements are intentionally isolated from any future real purchases. */
public final class ContentAccess {
    public static final int CHARACTER=0,MAP=1,ITEM=2;
    public static final String ACTORS="demo_characters",MAPS="demo_maps",ITEMS="demo_items",ALL="demo_complete";
    public static final String[] PACKS={ACTORS,MAPS,ITEMS,ALL};
    public static final String[] PACK_NAMES={"Más personajes","Nuevos escenarios","Objetos traviesos","Colección completa"};
    public static final String[] PACK_DETAILS={
        "Castor, Zorro, Gato, Oso, Rana, Topo y Tu amigo. Siete personajes adicionales.",
        "Bosque, Playa y Nieve. Tres escenarios adicionales.",
        "Cactus, Roca y Cono de tránsito. Tres objetos adicionales para evitar.",
        "Todos los personajes, escenarios y objetos adicionales del catálogo actual. No incluye contenido futuro."};
    private final boolean demoAllowed;
    private final Set<String> demo=new HashSet<String>();
    public ContentAccess(boolean demoAllowed){this.demoAllowed=demoAllowed;}
    public static int count(int category){return category==CHARACTER?9:category==MAP||category==ITEM?5:0;}
    public static String packFor(int category){return category==CHARACTER?ACTORS:category==MAP?MAPS:category==ITEM?ITEMS:"";}
    private static boolean known(String id){for(String pack:PACKS)if(pack.equals(id))return true;return false;}
    public boolean demoOwned(String pack){
        if(!demoAllowed||!known(pack))return false;
        if(demo.contains(ALL)||demo.contains(pack))return true;
        return ALL.equals(pack)&&demo.contains(ACTORS)&&demo.contains(MAPS)&&demo.contains(ITEMS);
    }
    public boolean available(int category,int index){
        if(index<0||index>=count(category))return false;
        return index<2||demoOwned(packFor(category));
    }
    public int safeSelection(int category,int index){return available(category,index)?index:0;}
    public int availableCount(int category){int n=0;for(int i=0;i<count(category);i++)if(available(category,i))n++;return n;}
    public boolean unlockDemo(String pack){if(!demoAllowed||!known(pack)||demoOwned(pack))return false;demo.add(pack);return true;}
    public void resetDemo(){demo.clear();}
    public String encodeDemo(){ArrayList<String> ids=new ArrayList<String>(demo);Collections.sort(ids);StringBuilder out=new StringBuilder();for(String id:ids){if(out.length()>0)out.append(',');out.append(id);}return out.toString();}
    public static ContentAccess decodeDemo(boolean enabled,String data){
        ContentAccess access=new ContentAccess(enabled);if(data!=null)for(String id:data.split(","))if(known(id))access.demo.add(id);return access;
    }
}
