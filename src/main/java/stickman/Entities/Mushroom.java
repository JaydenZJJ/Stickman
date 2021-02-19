package stickman.Entities;

import stickman.levels.Observer;

public class Mushroom extends Entity implements Subject{

    private static final String imagePath = "/mushroom.png";
    private Observer ob;

    public Mushroom (double xPos, double yPos, double width, double height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.layer = Layer.FOREGROUND;
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }

    @Override
    public Entity newInstance() {
        return new Mushroom(this.getXPos(),this.getYPos(), this.width, this.height);
    }

    @Override
    public void attach(Observer ob) {
        this.ob = ob;
    }

    @Override
    public void detach() {
        this.ob = null;
    }

    @Override
    public void notifyScoreChanges() {
        // when hero picks up an mushroom, notify level to add 100 to score counter
        if (this.ob!=null) {
            ob.update(50);
        }
    }
}
