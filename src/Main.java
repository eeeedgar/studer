import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main extends JFrame {
    private List<Figure> figures = new ArrayList<>();

    private FigureType figureType;
    public int countOfClick = 0;
    private final MyPanel panel;

    public Main(String title) {
        super(title);
        setBounds(10, 50, 600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createFileMenu1());
        setJMenuBar(menuBar);
        panel = new MyPanel(true);
        add(panel);
        setVisible(true);
    }

    private JMenu createFileMenu1() {
        JMenu menuFile1 = new JMenu("Инструмент рисования");

        JMenuItem Point = new JMenuItem("Точка");
        JMenuItem Line = new JMenuItem("Прямая");
        JMenuItem Ellipse = new JMenuItem("Эллипс");
        JMenuItem Rectangle = new JMenuItem("Прямоугольник");
        JMenuItem Circle = new JMenuItem("Круг");
        JMenuItem Square = new JMenuItem("Квадрат");

        menuFile1.add(Point);
        menuFile1.addSeparator();
        menuFile1.add(Line);
        menuFile1.addSeparator();
        menuFile1.add(Ellipse);
        menuFile1.addSeparator();
        menuFile1.add(Rectangle);
        menuFile1.addSeparator();
        menuFile1.add(Circle);
        menuFile1.addSeparator();
        menuFile1.add(Square);

        Point.addActionListener(actionEvent -> figureType = FigureType.point);
        Line.addActionListener(actionEvent -> figureType = FigureType.line);
        Ellipse.addActionListener(actionEvent -> figureType = FigureType.ellipse);
        Rectangle.addActionListener(actionEvent -> figureType = FigureType.rectangle);
        Circle.addActionListener(actionEvent -> figureType = FigureType.circle);
        Square.addActionListener(actionEvent -> figureType = FigureType.square);

        return menuFile1;
    }

    private JMenu createFileMenu() {
        JMenu menuFile = new JMenu("Файл");
        JMenuItem open = new JMenuItem("Открыть...");
        JMenuItem save = new JMenuItem("Сохранить...");
        JMenuItem exit = new JMenuItem("Выход");
        menuFile.add(open);
        menuFile.addSeparator();
        menuFile.add(save);
        menuFile.addSeparator();
        menuFile.add(exit);
        exit.addActionListener(actionEvent -> System.exit(0));
        open.addActionListener(actionEvent -> {
            System.out.println("Щелкнули по пункту Открыть...");
            System.out.println(panel.point);
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file == null) {
                    return;
                }

                String fileName = file.getAbsolutePath();
                try {
                    readFromFile(fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        save.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file == null) {
                    return;
                }
                // save to file
                String fileName = file.getAbsolutePath();
                saveToFile(fileName);
            }
        });
        return menuFile;
    }

    private void saveToFile(String filename) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(filename, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        for (Figure f : figures) {
            String line = f.type.name() + " " + f.beginPoint.x + " " + f.beginPoint.y + " " + f.endPoint.x + " " + f.endPoint.y;
            writer.println(line);
        }
        writer.close();
    }

    private void readFromFile(String filename) throws IOException {
        figures = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            String line = br.readLine();

            while (!line.isEmpty()) {
                String[] data = line.split(" ");
                addFigure(data);
                line = br.readLine();
            }
        }
    }

    private void addFigure(String[] data) {
        System.out.println("add figure" + Arrays.stream(data).toList());
        FigureType type = FigureType.fromString(data[0]);
        Point beginPoint = new Point(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
        Point endPoint = new Point(Integer.parseInt(data[3]), Integer.parseInt(data[4]));
        figures.add(new Figure(type, beginPoint, endPoint));
    }

    public static void main(String[] args) {
        Main main = new Main("Заголовок окна");
    }

    class MyPanel extends JPanel implements MouseListener, MouseMotionListener {
        public Point point = null;
        public Point beginPoint = null;
        public Point endPoint = null;;

        public MyPanel(boolean isDoubleBuffered) {
            super(isDoubleBuffered);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            drawSaved(g);
            if (figureType == FigureType.point && point != null) {
                g.fillOval(point.x - 5, point.y - 5, 9, 9);
            } else {
                drawFigure(g);
            }
            g.drawString("Число кликов мыши: " + countOfClick, 10, 20);
        }

        private void reload() {
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            System.out.println("mouseClicked");
            point = mouseEvent.getPoint();
            countOfClick++;
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            System.out.println("mousePressed");
            beginPoint = mouseEvent.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            System.out.println("mouseReleased");
            endPoint = mouseEvent.getPoint();
            saveFigure();
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
            System.out.println("mouseEntered");
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            System.out.println("mouseExited");
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            endPoint = mouseEvent.getPoint();
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent mouseEvent) {
            repaint();
        }

        private void saveFigure() {
            figures.add(new Figure(figureType, beginPoint, endPoint));
        }

        private void drawSaved(Graphics g) {
            System.out.println(figures.size());

            for (Figure f : figures) {
                drawFigure(g, f);
            }
        }

        private void drawFigure(Graphics g) {
            if (endPoint != null && beginPoint != null) {
                if (figureType == FigureType.line) {
                    g.drawLine(beginPoint.x, beginPoint.y, endPoint.x, endPoint.y);
                }
                if (figureType == FigureType.ellipse) {
                    g.drawOval(beginPoint.x, beginPoint.y, endPoint.x - beginPoint.x, endPoint.y - beginPoint.y); // на ск вычитать?
                }
                if (figureType == FigureType.rectangle) {
                    g.drawRect(beginPoint.x, beginPoint.y, endPoint.x - beginPoint.x, endPoint.y - beginPoint.y);
                }
                if (figureType == FigureType.circle) {
                    g.drawOval(beginPoint.x, beginPoint.y, endPoint.x - beginPoint.x, endPoint.x - beginPoint.x);  // как перерисовывать и учитывать что икс равен игрек
                }
                if (figureType == FigureType.square) {
                    g.drawRect(beginPoint.x, beginPoint.y, endPoint.x - beginPoint.x, endPoint.x - beginPoint.x);
                }
            }
        }

        private void drawFigure(Graphics g, Figure f) {
            if (f.type == FigureType.point) {
                g.fillOval(f.beginPoint.x - 5, f.beginPoint.y - 5, 9, 9);
            }
            if (f.type == FigureType.line) {
                g.drawLine(f.beginPoint.x, f.beginPoint.y, f.endPoint.x, f.endPoint.y);
            }
            if (f.type == FigureType.ellipse) {
                g.drawOval(f.beginPoint.x, f.beginPoint.y, f.endPoint.x - f.beginPoint.x, f.endPoint.y - f.beginPoint.y); // на ск вычитать?
            }
            if (f.type == FigureType.rectangle) {
                g.drawRect(f.beginPoint.x, f.beginPoint.y, f.endPoint.x - f.beginPoint.x, f.endPoint.y - f.beginPoint.y);
            }
            if (f.type == FigureType.circle) {
                g.drawOval(f.beginPoint.x, f.beginPoint.y, f.endPoint.x - f.beginPoint.x, f.endPoint.x - f.beginPoint.x);  // как перерисовывать и учитывать что икс равен игрек
            }
            if (f.type == FigureType.square) {
                g.drawRect(f.beginPoint.x, f.beginPoint.y, f.endPoint.x - f.beginPoint.x, f.endPoint.x - f.beginPoint.x);
            }
        }
    }
}

class Figure {
    public FigureType type;
    public Point beginPoint;
    public Point endPoint;

    public Figure(FigureType type, Point beginPoint, Point endPoint) {
        this.type = type;
        this.beginPoint = beginPoint;
        this.endPoint = endPoint;
    }

}

enum FigureType {
    point("point"),
    line("line"),
    ellipse("ellipse"),
    rectangle("rectangle"),
    square("square"),
    circle("circle");

    private final String text;

    FigureType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static FigureType fromString(String text) {
        for (FigureType b : FigureType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}