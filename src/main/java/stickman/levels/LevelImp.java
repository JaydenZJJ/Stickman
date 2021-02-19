package stickman.levels;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import stickman.Entities.*;
import stickman.view.BackgroundDrawer;
import stickman.view.SkyOnlyBackground;

import java.util.ArrayList;
import java.util.List;

public class LevelImp implements Level, Observer, Cloneable{

    private List<Entity> entities;
    private Entity hero;
    private BackgroundDrawer bg_drawer;
    private boolean left = false;
    private boolean right = false;
    private final double gravity = 0.1;
    private boolean heroHasGrown = false;
    private boolean levelLost = false;
    private boolean levelWon = false;
    private boolean heroCanShoot = false;
    private int jumpCount = 0;

    // EXTENSION - Level Transition
    Entity youWin;
    boolean lastLevel;
    boolean heroIsDead = false;

    // modification to remove concurrent modification exception
    private List<Entity> toRemove;
    private List<Entity> toAdd;

    // modification to track time/score
    boolean firstTracker = true;
    long levelStartTime;
    long levelExpectFinishTime;
    int currentLevelScore;
    int secondsPassed;

    // EXTENSION - Memento - Stats of Hero
    long hp;
    double xPos;
    double yPos;

    // EXTENSION - Level loading
    boolean loaded = false;
    int loadTimer = 0;
    public LevelImp(List<Entity> entities, Entity hero, BackgroundDrawer bg_drawer, long levelExpectFinishTime, boolean lastLevel) {
        this.entities = entities;
        this.hero = hero;
        this.bg_drawer = bg_drawer;
        this.toRemove = new ArrayList<>();
        this.toAdd = new ArrayList<>();

        // EXTENSION -- Attach observer AND Score tracker
        this.currentLevelScore = 0;
        this.secondsPassed = 0;
        this.levelExpectFinishTime = levelExpectFinishTime;
        for (Entity entity : entities){
            if (entity.getClass() == Mushroom.class || entity.getClass() == Slime.class){
                ((Subject) entity).attach(this);
            }
        }

        // EXTENSION -- level transition
        this.lastLevel = lastLevel;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }

    @Override
    public BackgroundDrawer getBGDrawer() {
        return this.bg_drawer;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    //@Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void tick() {

        //        if (levelLost || levelWon) return;
        if (lastLevel & levelWon){
            wonAnimation();
            return;
        }
        else if (levelWon){
            return;
        }
        else if (levelLost){
            return;
        }

        // EXTENSION: track level elapsed time
        if (firstTracker){
            levelStartTime = System.currentTimeMillis();
            firstTracker = false;
        }
        // EXTENSION
        // if 1 second has passed, then check whether it is below or above expected time
        long currentTime = System.currentTimeMillis();

        if (!loaded) {
            if ((currentTime - (levelStartTime + secondsPassed * 1000)) > 1000) {
                // if it is below expected finish time, then add 1 point
                if (secondsPassed < levelExpectFinishTime) {
                    this.currentLevelScore += 1;
                }
                // else minus 1 point
                else {
                    // only minus if it's not below 0
                    if (currentLevelScore > 0) {
                        this.currentLevelScore -= 1;
                    }
                }
                secondsPassed++;
            }
        }
        else{

            if (currentTime - (levelStartTime+loadTimer*1000) > 1000) {

                // if it is below expected finish time, then add 1 point
                if (secondsPassed < levelExpectFinishTime) {
                    this.currentLevelScore += 1;
                }
                // else minus 1 point
                else {
                    // only minus if it's not below 0
                    if (currentLevelScore > 0) {
                        this.currentLevelScore -= 1;
                    }
                }
                secondsPassed++;
                loadTimer++;
            }
        }

        // apply gravity to all objects with velocity.
        applyGravity();

        // Set Hero's direction.
        updateEntitiesPosition();
        entities.removeAll(toRemove);
        toRemove = new ArrayList<>();

        for (Entity ent_a : getMovingEntities()) {
            for (Entity ent_b : entities) {

                // Perform actions based on which Entities intersect each other.
                if (ent_a.intersects(ent_b)) {

                    // Certain Entity interactions have consequences.
                    if (heroIntersectsWithSlime(ent_a, ent_b) ||
                        heroIntersectsWithMushroom(ent_a, ent_b) ||
                        heroHasFallen() ||
                        bulletIntersectsWithSlime(ent_a, ent_b) ||
                        heroIntersectsWithFlag(ent_a, ent_b) ||
                        intersectionRequiresNoAction(ent_a, ent_b))
                        continue;

                    // Calculate the left and right overlap of the Entity's from both directions.
                    // The smallest overlap is assumed to be the direction of travel.
                    boolean from_left;
                    boolean from_top;
                    double x_overlap = 0;
                    double y_overlap = 0;

                    // X-axis overlap.
                    if (ent_a.getXPos() < ent_b.getXPos()) {
                        x_overlap = Math.abs(ent_a.getXPos() + ent_a.getWidth() - ent_b.getXPos());
                        from_left = true;
                    } else {
                        x_overlap = Math.abs(ent_b.getXPos() + ent_b.getWidth() - ent_a.getXPos());
                        from_left = false;
                    }

                    // Y-axis overlap.
                    if (ent_a.getYPos() < ent_b.getYPos()) {
                        y_overlap = Math.abs(ent_a.getYPos() + ent_a.getHeight() - ent_b.getYPos());
                        from_top = true;
                    } else {
                        y_overlap = Math.abs(ent_b.getYPos() + ent_b.getHeight() - ent_a.getYPos());
                        from_top = false;
                    }


                    // Direction of travel is on the x-axis.
                    if (x_overlap < y_overlap) {
                        if (from_left)
                            ent_a.setXPos(ent_b.getXPos() - ent_a.getWidth());

                        // From right
                        else
                            ent_a.setXPos(ent_b.getXPos() + ent_b.getWidth());

                        if (ent_a != hero)
                            ent_a.setXVel(ent_a.getXVel() * -1);

                    // Direction of travel is on the y-axis.
                    } else {
                        if (from_top) {
                            ent_a.setYPos(ent_b.getYPos() - ent_a.getHeight());
                            if (ent_a == hero) jumpCount = 0; // Allow the hero to jump again.
                        }

                        // From bottom
                        else
                            ent_a.setYPos(ent_b.getYPos() + ent_b.getHeight());

                        if (ent_a.getClass() == Bullet.class)
                            ent_a.setYVel(ent_a.getYVel() * -1);
                        else
                            ent_a.setYVel(0);
                    }
                }
            }
        }

        entities.removeAll(toRemove);
        toRemove = new ArrayList<>();
        entities.addAll(toAdd);
        toAdd = new ArrayList<>();
    }

    private boolean intersectionRequiresNoAction(Entity a, Entity b) {
        if  ((a.getClass() == b.getClass()) ||
             (a.getClass() == Mushroom.class) ||
             (b.getClass() == Mushroom.class) ||
             (a.getClass() == Ghost.class) ||
             (b.getClass() == Ghost.class) ||
             (a == hero && b.getClass() == Bullet.class) ||
             (b == hero && a.getClass() == Bullet.class) ||
             (a.getClass() == Flag.class) ||
             (b.getClass() == Flag.class))
            return true;
        else
            return false;
    }

    private boolean heroIntersectsWithMushroom(Entity a, Entity b) {
        if ((a.getClass() == Mushroom.class && b == hero) ||
            (b.getClass() == Mushroom.class && a == hero)) {

            // EXTENSION - OBSERVER + SCORE TRACKER
            if (a.getClass() == Mushroom.class){
                ((Mushroom) a).notifyScoreChanges();
                ((Mushroom) a).detach();
            }
            else{
                ((Mushroom) b).notifyScoreChanges();
                ((Mushroom) b).detach();
            }

            if (!heroCanShoot)
                heroCanShoot = true;

            // Delete the mushroom
            if (a.getClass() == Mushroom.class)
                toRemove.add(a);
//                entities.remove(a);
            else
                toRemove.add(b);
//                entities.remove(b);

            return true;
        }
        return false;
    }

    private boolean heroHasFallen() {
        if (hero.getYPos() > 500) {
            Hero hero = (Hero) this.hero;
            if (hero.getHp()==1){
                for (Entity ent: getMovingEntities ()) {
                    ent.setXVel(0);
                    ent.setYVel(0);
                }
                toRemove.add(hero);
//            entities.remove(hero);
                heroIsDead = true;
                levelLost = true;
                return true;
            }
            else{
                hero.resetHeroPos();
                hero.minusHp();
            }
        }
        return false;
    }

    private boolean heroIntersectsWithSlime(Entity a, Entity b) {
        if ((a == hero && b.getClass() == Slime.class) ||
            (a.getClass() == Slime.class && b == hero)) {
            Hero hero = (Hero) this.hero;
            if (hero.getHp()==1){
                for (Entity ent: getMovingEntities ()) {
                    ent.setXVel(0);
                    ent.setYVel(0);
                }
                toRemove.add(hero);
                heroIsDead = true;
                levelLost = true;
                return true;
            }
            else{
                hero.resetHeroPos();
                hero.minusHp();
                levelLost = true;
            }
        }
        return false;
    }

    private boolean bulletIntersectsWithSlime(Entity a, Entity b) {
        if ((a.getClass() == Bullet.class && b.getClass() == Slime.class) ||
            (b.getClass() == Bullet.class && a.getClass() == Slime.class)) {

            // EXTENSION -- OBSERVER + SCORE
            if (a.getClass() == Slime.class){
                ((Slime) a).notifyScoreChanges();
                ((Slime) a).detach();
            }
            else{
                ((Slime) b).notifyScoreChanges();
                ((Slime) b).detach();
            }

            Entity ghost;
            if (a.getClass() == Slime.class)
                ghost = new Ghost(a.getXPos(), a.getYPos());
            else
                ghost = new Ghost(b.getXPos(), b.getYPos());;

//            entities.remove(a);
//            entities.remove(b);
            toRemove.add(a);
            toRemove.add(b);
            toAdd.add(ghost);
//            entities.add(ghost);

            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17),
                    t -> {ghost.setYPos(ghost.getYPos() - 3);}));

            timeline.setCycleCount(10000);
            timeline.play();
            return true;
        }
        return false;
    }

    private boolean heroIntersectsWithFlag(Entity a, Entity b) {
        if ((a == hero && b.getClass() == Flag.class) ||
            (a.getClass() == Flag.class && b == hero)) {

            for (Entity ent: getMovingEntities()) {
                ent.setXVel(0);
                ent.setYVel(0);
            }

            levelWon = true;


            if (lastLevel) {
                youWin = new YouWinBanner(hero.getXPos() - 200, hero.getYPos());
                toAdd.add(youWin);
            }
//            entities.add(youWin);


            return true;
        }
        return false;
    }

    private void wonAnimation(){
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17),
//                t -> {youWin.setYPos(hero.getYPos());}));
                t->{}));
        timeline.setCycleCount(110);
        timeline.play();
    }

    private List<Entity> getMovingEntities() {
        List<Entity> moving_ents = new ArrayList<>();
        for (Entity ent: entities) {
            if (ent == hero) continue; // put hero in last.
            if (ent.getXVel() != 0 || ent.getYVel() != 0) {
                moving_ents.add(ent);
            }
        }

        // This is to prevent the hero being pushed into walls etc.
        // The hero is the last to be corrected.
        moving_ents.add(hero);
        return moving_ents;
    }

    private void updateEntitiesPosition() {
        if (left)
            hero.setXPos(hero.getXPos() - hero.getXVel());
        if (right)
            hero.setXPos(hero.getXPos() + hero.getXVel());
        hero.setYPos(hero.getYPos() + hero.getYVel());

        for (Entity ent : getMovingEntities()) {
            if (ent == hero) continue;
            ent.setXPos(ent.getXPos() + ent.getXVel());

            // Bullet's are not controlled by gravity, but independently.
            // Need to update the Bullet's y-axis too.
            // Delete the bullet if it has travelled far away.
            if (ent.getClass() == Bullet.class) {
                ent.setYPos(ent.getYPos() + ent.getYVel());
                double x_travel = Math.abs(hero.getXPos() - ent.getXPos());
                double y_travel = Math.abs(hero.getYPos() - ent.getYPos());
                if (x_travel > 1000 || y_travel > 1000)
                    toRemove.add(ent);
//                    entities.remove(ent);
            }
        }
    }

    private void applyGravity() {
        for (Entity ent : entities) {
            if (ent.getClass() == Bullet.class || ent.getClass() == YouWinBanner.class) continue; // bullets and win flag not effected by gravity.
            if (!(ent.getXVel() == 0) || !(ent.getYVel() == 0)) {
                ent.setYVel(ent.getYVel() + gravity);
                ent.setYPos(ent.getYPos() + ent.getYVel());
            }
        }
    }

    @Override
    public double getFloorHeight() {
        return 300;
    }

    @Override
    public double getHeroX() {
        return hero.getXPos();
    }

    @Override
    public double getHeroY() {
        return hero.getYPos();
    }

    @Override
    public void setHeroX(double xPos) {
        hero.setXPos(xPos);
    }

    @Override
    public boolean jump() {
        if (levelWon || levelLost)
            return false;

        // Can only jump twice before landing again.
        // Jump count is reset to 0 within tick() if the hero intersects
        // another entity from the top.
        if (jumpCount < 2) {
            jumpCount += 1;
            hero.setYVel(-3);
            return true;
        }
        return false;
    }

    @Override
    public boolean shoot() {
        if (levelLost || levelWon || !heroCanShoot)
            return false;

        Entity bullet = new Bullet(hero.getXPos(), hero.getYPos());
        if (left)
            bullet.setXVel(Math.abs(bullet.getXVel()) * -1);
        entities.add(bullet);
        return true;
    }

    @Override
    public boolean moveLeft() {
        right = false;
        left = true;
        return true;
    }

    @Override
    public boolean moveRight() {
        right = true;
        left = false;
        return true;
    }

    @Override
    public boolean stopMoving() {
        right = false;
        left = false;
        return true;
    }

    @Override
    public boolean levelLost() {
        return levelLost;
    }

    @Override
    public boolean levelWon() {
        return levelWon;
    }

    // EXTENSION -- Score tracker
    @Override
    public int currentScoreInfo(){
        return currentLevelScore;
    }

    @Override
    public void update(int scoreAdded) {
        this.currentLevelScore += scoreAdded;
    }

    @Override
    public int getSecondsPassed(){
        return secondsPassed;
    }

    @Override
    public Entity getHero(){
        return this.hero;
    }

    @Override
    public boolean isHeroDead(){
        return this.heroIsDead;
    }

    public Object clone() throws CloneNotSupportedException{
        // All the primitive values will be saved by using clone.
        LevelImp myClone = (LevelImp) super.clone();
        // Store Hero's primitive variables
        Hero hero = (Hero) this.hero;
        myClone.hp = hero.getHp();
        myClone.xPos = hero.getXPos();
        myClone.yPos = hero.getYPos();
//        myClone.levelStartTime = levelStartTime+secondsPassed;
        myClone.currentLevelScore = currentLevelScore;
        myClone.firstTracker = true;
//        myClone.secondsPassed = secondsPassed+;
        System.out.println(secondsPassed);

        // Now we have to manually overwrite the reference variables
        myClone.entities = new ArrayList<>();
        myClone.toAdd = new ArrayList<>();
        myClone.toRemove = new ArrayList<>();
        myClone.bg_drawer = new SkyOnlyBackground();
        for (Entity entity : entities){
            Entity newEntity;
            if (entity.getClass() == Hero.class){
                myClone.hero = Hero.getHero();
                newEntity = myClone.hero;
            }
            else if (entity.getClass() == YouWinBanner.class){
                myClone.youWin = entity;
                newEntity = entity;
            }
            else{
                newEntity = entity.newInstance();
            }
            myClone.entities.add(newEntity);
            if (toAdd.contains(entity)){
                myClone.toAdd.add(newEntity);
            }
            else if (toRemove.contains(entity)){
                myClone.toRemove.add(newEntity);
            }
        }
        System.out.println("OLD SAVE: "+ entities.size());
        System.out.println("NEW SAVE: "+ myClone.entities.size());
        return myClone;
    }

    public long getHeroHp(){
        return this.hp;
    }

    public double getXPos(){
        return this.xPos;
    }

    public double getYPos(){
        return this.yPos;
    }

    public void setLoaded(boolean b){
        this.loaded = b;
    }
}
