package com.rey.pulso;

import android.content.Context;
import android.media.*;
import android.os.Build;
import java.io.*;

/** Direct PCM playback avoids device-specific MediaPlayer/resource decoding failures. */
public final class AmbientMusic implements AudioManager.OnAudioFocusChangeListener {
    public interface StatusListener {void changed(String status);}
    private final Context context;
    private final AudioManager audio;
    private AudioTrack track;
    private AudioFocusRequest focusRequest;
    private boolean requested,focused,ducked;
    private boolean failed,released;
    private float volume=.60f;
    private String error="";
    private StatusListener listener;
    public AmbientMusic(Context context){this.context=context;audio=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);}
    public boolean available(){return !failed;}
    public void setListener(StatusListener listener){this.listener=listener;notifyStatus();}
    public void setVolume(float value){volume=Math.max(0,Math.min(1,value));if(track!=null)track.setVolume(ducked?volume*.2f:volume);notifyStatus();}
    public String status(){
        if(failed)return "Audio no disponible: "+error;
        if(!requested)return "Música en pausa";
        if(!focused)return "Esperando permiso de audio; pulsa Probar sonido";
        if(audio.getStreamVolume(AudioManager.STREAM_MUSIC)==0)return "Volumen multimedia del teléfono en cero";
        return volume==0?"Volumen de música en cero":"Reproduciendo música · "+Math.round(volume*100)+" %";
    }
    private void notifyStatus(){if(listener!=null)listener.changed(status());}
    public void setActive(boolean active){
        if(released)return;
        if(requested==active&&(!active||focused)){notifyStatus();return;}
        requested=active;
        if(active){
            if(Build.VERSION.SDK_INT>=26){
                if(focusRequest==null)focusRequest=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attributes()).setOnAudioFocusChangeListener(this).build();
                focused=audio.requestAudioFocus(focusRequest)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            }else focused=audio.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }else{focused=false;abandon();}
        sync();
    }
    private AudioAttributes attributes(){return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();}
    public void retry(){failed=false;error="";setActive(true);if(focused)sync();}
    private byte[] loadPcm()throws IOException {
        int resource=context.getResources().getIdentifier("garden_ambient","raw",context.getPackageName());
        if(resource==0)throw new IOException("falta el archivo musical");
        InputStream input=context.getResources().openRawResource(resource);
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{byte[] bytes=new byte[8192];int n;while((n=input.read(bytes))!=-1)output.write(bytes,0,n);}finally{input.close();}
        byte[] wav=output.toByteArray();
        if(wav.length<44||wav[0]!='R'||wav[8]!='W'||wav[36]!='d')throw new IOException("formato de música no válido");
        return java.util.Arrays.copyOfRange(wav,44,wav.length);
    }
    private void sync(){
        try{
            if(!requested||!focused){if(track!=null&&track.getPlayState()==AudioTrack.PLAYSTATE_PLAYING)track.pause();notifyStatus();return;}
            if(failed){notifyStatus();return;}
            if(track==null){
                byte[] pcm=loadPcm();
                AudioFormat format=new AudioFormat.Builder().setSampleRate(22050).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();
                track=new AudioTrack.Builder().setAudioAttributes(attributes()).setAudioFormat(format).setBufferSizeInBytes(pcm.length).setTransferMode(AudioTrack.MODE_STATIC).build();
                if(track.write(pcm,0,pcm.length)!=pcm.length)throw new IOException("no se pudo cargar la música");
                // MODE_STATIC becomes initialized only after its PCM buffer is written.
                if(track.getState()!=AudioTrack.STATE_INITIALIZED)throw new IOException("salida de audio no disponible");
                if(track.setLoopPoints(0,pcm.length/2,-1)!=AudioTrack.SUCCESS)throw new IOException("no se pudo iniciar el bucle");
            }
            track.setVolume(ducked?volume*.2f:volume);if(track.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING)track.play();
        }catch(Exception failure){failed=true;error=failure.getMessage()==null?"error de reproducción":failure.getMessage();if(track!=null){track.release();track=null;}}
        notifyStatus();
    }
    public void onAudioFocusChange(int focus){
        if(focus==AudioManager.AUDIOFOCUS_GAIN){focused=true;ducked=false;}
        else if(focus==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){ducked=true;}
        else{focused=false;ducked=false;}
        sync();
    }
    private void abandon(){if(Build.VERSION.SDK_INT>=26&&focusRequest!=null)audio.abandonAudioFocusRequest(focusRequest);else audio.abandonAudioFocus(this);}
    public void release(){requested=false;focused=false;released=true;abandon();if(track!=null){track.release();track=null;}}
}
