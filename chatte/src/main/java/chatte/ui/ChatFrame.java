/*
 * MIT License
 * 
 * Copyright (c) 2018 OLopes
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package chatte.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.StyleSheet;

import chatte.config.ConfigService;
import chatte.msg.ChatMessage;
import chatte.msg.ConfigMessage;
import chatte.msg.ConnectedMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.NewFriendMessage;
import chatte.msg.StatusMessage;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;
import chatte.resources.ResourceManager;

public class ChatFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private ResourceManager resourceManager;
	private MessageBroker messageBroker;
	private ConfigService configService;
	private Friend self;
	
	private JTextPane chatLogPane = new JTextPane();
	private HTMLEditorKit chatLogKit = new HTMLEditorKit();
	private HTMLDocument chatLogDoc = new HTMLDocument();

	private JTextPane inputPane = new JTextPane();
	private HTMLEditorKit inputKit = new HTMLEditorKit();
	private HTMLDocument inputDoc = new HTMLDocument();

	private DefaultListModel<Friend> listModel = new DefaultListModel<Friend>();
	private JList<Friend> memberList = new JList<Friend>(listModel);
	private GifPicker picker;

	private String lastStyle="odd";
	private Friend lastUser=null;
	private Date lastReceived = new Date();

	private static final String TEXT_SUBMIT = "text-submit";
	private static final String INSERT_BREAK = "insert-break";
	
	public ChatFrame(ResourceManager resourceManager, MessageBroker messageBroker, ConfigService configService) {
		this.resourceManager = resourceManager;
		this.messageBroker = messageBroker;
		this.configService = configService;
		this.self = configService.getSelf();
		messageBroker.addListener(this);
		setupUi();

	}

	void setupUi() {
		picker = new GifPicker(this.resourceManager);

		chatLogPane.setEditorKit(chatLogKit);
		chatLogPane.setEditable(false);
		resetScreen();

		inputPane.setEditorKit(inputKit);
		inputPane.setDocument(inputDoc);

	    InputMap input = inputPane.getInputMap();
	    KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
	     KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
	    input.put(shiftEnter, INSERT_BREAK);  // input.get(enter)) = "insert-break"
	    input.put(enter, TEXT_SUBMIT);

	    AbstractAction submitAction  = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
	        public void actionPerformed(ActionEvent e) {
				doSendMessage();
	        }
	    };
	    
		inputPane.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == 0) {
					doSendMessage();
					e.consume();
				}
			}
		});
		
	    ActionMap actions = inputPane.getActionMap();
	    actions.put(enter, submitAction);
	    
		setTitle("Grande Chato");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		// setSize(1024, 768);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				doExit();
			}
		});
		setLayout(new BorderLayout());

		JScrollPane jsp = new JScrollPane(chatLogPane);
		JScrollPane jsi = new JScrollPane(inputPane);
		JScrollPane jsm = new JScrollPane(memberList);

		listModel.addElement(self);
		memberList.setCellRenderer(new FriendRenderer());
		final JPopupMenu listMenu = new JPopupMenu();
		listMenu.add(new JMenuItem(new AbstractAction("Add new") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Not implemented", "Not implemented", JOptionPane.PLAIN_MESSAGE);
			}
		}));
		listMenu.add(new JMenuItem(new AbstractAction("Edit") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Not implemented", "Not implemented", JOptionPane.PLAIN_MESSAGE);
			}
		}));
		listMenu.add(new JMenuItem(new AbstractAction("Send msg") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Not implemented", "Not implemented", JOptionPane.PLAIN_MESSAGE);
			}
		}));
		
		memberList.setComponentPopupMenu(listMenu);
		memberList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					// Double-click detected
					int index = memberList.locationToIndex(evt.getPoint());
					memberList.setSelectedIndex(index);
					JOptionPane.showMessageDialog(null, "Not implemented", "Not implemented", JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		
		
		JToolBar jtool = new JToolBar();
		jtool.setFloatable(false);
		JButton setupButton = new JButton();
		setupButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/setup.png")));
		setupButton.setToolTipText("Preferences");
		setupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openPreferences();
			}
		});
		JButton newEmoticonButton = new JButton();
		newEmoticonButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/add.png")));
		newEmoticonButton.setToolTipText("Add new emoticona");
		newEmoticonButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewEmoticon();
			}
		});
		
		JButton insertEmoticonButton = new JButton();
		insertEmoticonButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/emoji.png")));
		insertEmoticonButton.setToolTipText("Insert emoticona");
		insertEmoticonButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectEmoticon();
			}
		});
		JButton pasteEmoticonButton = new JButton();
		pasteEmoticonButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/paste.png")));
		pasteEmoticonButton.setToolTipText("Paste emoticona from clipboard");
		pasteEmoticonButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteEmoticon();
			}
		});
		JButton sendButton = new JButton();
		sendButton.setText("Send");
		sendButton.setFont(sendButton.getFont().deriveFont(Font.BOLD, 12.0f));
		sendButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/send.png")));
		sendButton.setToolTipText("Send message");
	    sendButton.setVerticalTextPosition(SwingConstants.CENTER);
	    sendButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSendMessage();
			}
		});
		
		JButton clsButton = new JButton("   PANIC! CLS! PANIC!   ");
		// clsButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/cls.png")));
		clsButton.setToolTipText("PANIC! CLEAR SCREEN!");
		clsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetScreen();
				setState(JFrame.ICONIFIED);
			}
		});
		
		JButton snipButton = new JButton();
		snipButton.setIcon(new ImageIcon(ChatFrame.class.getResource("/snip.png")));
		snipButton.setToolTipText("Snipping tool");
		snipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launchSnipTool();
			}
		});
		
		jtool.add(setupButton);
		jtool.add(insertEmoticonButton);
		jtool.add(newEmoticonButton);
		jtool.add(pasteEmoticonButton);
		jtool.add(snipButton);
		jtool.add(Box.createHorizontalGlue());
		jtool.add(clsButton);
		jtool.add(Box.createHorizontalGlue());
		jtool.add(sendButton);
		
		JPanel typinPanel = new JPanel();
		typinPanel.setLayout(new BoxLayout(typinPanel, BoxLayout.PAGE_AXIS));
		// typinPanel.setLayout(new BorderLayout());
		typinPanel.add(jtool);
		typinPanel.add(jsi);
		
		typinPanel.setMinimumSize(new Dimension(0, 100));
		jsm.setMinimumSize(new Dimension(100, 0));
		
		JSplitPane msgSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, typinPanel);
		msgSplit.setResizeWeight(1.0);
		// msgSplit.setDividerLocation(600);
		JSplitPane chatSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsm, msgSplit);
		chatSplit.setResizeWeight(0.0);
		// chatSplit.setDividerLocation(300);
		getContentPane().add(chatSplit, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem quitItem = new JMenuItem("Quit", 'Q');
		fileMenu.add(quitItem);
		// quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doExit();
			}
		});
		
		setJMenuBar(menuBar);
		
		setIconImage(new ImageIcon(ChatFrame.class.getResource("/cat-upsidedown-icon.png")).getImage());

	}
	
	void doExit() {
		if (JOptionPane.showConfirmDialog(ChatFrame.this, 
				"Are you sure to close this window?", "Really Closing?", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
			System.exit(0);
		}
	}
	
	void resetScreen() {
		lastUser = null;
		lastReceived = new Date();

		chatLogDoc = new HTMLDocument();
		chatLogPane.setDocument(chatLogDoc);

		// add some styles to the html
		StyleSheet styleSheet = chatLogDoc.getStyleSheet();
		styleSheet.addRule("h1 {font : 12px monaco; color : black; font-weight: bold;}");
		styleSheet.addRule("div {font : 10px monaco; color : black; margin-top: 0px;}");
		// styleSheet.addRule("hr {border: 0px; width:90%; color : #333; height: 1px; background-color : #333333; margin: 0px; margin: 0px;}");
		// styleSheet.addRule(".rule {height: 1px; border-bottom: 1px solid #333333; margin-left: 10px; margin-right:10px;}");
		styleSheet.addRule(".even {background-color : #fcfcfc;}");
		styleSheet.addRule(".odd {background-color : #f5f5f5;}");
		styleSheet.addRule(".timestamp {color:#999999; text-align: right;}");
		for(int i = 0; i < UserColors.colors.length; i++)
			styleSheet.addRule(".from"+i+" {color: #"+UserColors.colors[i]+"; font-weight: bold; border-top: 1px solid #888888;}");
		styleSheet.addRule(".message {color : #333333;}");
		
		try {
			chatLogKit.insertHTML(chatLogDoc, chatLogDoc.getLength(), "<h1>Welcome Grande Chato</h1>", 0, 0, HTML.Tag.H1);
		} catch (BadLocationException | IOException e1) {
		}
		
	}
	
	void launchSnipTool() {
		String cmd = "%WINDIR%\\system32\\SnippingTool.exe";
		ProcessBuilder processBuilder = new ProcessBuilder(
				"cmd.exe", "/c", "START", cmd
				);
		try {
			processBuilder.start();
		} catch (IOException e) {
		}
	}
	
	TypedMessage extractInputMessage() {
		StringWriter sw = new StringWriter();
		try {
			new HTMLWriter(sw, inputDoc, 0, inputDoc.getLength()) {{
				setIndentSpace(0);
				setLineSeparator("");
			}}.write();
		} catch (IOException | BadLocationException e) {
			e.printStackTrace();
		}

		String text = sw.toString().replaceFirst("<html><head></head><body>", "").replaceFirst("</body></html>$", "");
		
		// extract resources
		Set<String> resourceRefs = new HashSet<>();
	    ElementIterator iterator = new ElementIterator(inputDoc);
	    Element element;
	    while ((element = iterator.next()) != null) {
	      AttributeSet attributes = element.getAttributes();
	      Object name = attributes.getAttribute(StyleConstants.NameAttribute);
	      if ((name instanceof HTML.Tag) && (name == HTML.Tag.IMG)) {
	    	  Object src = attributes.getAttribute(HTML.Attribute.SRC);
	    	  if(src == null) continue;
	    	  resourceRefs.add(String.valueOf(src).replaceFirst("chato:", ""));
	      }
	    }
		
		return new TypedMessage(self, text, resourceRefs);
	}

	void doSendMessage() {
		TypedMessage message = extractInputMessage();
		messageBroker.sendMessage(message);
		inputPane.setText(null);
		inputPane.requestFocus();
	}

	@MessageListener
	public void configUpdated(ConfigMessage msg) {
		// update nick
		
	}
	
	@MessageListener
	public void messageTyped(TypedMessage msg) {
		chatMessageReceived(new ChatMessage(msg));
	}
	
	@MessageListener
	public void chatMessageReceived(final ChatMessage msg) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayChatMessage(msg);
			}
		});
	}
	
	@MessageListener
	public void welcomeFriend(WelcomeMessage message) {
		// new friend just connected. Update Clients panel
		final Friend friend = message.getFrom();
		if(!listModel.contains(friend)) {
			listModel.addElement(friend);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayStatusMessage(new ConnectedMessage(friend));
			}
		});
	}
	
	@MessageListener
	public void byebyeFriend(final DisconnectedMessage message) {
		// new friend just disconnected.
		Friend friend = message.getFrom();
		listModel.removeElement(friend);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayStatusMessage(message);
			}
		});
	}
	
	boolean neq(String a, String b) {
		if(a == null || b == null) return false;
		return !a.equals(b);
	}
	
	void selectEmoticon() {
		if(picker.showSelectionDialog(this)) {
			String selectedImage = picker.getSelectedResource();
			insertEmoticon(selectedImage);
		}
		inputPane.requestFocus();
	}

	void addNewEmoticon() {
		JFileChooser jfc = new JFileChooser();
		int sel = jfc.showOpenDialog(this);
		if(sel == JFileChooser.APPROVE_OPTION) {
			String newRes = resourceManager.addResource(jfc.getSelectedFile());
			insertEmoticon("chato:"+newRes);
		}
		inputPane.requestFocus();
	}
	
	void insertEmoticon(String url) {
		String img = String.format("<img src=\"%s\" />", url);

		try {
			inputKit.insertHTML(inputDoc, inputPane.getCaretPosition(), img, 0, 0, HTML.Tag.IMG);
		} catch (Exception ex) {
		}
	}
	
	void pasteEmoticon() {
		try {
			Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				Image img = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
				String resource = resourceManager.addResource(img);
				img = null;
				if(null != resource)
					insertEmoticon("chato:"+resource);
			}
		} catch (Exception e) {}
	}
	
	void openPreferences() {
		SetupDialog dialog = new SetupDialog();
		dialog.setNick(self.getNick());
		dialog.setLport(self.getPort());
		dialog.setRhost("");
		dialog.setRport(6666);
		
		
		if(dialog.openDialog(this)) {
			// Send reconfigure message
			// check if something changed
			if(dialog.getLport() != self.getPort() || !self.getNick().equals(dialog.getNick())) {
				self.setNick(dialog.getNick());
				self.setPort(dialog.getLport());
				configService.addFriend(self);
			}
			
			if(!dialog.getRhost().isEmpty() && dialog.getRport() > 0) {
				try {
					InetAddress addr = InetAddress.getByName(dialog.getRhost());
					String ip = addr.getHostAddress();
					Friend friend = configService.getFriend(ip);
					if(friend == null) {
						friend = new Friend();
						friend.setNick(ip);
						friend.setHost(ip);
					}
					friend.setPort(dialog.getRport());
					configService.addFriend(friend);
					messageBroker.sendMessage(new NewFriendMessage(friend));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		inputPane.requestFocus();
	}
	
	void displayStatusMessage(StatusMessage msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(msg.getStatus()).append("\">");
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		sb.append("<span class=\"timestamp\">").append(fmt.format(new Date())).append("</span> ")
		.append(msg.getFrom())
		.append(" &lt;").append(msg.getStatus()).append("&gt;</div>");

		try {
			chatLogKit.insertHTML(chatLogDoc, chatLogDoc.getLength(), sb.toString(), 0, 0, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		chatLogPane.select(chatLogDoc.getLength(),chatLogDoc.getLength());
	}

	void displayChatMessage(final ChatMessage msg) {

		System.out.println("Chat message recieved from "+msg.getFrom());
		System.out.println(msg.getMessage());
		
		Date received = new Date();
		boolean newUser = false, newDate = false;
		if(lastUser != msg.getFrom()) {
			newUser = true;
			lastUser = msg.getFrom();
			lastStyle = "even".equals(lastStyle)?"odd":"even";
		}
		
		if(newUser || received.getTime()-lastReceived.getTime() > 60000L /*300000L*/) {
			lastReceived= received;
			newDate = true;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(lastStyle).append("\">");
		if(newUser || newDate) {
			int idx = listModel.indexOf(msg.getFrom())%UserColors.colors.length;
			SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
			sb.append("<div class=\"from").append(idx).append("\">")
			.append("<span class=\"timestamp\">").append(fmt.format(new Date())).append("</span> ")
			.append(msg.getFrom().getNick())
			.append("</div>");
		}

		sb
		.append("<div class=\"message\">").append(msg.getMessage()).append("</div>")
		.append("</div>");
		// sb.append("<div><img src=\"").append(new File("img/hello.gif").toURI().toURL()).append("\" /></div>");
		// System.out.println(sb);
		try {
			chatLogKit.insertHTML(chatLogDoc, chatLogDoc.getLength(), sb.toString(), 0, 0, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		chatLogPane.select(chatLogDoc.getLength(),chatLogDoc.getLength());
	}

}
