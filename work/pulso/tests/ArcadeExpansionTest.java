import com.rey.pulso.ArcadeEngine;
import java.util.Random;

public final class ArcadeExpansionTest {
    private static int checks;
    private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    private static ArcadeEngine.Slot first(ArcadeEngine g){for(ArcadeEngine.Slot s:g.slots)if(s.active&&!s.exiting)return s;return null;}
    private static int capture(ArcadeEngine g){ArcadeEngine.Slot s=first(g);if(s==null){g.advance(180);s=first(g);}if(s==null)throw new AssertionError("no target");return g.tap(s.x,s.y);}
    public static void main(String[] args){
        ArcadeEngine g=new ArcadeEngine(new Random(42));g.start(0,1);g.setBounds(340,460);
        for(int n=1;n<=60;n++){
            check(capture(g)==(n%20==0?ArcadeEngine.BONUS:ArcadeEngine.HIT),"one point, life each twenty");
            check(g.score==n&&g.hits==n&&g.streak==n&&g.bestStreak==n,"hit stats and streak");
            check(g.lives==3+n/20,"bonus lives");
        }
        check(g.bonusLives==3,"bonus stats");g.advance(180);ArcadeEngine.Slot s=first(g);
        int before=g.score;check(g.tap(s.x-g.radius*49/36,s.y+g.radius*52/36)==ArcadeEngine.HIT,"full silhouette including bottom of hole");
        check(g.tap(s.x,s.y)==ArcadeEngine.NONE&&g.score==before+1,"ignore duplicate tap during successful exit");
        g.advance(180);s=first(g);g.advance(s.remaining);check(g.escaped==1&&g.streak==0&&g.bestStreak==61,"escape breaks only current streak");
        g.start(0,1);g.setBounds(340,460);int life=g.lives;g.advance(first(g).remaining);check(g.lives==life-1&&g.escaped==1,"classic miss costs life");
        while(g.playing){g.advance(180);if(first(g)!=null)g.advance(first(g).remaining);}
        check(g.lives==0&&g.finishReason.equals("Sin vidas"),"classic ends at zero");
        g.start(1,1);g.advance(59999);check(g.playing&&g.timeLeft==1&&g.lives==3,"timed survives misses and lasts sixty seconds");
        check(g.advance(1)==ArcadeEngine.FINISHED&&!g.playing&&g.timeLeft==0&&g.playedMs==60000,"timed exact deadline");
        check(g.tap(0,0)==ArcadeEngine.NONE,"no score after timer");
        g.start(1,1);for(int n=0;n<40;n++)check(capture(g)==ArcadeEngine.HIT,"timed has no life bonus");check(g.bonusLives==0&&g.lives==3,"timed lives not used");
        g.start(0,0);long relaxed=first(g).duration;g.start(0,1);long normal=first(g).duration;g.start(0,2);check(relaxed>normal&&normal>first(g).duration,"difficulty speed order");
        g.start(2,1);g.setBounds(340,460);
        for(int n=0;n<200&&g.liveTargets()!=2;n++){if(first(g)==null)g.advance(180);if(g.liveTargets()!=2)capture(g);}
        check(g.liveTargets()==2,"two simultaneous characters available in burrows");
        ArcadeEngine.Slot a=first(g);int old=g.score;float x=a.x,y=a.y;g.tap(x,y);check(g.liveTargets()==1,"other target remains active");check(g.tap(x,y)==ArcadeEngine.NONE&&g.score==old+1,"same target cannot score twice");g.tap(first(g).x,first(g).y);check(g.score==old+2,"both targets score independently");
        byte[] snapshot=g.snapshot();ArcadeEngine restored=new ArcadeEngine(new Random(2));check(restored.restore(snapshot),"snapshot restore");
        check(restored.score==g.score&&restored.streak==g.streak&&restored.mode==g.mode&&restored.level==g.level,"restores category and stats");
        for(int i=0;i<6;i++)check(restored.slots[i].remaining==g.slots[i].remaining&&restored.slots[i].exitRemaining==g.slots[i].exitRemaining&&restored.slots[i].active==g.slots[i].active,"exact timers and transitions preserved");
        check(!restored.restore(new byte[]{1,2,3}),"malformed state rejected");
        int traps=0;
        for(int mode=0;mode<3;mode++)for(int level=0;level<3;level++)for(int seed=0;seed<20;seed++){
            g=new ArcadeEngine(new Random(seed));g.start(mode,level);g.setBounds(300+seed,420+seed);
            check(g.key().equals("arcade_"+mode+"_"+level),"distinct record category");
            for(int n=0;n<100&&g.playing;n++){
                if(first(g)==null)g.advance(180);
                check(g.liveTargets()>=1&&g.liveTargets()<=(mode==2?2:1),"active target limit");
                for(ArcadeEngine.Slot slot:g.slots)if(slot.active&&!slot.exiting){
                    check(slot.x-g.radius*49/36>=0&&slot.x+g.radius*49/36<=g.width&&slot.y-g.radius*38/36>=0&&slot.y+g.radius*52/36<=g.height,"complete hitbox within board");
                    if(g.trap)check(!g.contains(slot,g.trapX,g.trapY),"trap outside every active hitbox");
                }
                if(g.trap&&n%13==0){int score=g.score,oldLife=g.lives;int event=g.tap(g.trapX,g.trapY);traps++;check(g.trapsTouched>0&&g.score==score&&g.streak==0,"trap stats and no extra score");check(g.lives==oldLife-(mode==1?0:1),"mode-specific trap penalty");if(event==ArcadeEngine.FINISHED)break;}
                capture(g);
            }
        }
        check(traps>100,"trap collisions exercised in multiple modes");
        System.out.println("PASS: "+checks+" expanded arcade checks across nine categories (timer, two targets, streaks, lives, hitboxes, stats and exact resume).");
    }
}
