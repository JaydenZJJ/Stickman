package stickman.model;

import org.json.simple.JSONObject;
import stickman.Entities.Entity;
import stickman.levels.Level;
import stickman.levels.LevelDirector;
import stickman.levels.LevelImp;
import stickman.view.BackgroundDrawer;

import java.util.List;

public class Memento {
	// This class will contain a deep copy of every attribute of the level/game engine that is required for S/L

	// Fields related to GE - Score
	int totalScore;
	boolean lastCount;

	double heroXPos;
	double heroYPos;
	long heroHp;

	// Fields related to GE - Level Transition
	int currentLevelNumber;

	// Deep Copy of current level
	Level currentLevel;

	public Memento(int totalScore, boolean lastCount, int currentLevelNumber, Level currentLevel, long heroHp, double heroXPos, double heroYPos) {
		this.totalScore = totalScore;
		this.lastCount = lastCount;
		this.currentLevelNumber = currentLevelNumber;
		this.currentLevel = currentLevel;
		this.heroXPos = heroXPos;
		this.heroYPos = heroYPos;
		this.heroHp = heroHp;
	}
	public Memento(int totalScore, boolean lastCount, int currentLevelNumber, Level currentLevel) {
		this.totalScore = totalScore;
		this.lastCount = lastCount;
		this.currentLevelNumber = currentLevelNumber;
		this.currentLevel = currentLevel;
		this.heroXPos = ((LevelImp) currentLevel).getXPos();
		this.heroYPos = ((LevelImp) currentLevel).getYPos();
		this.heroHp = ((LevelImp) currentLevel).getHeroHp();
	}

	public int getTotalScore(){
		return this.totalScore;
	}

	public boolean getLastCount(){
		return this.lastCount;
	}

	public int getCurrentLevelNumber(){
		return this.currentLevelNumber;
	}
	
	public Level getCurrentLevel(){
		return this.currentLevel;
	}

	public Memento getNewInstance(){
		int score = totalScore;
		boolean count = lastCount;
		int levelNumber = currentLevelNumber;
		LevelImp level = null;
		try{
			level = (LevelImp)((LevelImp)currentLevel).clone();
		}
		catch (CloneNotSupportedException e){
			System.out.println(e);
			System.exit(1);
		}
		return new Memento(score,count,levelNumber,level, heroHp, heroXPos, heroYPos);
	}
}
