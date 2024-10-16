package jagex2.client;

import android.graphics.Paint;
import android.graphics.Typeface;

import jagex2.graphics.Pix24;
import jagex2.graphics.PixMap;
import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

import java.awt.*;
import java.awt.event.*;

// name taken from rsc
@OriginalClass("client!a")
public class GameShell implements Runnable {

	public Font font;
	public Font fontBold;
	public Paint fontMetrics;
	public Paint fontBoldMetrics;

	@OriginalMember(owner = "client!a", name = "g", descriptor = "I")
	private int state;

	@OriginalMember(owner = "client!a", name = "h", descriptor = "I")
	private int deltime = 20;

	@OriginalMember(owner = "client!a", name = "i", descriptor = "I")
	protected int mindel = 1;

	@OriginalMember(owner = "client!a", name = "j", descriptor = "[J")
	private final long[] otim = new long[10];

	@OriginalMember(owner = "client!a", name = "k", descriptor = "I")
	public int fps;

	@OriginalMember(owner = "client!a", name = "l", descriptor = "I")
	protected int screenWidth;

	@OriginalMember(owner = "client!a", name = "m", descriptor = "I")
	protected int screenHeight;

	@OriginalMember(owner = "client!a", name = "n", descriptor = "Ljava/awt/Graphics;")
	public static Graphics graphics;

	@OriginalMember(owner = "client!a", name = "o", descriptor = "Lclient!qb;")
	protected PixMap drawArea;

	@OriginalMember(owner = "client!a", name = "p", descriptor = "[Lclient!hb;")
	private final Pix24[] temp = new Pix24[6];

	@OriginalMember(owner = "client!a", name = "q", descriptor = "Lclient!b;")
	public static ViewBox frame;

	@OriginalMember(owner = "client!a", name = "r", descriptor = "Z")
	private boolean refresh = true;

	@OriginalMember(owner = "client!a", name = "s", descriptor = "I")
	protected static int idleCycles;

	@OriginalMember(owner = "client!a", name = "t", descriptor = "I")
	protected static int mouseButton;

	@OriginalMember(owner = "client!a", name = "u", descriptor = "I")
	protected static int mouseX;

	@OriginalMember(owner = "client!a", name = "v", descriptor = "I")
	protected static int mouseY;

	@OriginalMember(owner = "client!a", name = "w", descriptor = "I")
	protected static int mouseClickButton;

	@OriginalMember(owner = "client!a", name = "x", descriptor = "I")
	protected static int mouseClickX;

	@OriginalMember(owner = "client!a", name = "y", descriptor = "I")
	protected static int mouseClickY;

	@OriginalMember(owner = "client!a", name = "z", descriptor = "[I")
	protected static int[] actionKey = new int[128];

	@OriginalMember(owner = "client!a", name = "A", descriptor = "[I")
	private static int[] keyQueue = new int[128];

	@OriginalMember(owner = "client!a", name = "B", descriptor = "I")
	private int keyQueueReadPos;

	@OriginalMember(owner = "client!a", name = "C", descriptor = "I")
	private static int keyQueueWritePos;

	private boolean hasFocus = true;



	@OriginalMember(owner = "client!a", name = "run", descriptor = "()V")
	public void run() {
/*		this.getBaseComponent().addMouseListener(this);
		this.getBaseComponent().addMouseMotionListener(this);
		this.getBaseComponent().addKeyListener(this);
		this.getBaseComponent().addFocusListener(this);

		if (this.frame != null) {
			this.frame.addWindowListener(this);
		}*/

		this.drawProgress("Loading...", 0);
		this.load();

		@Pc(41) int opos = 0;
		@Pc(43) int ratio = 256;
		@Pc(45) int delta = 1;
		@Pc(47) int count = 0;

		for (@Pc(49) int i = 0; i < 10; i++) {
			this.otim[i] = System.currentTimeMillis();
		}

		@Pc(62) long last = System.currentTimeMillis();
		while (this.state >= 0) {
			if (this.state > 0) {
				this.state--;

				if (this.state == 0) {
					this.shutdown();
					return;
				}
			}

			@Pc(82) int lastRatio = ratio;
			@Pc(84) int lastDelta = delta;
			ratio = 300;
			delta = 1;
			last = System.currentTimeMillis();

			if (this.otim[opos] == 0L) {
				ratio = lastRatio;
				delta = lastDelta;
			} else if (last > this.otim[opos]) {
				ratio = (int) ((this.deltime * 2560L) / (last - this.otim[opos]));
			}

			if (ratio < 25) {
				ratio = 25;
			} else if (ratio > 256) {
				ratio = 256;
				delta = (int) ((long) this.deltime - (last - this.otim[opos]) / 10L);
			}

			this.otim[opos] = last;
			opos = (opos + 1) % 10;

			if (delta > 1) {
				for (@Pc(164) int i = 0; i < 10; i++) {
					if (this.otim[i] != 0L) {
						this.otim[i] += delta;
					}
				}
			}

			if (delta < this.mindel) {
				delta = this.mindel;
			}

			try {
				Thread.sleep(delta);
			} catch (@Pc(198) InterruptedException ignored) {
			}

			while (count < 256) {
				this.update();
				this.mouseClickButton = 0;
				this.keyQueueReadPos = this.keyQueueWritePos;
				count += ratio;
			}

			count &= 0xFF;

			if (this.deltime > 0) {
				this.fps = ratio * 1000 / (this.deltime * 256);
			}

			this.draw();
		}

		if (this.state == -1) {
			this.shutdown();
		}
	}

	@OriginalMember(owner = "client!a", name = "a", descriptor = "(I)V")
	private void shutdown() {
		this.state = -2;
		this.unload();

		if (this.frame != null) {
			try {
				System.exit(0);
			} catch (@Pc(25) Throwable ignored) {
			}
		}
	}

	@OriginalMember(owner = "client!a", name = "a", descriptor = "(II)V")
	protected final void setFramerate(@OriginalArg(1) int fps) {
		this.deltime = 1000 / fps;
	}

/*	@OriginalMember(owner = "client!a", name = "start", descriptor = "()V")
	@Override
	public final void start() {
		if (this.state >= 0) {
			this.state = 0;
		}
	}

	@OriginalMember(owner = "client!a", name = "stop", descriptor = "()V")
	@Override
	public final void stop() {
		if (this.state >= 0) {
			this.state = 4000 / this.deltime;
		}
	}

	@OriginalMember(owner = "client!a", name = "destroy", descriptor = "()V")
	@Override
	public final void destroy() {
		this.state = -1;

		try {
			Thread.sleep(1000L);
		} catch (@Pc(6) Exception ignored) {
		}

		if (this.state == -1) {
			this.shutdown();
		}
	}*/

	@OriginalMember(owner = "client!a", name = "update", descriptor = "(Ljava/awt/Graphics;)V")
	public final void update(@OriginalArg(0) Graphics g) {
		if (this.graphics == null) {
			this.graphics = g;
		}

		this.refresh = true;
		this.refresh();
	}

	@OriginalMember(owner = "client!a", name = "paint", descriptor = "(Ljava/awt/Graphics;)V")
	public final void paint(@OriginalArg(0) Graphics g) {
		if (this.graphics == null) {
			this.graphics = g;
		}

		this.refresh = true;
		this.refresh();
	}

	@OriginalMember(owner = "client!a", name = "mousePressed", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public static void mousePressed(int x, int y, int button) {
		idleCycles = 0;
		mouseClickX = x;
		mouseClickY = y;

		try {
			if (button == 2) {
				mouseClickButton = 2;
				mouseButton = 2;
			} else if (button == 1) {
				mouseClickButton = 1;
				mouseButton = 1;
			}

			if (InputTracking.enabled) {
				InputTracking.mousePressed(x, y, button == 2 ? 1 : 0);
			}
		} catch (NoSuchMethodError ex) {
			if (button == 2) {
				mouseClickButton = 2;
				mouseButton = 2;
			} else {
				mouseClickButton = 1;
				mouseButton = 1;
			}

			if (InputTracking.enabled) {
				InputTracking.mousePressed(x, y, button == 2 ? 1 : 0);
			}
		}
	}

	@OriginalMember(owner = "client!a", name = "mouseReleased", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public static void mouseReleased(int button) {
		idleCycles = 0;
		mouseButton = 0;

		try {
			if (InputTracking.enabled) {
				InputTracking.mouseReleased(button == 2 ? 1 : 0);
			}
		} catch (NoSuchMethodError ex) {
			if (InputTracking.enabled) {
				InputTracking.mouseReleased(button == 2 ? 1 : 0);
			}
		}
	}

	@OriginalMember(owner = "client!a", name = "mouseClicked", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public final void mouseClicked() {
	}

	@OriginalMember(owner = "client!a", name = "mouseEntered", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public static void mouseEntered() {
		if (InputTracking.enabled) {
			InputTracking.mouseEntered();
		}
	}

	@OriginalMember(owner = "client!a", name = "mouseExited", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public static void mouseExited() {
		// mapview applet
		idleCycles = 0;
		mouseX = -1;
		mouseY = -1;

		if (InputTracking.enabled) {
			InputTracking.mouseExited();
		}
	}

	@OriginalMember(owner = "client!a", name = "mouseDragged", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public static void mouseDragged(int x, int y) {
		idleCycles = 0;
		mouseX = x;
		mouseY = y;

		if (InputTracking.enabled) {
			InputTracking.mouseMoved(x, y);
		}
	}

	@OriginalMember(owner = "client!a", name = "mouseMoved", descriptor = "(Ljava/awt/event/MouseEvent;)V")
	public static void mouseMoved(int x, int y) {
		idleCycles = 0;
		mouseX = x;
		mouseY = y;

		if (InputTracking.enabled) {
			InputTracking.mouseMoved(x, y);
		}
	}



	@OriginalMember(owner = "client!a", name = "keyPressed", descriptor = "(Ljava/awt/event/KeyEvent;)V")
	public static void keyPressed(@OriginalArg(0) int c) {
		idleCycles = 0;

		@Pc(7) int code = c;
		@Pc(10) int ch = c;

		if (ch < 30) {
			ch = 0;
		}

		if (code == 37) {
			// KEY_LEFT
			ch = 1;
		} else if (code == 39) {
			// KEY_RIGHT
			ch = 2;
		} else if (code == 38) {
			// KEY_UP
			ch = 3;
		} else if (code == 40) {
			// KEY_DOWN
			ch = 4;
		} else if (code == 17) {
			// CONTROL
			ch = 5;
		} else if (code == 16) {
			// SHIFT
			ch = 6; // (custom)
		} else if (code == 18) {
			// ALT
			ch = 7; // (custom)
		} else if (code == 8) {
			// BACKSPACE
			ch = 8;
		} else if (code == 127) {
			// DELETE
			ch = 8;
		} else if (code == 9) {
			ch = 9;
		} else if (code == 10) {
			// ENTER
			ch = 10;
		} else if (code >= 112 && code <= 123) {
			//ch = code + 1008 - 112;
		} /*else if (code == 36) {
			ch = 1000;
		} else if (code == 35) {
			ch = 1001;
		} else if (code == 33) {
			ch = 1002;
		} else if (code == 34) {
			ch = 1003;
		}*/

		if (ch > 0 && ch < 128) {
			actionKey[ch] = 1;
		}

		if (ch > 4) {
			keyQueue[keyQueueWritePos] = ch;
			keyQueueWritePos = keyQueueWritePos + 1 & 0x7F;
		}

		if (InputTracking.enabled) {
			InputTracking.keyPressed(ch);
		}
	}

	@OriginalMember(owner = "client!a", name = "keyReleased", descriptor = "(Ljava/awt/event/KeyEvent;)V")
	public static void keyReleased(@OriginalArg(0) int c) {
		idleCycles = 0;

		@Pc(5) int code = c;
		@Pc(8) int ch = c;

		if (ch < 30) {
			ch = 0;
		}

		if (code == 37) {
			// KEY_LEFT
			ch = 1;
		} else if (code == 39) {
			// KEY_RIGHT
			ch = 2;
		} else if (code == 38) {
			// KEY_UP
			ch = 3;
		} else if (code == 40) {
			// KEY_DOWN
			ch = 4;
		} else if (code == 17) {
			// CONTROL
			ch = 5;
		} else if (code == 16) {
			// SHIFT
			ch = 6; // (custom)
		} else if (code == 18) {
			// ALT
			ch = 7; // (custom)
		} else if (code == 8) {
			// BACKSPACE
			ch = 8;
		} else if (code == 127) {
			// DELETE
			ch = 8;
		} else if (code == 9) {
			ch = 9;
		} else if (code == 10) {
			// ENTER
			ch = 10;
		} else if (code >= 112 && code <= 123) {
			//ch = code + 1008 - 112;
		} /*else if (code == 36) {
			ch = 1000;
		} else if (code == 35) {
			ch = 1001;
		} else if (code == 33) {
			ch = 1002;
		} else if (code == 34) {
			ch = 1003;
		}*/

		if (ch > 0 && ch < 128) {
			actionKey[ch] = 0;
		}

		if (InputTracking.enabled) {
			InputTracking.keyReleased(ch);
		}
	}

	@OriginalMember(owner = "client!a", name = "keyTyped", descriptor = "(Ljava/awt/event/KeyEvent;)V")
	public static void keyTyped(@OriginalArg(0) int c) {
	}

	@OriginalMember(owner = "client!a", name = "focusGained", descriptor = "(Ljava/awt/event/FocusEvent;)V")
	public final void focusGained(@OriginalArg(0) int c) {
		this.hasFocus = true; // mapview applet
		this.refresh = true;
		this.refresh();

		if (InputTracking.enabled) {
			InputTracking.focusGained();
		}
	}

	@OriginalMember(owner = "client!a", name = "focusLost", descriptor = "(Ljava/awt/event/FocusEvent;)V")
	public final void focusLost(@OriginalArg(0) int c) {
		this.hasFocus = false; // mapview applet
		if (InputTracking.enabled) {
			InputTracking.focusLost();
		}
	}

	@OriginalMember(owner = "client!a", name = "a", descriptor = "(Z)I")
	protected final int pollKey() {
		@Pc(1) int key = -1;
		if (this.keyQueueWritePos != this.keyQueueReadPos) {
			key = this.keyQueue[this.keyQueueReadPos];
			this.keyQueueReadPos = this.keyQueueReadPos + 1 & 0x7F;
		}
		return key;
	}
/*

	@OriginalMember(owner = "client!a", name = "windowActivated", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowActivated(@OriginalArg(0) WindowEvent e) {
	}

	@OriginalMember(owner = "client!a", name = "windowClosed", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowClosed(@OriginalArg(0) WindowEvent e) {
	}

	@OriginalMember(owner = "client!a", name = "windowClosing", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowClosing(@OriginalArg(0) WindowEvent e) {
		this.destroy();
	}

	@OriginalMember(owner = "client!a", name = "windowDeactivated", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowDeactivated(@OriginalArg(0) WindowEvent e) {
	}

	@OriginalMember(owner = "client!a", name = "windowDeiconified", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowDeiconified(@OriginalArg(0) WindowEvent e) {
	}

	@OriginalMember(owner = "client!a", name = "windowIconified", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowIconified(@OriginalArg(0) WindowEvent e) {
	}

	@OriginalMember(owner = "client!a", name = "windowOpened", descriptor = "(Ljava/awt/event/WindowEvent;)V")
	public final void windowOpened(@OriginalArg(0) WindowEvent e) {
	}
*/

	@OriginalMember(owner = "client!a", name = "a", descriptor = "()V")
	protected void load() {
	}

	@OriginalMember(owner = "client!a", name = "b", descriptor = "(I)V")
	protected void update() {
	}

	@OriginalMember(owner = "client!a", name = "a", descriptor = "(B)V")
	protected void unload() {
	}

	@OriginalMember(owner = "client!a", name = "b", descriptor = "(Z)V")
	protected void draw() {
	}

	@OriginalMember(owner = "client!a", name = "c", descriptor = "(I)V")
	protected void refresh() {
	}

/*	@OriginalMember(owner = "client!a", name = "b", descriptor = "(B)Ljava/awt/Component;")
	public Component getBaseComponent() {
		if (this.frame != null) {
			return this.frame;
		}

		return this;
	}*/

	@OriginalMember(owner = "client!a", name = "a", descriptor = "(Ljava/lang/Runnable;I)V")
	public void startThread(@OriginalArg(0) Runnable runnable, @OriginalArg(1) int priority) {
		@Pc(4) Thread thread = new Thread(runnable);
		thread.start();
		thread.setPriority(priority);
	}

	@OriginalMember(owner = "client!a", name = "a", descriptor = "(ZLjava/lang/String;I)V")
	protected void drawProgress(@OriginalArg(1) String message, @OriginalArg(2) int progress) {/*
		while (this.graphics == null) {
			this.graphics = this.getBaseComponent().getGraphics();

			try {
				this.getBaseComponent().repaint();
			} catch (@Pc(22) Exception ignored) {
			}

			try {
				Thread.sleep(1000L);
			} catch (@Pc(26) Exception ignored) {
			}
		}

		@Pc(37) Font bold = new Font("Helvetica", Font.BOLD, 13);
		@Pc(44) FontMetrics boldMetrics = this.getBaseComponent().getFontMetrics(bold);
		@Pc(51) Font plain = new Font("Helvetica", Font.PLAIN, 13);
		FontMetrics plainMetrics = this.getBaseComponent().getFontMetrics(plain);

		if (this.refresh) {
			this.graphics.setColor(Color.black);
			this.graphics.fillRect(0, 0, this.screenWidth, this.screenHeight);
			this.refresh = false;
		}

		@Pc(84) Color barColor = new Color(140, 17, 17);
		@Pc(91) int y = this.screenHeight / 2 - 18;

		this.graphics.setColor(barColor);
		this.graphics.drawRect(this.screenWidth / 2 - 152, y, 304, 34);
		this.graphics.fillRect(this.screenWidth / 2 - 150, y + 2, progress * 3, 30);

		this.graphics.setColor(Color.black);
		this.graphics.fillRect(this.screenWidth / 2 + progress * 3 - 150, y + 2, 300 - progress * 3, 30);

		this.graphics.setFont(bold);
		this.graphics.setColor(Color.white);
		this.graphics.drawString(message, (this.screenWidth - boldMetrics.stringWidth(message)) / 2, y + 22);*/
		System.out.println("[" + progress + "%]: " + message);
		if (fontMetrics == null) {
			android.graphics.Paint paint = new android.graphics.Paint();
			paint.setTextSize(13);
			paint.setTypeface(Typeface.create("SansSerif", Typeface.NORMAL));
			font = new java.awt.Font("SansSerif", Font.PLAIN, 13);
			fontMetrics = paint;

			android.graphics.Paint paint1 = new Paint();
			paint1.setTextSize(13);
			paint1.setTypeface(Typeface.create("SansSerif", Typeface.BOLD));
			fontBold = new java.awt.Font("SansSerif", Font.BOLD, 13);
			fontBoldMetrics = paint1;
		}
	}
}
