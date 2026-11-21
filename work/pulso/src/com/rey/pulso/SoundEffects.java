package com.rey.pulso;
import android.content.Context;
import android.media.*;
import java.util.HashSet;

public final class SoundEffects {
    private final SoundPool pool;
    private final java.util.Set<Integer> ready=new HashSet<Integer>();
    private final int hit,hurt,bonus,streak,finish;
    private boolean enabled=true,released;
    public SoundEffects(Context context){
        AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build();
        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){public void onLoadComplete(SoundPool p,int id,int status){if(status==0)ready.add(id);}});
        hit=load(context,"sfx_hit");hurt=load(context,"sfx_hurt");bonus=load(context,"sfx_bonus");streak=load(context,"sfx_streak");finish=load(context,"sfx_finish");
    }
    private int load(Context c,String name){int id=c.getResources().getIdentifier(name,"raw",c.getPackageName());return id==0?0:pool.load(c,id,1);}
    public void setEnabled(boolean value){enabled=value;if(!value)pause();}
    public boolean play(int event,boolean milestone){
        if(!enabled||released)return false;int id=event==ArcadeEngine.FINISHED?finish:event==ArcadeEngine.BONUS?bonus:event==ArcadeEngine.HURT?hurt:milestone?streak:hit;
        return ready.contains(id)&&pool.play(id,.6f,.6f,1,0,1)>0;
    }
    public void pause(){if(!released)pool.autoPause();}
    public void release(){released=true;pool.release();ready.clear();}
}
