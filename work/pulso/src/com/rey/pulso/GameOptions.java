package com.rey.pulso;

public final class GameOptions {
    private GameOptions(){}
    public static final String[] CHARACTERS={"Mapache","Conejo","Castor","Zorro","Gato","Oso","Rana","Topo","Tu amigo"};
    public static final String[] MAPS={"Jardín","Granja","Bosque","Playa","Nieve"};
    public static final String[] ITEMS={"Bote de basura","Bota vieja","Cactus","Roca","Cono de tránsito"};
    public static int valid(int value,int length){return value>=0&&value<length?value:0;}
}
