package stickman.Entities;

import stickman.levels.Observer;

public interface Subject {
    void attach(Observer ob);
    void detach();
    void notifyScoreChanges();
}
