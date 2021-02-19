package stickman.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import stickman.Entities.Entity;
import stickman.Entities.Hero;
import stickman.levels.Level;
import stickman.model.GameEngine;
import java.util.ArrayList;
import java.util.List;

public class GameWindow {
    private final int width;
    private final int height;
    private Scene scene;
    private Pane pane;
    private GameEngine model;
    private List<EntityView> entityViews;

    // EXTENSION -- Score/HP Counter
    private Text scoreCounter;
    private Text timeCounter;
    private Text hpCounter;
    private Text totalScoreCounter;

    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;
    private static final double X_VIEWPORT_MARGIN = 200;
    private static final double Y_VIEWPORT_MARGIN = 100;

    public GameWindow(GameEngine model, int width, int height) {
        this.model = model;
        this.pane = new Pane();
        this.width = width;
        this.height = height;
        this.scene = new Scene(pane, width, height);

        this.entityViews = new ArrayList<>();

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(model);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);

        // EXTENSION -- Score Counter
        scoreCounter = new Text();
        scoreCounter.setFill(Color.BLACK);
        scoreCounter.setX(0.0);
        scoreCounter.setY(10.0);
        scoreCounter.setViewOrder(0.0);
        pane.getChildren().add(scoreCounter);
        // EXTENSION -- Score Counter
        totalScoreCounter = new Text();
        totalScoreCounter.setFill(Color.BLACK);
        totalScoreCounter.setX(0.0);
        totalScoreCounter.setY(20.0);
        totalScoreCounter.setViewOrder(0.0);
        pane.getChildren().add(totalScoreCounter);
        // EXTENSION -- Time Counter
        timeCounter = new Text();
        timeCounter.setFill(Color.BLACK);
        timeCounter.setX(0.0);
        timeCounter.setY(30.0);
        timeCounter.setViewOrder(0.0);
        pane.getChildren().add(timeCounter);
        // EXTENSION -- HP Counter
        hpCounter = new Text();
        hpCounter.setFill(Color.BLACK);
        hpCounter.setX(0.0);
        hpCounter.setY(40.0);
        hpCounter.setViewOrder(0.0);
        pane.getChildren().add(hpCounter);

        model.getCurrentLevel().getBGDrawer().draw(model, pane);
    }

    public Scene getScene() {
        return this.scene;
    }

    public void run() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17),
                t -> this.draw()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void draw() {
        model.tick();
        scoreCounter.setText("Current Level Score:  "+(model.scoreInfo()));
        timeCounter.setText("Current Elapsed Time:  "+(model.getCurrentLevel().getSecondsPassed()));
        hpCounter.setText("Current HP Left:  "+((Hero)(model.getCurrentLevel().getHero())).getHp());
        totalScoreCounter.setText("Total Score:  "+model.totalScoreInfo());
        Level currentLevel = model.getCurrentLevel();
        List<Entity> entities = currentLevel.getEntities();

        for (EntityView entityView: entityViews) {
            entityView.markForDelete();
        }

        double heroXPos = model.getCurrentLevel().getHeroX();
        double heroYPos = model.getCurrentLevel().getHeroY();
        heroXPos -= xViewportOffset;
        heroYPos -= yViewportOffset;

        // Correct X-axis camera
        if (heroXPos < X_VIEWPORT_MARGIN) {
            if (xViewportOffset >= 0) { // Don't go further left than the start of the level
                xViewportOffset -= X_VIEWPORT_MARGIN - heroXPos;
                if (xViewportOffset < 0) {
                    xViewportOffset = 0;
                    model.getCurrentLevel().setHeroX(X_VIEWPORT_MARGIN);
                }
            }
        } else if (heroXPos > width - X_VIEWPORT_MARGIN) {
            xViewportOffset += heroXPos - (width - X_VIEWPORT_MARGIN);
        }

        // Correct Y-axis camera
        if (heroYPos > (height - Y_VIEWPORT_MARGIN)) {
            yViewportOffset += heroYPos - (height - Y_VIEWPORT_MARGIN);
            if (yViewportOffset > 0)
                yViewportOffset = 0;
        } else if (heroYPos < Y_VIEWPORT_MARGIN) {
            yViewportOffset -= Y_VIEWPORT_MARGIN - heroYPos;
        }

        currentLevel.getBGDrawer().update(xViewportOffset, yViewportOffset);

        for (Entity entity: entities) {
            boolean notFound = true;
            for (EntityView view: entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update(xViewportOffset, yViewportOffset);
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (EntityView entityView: entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }
        entityViews.removeIf(EntityView::isMarkedForDelete);
    }
}
