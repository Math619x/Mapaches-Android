import com.rey.pulso.GameEngine;
import com.rey.pulso.ScoreTable;
import java.util.Random;

public final class ArcadeChangesTest {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        // All four corners of the maximum silhouette, center and bottom of hole
        // must score at emergence, full display and the final retreat.
        for(int age:new int[]{0,20,180,1400})for(int point=0;point<6;point++){
            GameEngine g=new GameEngine(new Random(9));g.setBounds(340,480);g.start();g.advance(age);
            float left=g.hitLeft(),right=g.hitRight(),top=g.hitTop(),bottom=g.hitBottom();
            float[] xs={left,right,left,right,g.x,g.x};float[] ys={top,top,bottom,bottom,g.y,bottom-1};
            check(g.tap(xs[point],ys[point])==GameEngine.HIT,"fixed full-size hitbox at age "+age);
            check(g.score==1,"one point");g.advance(80);
            check(g.tap(xs[point],ys[point])==GameEngine.NONE&&g.score==1,"successful target cannot score again while hiding");
        }
        GameEngine g=new GameEngine(new Random(4));g.setBounds(340,480);g.start();
        check(g.tap(g.hitLeft()-1,g.y)==GameEngine.NONE,"outside left does not score");
        check(g.tap(g.x,g.hitBottom()+1)==GameEngine.NONE,"outside hole does not score");
        ScoreTable scores=new ScoreTable();
        check(scores.entries().isEmpty(),"empty table");
        check(!scores.add("zero","NICK",0),"zero scores excluded");
        for(int i=1;i<=15;i++)scores.add("game-"+i,"Jugador "+i,i*10);
        check(scores.entries().size()==10,"only ten records retained");
        check(scores.entries().get(0).score==150&&scores.entries().get(9).score==60,"sorted highest first");
        check(!scores.add("game-15","DUPLICADO",999),"same completed game saved only once");
        check(scores.entries().get(0).score==150,"duplicate cannot overwrite score");
        check(!scores.add("new-tie","EMPATE",60),"older record wins tied cutoff");
        check(scores.add("new-win","NUEVO",61),"qualifying score replaces lowest");
        check(scores.rank("game-6")==0,"displaced score removed");
        scores.rename("game-15","  José 🦝  ");
        check(scores.find("game-15").nick.equals("José 🦝"),"nick supports Spanish and emoji");
        scores.rename("game-14","\n\t");check(scores.find("game-14").nick.equals("MAPACHE"),"empty nick defaults safely");
        check(ScoreTable.cleanNick("ABCDEFGHIJKLMNO").equals("ABCDEFGHIJKL"),"nick limited to twelve characters");
        ScoreTable restored=ScoreTable.decode(scores.encode());
        check(restored.entries().size()==10,"all records restore");
        check(restored.find("game-15").nick.equals("José 🦝"),"nick survives persistence");
        check(restored.entries().get(0).score==150,"scores survive persistence");
        ScoreTable damaged=ScoreTable.decode("malformed\nbad\tnick\tnotanumber\n"+scores.encode());
        check(damaged.entries().size()==10,"malformed rows do not lose valid records");
        check(scores.encode().equals(ScoreTable.decode(scores.encode()).encode()),"stable persistence roundtrip");
        System.out.println("PASS: "+checks+" arcade changes checks (maximum hitbox, retreat, duplicate prevention, ranking and nick persistence).");
    }
}
