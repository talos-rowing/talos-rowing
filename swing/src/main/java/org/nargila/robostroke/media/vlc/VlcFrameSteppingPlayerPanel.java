package org.nargila.robostroke.media.vlc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nargila.robostroke.common.ClockTime;

public class VlcFrameSteppingPlayerPanel extends JPanel {

    public interface TimeChangeListener {
        public void onTimeChanged(long time);
    }

    private final JButton btnPlay;
    private final JButton btnNext;
    private TimeChangeListener timeListener;
    private final JSlider slider;
    private final JButton btnSkipBack;
    private final JButton btnSkipForeward;
    private final JLabel lblTime;
    private final JPanel movieArea;
    private VlcExternalMedia vlcMedia;

    /**
     * Create the panel.
     */
    public VlcFrameSteppingPlayerPanel() {
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        slider = new JSlider();
        slider.setValue(0);
        slider.setMaximum(1000);
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (slider.getValueIsAdjusting()) {
                    double pos = (double) slider.getValue() / slider.getMaximum();
                    vlcMedia.setTime((long) (pos * vlcMedia.getDuration()));
                }
            }
        });

        panel.add(slider);

        JPanel panel_1 = new JPanel();
        panel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        btnPlay = new JButton(">|=");
        btnPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vlcMedia.pause();
            }
        });
        panel_1.add(btnPlay);

        btnSkipBack = new JButton("-3");
        btnSkipBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vlcMedia.setTime(vlcMedia.getTime() -3000);
                updateTime();
            }
        });
        panel_1.add(btnSkipBack);

        btnSkipForeward = new JButton("+3");
        btnSkipForeward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vlcMedia.setTime(vlcMedia.getTime() + 3000);
                updateTime();
            }
        });
        panel_1.add(btnSkipForeward);

        btnNext = new JButton(">>");
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vlcMedia.step();
                updateTime();
            }
        });
        panel_1.add(btnNext);

        JPanel panel_3 = new JPanel();
        panel_1.add(panel_3);

        lblTime = new JLabel("00:00:00,000");
        panel_3.add(lblTime);

        movieArea = new JPanel();
        movieArea.setBackground(Color.YELLOW);             

        add(movieArea, BorderLayout.CENTER);
        movieArea.setLayout(new BorderLayout(0, 0));

    }

    public void setTimeListener(TimeChangeListener listener) {
        this.timeListener = listener;		
    }

    private void updateTime() {
        long time = vlcMedia.getTime();
        timeListener.onTimeChanged(time);
        lblTime.setText(ClockTime.fromMillis(time).toString());
        double pos = (double)time / vlcMedia.getDuration();
        slider.setValue((int) (pos * slider.getMaximum()));
    }

    public static void main(String[] args) throws Exception {

        final VlcFrameSteppingPlayerPanel player = new VlcFrameSteppingPlayerPanel();

        JFrame f = new JFrame("Test Player");
        //	    f.setIconImage(new ImageIcon(MinimalTestPlayer.class.getResource("/icons/vlcj-logo.png")).getImage());
        f.setSize(800, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                player.stop();
            }
        });

        JPanel p = new JPanel(new BorderLayout());

        final JLabel lblTime = new JLabel();
        lblTime.setText("00:00:00,000");
        lblTime.setFont(Font.getFont(Font.MONOSPACED));

        p.add(lblTime, BorderLayout.SOUTH);

        p.add(player, BorderLayout.CENTER);

        f.setContentPane(p);

        f.setVisible(true);

        player.setTimeListener(new TimeChangeListener() {

            @Override
            public void onTimeChanged(long time) {
                updateTime(lblTime, time);
            }

            private void updateTime(final JLabel lblTime, long time) {
                ClockTime t = ClockTime.fromMillis(time);
                lblTime.setText(t.toString());
            }
        });

        player.play(args[0]);
    }

    public void play(String mrl) throws Exception {
        vlcMedia = new VlcExternalMedia(new File(mrl), movieArea);
        vlcMedia.play();
//        vlc.getMediaPlayer().playMedia(mrl);		
    }

    public void stop() {
        vlcMedia.stop();
    }
    
    public JPanel getMovieArea() {
        return movieArea;
    }
}
