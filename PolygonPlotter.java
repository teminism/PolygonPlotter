package vsc6;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/*
 *  PolygonPlotter draws a regular polygon with a given number of sides,
 *  and also draws all its diagonals. It then animates the polygon by
 *  rotating it and zooming in and out.
 *
 *  Concepts illustrated by this code:
 *    * 2D graphics,
 *    * animation,
 *    * ragged arrays (non-rectangular 2D arrays).
 *
 *  Other concepts discussed
 *    * the 'final' keyword,
 *    * access modifiers,
 *    * exception handling.
 */
public class PolygonPlotter extends Plot /* which extends JComponent */ {

    /*
     *  The number of vertices of the polygon. This is set by the con-
     *  structor but then must not be changed, so it is declared to be
     *  final. This means it's safe to make it public. Typically, we
     *  make variables private to avoid them being altered by code
     *  that's outside our control, because that might make our data
     *  inconsistent -- for example, this class has an array of Color
     *  objects that stores the colours of the lines from each point,
     *  so we wouldn't want somebody to change numPoints so it didn't
     *  match the size of that array. But being final prevents that;
     *  we may as well leave the variable public so that a user of our
     *  component can look at it if they need to.
     */
    public final int numPoints;

    /*
     *  Constants to control the animation: the delay between frames (in
     *  milliseconds) and the amount the image rotates per frame (in rad-
     *  ians). Note that these are declared final, so their values cannot
     *  change, and that their names are in uppercase, which is Java's
     *  convention for final variables that are initialized to fixed val-
     *  ues. This is in contrast with numPoints, which is final but which
     *  can be initialized to any value by the constructor.
     */
    private static final int ANIMATION_DELAY = 20;
    private static final double ROTATION_AMOUNT = Math.PI/1000d;

    /*
     *  The current rotation angle of the animation.
     */
    private double angle = 0;

    /*
     *  A random number generator. This is an alternative to using
     *  Math.random() and having to convert the resulting double in the
     *  range 0<=d<1 to the range you actually want.
     */
    private Random rand = new Random();

    /*
     *  This array will store the colour of each line in the plot.
     */
    private Color[][] colors;

    /*
     *  The constructor takes an int argument which is the number of
     *  points. While writing these comments, I realised that a
     *  polygon should have at least two points (three, really, but
     *  the program still works with two) so I added a check for that.
     *  IllegalArgumentException is an unchecked exception so we don't
     *  need to declare that we throw it and the caller of the con-
     *  structor doesn't need to catch it. But, if somebody tries to
     *  create a polygon with -17 sides, it's better that they get a
     *  meaningful exception than if their code crashes with some
     *  random error, such as a NegativeArraySizeException, that will
     *  make no sense to them. ("I wasn't trying to create an array.
     *  What are you talking about?")
     */
    public PolygonPlotter (int numPoints) {
        /*
         *  A valid polygon must have at least two points. The code
         *  will probably run with zero or one points, but will throw
         *  unhelpful exceptions if numPoints < 0.
         */
        if (numPoints < 2)
            throw new IllegalArgumentException ("Polygon must have at least two points.");

        /*
         *  Variable initialization and plot scaling.
         */
        this.numPoints = numPoints;
        setScaleX (-1.1, 1.1);
        setScaleY (-1.1, 1.1);

        /*
         *  Each line in the plot has its own colour. We generate the
         *  Color objects here and store them in a 2D array, so that
         *  colors[i][j] is the colour of the line from point i to
         *  point j. This has two benefits: it makes paintComponent()
         *  run faster because it has less work to do, and, if we use
         *  random colours, we'll get the same colours every time the
         *  window redraws, rather than picking new random colours
         *  each time.
         *
         *  Note that the line from j to i would be the same line as
         *  the one from i to j, so we only need one colour for it.
         *  Also, there's no line from a point to itself. This means
         *  that we only care about elements colors[i][j] where i<j.
         *  Declaring an array with new Color[numPoints][numPoints]
         *  would be somewhat wasteful, as the array would be a little
         *  under half-full. This isn't a huge deal (the array isn't
         *  very big so we wouldn't be wasting much memory) but I've
         *  used it as an opportunity to illustrate so-called ragged
         *  arrays: 2D arrays where the rows have different lengths.
         *  This can be done because a 2D array is literally an array
         *  of 1D arrays and we can make these have different lengths.
         */
        colors = new Color[numPoints][];
        for (int i = 0; i < numPoints; i++) {
            /*
             *  Create the i-th inner array with length i.
             */
            colors[i] = new Color[i];

            /*
             * Assign the colours in the inner array.
             */
            for (int j = 0; j < i; j++) {
                /*
                 *  Purely random colours.
                 */
                //colors[i][j] = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
                /*
                 *  Colours based on blending orange and blue.
                 */
                colors[i][j] = new Color(scaleToColor(i), scaleToColor(j), 255 - (scaleToColor(i) + scaleToColor(j)) / 2);
            }
        }
    }

    /*
     *  A method used in the constructor, to avoid typing the same
     *  formula again and again.
     */
    private int scaleToColor (int x) {
        return x*256/numPoints;
    }

    /*
     *  paintComponent() method to draw the animation. This is not
     *  called explicitly: Java will call it for us whenever the
     *  window needs to be redrawn, e.g., when it's resized or
     *  uncovered. Also, when we call repaint().
     */
    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent (g);

        /*
         *  Set the parameters of the Plot component based on the
         *  current window size. Setting both width and height to
         *  the minimum of the component's width and height ensures
         *  a square plot that fits completely within the window.
         */
        width = height = Math.min (this.getWidth(),this.getHeight());

        /*
         *  Mess around with the scaling so we zoom in and out.
         */
        double scaling = 0.5 + Math.sin (angle);
        setScaleX (-scaling,+scaling);
        setScaleY (-scaling,+scaling);

        /*
         *  Draw the polygon. The repeated calculation should really
         *  be pulled out into a separate method.
         */
        for (int i = 0; i < numPoints; i++)
            for (int j = 0; j < i; j++) {
                g.setColor (colors[i][j]);
                g.drawLine(scaleX(Math.cos(angle + 2 * Math.PI * i / numPoints)),
                        scaleY(Math.sin(angle + 2 * Math.PI * i / numPoints)),
                        scaleX(Math.cos(angle + 2 * Math.PI * j / numPoints)),
                        scaleY(Math.sin(angle + 2 * Math.PI * j / numPoints)));
            }
    }

    /*
     *  Main method to set up the window and run the animation.
     */
    public static void main (String[] args) {
        JFrame f = new JFrame ("Polygon Plotter");
        f.setSize (500,500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        PolygonPlotter polyPlot = new PolygonPlotter(20);
        f.add (polyPlot);
        f.setVisible(true);

        /*
         *  Loop forever (or, at least, until the program is closed).
         */
        while (true) {
            try {
                Thread.sleep(ANIMATION_DELAY);
            } catch (InterruptedException ignored) {
                /*
                 *  Normally, you should do something to handle an
                 *  exception: ignoring it is almost always the wrong
                 *  idea. However, in this case, what's the worst that
                 *  could happen? We tried to sleep for some amount of
                 *  time and, for some reason, the sleep ended early.
                 *  Worst case is that the next frame will be drawn a
                 *  small fraction of a second too early, which isn't
                 *  a big deal. Perhaps, though, we should still print
                 *  a warning message to System.err, since the excep-
                 *  tion might be an indication of some underlying
                 *  problem.
                 *
                 *  Calling the exception parameter "ignored" tells
                 *  IntelliJ not to warn us about the empty exception
                 *  handler.
                 */
            }

            /*
             *  Rotate by a small amount each frame. The %= operator
             *  calculates the remainder after division for doubles
             *  as well as ints, and it's used here to ensure the
             *  angle is always a number between 0 and 2pi. Without
             *  this, the animation would stop after a very, very
             *  long time, since a double can only store so many
             *  significant figures. This means it can't tell the
             *  difference between [big number] and [bignumber]+0.1
             *  so adding the rotation amount stops having any effect.
             */
            polyPlot.angle += ROTATION_AMOUNT;
            polyPlot.angle %= 2*Math.PI;

            polyPlot.repaint();
        }
    }
}
