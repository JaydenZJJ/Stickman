package stickman.Entities;

import stickman.Entities.HeroPathState.HeroPathStateContext;

public class Hero extends Entity {
    private HeroPathStateContext pathContext;
    // EXTENSION - HP
    private long hp;
    // EXTENSION - reset hero
    private double startingX;
    private double startingY;

    // EXTENSION - Refactoring: Singleton
    private static Hero chosenOne;
    private boolean init = false;

    private Hero(){}

    public static Hero getHero(){
        if (chosenOne == null){
            chosenOne = new Hero();
        }
        return chosenOne;
    }

    public void initHero(double xPos, double yPos, HeroPathStateContext.Actor character, long hp) {
        pathContext = new HeroPathStateContext(character, xPos);
        this.xPos = xPos;
        startingX = xPos;
        this.yPos = yPos;
        startingY = yPos;
        this.xVel = 1;
        this.hp = hp;
        layer = Layer.FOREGROUND;
        init = true;
    }

    /*
    @Override
    public double getYPos() {
        this.yPos += this.yVel;
        return yPos;
    }

     */

    @Override
    public String getImagePath() {
        return pathContext.getPath(xPos);
    }

    @Override
    public Entity newInstance(){return null;}

    @Override
    public void setXVel(double xVel) {
        // Do nothing as the Hero's velocity is constant.
    }

    // EXTENSION -- LIFE/RESET
    public long getHp(){
        return this.hp;
    }

    public void resetHeroPos(){
        this.xPos = startingX;
        this.yPos = startingY;
    }

    public void minusHp(){
        this.hp--;
    }

    public boolean isInit(){
        return this.init;
    }

    public void setHp(long hp){
        this.hp = hp;
    }
}