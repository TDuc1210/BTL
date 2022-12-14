package uet.oop.bomberman.objects;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import uet.oop.bomberman.Input.InputHandler;
import uet.oop.bomberman.countdown.CountDown;
import uet.oop.bomberman.levels.LevelLoader;
import com.sun.javafx.perf.PerformanceTracker;
import uet.oop.bomberman.menu.LoseMenu;
import uet.oop.bomberman.menu.MainMenu;
import uet.oop.bomberman.menu.PauseMenu;
import uet.oop.bomberman.menu.StageMenu;
import uet.oop.bomberman.score.Score;
import uet.oop.bomberman.sound.Sound;

public class BombermanGame extends Application {
    public static int LEVEL = 1;
    public static int LIVES = 2;
    private final LevelLoader lvLoad = new LevelLoader();
    private static final Group root = new Group();
    private static final Scene scene = new Scene(root);
    private final String TITLE = "BombermanGame";
    private final InputHandler _input = new InputHandler();
    private GraphicsContext gc;
    private Canvas canvas;
    private static PerformanceTracker tracker;
    private MainMenu mainMenu = new MainMenu();
    private StageMenu stageMenu = new StageMenu();
    private PauseMenu pauseMenu = new PauseMenu();
    private LoseMenu loseMenu = new LoseMenu();
    public static boolean canvasAdded = true;
    public static CountDown countdown = new CountDown(200);

    @Override
    public void start(Stage stage) {
        Score.readHighScore();
        countdown.pause();
        Sound.playBGM();
        stage.getIcons().add(new Image("/textures/icon.jfif"));

        canvas = new Canvas(LevelLoader.SCREEN_WIDTH, LevelLoader.SCREEN_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        mainMenu.setUpMainMenu(canvas.getWidth(), canvas.getHeight(), lvLoad);

        root.getChildren().addAll(canvas, mainMenu);

        stage.setScene(scene);
        stage.show();

        _input.prepareActionHandlers(scene);

        tracker = PerformanceTracker.getSceneTracker(scene);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!Board.BGMusic) {
                    Sound.mediaPlayer.pause();
                } else {
                    Sound.mediaPlayer.play();
                }
                if (lvLoad.board.getLevelOver()) {
                    if (lvLoad.board.isPassLevel()) {
                        mainMenu.unlockStage(LEVEL + 1);
                        MainMenu.writeStageStatus();
                        mainMenu = new MainMenu();
                        mainMenu.setUpMainMenu(canvas.getWidth(), canvas.getHeight(), lvLoad);
                        stageMenu = new StageMenu();
                        stageMenu.setUpStageMenu(canvas.getWidth(), canvas.getHeight(), LEVEL, root, mainMenu, canvas, lvLoad);
                        root.getChildren().clear();
                        root.getChildren().add(stageMenu);
                    } else {
                        mainMenu = new MainMenu();
                        mainMenu.setUpMainMenu(canvas.getWidth(), canvas.getHeight(), lvLoad);
                        loseMenu = new LoseMenu();
                        loseMenu.setUpLoseMenu(canvas.getWidth(), canvas.getHeight(), LEVEL, root, mainMenu, canvas, lvLoad);
                        root.getChildren().clear();
                        root.getChildren().add(loseMenu);
                    }
                    lvLoad.board.setLevelOver(false);
                }
                if (!lvLoad.board.bombers.isEmpty()) {
                    _input.handleInput(lvLoad.board.bombers.get(0), lvLoad.board, lvLoad);
                }
                if (mainMenu.isStartGame()) {
                    if (canvasAdded) {
                        root.getChildren().clear();
                        root.getChildren().add(canvas);
                        canvasAdded = false;
                    }
                    if (Board.Pause) {
                        if (!root.getChildren().contains(pauseMenu)) {
                            pauseMenu = new PauseMenu();
                            pauseMenu.setUpPauseMenu(canvas.getWidth(), canvas.getHeight(), root, lvLoad);
                            root.getChildren().add(pauseMenu);
                        }
                    } else {
                        root.getChildren().remove(pauseMenu);
                    }
                    lvLoad.introLevel.show(gc, canvas.getWidth(), canvas.getHeight());
                    if (!lvLoad.introLevel.getShowIntro()) {
                        render();
                        update();
                    }
                }
                stage.setTitle(TITLE + "| " + (int) getFPS() + " rates");
            }
        };
        timer.start();
    }

    public void stop() {
        MainMenu.writeStageStatus();
        Score.writeHighScore();
    }

    public void update() {
        lvLoad.board.updateAllEntity(lvLoad);
    }

    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        lvLoad.board.renderAllEntity(gc, countdown);
    }

    private float getFPS() {
        float fps = tracker.getAverageFPS();
        tracker.resetAverageFPS();
        return fps;
    }
}