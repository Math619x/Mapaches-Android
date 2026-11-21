package com.rey.pulso;

import java.util.*;
import java.net.URLEncoder;
import java.net.URLDecoder;

/** Ten local arcade records. Stable ties favor the earlier record. */
public final class ScoreTable {
    public static final int LIMIT=10;
    public static final class Entry {
        public final String id;
        public final int score;
        public String nick;
        Entry(String id,String nick,int score){this.id=id;this.nick=cleanNick(nick);this.score=score;}
    }
    private final ArrayList<Entry> rows=new ArrayList<Entry>();
    public List<Entry> entries(){return Collections.unmodifiableList(rows);}
    public int rank(String id){for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(id))return i+1;return 0;}
    public Entry find(String id){for(Entry row:rows)if(row.id.equals(id))return row;return null;}
    public boolean add(String id,String nick,int score){
        if(id==null||id.isEmpty()||score<=0||rank(id)!=0)return false;
        rows.add(new Entry(id,nick,score));
        Collections.sort(rows,new Comparator<Entry>(){public int compare(Entry a,Entry b){return Integer.compare(b.score,a.score);}});
        while(rows.size()>LIMIT)rows.remove(rows.size()-1);
        return rank(id)>0;
    }
    public void rename(String id,String nick){Entry row=find(id);if(row!=null)row.nick=cleanNick(nick);}
    public static String cleanNick(String text){
        if(text==null)return "MAPACHE";
        StringBuilder out=new StringBuilder();int count=0;
        for(int i=0;i<text.length()&&count<12;){int code=text.codePointAt(i);i+=Character.charCount(code);if(Character.isISOControl(code)||Character.getType(code)==Character.FORMAT)continue;out.appendCodePoint(code);count++;}
        String clean=out.toString().trim();return clean.isEmpty()?"MAPACHE":clean;
    }
    public String encode(){
        StringBuilder out=new StringBuilder();
        try{for(Entry row:rows)out.append(URLEncoder.encode(row.id,"UTF-8")).append('\t').append(URLEncoder.encode(row.nick,"UTF-8")).append('\t').append(row.score).append('\n');}
        catch(java.io.UnsupportedEncodingException impossible){throw new AssertionError(impossible);}
        return out.toString();
    }
    public static ScoreTable decode(String data){
        ScoreTable result=new ScoreTable();if(data==null)return result;
        for(String line:data.split("\n")){
            try{String[] values=line.split("\t",-1);if(values.length==3)result.add(URLDecoder.decode(values[0],"UTF-8"),URLDecoder.decode(values[1],"UTF-8"),Integer.parseInt(values[2]));}
            catch(Exception malformed){/* Keep the other valid records. */}
        }
        return result;
    }
}
