import com.rey.pulso.GameEngine;
import java.util.Random;

public final class ThemedGameTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void emerge(GameEngine g){if(g.exiting)g.advance(160);g.advance(200);}
    public static void main(String[] args){
        GameEngine g=new GameEngine(new Random(42));g.setBounds(340,480);g.start();
        check(g.score==0&&g.lives==3&&g.playing,"new game");
        check(g.visibility()==0,"emerges from fully hidden");
        check(g.tap(-1000,-1000)==GameEngine.NONE,"background cannot score during emergence");
        g.advance(90);check(g.visibility()>.4f&&g.visibility()<.6f,"half emerged");
        g.advance(90);check(g.visibility()==1,"fully visible");
        check(g.tap(-100,-100)==GameEngine.NONE&&g.lives==3,"background does not hurt");
        for(int i=1;i<=60;i++){
            emerge(g);int result=g.tap(g.x,g.y);
            check(g.score==i,"one point for each hit");
            check(result==(i%20==0?GameEngine.BONUS:GameEngine.HIT),"extra life every 20 points");
            check(g.lives==3+i/20,"bonus is awarded once");
            check(g.exiting,"successful hit hides mapache before next wave");
            check(g.tap(g.x,g.y)==GameEngine.NONE&&g.score==i,"no repeated points during exit");
        }
        g.advance(80);check(g.visibility()>.4f&&g.visibility()<.6f,"half hidden after hit");
        g.advance(80);check(!g.exiting&&g.visibility()==0,"new wave starts hidden");
        check(g.durationMs==730,"difficulty increases with score");
        g.advance(200);long before=g.remainingMs;g.setBounds(260,320);check(g.remainingMs==before,"resize preserves countdown");
        check(g.advance(0)==GameEngine.NONE&&g.remainingMs==before,"no time advances when paused");
        g.advance(before-100);check(g.visibility()<1&&g.visibility()>0,"naturally retreats before expiry");
        check(g.advance(100)==GameEngine.HURT&&g.lives==5,"escape loses one life");
        while(g.lives>0){if(g.exiting)g.advance(160);int expected=g.lives==1?GameEngine.FINISHED:GameEngine.HURT;check(g.advance(g.remainingMs)==expected,"each missed wave costs one life");}
        check(!g.playing&&g.lives==0,"zero lives ends game");
        check(g.tap(g.x,g.y)==GameEngine.NONE,"no interaction after game over");
        int traps=0;
        for(int seed=0;seed<100;seed++){
            g=new GameEngine(new Random(seed));g.setBounds(260+seed,320+seed);g.start();
            for(int i=0;i<100;i++){
                emerge(g);
                check(g.x>=g.radius*1.25f&&g.x<=g.width-g.radius*1.25f,"illustration within horizontal bounds");
                check(g.y>=g.radius&&g.y<=g.height-g.radius*1.4f,"hole and face within vertical bounds");
                check(g.durationMs>=550,"minimum time floor");
                if(g.trap){float dx=g.x-g.trapX,dy=g.y-g.trapY;check(dx*dx+dy*dy>4*g.radius*g.radius,"target and bin do not overlap");}
                int old=g.score;g.tap(g.x,g.y);check(g.score==old+1,"visible target hit");
            }
            // Force a wave that contains a bin, then tap it away from the animal.
            for(int i=0;i<20&&!g.trap;i++){emerge(g);g.tap(g.x,g.y);}
            emerge(g);
            if(g.trap){int score=g.score,lives=g.lives;g.tap(g.trapX,g.trapY);check(g.score==score&&g.lives==lives-1,"bin costs life, awards no points");traps++;}
        }
        check(traps>30,"multiple bin collisions exercised");
        g.restore(87,2);check(g.score==87&&g.lives==2&&g.playing,"restores session");
        System.out.println("PASS: "+checks+" themed-game checks; bonus lives, emergence, exit, collision and 100 seeded sessions.");
    }
}
