package com.rey.pulso;
import android.graphics.*;

/** Original code-drawn characters, including a friendly portrait from the user's reference. */
public final class ThemeArt {
    private final GameArt original=new GameArt();
    private final Paint p=new Paint(3);private final Path path=new Path();
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private void rect(Canvas c,float x,float y,float xx,float yy,int color){fill(color);c.drawRect(x,y,xx,yy,p);}
    private void oval(Canvas c,float x,float y,float rx,float ry,int color){fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float sw){fill(color);p.setStrokeWidth(sw);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);}
    private void triangle(Canvas c,float x,float y,float xx,float yy,float xxx,float yyy,int color){fill(color);path.reset();path.moveTo(x,y);path.lineTo(xx,yy);path.lineTo(xxx,yyy);path.close();c.drawPath(path,p);}
    public void background(Canvas c,float w,float h,int map){
        if(map==0){original.garden(c,w,h);return;}
        c.save();path.reset();path.addRoundRect(0,0,w,h,24,24,Path.Direction.CW);c.clipPath(path);
        int sky=map==4?0xffd9e8ed:map==3?0xffbde5e9:map==2?0xffdbe6c9:0xffffe9bb;
        int ground=map==4?0xffeff4ec:map==3?0xfff1da9d:map==2?0xffb3c48b:0xffd9d48d;
        rect(c,0,0,w,h,ground);rect(c,0,0,w,h*.26f,sky);oval(c,w*.81f,30,18,18,map==4?0xfffbf5dd:0xffffd17e);
        for(int j=0;j<3;j++){float x=w*(.10f+j*.34f);oval(c,x,30+j*7,27,8,0xfffdfbf0);oval(c,x+5,23+j*7,14,10,0xfffdfbf0);}
        float horizon=Math.min(h*.29f,125);
        if(map==1){
            // Barn, silo and crop rows.
            float bx=w*.13f,by=horizon-7;
            rect(c,bx,by-40,bx+75,by+25,0xffc47d65);triangle(c,bx-9,by-40,bx+37,by-70,bx+84,by-40,0xff8c6855);
            rect(c,bx+25,by-10,bx+51,by+25,0xfff3d8b4);line(c,bx+25,by-10,bx+51,by+25,0xffa7765d,2);line(c,bx+51,by-10,bx+25,by+25,0xffa7765d,2);
            rect(c,w*.71f,by-32,w*.71f+35,by+25,0xffb4b6a0);oval(c,w*.71f+17.5f,by-32,17.5f,10,0xffa1a891);
            for(int i=0;i<5;i++){float yy=horizon+42+i*35;line(c,15,yy,w-15,yy+8,0xffbdc17a,6);for(int j=0;j<8;j++){float xx=25+j*(w-50)/7;line(c,xx,yy,xx-3,yy-8,0xff88a363,2);line(c,xx,yy,xx+5,yy-5,0xff88a363,2);}}
        }else if(map==2||map==4){
            for(int j=0;j<7;j++){float xx=j*w/6f;tree(c,xx,horizon+17,0.8f+(j%3)*.12f,map==4);}
            if(map==2){for(int j=0;j<8;j++){float xx=18+(j*63)%Math.max(1,(int)(w-30)),yy=horizon+55+(j*47)%Math.max(1,(int)(h-horizon-80));rect(c,xx-2,yy,xx+2,yy+8,0xffe7d6b2);oval(c,xx,yy,8,4,0xffc88870);oval(c,xx-3,yy-1,1,1,0xfff7ead0);}}
            else{oval(c,w*.83f,horizon+46,19,16,0xffd9e5e4);oval(c,w*.83f,horizon+25,13,13,0xfffafff6);oval(c,w*.83f-4,horizon+22,1.2f,1.2f,0xff526b66);oval(c,w*.83f+4,horizon+22,1.2f,1.2f,0xff526b66);triangle(c,w*.83f,horizon+27,w*.83f+10,horizon+29,w*.83f,horizon+31,0xffd89c69);}
        }else{
            rect(c,0,horizon-35,w,horizon+30,0xff8bcac9);for(int j=0;j<5;j++)line(c,j*w/5,horizon+15,j*w/5+30,horizon+11,0xffd8f0df,3);
            float px=w*.14f,py=horizon+35;line(c,px,py,px+10,py-69,0xffab8c5c,9);
            for(int j=0;j<5;j++){float a=(float)(j*Math.PI*.45);line(c,px+10,py-69,px+10+(float)Math.cos(a)*32,py-77+(float)Math.sin(a)*15,0xff729a68,9);}
            for(int j=0;j<7;j++){float xx=20+(j*59)%Math.max(1,(int)(w-30)),yy=horizon+68+(j*41)%Math.max(1,(int)(h-horizon-85));oval(c,xx,yy,5,3,j%2==0?0xffdfbca2:0xfff9e7c5);}
        }
        c.restore();
    }
    private void tree(Canvas c,float x,float y,float scale,boolean snow){
        c.save();c.translate(x,y);c.scale(scale,scale);rect(c,-4,-2,4,23,0xff9e8b68);
        triangle(c,-27,10,0,-64,27,10,snow?0xff9bbab0:0xff6e9569);triangle(c,-23,-10,0,-68,23,-10,snow?0xffb9d0c5:0xff82a477);
        if(snow){triangle(c,-13,-36,0,-68,13,-36,0xfff5f8ed);line(c,-19,-9,20,-9,0xffedf5e9,4);}
        c.restore();
    }
    public void character(Canvas c,float x,float y,float radius,float visible,long time,int type){
        if(type==0){original.raccoon(c,x,y,radius,visible,time);return;}
        c.save();c.translate(x,y);c.scale(radius/36,radius/36);
        oval(c,0,34,49,15,0xffa2b978);oval(c,0,31,45,14,0xff9c754f);oval(c,0,29,40,10,0xff443f33);
        c.save();c.clipRect(-53,-60,53,30);c.translate(0,(1-visible)*82);
        if(type==8)friend(c,time);else animal(c,type,time);
        c.restore();fill(0xffab865a);path.reset();path.moveTo(-44,31);path.quadTo(0,49,44,31);path.lineTo(44,37);path.quadTo(0,56,-44,37);path.close();c.drawPath(path,p);
        line(c,-38,37,-42,30,0xff71964e,3);line(c,34,38,37,30,0xff71964e,3);c.restore();
    }
    private void animal(Canvas c,int type,long time){
        int fur=type==1?0xffe7dfc8:type==2?0xffa87e59:type==3?0xffce8d58:type==4?0xffd3a16d:type==5?0xffb99773:type==6?0xff9bc17b:0xff9d8e82;
        int cream=type==6?0xffd9e5a9:0xfff2e7ce;
        if(type==2){c.save();c.rotate(-27,25,23);oval(c,25,23,14,26,0xff8b694d);for(int j=0;j<4;j++)line(c,15,8+j*10,35,8+j*10,0xff705743,1);for(int j=0;j<3;j++)line(c,19+j*6,0,19+j*6,45,0xff705743,1);c.restore();}
        oval(c,0,26,24,25,fur);oval(c,0,25,14,21,cream);
        if(type==1){oval(c,-15,-21,8,16,fur);oval(c,15,-21,8,16,fur);oval(c,-15,-22,4,11,0xffd7afa2);oval(c,15,-22,4,11,0xffd7afa2);}
        else if(type==3||type==4){triangle(c,-32,-12,-27,-37,-10,-22,fur);triangle(c,32,-12,27,-37,10,-22,fur);triangle(c,-27,-19,-25,-30,-17,-22,0xffdfb3a1);triangle(c,27,-19,25,-30,17,-22,0xffdfb3a1);}
        else if(type!=6){oval(c,-23,-23,11,12,fur);oval(c,23,-23,11,12,fur);oval(c,-23,-23,6,7,0xffc9ac98);oval(c,23,-23,6,7,0xffc9ac98);}
        oval(c,0,-1,type==1?28:34,27,fur);
        if(type==6){oval(c,-19,-20,13,13,fur);oval(c,19,-20,13,13,fur);oval(c,0,11,26,14,cream);}
        if(type==3){triangle(c,-33,-3,-3,10,-14,24,cream);triangle(c,33,-3,3,10,14,24,cream);}
        if(type==4){line(c,-9,-23,-6,-13,0xffaa754e,3);line(c,0,-26,0,-14,0xffaa754e,3);line(c,9,-23,6,-13,0xffaa754e,3);}
        float eyeY=type==6?-21:-6,eyeX=type==6?19:12;
        if(time%3100>2940){line(c,-eyeX-4,eyeY,-eyeX+4,eyeY,0xff394b37,2);line(c,eyeX-4,eyeY,eyeX+4,eyeY,0xff394b37,2);}
        else for(int side:new int[]{-1,1}){oval(c,side*eyeX,eyeY,6,7,0xfffcfff0);oval(c,side*eyeX+1,eyeY+1,3,4.5f,0xff344a3c);oval(c,side*eyeX,eyeY-1,1,1.5f,0xffffffff);}
        if(type!=6){oval(c,-8,12,11,10,cream);oval(c,8,12,11,10,cream);oval(c,0,7,type==7?9:5,4,type==1||type==7?0xffce9c95:0xff475449);}
        if(type==2){rect(c,-7,16,-.5f,26,0xfffff8e4);rect(c,.5f,16,7,26,0xfffff8e4);}
        else{line(c,-6,17,0,20,0xff6c6a51,1.5f);line(c,0,20,6,17,0xff6c6a51,1.5f);}
        if(type==4||type==1||type==7){for(int side:new int[]{-1,1}){line(c,side*18,10,side*31,7,0xff887966,1);line(c,side*19,14,side*33,15,0xff887966,1);}}
        oval(c,-23,27,9,6,fur);oval(c,23,27,9,6,fur);
    }
    private void friend(Canvas c,long time){
        // Side-swept dark hair, glasses, smile and a white/navy patterned shirt.
        oval(c,0,27,29,27,0xfff6f2e5);triangle(c,-29,13,-16,5,-20,34,0xff485f7b);triangle(c,29,13,16,5,20,34,0xff485f7b);
        for(int side:new int[]{-1,1}){line(c,side*25,17,side*17,12,0xffe4e2cf,2);line(c,side*25,21,side*16,15,0xffd1d9d3,2);}
        oval(c,0,30,14,11,0xff53677c);oval(c,0,30,10,8,0xfff7f0dd);line(c,-10,23,8,37,0xffb6826e,3);line(c,-3,20,-3,41,0xff526479,2);
        rect(c,-7,5,7,19,0xffcda78b);oval(c,-25,-2,6,10,0xffd8b79b);oval(c,25,-2,6,10,0xffd8b79b);
        oval(c,0,-5,26,29,0xffe1bea0);oval(c,-18,7,5,3,0xffd4a18f);oval(c,18,7,5,3,0xffd4a18f);
        // Thick, swept fringe rather than a generic round cap.
        fill(0xff303b3c);path.reset();path.moveTo(-28,3);path.quadTo(-39,-20,-18,-33);path.quadTo(3,-43,23,-28);path.quadTo(33,-19,28,4);path.lineTo(23,-15);path.quadTo(19,-22,12,-24);path.quadTo(2,-12,-20,-9);path.lineTo(-23,5);path.close();c.drawPath(path,p);
        line(c,-22,-20,4,-30,0xff47514e,2);line(c,-25,-15,-4,-25,0xff47514e,1.5f);
        boolean blink=time%3100>2940;
        for(int side:new int[]{-1,1}){if(blink)line(c,side*11-4,-3,side*11+4,-3,0xff493e32,1.5f);else{oval(c,side*11,-3,5,3.8f,0xfffaf5e9);oval(c,side*11,-3,2,2.5f,0xff493e32);}}
        fill(0xff756c55);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);c.drawRoundRect(-23,-10,-2,4,4,4,p);c.drawRoundRect(2,-10,23,4,4,4,p);line(c,-2,-5,2,-5,0xff756c55,1.7f);
        line(c,1,-1,-1,7,0xffbe9379,1.4f);line(c,-1,7,4,7,0xffbe9379,1.4f);
        oval(c,0,14,10,5,0xff8e5b4a);oval(c,0,12.5f,8,2.5f,0xfffff6df);
        for(int j=0;j<12;j++){float xx=(j-5.5f)*3.1f,yy=21-Math.abs(j-5.5f)*.4f;oval(c,xx,yy,.55f,.65f,0xffa5917b);}
        oval(c,-23,28,8,5,0xffe1bea0);oval(c,23,28,8,5,0xffe1bea0);
    }
    public void item(Canvas c,float x,float y,float radius,long time,boolean shake,int type){
        if(type==0){original.bin(c,x,y,radius,time,shake);return;}
        c.save();c.translate(x,y);c.scale(radius/36,radius/36);if(shake)c.rotate((float)Math.sin(time*.08)*8);oval(c,0,34,34,8,0xffa3af82);
        if(type==1){fill(0xffa17c61);path.reset();path.moveTo(-18,-31);path.lineTo(8,-31);path.lineTo(10,7);path.quadTo(31,4,31,24);path.lineTo(31,31);path.lineTo(-21,31);path.close();c.drawPath(path,p);rect(c,-24,28,33,35,0xff6b6551);rect(c,-22,-34,12,-24,0xffc0a079);line(c,5,9,14,17,0xffe0c49a,3);line(c,3,16,12,24,0xffe0c49a,3);}
        else if(type==2){fill(0xffcf9678);c.drawRoundRect(-21,14,21,34,4,4,p);triangle(c,-23,14,23,14,15,36,0xffb78369);fill(0xff74966a);c.drawRoundRect(-9,-33,9,16,8,8,p);line(c,-7,-4,-23,-4,0xff74966a,10);line(c,-23,-4,-23,-19,0xff74966a,10);line(c,7,4,23,4,0xff74966a,10);line(c,23,4,23,-11,0xff74966a,10);for(int j=0;j<5;j++)line(c,-8,-25+j*8,-12,-26+j*8,0xffe1ddb6,1.5f);}
        else if(type==3){fill(0xff929d99);path.reset();path.moveTo(-32,22);path.lineTo(-26,-11);path.lineTo(-10,-29);path.lineTo(17,-22);path.lineTo(31,1);path.lineTo(29,26);path.lineTo(4,34);path.lineTo(-26,32);path.close();c.drawPath(path,p);triangle(c,-26,-11,-10,-29,3,5,0xffb0bab0);triangle(c,3,5,17,-22,31,1,0xffa4ada5);line(c,-13,15,0,8,0xff78867f,2);line(c,0,8,12,13,0xff78867f,2);}
        else{fill(0xff8c7d65);c.drawRoundRect(-32,26,32,35,5,5,p);triangle(c,-24,26,0,-35,24,26,0xffd79063);triangle(c,-19,14,19,14,14,2,0xffffe6be);triangle(c,-9,-10,9,-10,5,-21,0xffffe6be);}
        c.restore();
    }
}
