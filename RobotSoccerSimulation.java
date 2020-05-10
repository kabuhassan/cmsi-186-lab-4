import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class RobotSoccerSimulation extends JPanel {
    private static final long serialVersionUID = -5228718339006830546L;

    // The soccer field
    private static double WIDTH = 400;
    private static double HEIGHT = 600;

    // Simulation parameters: in your lab, set these from command line arguments
    private static double PLAYER_RADIUS;
    private static double ENEMY_RADIUS;
    private static double PLAYER_SPEED;
    private static double ENEMY_SPEED;
    private static double FRICTION;

    // Initially null; Set this to a string to end the simulation
    private volatile String endMessage;

    public RobotSoccerSimulation() {
        this.balls = new Ball[]{
                new Ball(0.0, HEIGHT, PLAYER_RADIUS, PLAYER_SPEED, Color.BLUE),
                new Ball(WIDTH * 0.25, 40, ENEMY_RADIUS, ENEMY_SPEED, Color.RED),
                new Ball(WIDTH * 0.75, 40, ENEMY_RADIUS, ENEMY_SPEED, Color.RED),
                new Ball(WIDTH / 2, HEIGHT / 2, ENEMY_RADIUS, ENEMY_SPEED, Color.RED)
        };
    }

    static class Ball {
        private double x;
        private double y;
        private double radius;
        private double speed;
        private Color color;

        Ball(double x, double y, double radius, double speed, Color color) {
            // You know what to do here :)
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.color = color;
        }

        void moveToward(double targetX, double targetY) {
            // Fill this in
            double dx = targetX - this.x;
            double dy = targetY - this.y;
            double v = this.speed / Math.hypot(dx, dy);
            this.x = this.x + v * dx;
            this.y = this.y + v * dy;
        }

        // Slow down the ball by FRICTION. Don't let it go negative, though!
        void applyFriction() {
            // Fill this in
            this.speed = this.speed - FRICTION;
        }

        // Returns whether the ball is *entirely* inside the goal
        boolean inside(Goal goal) {
            return (this.x - this.radius > goal.x - goal.w / 2) &&
                    (this.x + this.radius < goal.x + goal.w / 2) &&
                    (this.y - this.radius > goal.y - goal.h / 2) &&
                    (this.y + this.radius < goal.y + goal.h / 2); // <--------- FIX THIS
        }
    }

    private static Ball[] balls;

    private static class Goal {
        double x = WIDTH / 2;
        double y = 0;
        double w = 100;
        double h = 100;
    }

    private static Goal goal = new Goal();

    // You don't need to touch this one.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (var ball : balls) {
            g.setColor(ball.color);
            g.fillOval((int) (ball.x - ball.radius), (int) (ball.y - ball.radius), (int) ball.radius * 2,
                    (int) ball.radius * 2);
        }
        g.setColor(new Color(255, 255, 255, 128));
        g.fillRect((int) (goal.x - goal.w / 2), (int) (goal.y - goal.h / 2), (int) goal.w, (int) goal.h);
        if (endMessage != null) {
            g.setFont(new Font("Arial", Font.PLAIN, 50));
            g.setColor(Color.RED);
            g.drawString(endMessage, 30, (int) HEIGHT / 2);
        }
    }

    private void runTheAnimation() {
        while (endMessage == null) {
            //
            // TODO: Update the state of the simulation.
            for (Ball ball : balls) {
                ball.applyFriction();
                if (ball == balls[0])
                    ball.moveToward(goal.x, goal.y);
                else ball.moveToward(balls[0].x, balls[0].y);
            }
            adjustIfCollisions();
            endSimulationIfNecessary();
            // Note that it is okay, and in fact preferred, for you to add helper methods.
            // For example, you should probably write private helper methods for collision
            // detection and adjustment, and for ending the simulation by setting the
            // proper endMessage.
            //
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    private void endSimulationIfNecessary() {
        if (balls[0].speed <= 0) {
            endMessage = "Oh no !";
        } else if (balls[0].inside(goal)) {
            endMessage = "GOOOAAAL!!!";
        }
    }


    private void adjustIfCollisions() {
        for (Ball b1 : balls) {
            for (Ball b2 : balls) {
                if (b1 != b2) {
                    double dx = b2.x - b1.x;
                    double dy = b2.y - b1.y;
                    double distance = Math.hypot(dx, dy);
                    double overlap = b1.radius + b2.radius - distance;
                    if (overlap > 0) {
                        double adjustX = (overlap / 2) * (dx / distance);
                        double adjustY = (overlap / 2) * (dy / distance);
                        b1.x -= adjustX;
                        b1.y -= adjustY;
                        b2.x += adjustX;
                        b2.y += adjustY;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        RobotSoccerSimulation.PLAYER_RADIUS = Double.parseDouble(args[0]);
        RobotSoccerSimulation.ENEMY_RADIUS = Double.parseDouble(args[1]);
        RobotSoccerSimulation.PLAYER_SPEED = Double.parseDouble(args[2]);
        RobotSoccerSimulation.ENEMY_SPEED = Double.parseDouble(args[3]);
        RobotSoccerSimulation.FRICTION = Double.parseDouble(args[4]);
        SwingUtilities.invokeLater(() -> {
            var panel = new RobotSoccerSimulation();
            panel.setBackground(Color.GREEN.brighter());
            var frame = new JFrame("Robotic Soccer");

            frame.setSize(400, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
            new Thread(panel::runTheAnimation).start();
        });
    }
}
