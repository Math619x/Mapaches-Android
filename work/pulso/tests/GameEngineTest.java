import com.rey.pulso.GameEngine;
import java.util.Random;

public final class GameEngineTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        GameEngine g=new GameEngine(new Random(42));g.setBounds(340,480);g.start();
        check(g.score==0&&g.lives==3&&g.playing,"new game");
        check(g.tap(-100,-100)==GameEngine.NONE&&g.lives==3,"empty taps do not hurt");
        for(int i=1;i<=60;i++){
            int result=g.tap(g.x,g.y);
            check(g.score==i,"score advances exactly once");
            check(result==(i%20==0?GameEngine.BONUS:GameEngine.HIT),"bonus occurs at each threshold");
            check(g.lives==3+i/20,"one extra life per 20 points");
        }
        check(g.durationMs==730,"difficulty increases");
        long before=g.remainingMs;g.setBounds(260,320);check(g.remainingMs==before,"resizing preserves time");
        check(g.advance(0)==GameEngine.NONE&&g.remainingMs==before,"pause makes no progress");
        g.advance(before-1);check(g.lives==6,"target alive until deadline");
        check(g.advance(1)==GameEngine.HURT&&g.lives==5,"deadline loses one life");
        while(g.lives>1)check(g.advance(g.remainingMs)==GameEngine.HURT,"timeout loses one life");
        check(g.advance(g.remainingMs)==GameEngine.FINISHED&&!g.playing&&g.lives==0,"game ends at zero lives");
        int score=g.score;check(g.tap(g.x,g.y)==GameEngine.NONE&&g.score==score,"no score after game over");
        check(g.advance(10000)==GameEngine.NONE,"finished game stays finished");
        g.start();check(g.score==0&&g.lives==3,"restart clears previous session");
        int trapTests=0;
        for(int seed=0;seed<100;seed++){
            g=new GameEngine(new Random(seed));g.setBounds(260+seed,320+seed);g.start();
            for(int i=0;i<130;i++){
                check(g.x>=g.radius&&g.x<=g.width-g.radius&&g.y>=g.radius&&g.y<=g.height-g.radius,"target within bounds");
                check(g.durationMs>=550,"time floor");
                if(g.trap){
                    float dx=g.x-g.trapX,dy=g.y-g.trapY;
                    check(dx*dx+dy*dy>4*g.radius*g.radius,"trap never overlaps target");
                    if(trapTests<100){
                        int oldScore=g.score,oldLives=g.lives;
                        int result=g.tap(g.trapX,g.trapY);trapTests++;
                        check(result==GameEngine.HURT||result==GameEngine.FINISHED,"trap hurts");
                        check(g.score==oldScore&&g.lives==oldLives-1,"trap does not award score");
                        if(!g.playing)g.start();
                    }
                }
                g.tap(g.x,g.y);
            }
        }
        check(trapTests==100,"traps were exercised");
        g.restore(87,2);check(g.score==87&&g.lives==2&&g.playing,"restore active session");
        g.restore(4,0);check(!g.playing,"zero-life session cannot resume");
        System.out.println("PASS: "+checks+" rule and geometry checks, 100 seeded runs.");
    }
}
