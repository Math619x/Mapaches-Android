package com.rey.pulso;
import android.graphics.*;

public final class ArcadeArt {
    private final ThemeArt theme=new ThemeArt();
    private final Paint p=new Paint(3);private final Path path=new Path();
    public void background(Canvas c,float w,float h,int map){theme.background(c,w,h,map);}
    public void item(Canvas c,float x,float y,float r,long time,boolean shake,int type){theme.item(c,x,y,r,time,shake,type);}
    public void hole(Canvas c,float x,float y,float r,int type){theme.character(c,x,y,r,0,0,type);}
    public void character(Canvas c,float x,float y,float r,float visibility,long time,int type,int reaction){
        theme.character(c,x,y,r,visibility,time,type);
        if(reaction!=1||visibility<.12f)return;
        c.save();c.translate(x,y);c.scale(r/36,r/36);c.clipRect(-53,-60,53,30);c.translate(0,(1-visibility)*(type==0?78:82));
        p.setStyle(Paint.Style.FILL);p.setColor(0xff725145);c.drawOval(-10,12,10,23,p);p.setColor(0xfffff2d6);c.drawOval(-8,12,8,16,p);
        float eyeY=type==6?-21:type==8?-3:type==0?-5:-6;
        float eyeX=type==6?19:type==0?14:type==8?11:12;
        // A little crescent wink, contained inside the existing eye/lens.
        p.setColor(type==0?0xff344a43:type==8?0xffe1bea0:0xfff2e7ce);c.drawOval(eyeX-5,eyeY-5,eyeX+5,eyeY+5,p);
        p.setColor(0xff344a3c);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);path.reset();path.moveTo(eyeX-4,eyeY+1);path.quadTo(eyeX,eyeY-3,eyeX+4,eyeY+1);c.drawPath(path,p);c.restore();
    }
    public void leaves(Canvas c,float x,float y,float radius,float progress){
        for(int i=0;i<7;i++){
            double angle=i*Math.PI*2/7;float distance=radius*.35f+progress*radius*1.65f;
            float px=x+(float)Math.cos(angle)*distance,py=y+(float)Math.sin(angle)*distance-progress*radius*.45f;
            c.save();c.translate(px,py);c.rotate(i*37+progress*140);p.setStyle(Paint.Style.FILL);p.setColor(i%2==0?0xff7b9c59:0xffdfb46c);p.setAlpha((int)(255*(1-progress)));c.drawOval(-3,-6,3,6,p);p.setAlpha(255);c.restore();
        }
    }
}
