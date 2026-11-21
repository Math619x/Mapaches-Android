import com.rey.pulso.ContentAccess;

public final class ContentAccessTest {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        ContentAccess a=new ContentAccess(true);
        for(int category=0;category<3;category++){
            check(a.availableCount(category)==2,"exactly two free in each category");
            check(a.available(category,0)&&a.available(category,1),"first two are free");
            for(int i=2;i<ContentAccess.count(category);i++)check(!a.available(category,i),"paid candidate starts locked");
            check(!a.available(category,-1)&&!a.available(category,99),"invalid content cannot be selected");
            check(a.safeSelection(category,1)==1&&a.safeSelection(category,4)==0,"existing locked selections return safely to free content");
        }
        check(!a.available(99,0),"invalid category cannot unlock");
        check(a.unlockDemo(ContentAccess.ACTORS),"explicit test unlock succeeds");
        check(a.availableCount(0)==9&&a.availableCount(1)==2&&a.availableCount(2)==2,"actor pack does not unlock other categories");
        check(!a.unlockDemo(ContentAccess.ACTORS),"idempotent unlock");
        ContentAccess restored=ContentAccess.decodeDemo(true,a.encodeDemo());
        check(restored.available(0,8)&&!restored.available(1,4),"demo grant survives restart");
        check(restored.unlockDemo(ContentAccess.MAPS)&&restored.availableCount(1)==5,"map pack unlocks maps");
        check(restored.unlockDemo(ContentAccess.ITEMS)&&restored.availableCount(2)==5,"object pack unlocks objects");
        check(restored.demoOwned(ContentAccess.ALL),"three category packs cover current complete catalog");
        check(!restored.unlockDemo(ContentAccess.ALL),"complete bundle not duplicated when all included packs owned");
        restored.resetDemo();check(restored.availableCount(0)==2&&restored.availableCount(1)==2&&restored.availableCount(2)==2,"reset returns to six free options");
        check(restored.encodeDemo().isEmpty(),"test grants removed on reset");
        check(restored.unlockDemo(ContentAccess.ALL),"complete demo bundle");
        check(restored.availableCount(0)==9&&restored.availableCount(1)==5&&restored.availableCount(2)==5,"bundle grants all current content");
        check(!restored.unlockDemo("unknown")&&!restored.demoOwned("unknown"),"unknown products grant nothing");
        ContentAccess production=ContentAccess.decodeDemo(false,restored.encodeDemo());
        for(int category=0;category<3;category++)check(production.availableCount(category)==2,"demo flags never become real purchase entitlements");
        check(!production.unlockDemo(ContentAccess.ALL),"test unlocks disabled when demo mode off");
        check(!ContentAccess.decodeDemo(true,"all,paid,unlocked,anything").available(0,8),"unrecognized saved values cannot unlock content");
        check(ContentAccess.decodeDemo(true,null).availableCount(0)==2,"missing preferences defaults free");
        System.out.println("PASS: "+checks+" store access checks (two-free defaults, category gates, persistence, reset and test/real separation).");
    }
}
