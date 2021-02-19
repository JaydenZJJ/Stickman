package stickman.model;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import stickman.Entities.Entity;
import stickman.Entities.Ghost;
import stickman.Entities.Hero;
import stickman.Entities.Tree;
import stickman.levels.*;
import org.json.simple.JSONObject;

// In the extension, GameEngineImpl act as the originator class in the Memento pattern
public class GameEngineImpl implements GameEngine {
    Level currentLevel;
    int currentLevelNumber;
    LevelDirector levelDirector;
    JSONObject configuration;

    // EXTENSION - Level Transition
    long levelCount;

    // EXTENSION - Total Score
    int totalScore;
    boolean lastCount = true;

    // EXTENSION - Memento - Snapshot
//    Snapshot snapshot;
    // EXTENSION - Caretaker
    CareTaker careTaker;

    public GameEngineImpl(JSONObject configuration) {
        this.configuration = configuration;
        this.currentLevelNumber = 1;

        // EXTENSION - Level Transition
        this.levelCount = (long)((JSONObject)configuration.get("game")).get("levelcount");

        // EXTENSION - Score Tracker
        this.totalScore = 0;

        // EXTENSION - Caretaker
        careTaker = new CareTaker();

        startLevel();
    }

    private void loadLevel(int levelNumber) {
        JSONObject levels = (JSONObject)configuration.get("levels");

        String key = String.valueOf(levelNumber);
        JSONObject level = (JSONObject)levels.get(key);

        if (level != null) {
            // if the upcoming level is the last level, then tell the level builder
            if (this.currentLevelNumber == this.levelCount) {
                levelDirector = new LevelDirector(new DefaultLevelBuilder(level, true));
            } else {
                levelDirector = new LevelDirector(new DefaultLevelBuilder(level, false));
            }
            currentLevel = levelDirector.construct();
        }
    }

    @Override
    public Level getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public void startLevel() {
        loadLevel(this.currentLevelNumber);
    }

    @Override
    public boolean jump() {
        return currentLevel.jump();
    }

    @Override
    public boolean moveLeft() {
        currentLevel.moveLeft();
        return false;
    }

    @Override
    public boolean moveRight() {
        currentLevel.moveRight();
        return false;
    }

    @Override
    public boolean stopMoving() {
        currentLevel.stopMoving();
        return false;
    }

    @Override
    public void tick() {
        if (currentLevel.isHeroDead() & currentLevel.levelLost()){
            System.exit(1);
        }
        else if (currentLevel.levelLost()) {
            startLevel();
        }
        else if (currentLevel.levelWon()){
            if (this.currentLevelNumber!=this.levelCount){
                this.currentLevelNumber+=1;
                updateTotalScore();
                startLevel();
            }
            else{
                if(lastCount) {
                    updateTotalScore();
                    lastCount = false;
                }
            }
        }
        currentLevel.tick();
    }

    // EXTENSION -- Score Tracker
    @Override
    public int scoreInfo(){
        return getCurrentLevel().currentScoreInfo();
    }

    public int totalScoreInfo(){
        return this.totalScore;
    }

    public void updateTotalScore(){
        this.totalScore+=getCurrentLevel().currentScoreInfo();
    }

    // EXTENSION -- CREATE SNAPSHOT
    public void createMemento(){
        LevelImp level = null;
        try{
            level = (LevelImp)((LevelImp)currentLevel).clone();
        }
        catch (CloneNotSupportedException e){
            System.out.println(e);
            System.exit(1);
        }
        System.out.println(level.getXPos()+","+level.getYPos());
        careTaker.add(new Memento(totalScore, lastCount, currentLevelNumber, level));
    }

    public void getInfoFromMemento(){
        Memento memento = careTaker.get();
        if (memento==null){
            System.out.println("No save!");
            return;
        }
        this.totalScore = memento.getTotalScore();
        this.lastCount = memento.getLastCount();
        this.currentLevelNumber = memento.getCurrentLevelNumber();
        this.currentLevel = memento.getCurrentLevel();
        ((LevelImp)this.currentLevel).setLoaded(true);
        for (Entity entity : this.currentLevel.getEntities()){
            if (entity.getClass() == Ghost.class){
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17),
                        t -> {entity.setYPos(entity.getYPos() - 3);}));

                timeline.setCycleCount(10000);
                timeline.play();
            }
            else if(entity.getClass() == Hero.class){
                LevelImp castedLvl = (LevelImp)this.currentLevel;

                ((Hero) entity).setHp(memento.heroHp);
                entity.setXPos(memento.heroXPos);
                entity.setYPos(memento.heroYPos);
            }
        }
    }
}
