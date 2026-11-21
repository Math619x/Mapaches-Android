package com.rey.pulso;

import android.graphics.*;

/** Small original illustrations drawn locally; no downloads or image textures. */
public final class GameArt {
    private final Paint p=new Paint(3);
    private final Path path=new Path();
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){fill(color);c.drawOval(l,t,r,b,p);}
    private void line(Canvas c,float x,float y,float x2,float y2,int color,float stroke){fill(color);p.setStrokeWidth(stroke);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,x2,y2,p);}
    private void ellipse(Canvas c,float x,float y,float rx,float ry,int color){oval(c,x-rx,y-ry,x+rx,y+ry,color);}
    public void garden(Canvas c,float w,float h){
        c.save();path.reset();path.addRoundRect(0,0,w,h,24,24,Path.Direction.CW);c.clipPath(path);
        fill(0xffc2dfa0);c.drawRect(0,0,w,h,p);
        fill(0xffdff0df);c.drawRect(0,0,w,h*.22f,p);
        ellipse(c,w*.8f,33,20,20,0xffffe9a0);
        for(int j=0;j<3;j++){float x=w*(.12f+j*.33f);ellipse(c,x,22+j*8,24,8,0xfff9fff1);ellipse(c,x-7,17+j*8,12,10,0xfff9fff1);}
        float fenceY=Math.min(100,h*.21f);
        for(float x=-10;x<w+20;x+=24){fill(0xffd7ba87);c.drawRoundRect(x,fenceY-23,x+17,fenceY+33,5,5,p);line(c,x+3,fenceY-15,x+3,fenceY+26,0xffe9cfa0,2);}
        fill(0xffb68e61);c.drawRoundRect(0,fenceY+7,w,fenceY+14,2,2,p);
        for(int j=0;j<7;j++){float x=j*w/6f;ellipse(c,x,fenceY+37,38,17,j%2==0?0xff90b872:0xffa2c480);}
        ellipse(c,w*.18f,h*.68f,w*.35f,h*.18f,0xffcce5aa);
        ellipse(c,w*.94f,h*.43f,w*.31f,h*.16f,0xffb8d68b);
        for(int j=0;j<24;j++){
            float x=13+(j*67.3f)%Math.max(1,w-26),y=fenceY+62+(j*47.1f)%Math.max(1,h-fenceY-78);
            line(c,x-3,y+3,x-5,y-2,0xff99ba75,1.4f);line(c,x,y+3,x+2,y-3,0xff99ba75,1.4f);
        }
        flower(c,20,h-25,0xfffff5c7);flower(c,w-24,h-40,0xffffd5b4);flower(c,w-43,h-23,0xfff9fff0);
        flower(c,35,fenceY+63,0xffffd5b4);flower(c,w-27,fenceY+75,0xfff9fff0);
        c.restore();
    }
    private void flower(Canvas c,float x,float y,int color){
        line(c,x,y+10,x,y,0xff709857,2);
        for(int i=0;i<5;i++){double a=i*Math.PI*2/5;ellipse(c,x+(float)Math.cos(a)*3,y+(float)Math.sin(a)*3,2.5f,2.5f,color);}
        ellipse(c,x,y,2,2,0xffe4ab55);
    }
    public void raccoon(Canvas c,float x,float y,float radius,float visibility,long time){
        c.save();c.translate(x,y);float s=radius/36;c.scale(s,s);
        // The foreground lip and clipping make the character physically descend into its hole.
        ellipse(c,0,34,49,15,0xffa2b978);ellipse(c,0,31,45,14,0xff9c754f);ellipse(c,0,29,40,10,0xff443f33);
        c.save();c.clipRect(-53,-60,53,30);
        c.translate(0,(1-visibility)*78);
        // Striped tail peeking from behind the body.
        c.save();c.rotate(-30,23,18);oval(c,12,-2,37,46,0xff89948e);
        for(int j=0;j<3;j++)oval(c,13,7+j*12,37,13+j*12,0xff43514e);c.restore();
        ellipse(c,0,25,25,27,0xff7f8e86);ellipse(c,0,24,16,22,0xffc2cbb6);
        // Ears and head outline.
        ellipse(c,-23,-23,12,15,0xff4a5b53);ellipse(c,23,-23,12,15,0xff4a5b53);
        ellipse(c,-23,-23,7,10,0xffd5bfaa);ellipse(c,23,-23,7,10,0xffd5bfaa);
        ellipse(c,0,-1,36,31,0xff4a5b53);ellipse(c,0,-3,33,28,0xffaeb9a7);
        ellipse(c,-24,8,12,15,0xffdce3cf);ellipse(c,24,8,12,15,0xffdce3cf);
        // Distinctive raccoon mask.
        c.save();c.rotate(12,-14,-4);ellipse(c,-14,-4,17,12,0xff344a43);c.restore();
        c.save();c.rotate(-12,14,-4);ellipse(c,14,-4,17,12,0xff344a43);c.restore();
        boolean blink=time%3100>2940;
        if(blink){line(c,-20,-3,-10,-3,0xffedf3df,2);line(c,10,-3,20,-3,0xffedf3df,2);}
        else{
            ellipse(c,-14,-5,6.5f,7.5f,0xfff7ffe9);ellipse(c,14,-5,6.5f,7.5f,0xfff7ffe9);
            ellipse(c,-12,-4,3.8f,5,0xff263c35);ellipse(c,12,-4,3.8f,5,0xff263c35);
            ellipse(c,-13,-6,1.5f,1.8f,0xffffffff);ellipse(c,11,-6,1.5f,1.8f,0xffffffff);
        }
        ellipse(c,0,12,16,12,0xfff1edda);
        fill(0xff2d433a);path.reset();path.moveTo(-7,5);path.quadTo(0,1,7,5);path.quadTo(6,12,0,12);path.quadTo(-6,12,-7,5);c.drawPath(path,p);
        line(c,0,11,0,15,0xff435447,1.5f);line(c,-4,17,0,15,0xff435447,1.5f);line(c,0,15,4,17,0xff435447,1.5f);
        ellipse(c,-26,10,5,2.5f,0xffdebba7);ellipse(c,26,10,5,2.5f,0xffdebba7);
        // Little paws hook over the rim as the animal peeks out.
        ellipse(c,-23,27,9,6,0xff576b5d);ellipse(c,23,27,9,6,0xff576b5d);
        for(int j=0;j<3;j++){line(c,-27+j*3,27,-27+j*3,30,0xffb7c4ac,1);line(c,19+j*3,27,19+j*3,30,0xffb7c4ac,1);}
        c.restore();
        fill(0xffab865a);path.reset();path.moveTo(-44,31);path.quadTo(0,49,44,31);path.lineTo(44,37);path.quadTo(0,56,-44,37);path.close();c.drawPath(path,p);
        line(c,-38,37,-42,30,0xff71964e,3);line(c,-36,38,-34,31,0xff71964e,2);line(c,34,38,37,30,0xff71964e,3);
        c.restore();
    }
    public void bin(Canvas c,float x,float y,float radius,long time,boolean shake){
        c.save();c.translate(x,y);float s=radius/36;c.scale(s,s);if(shake)c.rotate((float)Math.sin(time*.08)*8);
        ellipse(c,0,34,34,8,0xff9eb47c);
        // Rusty red bin contrasts with the grey raccoon, without an abstract X target.
        fill(0xff735e4b);c.drawRoundRect(-26,-25,26,30,5,5,p);
        fill(0xffd99373);path.reset();path.moveTo(-23,-22);path.lineTo(23,-22);path.lineTo(20,29);path.quadTo(0,36,-20,29);path.close();c.drawPath(path,p);
        for(int j=-1;j<=1;j++)line(c,j*12,-13,j*10,23,0xffb9735e,3);
        fill(0xffb87860);c.drawRoundRect(-31,-29,31,-19,4,4,p);line(c,-28,-28,28,-28,0xfff3b391,2);
        fill(0xff735e4b);c.drawRoundRect(-11,-37,11,-28,4,4,p);fill(0xffe6b18a);c.drawRoundRect(-7,-34,7,-29,2,2,p);
        // A tiny painted leaf, to make it look like a garden bin.
        c.save();c.rotate(-28);ellipse(c,0,7,6,10,0xfff6dfb5);line(c,0,3,0,15,0xffb97961,1);c.restore();
        c.restore();
    }
}
