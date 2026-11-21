package com.rey.pulso;

import java.util.Random;
import java.io.*;

/** Complete arcade session. No Android dependency, so all rules can run on the JVM. */
public final class ArcadeEngine {
    public static final int CLASSIC=0,TIMED=1,BURROWS=2;
    public static final int NONE=0,HIT=1,HURT=2,BONUS=3,FINISHED=4;
    public static final String[] MODES={"Clásico","Contrarreloj","Madrigueras"};
    public static final String[] LEVELS={"Relajado","Normal","Difícil"};
    public static final class Slot {
        public float x,y,exitFrom;
        public boolean active,exiting;
        public long remaining,duration,exitRemaining;
        public int reaction;
        public float visibility(){
            if(!active)return 0;
            if(exiting)return exitFrom*smooth(Math.max(0,exitRemaining)/180f);
            float t=Math.min(180,duration*.24f);
            return smooth(Math.min((duration-remaining)/t,remaining/t));
        }
        private static float smooth(float t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    }
    public final Slot[] slots=new Slot[6];
    public float width=340,height=450,radius=34,trapX,trapY,eventX,eventY;
    public int mode=CLASSIC,level=1,score,lives=3,hits,escaped,trapsTouched,emptyTaps,streak,bestStreak,bonusLives,shown,wave,eventStreak,cause;
    public boolean playing,trap,milestone;
    public long timeLeft=60000,playedMs;
    public String finishReason="";
    private final Random random;
    public ArcadeEngine(Random random){this.random=random;for(int i=0;i<slots.length;i++)slots[i]=new Slot();}
    public void start(int mode,int level){
        this.mode=GameOptions.valid(mode,MODES.length);this.level=GameOptions.valid(level,LEVELS.length);
        score=hits=escaped=trapsTouched=emptyTaps=streak=bestStreak=bonusLives=shown=wave=eventStreak=0;
        lives=3;timeLeft=60000;playedMs=0;playing=true;milestone=false;finishReason="";cause=0;spawn();
    }
    public String key(){return "arcade_"+mode+"_"+level;}
    public void setBounds(float width,float height){
        float oldW=this.width,oldH=this.height;this.width=Math.max(1,width);this.height=Math.max(1,height);
        radius=mode==BURROWS?Math.min(34,Math.min(this.width*.12f,this.height*.075f)):Math.min(34,Math.min(this.width,this.height)*.12f);
        if(mode==BURROWS){trapX=trapX/oldW*this.width;trapY=trapY/oldH*this.height;layoutHoles();for(int i=0;i<slots.length;i++){if(slots[i].active&&overlaps(slots[i],trapX,trapY))trap=false;}}
        else if(playing){for(Slot s:slots){s.x=clamp(s.x/oldW*this.width,radius*1.45f+6,this.width-radius*1.45f-6);s.y=clamp(s.y/oldH*this.height,radius*1.45f+28,this.height-radius*1.45f-6);}trapX=trapX/oldW*this.width;trapY=trapY/oldH*this.height;if(trap&&overlaps(slots[0],trapX,trapY))trap=false;}
    }
    private float clamp(float x,float lo,float hi){return hi<lo?(lo+hi)/2:Math.max(lo,Math.min(hi,x));}
    public int advance(long dt){
        if(!playing||dt<=0)return NONE;
        milestone=false;long step=mode==TIMED?Math.min(dt,timeLeft):dt;playedMs+=step;
        if(mode==TIMED){timeLeft-=step;if(timeLeft<=0){timeLeft=0;playing=false;finishReason="Se acabó el tiempo";cause=4;return FINISHED;}}
        int event=NONE;boolean active=false;
        for(Slot s:slots){
            if(!s.active)continue;
            if(s.exiting){s.exitRemaining-=step;if(s.exitRemaining<=0)s.active=false;}
            else{
                s.remaining-=step;
                if(s.remaining<=0){escaped++;streak=0;eventStreak=0;cause=1;eventX=s.x;eventY=s.y;s.reaction=2;exit(s,0);
                    if(mode!=TIMED){lives--;if(lives<=0){lives=0;playing=false;finishReason="Sin vidas";return FINISHED;}}
                    event=HURT;
                }
            }
            active|=s.active;
        }
        if(!active)spawn();
        return event;
    }
    public int tap(float x,float y){
        if(!playing)return NONE;
        milestone=false;
        for(Slot s:slots){
            if(s.active&&!s.exiting&&contains(s,x,y)){
                score++;hits++;streak++;bestStreak=Math.max(bestStreak,streak);eventStreak=streak;cause=3;
                milestone=streak==5||streak%10==0;eventX=s.x;eventY=s.y;s.reaction=1;exit(s,s.visibility());
                boolean bonus=score%20==0&&mode!=TIMED;if(bonus){lives++;bonusLives++;}return bonus?BONUS:HIT;
            }
        }
        // A captured target is still visible during its exit; ignore repeat taps.
        for(Slot s:slots)if(s.active&&s.exiting&&contains(s,x,y))return NONE;
        if(trap&&Math.hypot(x-trapX,y-trapY)<=radius){
            trapsTouched++;streak=0;eventStreak=0;trap=false;eventX=trapX;eventY=trapY;cause=2;
            if(mode!=TIMED){lives--;if(lives<=0){lives=0;playing=false;finishReason="Sin vidas";return FINISHED;}}
            return HURT;
        }
        emptyTaps++;return NONE;
    }
    public boolean contains(Slot s,float x,float y){return x>=s.x-radius*49/36&&x<=s.x+radius*49/36&&y>=s.y-radius*38/36&&y<=s.y+radius*52/36;}
    private boolean overlaps(Slot s,float x,float y){return !(s.x+radius*49/36+12<x-radius||x+radius+12<s.x-radius*49/36||s.y+radius*52/36+12<y-radius*1.1f||y+radius*1.2f+12<s.y-radius*38/36);}
    private void exit(Slot s,float from){s.exiting=true;s.exitRemaining=180;s.exitFrom=from;}
    public int liveTargets(){int count=0;for(Slot s:slots)if(s.active&&!s.exiting)count++;return count;}
    private void layoutHoles(){
        for(int i=0;i<6;i++){slots[i].x=width*(i%2==0?.26f:.74f);slots[i].y=height*(.20f+(i/2)*.30f);}
    }
    private void spawn(){
        wave++;for(Slot s:slots){s.active=false;s.exiting=false;s.reaction=0;}
        long duration=level==0?Math.max(900,1900-score*9L):level==2?Math.max(400,1100-score*14L):Math.max(550,1450-score*12L);
        int first=0,second=-1;
        if(mode==BURROWS){
            layoutHoles();first=random.nextInt(6);int threshold=level==0?20:level==2?5:10;
            if(score>=threshold&&random.nextFloat()<(level==0?.35f:level==2?.75f:.55f)){second=(first+1+random.nextInt(5))%6;duration+=180;}
        }else{
            float margin=radius*1.45f+6,top=Math.min(24,height*.08f);slots[0].x=position(width,margin);slots[0].y=top+position(height-top,margin);
        }
        activate(slots[first],duration);if(second>=0)activate(slots[second],duration+120);
        trap=score>=5&&random.nextFloat()<(level==0?.35f:level==2?.75f:.60f);
        if(trap){
            boolean placed=false;
            for(int attempt=0;attempt<50;attempt++){
                if(mode==BURROWS){int slot=random.nextInt(6);trapX=slots[slot].x;trapY=slots[slot].y;}
                else{float margin=radius*1.45f+6,top=Math.min(24,height*.08f);trapX=position(width,margin);trapY=top+position(height-top,margin);}
                boolean clear=true;for(Slot s:slots)if(s.active&&overlaps(s,trapX,trapY))clear=false;
                if(clear){placed=true;break;}
            }
            trap=placed;
        }
    }
    private float position(float size,float margin){return size<=margin*2?size/2:margin+random.nextFloat()*(size-margin*2);}
    private void activate(Slot s,long duration){s.active=true;s.duration=duration;s.remaining=duration;shown++;}
    public void end(){playing=false;finishReason="Partida terminada";}
    /** Persist exact timers, animations, holes and stats. Does not count background time. */
    public byte[] snapshot(){
        try{ByteArrayOutputStream b=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(b);
            out.writeInt(1);out.writeInt(mode);out.writeInt(level);out.writeFloat(width);out.writeFloat(height);out.writeFloat(radius);
            for(int v:new int[]{score,lives,hits,escaped,trapsTouched,emptyTaps,streak,bestStreak,bonusLives,shown,wave,eventStreak})out.writeInt(v);
            out.writeLong(timeLeft);out.writeLong(playedMs);out.writeBoolean(playing);out.writeBoolean(trap);out.writeFloat(trapX);out.writeFloat(trapY);
            for(Slot s:slots){out.writeFloat(s.x);out.writeFloat(s.y);out.writeFloat(s.exitFrom);out.writeBoolean(s.active);out.writeBoolean(s.exiting);out.writeLong(s.remaining);out.writeLong(s.duration);out.writeLong(s.exitRemaining);out.writeInt(s.reaction);}
            out.flush();return b.toByteArray();
        }catch(IOException impossible){throw new AssertionError(impossible);}
    }
    public boolean restore(byte[] bytes){
        if(bytes==null)return false;
        try{DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));if(in.readInt()!=1)return false;
            mode=in.readInt();level=in.readInt();width=in.readFloat();height=in.readFloat();radius=in.readFloat();
            score=in.readInt();lives=in.readInt();hits=in.readInt();escaped=in.readInt();trapsTouched=in.readInt();emptyTaps=in.readInt();streak=in.readInt();bestStreak=in.readInt();bonusLives=in.readInt();shown=in.readInt();wave=in.readInt();eventStreak=in.readInt();
            timeLeft=in.readLong();playedMs=in.readLong();playing=in.readBoolean();trap=in.readBoolean();trapX=in.readFloat();trapY=in.readFloat();
            for(Slot s:slots){s.x=in.readFloat();s.y=in.readFloat();s.exitFrom=in.readFloat();s.active=in.readBoolean();s.exiting=in.readBoolean();s.remaining=in.readLong();s.duration=in.readLong();s.exitRemaining=in.readLong();s.reaction=in.readInt();}
            return mode>=0&&mode<3&&level>=0&&level<3&&width>0&&height>0&&radius>0&&score>=0&&lives>=0&&timeLeft>=0;
        }catch(Exception malformed){return false;}
    }
}
