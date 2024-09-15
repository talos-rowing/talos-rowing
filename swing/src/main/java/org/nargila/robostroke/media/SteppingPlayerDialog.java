package org.nargila.robostroke.media;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.media.vlc.VlcSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class SteppingPlayerDialog extends JDialog {

    private static final Logger logger = LoggerFactory.getLogger(SteppingPlayerDialog.class);

  private final JPanel contentPanel = new JPanel();
  private FrameSteppingPlayerPanel player;
  private String mrl;

  public final AtomicReference<Pair<String,ClockTime>> res = new  AtomicReference<Pair<String,ClockTime>>();
  private boolean stopRequested;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      SteppingPlayerDialog dialog = new SteppingPlayerDialog();
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      Pair<String, ClockTime> res = dialog.launch(args[0]);

      if (res.second != null) {
        System.out.println(res.second);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public SteppingPlayerDialog() {

    VlcSetup.setupCheckVlc(this);


    setModalityType(ModalityType.APPLICATION_MODAL);
    setModal(true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setTitle("Find Sync Mark");
    setBounds(100, 100, 450, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));
    {
      player = new FrameSteppingPlayerPanel();
      player.setBorder(new LineBorder(Color.GRAY, 5));
      contentPanel.add(player);

      player.setTimeListener(new FrameSteppingPlayerPanel.TimeChangeListener() {

        @Override
                public void onTimeChanged(long time) {
                    setRes("S:1", ClockTime.fromMillis(time));
        }
      });
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
          @Override
                    public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
          @Override
                    public void actionPerformed(ActionEvent e) {

            synchronized (res) {
              stop();
              res.set(null);
            }

            setVisible(false);
          }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
  }


  private void setRes(String m, ClockTime time) {
    synchronized (res) {
      if (!stopRequested) {
        res.set(Pair.create(m, time));
      }
    }
  }

  @Override
    protected void processWindowEvent(WindowEvent e) {

    super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_OPENED) {
            runPlayer();
        }
    }

  @Override
  public void dispose() {
    stop();
    super.dispose();
  }

  public void stop() {

    stopRequested = true;

    synchronized (res) {
      res.notifyAll();
    }
  }

  private void runPlayer() {
    synchronized (res) {
      new Thread() {
        @Override
        public void run() {

          synchronized (res) {

            res.notifyAll();

            try {
                player.play(mrl);
              res.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("error while trying to play " + mrl, e);
            }
          }

          player.stop();
        }
      }.start();

      try {
        res.wait();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }


  public Pair<String,ClockTime> launch(String mrl) {

    this.mrl = mrl;

    setVisible(true);

    stop();

    return res.get();
  }
}
