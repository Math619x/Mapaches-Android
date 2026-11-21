package com.rey.pulso;

import java.util.Random;

/** Game rules independent of Android. Coordinates are density-independent pixels. */
public final class GameEngine {
    public static final int NONE=0, HIT=1, HURT=2, BONUS=3, FINISHED=4;
    public int score, lives;
    public float width=320, height=400, x, y, trapX, trapY, radius=30;
    public boolean trap, playing, exiting;
    public long exitRemainingMs;
    private float exitFrom;
    public long remainingMs, durationMs;
    private final Random random;
    public GameEngine(Random random){this.random=random;}
    public void start(){score=0;lives=3;playing=true;spawn();}
    public void restore(int score,int lives){this.score=Math.max(0,score);this.lives=Math.max(0,lives);playing=this.lives>0;spawn();}
    public void setBounds(float width,float height){
        this.width=Math.max(1,width);this.height=Math.max(1,height);
        radius=Math.min(34,Math.min(this.width,this.height)*.12f);
        // Preserve the wave timer when a window changes size.
        long saved=remainingMs;spawn();remainingMs=saved>0?Math.min(saved,durationMs):durationMs;
    }
    public int advance(long elapsedMs){
        if(!playing||elapsedMs<=0)return NONE;
        if(exiting){exitRemainingMs-=elapsedMs;if(exitRemainingMs<=0)spawn();return NONE;}
        remainingMs-=elapsedMs;
        if(remainingMs<=0)return loseLife();
        return NONE;
    }
    public int tap(float px,float py){
        if(!playing||exiting)return NONE;
        // Fixed maximum silhouette: ears, body, paws, and the entire hole.
        // It remains tappable throughout emergence and the natural retreat.
        if(px>=hitLeft()&&px<=hitRight()&&py>=hitTop()&&py<=hitBottom()){
            score++;boolean bonus=score%20==0;if(bonus)lives++;
            beginExit();return bonus?BONUS:HIT;
        }
        if(trap&&distance2(px,py,trapX,trapY)<=radius*radius)return loseLife();
        return NONE;
    }
    public float hitLeft(){return x-radius*49/36;}
    public float hitRight(){return x+radius*49/36;}
    public float hitTop(){return y-radius*38/36;}
    public float hitBottom(){return y+radius*52/36;}
    private int loseLife(){
        lives--;if(lives<=0){lives=0;playing=false;return FINISHED;}
        beginExit();return HURT;
    }
    private void beginExit(){exitFrom=visibility();exiting=true;exitRemainingMs=160;}
    public float visibility(){
        if(exiting)return exitFrom*smooth(Math.max(0,exitRemainingMs)/160f);
        float transition=Math.min(180,durationMs*.24f);
        return smooth(Math.min((durationMs-remainingMs)/transition,remainingMs/transition));
    }
    private float smooth(float t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    private void spawn(){
        durationMs=Math.max(550,1450-score*12L);remainingMs=durationMs;
        exiting=false;exitRemainingMs=0;
        float margin=radius*1.45f+6;
        float topSpace=Math.min(24,height*.08f);
        x=position(width,margin);y=topSpace+position(height-topSpace,margin);
        trap=score>=5&&random.nextFloat()<.60f;
        if(trap){
            boolean placed=false;
            for(int i=0;i<40;i++){
                trapX=position(width,margin);trapY=topSpace+position(height-topSpace,margin);
                float binLeft=trapX-radius,binRight=trapX+radius;
                float binTop=trapY-radius*1.1f,binBottom=trapY+radius*1.2f;
                if(hitRight()+12<binLeft||binRight+12<hitLeft()||hitBottom()+12<binTop||binBottom+12<hitTop()){placed=true;break;}
            }
            trap=placed;
        }
    }
    private float position(float size,float margin){return size<=margin*2?size/2:margin+random.nextFloat()*(size-margin*2);}
    private float distance2(float a,float b,float c,float d){float dx=a-c,dy=b-d;return dx*dx+dy*dy;}
}
